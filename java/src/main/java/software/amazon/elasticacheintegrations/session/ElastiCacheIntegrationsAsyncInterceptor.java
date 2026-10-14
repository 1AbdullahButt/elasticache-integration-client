package software.amazon.elasticacheintegrations.session;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.elasticacheintegrations.model.ElastiCacheIntegrationsRequest;

/**
 * AWS SDK v2 interceptor used with {@link ElastiCacheIntegrationsAsyncHttpClient}.
 *
 * <p>The unsigned request and SDK operation metadata are retained in the per-execution SDK
 * attributes. Each HTTP attempt is handed to the async transport immediately before transmission.
 */
public final class ElastiCacheIntegrationsAsyncInterceptor implements ExecutionInterceptor {

    private final ElastiCacheIntegrationsRequestContext requestContext;
    private final ElastiCacheIntegrationsAsyncHttpClient httpClient;

    ElastiCacheIntegrationsAsyncInterceptor(
            ElastiCacheIntegrationsSession session,
            SdkAsyncHttpClient originClient,
            boolean ownsOriginClient,
            Executor executor,
            ExecutorService ownedExecutor,
            int maxBufferedRequestBytes,
            int maxConcurrentBodyCollectors) {
        this.requestContext = new ElastiCacheIntegrationsRequestContext();
        this.httpClient =
                new ElastiCacheIntegrationsAsyncHttpClient(
                        session,
                        requestContext,
                        originClient,
                        ownsOriginClient,
                        executor,
                        ownedExecutor,
                        maxBufferedRequestBytes,
                        maxConcurrentBodyCollectors);
    }

    /** Get the async HTTP transport that attempts Cache before the origin service. */
    public ElastiCacheIntegrationsAsyncHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public SdkHttpFullRequest modifyHttpRequest(
            Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
        SdkHttpFullRequest sdkRequest = (SdkHttpFullRequest) context.httpRequest();
        ElastiCacheIntegrationsRequest cacheRequest =
                SdkAdapters.toElastiCacheIntegrationsRequest(sdkRequest, executionAttributes, new byte[0]);
        requestContext.capture(cacheRequest, executionAttributes);
        return sdkRequest;
    }

    @Override
    public void beforeTransmission(
            Context.BeforeTransmission context, ExecutionAttributes executionAttributes) {
        requestContext.prepareAttempt(executionAttributes);
    }

    @Override
    public void afterExecution(
            Context.AfterExecution context, ExecutionAttributes executionAttributes) {
        requestContext.clear();
    }

    @Override
    public void onExecutionFailure(
            Context.FailedExecution context, ExecutionAttributes executionAttributes) {
        requestContext.clear();
    }
}
