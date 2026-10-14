package software.amazon.elasticacheintegrations.session;

import software.amazon.elasticacheintegrations.exception.ElastiCacheIntegrationsCacheUnavailable;
import software.amazon.elasticacheintegrations.exception.ElastiCacheIntegrationsConnectionError;
import software.amazon.elasticacheintegrations.exception.ElastiCacheIntegrationsConnectionLost;
import software.amazon.elasticacheintegrations.exception.ElastiCacheIntegrationsError;
import software.amazon.elasticacheintegrations.exception.ElastiCacheIntegrationsRejectedError;
import software.amazon.elasticacheintegrations.exception.ElastiCacheIntegrationsTimeoutError;
import software.amazon.elasticacheintegrations.model.ElastiCacheIntegrationsRequest;

/** Shared sync/async policy for handling a failed Cache request. */
final class ElastiCacheIntegrationsFallbackPolicy {

    private static final String ORIGIN_ERROR = "ORIGIN_ERROR";

    private ElastiCacheIntegrationsFallbackPolicy() {}

    static boolean shouldFallback(
            ElastiCacheIntegrationsSession session,
            ElastiCacheIntegrationsRequest request,
            ElastiCacheIntegrationsError failure) {
        return session.isOpen()
                && !session.isConnectionHealthy()
                && !(failure instanceof ElastiCacheIntegrationsCacheUnavailable)
                && !(failure instanceof ElastiCacheIntegrationsRejectedError)
                && isTransportFailure(failure)
                && Idempotency.isIdempotent(request.getMetadata());
    }

    static boolean shouldRetry(
            ElastiCacheIntegrationsSession session,
            ElastiCacheIntegrationsRequest request,
            ElastiCacheIntegrationsError failure) {
        if (failure instanceof ElastiCacheIntegrationsCacheUnavailable) {
            return session.isOpen();
        }
        if (!session.isConnectionHealthy() || !Idempotency.isIdempotent(request.getMetadata())) {
            return false;
        }
        if (failure instanceof ElastiCacheIntegrationsRejectedError) {
            return ORIGIN_ERROR.equals(((ElastiCacheIntegrationsRejectedError) failure).getCode());
        }
        return failure instanceof ElastiCacheIntegrationsTimeoutError;
    }

    private static boolean isTransportFailure(ElastiCacheIntegrationsError failure) {
        return failure instanceof ElastiCacheIntegrationsConnectionLost
                || failure instanceof ElastiCacheIntegrationsTimeoutError
                || failure instanceof ElastiCacheIntegrationsConnectionError;
    }
}
