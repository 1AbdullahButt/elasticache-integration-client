package software.amazon.elasticacheintegrations.exception;

/** Malformed RESP frame - garbage data received. */
public class ElastiCacheIntegrationsProtocolError extends ElastiCacheIntegrationsError {

    public ElastiCacheIntegrationsProtocolError(String message) {
        super(message);
    }

    public ElastiCacheIntegrationsProtocolError(String message, Throwable cause) {
        super(message, cause);
    }
}
