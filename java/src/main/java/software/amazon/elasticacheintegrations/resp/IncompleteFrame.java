package software.amazon.elasticacheintegrations.resp;

/**
 * Raised when data is incomplete (not enough bytes for a full RESP frame). NOT a subclass of
 * ElastiCacheIntegrationsError - this is a flow-control signal.
 */
public class IncompleteFrame extends Exception {

    public IncompleteFrame(String message) {
        super(message);
    }
}
