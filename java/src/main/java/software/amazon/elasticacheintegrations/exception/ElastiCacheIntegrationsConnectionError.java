package software.amazon.elasticacheintegrations.exception;

/** Connection bring-up failed. */
public class ElastiCacheIntegrationsConnectionError extends ElastiCacheIntegrationsError {

    public ElastiCacheIntegrationsConnectionError(String message) {
        super(message);
    }

    public ElastiCacheIntegrationsConnectionError(String message, Throwable cause) {
        super(message, cause);
    }
}
