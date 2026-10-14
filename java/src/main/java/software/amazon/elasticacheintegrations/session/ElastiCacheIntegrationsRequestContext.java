package software.amazon.elasticacheintegrations.session;

import software.amazon.awssdk.core.interceptor.ExecutionAttribute;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.elasticacheintegrations.model.ElastiCacheIntegrationsRequest;

/** Per-client, per-attempt handoff between an SDK interceptor and HTTP transport. */
final class ElastiCacheIntegrationsRequestContext {

    private static final ExecutionAttribute<ElastiCacheIntegrationsRequest> AWS_REQUEST =
            new ExecutionAttribute<>("ElastiCacheIntegrationsRequest");

    private final ThreadLocal<ElastiCacheIntegrationsRequest> currentAttempt = new ThreadLocal<>();

    void capture(ElastiCacheIntegrationsRequest request, ExecutionAttributes executionAttributes) {
        executionAttributes.putAttribute(AWS_REQUEST, request);
    }

    void prepareAttempt(ExecutionAttributes executionAttributes) {
        ElastiCacheIntegrationsRequest request = executionAttributes.getAttribute(AWS_REQUEST);
        if (request == null) {
            currentAttempt.remove();
        } else {
            currentAttempt.set(request);
        }
    }

    ElastiCacheIntegrationsRequest takeAttempt() {
        ElastiCacheIntegrationsRequest request = currentAttempt.get();
        currentAttempt.remove();
        return request;
    }

    void clear() {
        currentAttempt.remove();
    }
}
