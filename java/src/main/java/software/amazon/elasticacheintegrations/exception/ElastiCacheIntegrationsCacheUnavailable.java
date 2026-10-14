package software.amazon.elasticacheintegrations.exception;

/** Cache has no usable cache connection and its origin-fallback window ended. */
public final class ElastiCacheIntegrationsCacheUnavailable
        extends ElastiCacheIntegrationsConnectionError {
    public ElastiCacheIntegrationsCacheUnavailable(String message) {
        super(message);
    }
}
