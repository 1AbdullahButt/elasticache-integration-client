"""Client-side evaluation of the CEL routing predicates returned by ``photon.config``.

The proxy is authoritative about what may be cached: it returns an ordered list of predicates, each
a `CEL <https://cel.dev>`_ boolean expression plus the variables it binds. This module compiles those
expressions once and evaluates them against each intercepted request to decide "route to the cache"
or "pass through to the origin service". It is pure: no I/O, no session state, no service-specific
knowledge — ``"dynamodb"``, ``"GetItem"`` and ``"TableName"`` appear only inside the proxy-authored
expressions, never in this code.

Each predicate is evaluated against a fixed request context — ``service``, ``operation``, ``method``,
``url``, ``headers``, ``body`` (parsed JSON) — with the predicate's ``vars`` (e.g. ``ops``,
``allowed``) also in scope. A request routes to the cache when any predicate is ``true``;
first-match-wins.

Anything this module cannot evaluate resolves to *don't route*, because an unrouted request is merely
uncached while a mis-routed one is a correctness bug: an expression that fails to compile is dropped
at config time, and one that errors at evaluation counts as no-match.
"""

from __future__ import annotations

import json
import logging

import celpy

from photon.models import PhotonRequest

logger = logging.getLogger(__name__)

# Maximum request body to parse for evaluation. Mirrors the proxy's
# photon_max_request_body_bytes (512 KiB): a larger body is rejected server-side anyway, so parsing
# it locally only to pass through wastes client CPU.
_MAX_BODY_INSPECT_BYTES = 512 * 1024


class CompiledPredicate:
    """A compiled CEL program plus its bound variables, ready to evaluate per request."""

    __slots__ = ("_program", "_vars")

    def __init__(self, program: celpy.Runner, variables: dict) -> None:
        self._program = program
        # Convert the bound variables to CEL values once, at construction.
        self._vars = {name: celpy.json_to_cel(value) for name, value in variables.items()}

    def matches(self, context: dict) -> bool:
        """Whether this predicate is ``true`` for ``context`` (CEL-typed request fields).

        Any evaluation error counts as no-match: the expression is proxy-authored, but a body shape
        it did not anticipate must never route rather than raise into the request path.
        """
        activation = {**context, **self._vars}
        try:
            result = self._program.evaluate(activation)
        except celpy.CELEvalError:
            return False
        if isinstance(result, celpy.CELEvalError):
            return False
        try:
            return bool(result)
        except (TypeError, ValueError):
            return False


def compile_predicates(raw_config: dict | None) -> list[CompiledPredicate]:
    """Compile the ``predicates`` of a resolved config into evaluatable programs, in wire order.

    A predicate whose expression fails to compile is dropped (and logged), leaving the rest — a proxy
    using one construct this client's CEL version lacks costs only the caching that predicate would
    have enabled. Returns ``[]`` when there are no predicates, so every request passes through.
    """
    env = celpy.Environment()
    compiled: list[CompiledPredicate] = []
    for entry in _predicate_entries(raw_config):
        expr = entry.get("expr")
        if not isinstance(expr, str):
            logger.debug("Ignoring predicate without a string 'expr'")
            continue
        try:
            program = env.program(env.compile(expr))
        except celpy.CELParseError:
            logger.warning("Ignoring predicate with an uncompilable expression", exc_info=True)
            continue
        variables = entry.get("vars")
        compiled.append(
            CompiledPredicate(program, variables if isinstance(variables, dict) else {})
        )
    return compiled


def route(predicates: list[CompiledPredicate], request: PhotonRequest) -> bool:
    """True when ``request`` should go to the cache. First match wins; empty list routes nothing.

    Builds the request context once and reuses it across predicates.
    """
    if not predicates:
        return False
    context = {name: celpy.json_to_cel(value) for name, value in _build_context(request).items()}
    return any(predicate.matches(context) for predicate in predicates)


def _predicate_entries(raw_config: dict | None) -> list:
    """The ``predicates`` array of a resolved config, or ``[]`` if absent/malformed."""
    if not isinstance(raw_config, dict):
        return []
    entries = raw_config.get("predicates")
    return entries if isinstance(entries, list) else []


def _build_context(request: PhotonRequest) -> dict:
    """The CEL evaluation context for one request.

    ``service``/``operation`` come from the metadata the SDK hook derived from the event name; their
    location differs per service, the event name does not. ``body`` is the parsed JSON object, or an
    empty map when the request has no JSON body (e.g. a non-DynamoDB service).
    """
    return {
        "service": request.metadata.service,
        "operation": request.metadata.operation,
        "method": request.method,
        "url": request.url,
        "headers": dict(request.headers),
        "body": _parse_body(request.body),
    }


def _parse_body(body: object) -> dict:
    """Parse a request body into a JSON object, or ``{}`` when it cannot be inspected.

    ``{}`` on: empty, oversized, non-JSON, valid JSON that is not an object, or a non-bytes/str body.
    An expression referencing ``body.<field>`` then simply finds nothing and does not match.
    """
    if not body or not isinstance(body, (bytes, str)):
        return {}
    if len(body) > _MAX_BODY_INSPECT_BYTES:
        return {}
    try:
        parsed = json.loads(body)
    except (ValueError, TypeError):
        return {}
    return parsed if isinstance(parsed, dict) else {}
