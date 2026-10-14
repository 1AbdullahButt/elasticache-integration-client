package software.amazon.elasticacheintegrations.exception;

/** Per-request timeout. */
public class ElastiCacheIntegrationsTimeoutError extends ElastiCacheIntegrationsError {

    public ElastiCacheIntegrationsTimeoutError(String message) {
        super(message);
    }

    public ElastiCacheIntegrationsTimeoutError(String message, Throwable cause) {
        super(message, cause);
    }
}
