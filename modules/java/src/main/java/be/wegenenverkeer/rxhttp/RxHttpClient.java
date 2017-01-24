package be.wegenenverkeer.rxhttp;

import be.wegenenverkeer.rxhttp.aws.*;
import com.ning.http.client.*;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.filter.RequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static be.wegenenverkeer.rxhttp.CompleteResponseHandler.withCompleteResponse;
import static be.wegenenverkeer.rxhttp.ServerResponse.wrap;

/**
 * A Reactive HTTP Client
 * Created by Karel Maesen, Geovise BVBA on 05/12/14.
 */
public class RxHttpClient {

    final private static Logger logger = LoggerFactory.getLogger(RxHttpClient.class);

    final private AsyncHttpClient innerClient;
    final private RestClientConfig config;
    final private AwsSignature4Signer awsSigner;


    protected RxHttpClient(AsyncHttpClient innerClient, RestClientConfig config, AwsSignature4Signer signer) {
        this.innerClient = innerClient;
        this.config = config;
        this.awsSigner = signer;
    }

    protected RxHttpClient(AsyncHttpClient innerClient, RestClientConfig config) {
        this(innerClient, config, null);
    }


    /**
     * * Executes a request and returns an Observable for the complete response.
     */
    public <F> CompletableFuture<F> execute(ClientRequest request, Function<ServerResponse, F> transformer) {
        logger.info("Sending Request: " + request.toString());
        //Note: we don't use Observable.toBlocking().toFuture()
        //because we need a CompletableFuture so that interop with Scala is possible
        final CompletableFuture<F> future = new CompletableFuture<>();

        innerClient.executeRequest(request.unwrap(), new AsyncCompletionHandler<F>() {
            @Override
            public F onCompleted(Response response) throws Exception {
                try {
                    withCompleteResponse(response,
                            (r) -> {
                                F transformed = transformer.apply(wrap(response));
                                future.complete(transformed);
                            },
                            future::completeExceptionally,
                            future::completeExceptionally
                    );
                } catch (Throwable t) {
                    logger.error("onError handler failed: " + t.getMessage(), t);
                    future.completeExceptionally(t);
                }
                return null;
            }

            @Override
            public void onThrowable(Throwable t) {
                super.onThrowable(t);
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * Executes a request and returns an Observable for the complete response.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     * </p>
     *
     * @param request     the request to send
     * @param transformer a function that transforms the {@link ServerResponse} to a value of F
     * @param <F>         the type of return value
     * @return An Observable that returns the transformed server response.
     */
    public <F> Observable<F> executeToCompletion(ClientRequest request, Function<ServerResponse, F> transformer) {
        return Observable.defer(() -> {
            logger.info("Sending Request: " + request.toString());
            AsyncSubject<F> subject = AsyncSubject.create();
            innerClient.executeRequest(request.unwrap(), new AsyncCompletionHandlerWrapper<>(subject, transformer));
            return subject;
        });
    }

    /**
     * Returns a "cold" Observable for a stream of {@link ServerResponseElement}s.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     *
     * @param request the request to send
     * @return a cold observable of ServerResponseElements
     * @see Observable#defer
     */
    public Observable<ServerResponseElement> executeObservably(ClientRequest request) {
        return Observable.defer(() -> {
            BehaviorSubject<ServerResponseElement> subject = BehaviorSubject.create();
            innerClient.executeRequest(request.unwrap(), new AsyncHandlerWrapper(subject));
            return subject;
        });
    }

    /**
     * Returns a "cold" Observable for a stream of {@code T}.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     *
     * @param request   the request to send
     * @param transform the function that transforms the response body (chunks) into objects of type F
     * @param <F>       return type of the transform
     * @return a cold observable of ServerResponseElements
     * @see Observable#defer
     */
    public <F> Observable<F> executeObservably(ClientRequest request, Function<byte[], F> transform) {
        return Observable.defer(() -> {
            BehaviorSubject<ServerResponseElement> subject = BehaviorSubject.create();
            innerClient.executeRequest(request.unwrap(), new AsyncHandlerWrapper(subject));
            return subject
                    .filter(el -> el.match(e -> false, e -> false, e -> true, e -> true))
                    .map(el -> el.match(
                            e -> null, //won't happen, is filtered
                            e -> null, //won't happen, is filtered
                            e -> transform.apply(e.getBodyPartBytes()),
                            e -> transform.apply(e.getResponseBodyAsBytes())));
        });
    }

    /**
     * Returns the base URL that this client connects to.
     *
     * @return the base URL that this client connects to.
     */
    public String getBaseUrl() {
        return this.config.getBaseUrl();
    }

    /**
     * Returns the configured default ACCEPT header for requests created using this instance's
     * {@code ClientRequestBuilder}s.
     *
     * @return the configured default ACCEPT header for requests created using this instance's {@code ClientRequestBuilder}s.
     */
    public String getAccept() {
        return config.getAccept();
    }

    public boolean hasAwsRequestSigner(){
        return this.awsSigner != null;
    }

    public Optional<AwsSignature4Signer> getAwsRequestSigner(){
        return Optional.ofNullable(this.awsSigner);
    }

    /**
     * Closes the underlying connection
     */
    public void close() {
        this.innerClient.close();
    }

    /**
     * Returns a new {@code ClientRequestBuilder}.
     *
     * @return a new {@code ClientRequestBuilder}.
     */
    public ClientRequestBuilder requestBuilder() {
        return new ClientRequestBuilder(this);
    }


    private static class RestClientConfig {

        private String baseUrl = "";
        private String Accept = "application/json";

        private boolean throttling = false;
        private int throttlingMaxWait = 0;
        private int maxConnections = -1;

        public void enableThrottling() {
            this.throttling = true;
        }

        public void setThrottlingMaxWait(int throttlingMaxWait) {
            this.throttlingMaxWait = throttlingMaxWait;
        }

        public void setMaxConnections(int maxConn) {
            this.maxConnections = maxConn;
        }

        public boolean isThrottling() {
            return throttling;
        }

        public int getThrottlingMaxWait() {
            return throttlingMaxWait;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        void setBaseUrl(String baseUrl) {
            this.baseUrl = chopLastForwardSlash(baseUrl);
        }

        String getBaseUrl() {
            return baseUrl;
        }

        String getAccept() {
            return Accept;
        }

        void setAccept(String accept) {
            Accept = accept;
        }

        private static String chopLastForwardSlash(String url) {
            if (url.charAt(url.length() - 1) == '/') {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        }

    }


    /**
     * A Builder for {@code RxHttpClient} builders.
     */
    static public class Builder {

        private AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        final private RestClientConfig rcConfig = new RestClientConfig();

        private boolean isAws = false;
        private AwsRegion awsRegion;
        private AwsService awsService;
        private AwsCredentialsProvider awsCredentialsProvider;

        public RxHttpClient build() {
            addRestClientConfigsToConfigBuilder();
            AsyncHttpClientConfig config = configBuilder.build();

            BuildValidation validation = validate(config);

            validation.logWarnings(logger);
            if (validation.hasErrors()) {
                throw new IllegalStateException(validation.getErrorMessage());
            }

            if (isAws && awsCredentialsProvider == null) {
                throw new IllegalStateException("Aws endpoint specified, but no CredentialsProvider set.");
            }

            if (isAws) {
                AwsSignature4Signer signer = new AwsSignature4Signer(this.awsRegion,
                        this.awsService, this.awsCredentialsProvider.getAwsCredentials());
                return new RxHttpClient(new AsyncHttpClient(config), rcConfig, signer);
            } else {
                return new RxHttpClient(new AsyncHttpClient(config), rcConfig);
            }
        }

        /**
         * Perform additional configBuilder build steps based on rcConfig settings
         */
        private void addRestClientConfigsToConfigBuilder() {
            if(rcConfig.getMaxConnections() > 0) {
                configBuilder.setMaxConnections(rcConfig.getMaxConnections());
            }
            if (rcConfig.getMaxConnections() > 0 && rcConfig.isThrottling() && rcConfig.getThrottlingMaxWait() > 0) {
                addThrottling(rcConfig.getMaxConnections(), rcConfig.getThrottlingMaxWait());
            }
        }


        private void addThrottling(int maxConnections, int maxWait) {
            RequestFilter filter = new ThrottleRequestFilter(maxConnections, maxWait);
            configBuilder.addRequestFilter(filter);
        }

        /**
         * Sets the default Accept request-header for requests built using this instance.
         *
         * @param acceptHeaderValue the Media-range and accept-params to use as value for the Accept request-header field
         * @return this Builder
         * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">W3C HTTP 1.1 specs</a>.
         */
        public RxHttpClient.Builder setAccept(String acceptHeaderValue) {
            rcConfig.setAccept(acceptHeaderValue);
            return this;
        }

        /**
         * Sets the base URL for this client.
         * <p>
         * <p>The base url will be prepended to any relative URL path specified in the {@code RequestBuilder}</p>
         *
         * @param url the base URL for this instance
         * @return this.Builder
         */
        public RxHttpClient.Builder setBaseUrl(String url) {
            rcConfig.setBaseUrl(url);
            return this;
        }

        /**
         * Validates the Builder (used before building the client)
         *
         * @param config the AsyncHttpClient config object
         * @return a {@code BuildValidation} containing all Errors and Warnings
         */
        private BuildValidation validate(AsyncHttpClientConfig config) {
            BuildValidation bv = new BuildValidation();

            if (rcConfig.baseUrl.isEmpty()) {
                bv.addError("No baseURL is set");
            }

            try {
                new URL(rcConfig.baseUrl);
            } catch (MalformedURLException e) {
                bv.addError("Malformed URL: " + e.getMessage());
            }

            String messagePrefix = "RxHttpClient for " + rcConfig.baseUrl;

            if (!config.isAllowPoolingConnections()) {
                bv.addWarning(messagePrefix + " has connection pooling disabled!");
            }

            if (config.getMaxConnections() < 0 && rcConfig.isThrottling()) {
                bv.addError("Configured throttling, but no max. Connections set");
            }

            if (rcConfig.isThrottling() && rcConfig.getThrottlingMaxWait() <= 0) {
                bv.addError("Configured throttling, but timeout is set to " + rcConfig.getThrottlingMaxWait());
            }

            if (config.getMaxConnections() < 0) {
                bv.addWarning(messagePrefix + " has no maximum connections set!");
            }

            if (config.getConnectionTTL() < 0 && config.getPooledConnectionIdleTimeout() < 0) {
                bv.addWarning(messagePrefix + " has no connection TTL or pool idle timeout set!");
            }

            return bv;
        }


        /**
         * Set the maximum number of connections an {@link com.ning.http.client.AsyncHttpClient} can handle.
         *
         * @param maxConnections the maximum number of connections an {@link com.ning.http.client.AsyncHttpClient} can handle.
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setMaxConnections(int maxConnections) {
            //we set this setting first on the rcConfig object, because we need this information together with the other throttling settings
            // to properly configure the RequestThrottler
            rcConfig.setMaxConnections(maxConnections);
            return this;
        }

        /**
         * Set true if connection can be pooled by a ChannelPool. Default is true.
         *
         * @param allowPoolingConnections true if connection can be pooled by a ChannelPool
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setAllowPoolingConnections(boolean allowPoolingConnections) {
            configBuilder.setAllowPoolingConnections(allowPoolingConnections);
            return this;
        }

        /**
         * Throttles requests by blocking until connections in the pool become available, waiting for
         * the response to arrives before executing the next request.
         *
         * @param maxWait timeout in millisceconds
         * @return this Builder
         */
        public RxHttpClient.Builder setThrottling(int maxWait) {
            rcConfig.enableThrottling();
            rcConfig.setThrottlingMaxWait(maxWait);
            return this;
        }
        /**
         * Set the maximum time in millisecond an {@link com.ning.http.client.AsyncHttpClient} can wait when connecting to a remote host
         *
         * @param connectTimeOut the maximum time in millisecond an {@link com.ning.http.client.AsyncHttpClient} can wait when connecting to a remote host
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setConnectTimeout(int connectTimeOut) {
            configBuilder.setConnectTimeout(connectTimeOut);
            return this;
        }

//        /**
//         * Set the {@link com.ning.http.client.Realm}  that will be used for all requests.
//         *
//         * @param realm the {@link com.ning.http.client.Realm}
//         * @return a {@link be.wegenenverkeer.rest.RestClient.Builder}
//         */
//        public RestClient.Builder setRealm(Realm realm) {
//            configBuilder.setRealm(realm);
//            return this;
//        }

        /**
         * Set the {@link java.util.concurrent.ExecutorService} an {@link com.ning.http.client.AsyncHttpClient} use for handling
         * asynchronous response.
         * <p>
         * <p>By default a Cached Threadpool will be used, that will create threads as needed (see {@link Executors#newCachedThreadPool()})</p>
         *
         * @param applicationThreadPool the {@link java.util.concurrent.ExecutorService} an {@link com.ning.http.client.AsyncHttpClient} use for handling
         *                              asynchronous response.
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setExecutorService(ExecutorService applicationThreadPool) {
            configBuilder.setExecutorService(applicationThreadPool);
            return this;
        }

        /**
         * Set the maximum time in millisecond an {@link com.ning.http.client.ws.WebSocket} can stay idle.
         *
         * @param webSocketTimeout the maximum time in millisecond an {@link com.ning.http.client.ws.WebSocket} can stay idle.
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setWebSocketTimeout(int webSocketTimeout) {
            configBuilder.setWebSocketTimeout(webSocketTimeout);
            return this;
        }

        /**
         * Set the number of time a request will be retried when an {@link java.io.IOException} occurs because of a Network exception.
         *
         * @param maxRequestRetry the number of time a request will be retried
         * @return this
         */
        public RxHttpClient.Builder setMaxRequestRetry(int maxRequestRetry) {
            configBuilder.setMaxRequestRetry(maxRequestRetry);
            return this;
        }

//        public RestClient.Builder setTimeConverter(TimeConverter timeConverter) {
//            configBuilder.setTimeConverter(timeConverter);
//            return this;
//        }

        /**
         * Set the {@link javax.net.ssl.HostnameVerifier}
         *
         * @param hostnameVerifier {@link javax.net.ssl.HostnameVerifier}
         * @return this
         */
        public RxHttpClient.Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            configBuilder.setHostnameVerifier(hostnameVerifier);
            return this;
        }

//        /**
//         * Set an instance of {@link com.ning.http.client.ProxyServerSelector} used by an {@link com.ning.http.client.AsyncHttpClient}
//         *
//         * @param proxyServerSelector instance of {@link com.ning.http.client.ProxyServerSelector}
//         * @return a {@link be.wegenenverkeer.rest.RestClient.Builder}
//         */
//        public RestClient.Builder setProxyServerSelector(ProxyServerSelector proxyServerSelector) {
//            configBuilder.setProxyServerSelector(proxyServerSelector);
//            return this;
//        }

//        /**
//         * Remove an {@link com.ning.http.client.filter.RequestFilter} that will be invoked before {@link com.ning.http.client.AsyncHttpClient#executeObservably(com.ning.http.client.Request)}
//         *
//         * @param requestFilter {@link com.ning.http.client.filter.RequestFilter}
//         * @return this
//         */
//        public RestClient.Builder removeRequestFilter(RequestFilter requestFilter) {
//            configBuilder.removeRequestFilter(requestFilter);
//            return this;
//        }

        public RxHttpClient.Builder setEnabledProtocols(String[] enabledProtocols) {
            configBuilder.setEnabledProtocols(enabledProtocols);
            return this;
        }

//        /**
//         * Set an instance of {@link com.ning.http.client.ProxyServer} used by an {@link com.ning.http.client.AsyncHttpClient}
//         *
//         * @param proxyServer instance of {@link com.ning.http.client.ProxyServer}
//         * @return a {@link be.wegenenverkeer.rest.RestClient.Builder}
//         */
//        public RestClient.Builder setProxyServer(ProxyServer proxyServer) {
//            configBuilder.setProxyServer(proxyServer);
//            return this;
//        }

        /**
         * Configures this AHC instance to use relative URIs instead of absolute ones when talking with a SSL proxy or WebSocket proxy.
         *
         * @param useRelativeURIsWithConnectProxies use relative URIs with connect proxies
         * @return this
         * @since 1.8.13
         */
        public RxHttpClient.Builder setUseRelativeURIsWithConnectProxies(boolean useRelativeURIsWithConnectProxies) {
            configBuilder.setUseRelativeURIsWithConnectProxies(useRelativeURIsWithConnectProxies);
            return this;
        }

//        /**
//         * Set the maximum number of connections per hosts an {@link com.ning.http.client.AsyncHttpClient} can handle.
//         *
//         * @param maxConnectionsPerHost the maximum number of connections per host an {@link com.ning.http.client.AsyncHttpClient} can handle.
//         * @return a {@link RxHttpClient.Builder}
//         */
//        public RxHttpClient.Builder setMaxConnectionsPerHost(int maxConnectionsPerHost) {
//            configBuilder.setMaxConnectionsPerHost(maxConnectionsPerHost);
//            return this;
//        }

        public RxHttpClient.Builder setEnabledCipherSuites(String[] enabledCipherSuites) {
            configBuilder.setEnabledCipherSuites(enabledCipherSuites);
            return this;
        }

        /**
         * Set whether connections pooling is enabled.
         * <p>
         * <p>Default is set to true</p>
         *
         * @param allowPoolingSslConnections true if enabled
         * @return this
         */
        public RxHttpClient.Builder setAllowPoolingSslConnections(boolean allowPoolingSslConnections) {
            configBuilder.setAllowPoolingSslConnections(allowPoolingSslConnections);
            return this;
        }

//        /**
//         * Remove an {@link com.ning.http.client.filter.ResponseFilter} that will be invoked as soon as the response is
//         * received, and before {@link com.ning.http.client.AsyncHandler#onStatusReceived(com.ning.http.client.HttpResponseStatus)}.
//         *
//         * @param responseFilter an {@link com.ning.http.client.filter.ResponseFilter}
//         * @return this
//         */
//        public RestClient.Builder removeResponseFilter(ResponseFilter responseFilter) {
//            configBuilder.removeResponseFilter(responseFilter);
//            return this;
//        }

        /**
         * Sets whether AHC should use the default http.proxy* system properties
         * to obtain proxy information.  This differs from {@link #setUseProxySelector(boolean)}
         * in that AsyncHttpClient will use its own logic to handle the system properties,
         * potentially supporting other protocols that the the JDK ProxySelector doesn't.
         * <p>
         * If useProxyProperties is set to <code>true</code> but {@link #setUseProxySelector(boolean)}
         * was also set to true, the latter is preferred.
         * <p>
         * See http://download.oracle.com/javase/1.4.2/docs/guide/net/properties.html
         *
         * @param useProxyProperties whether AHC should use the default http.proxy* system properties
         */
        public RxHttpClient.Builder setUseProxyProperties(boolean useProxyProperties) {
            configBuilder.setUseProxyProperties(useProxyProperties);
            return this;
        }

        /**
         * Sets whether AHC should use the default JDK ProxySelector to select a proxy server.
         * <p>
         * See http://docs.oracle.com/javase/7/docs/api/java/net/ProxySelector.html
         *
         * @param useProxySelector whether AHC should use the default JDK ProxySelector to select a proxy server.
         */
        public RxHttpClient.Builder setUseProxySelector(boolean useProxySelector) {
            configBuilder.setUseProxySelector(useProxySelector);
            return this;
        }

//        /**
//         * Add an {@link com.ning.http.client.filter.ResponseFilter} that will be invoked as soon as the response is
//         * received, and before {@link com.ning.http.client.AsyncHandler#onStatusReceived(com.ning.http.client.HttpResponseStatus)}.
//         *
//         * @param responseFilter an {@link com.ning.http.client.filter.ResponseFilter}
//         * @return this
//         */
//        public RestClient.Builder addResponseFilter(ResponseFilter responseFilter) {
//            configBuilder.addResponseFilter(responseFilter);
//            return this;
//        }

        /**
         * Set the maximum number of HTTP redirect
         *
         * @param maxRedirects the maximum number of HTTP redirect
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setMaxRedirects(int maxRedirects) {
            configBuilder.setMaxRedirects(maxRedirects);
            return this;
        }

        public RxHttpClient.Builder setAcceptAnyCertificate(boolean acceptAnyCertificate) {
            configBuilder.setAcceptAnyCertificate(acceptAnyCertificate);
            return this;
        }

        public RxHttpClient.Builder setIOThreadMultiplier(int multiplier) {
            configBuilder.setIOThreadMultiplier(multiplier);
            return this;
        }

        /**
         * Configures this AHC instance to be strict in it's handling of 302 redirects
         * in a POST/Redirect/GET situation.
         *
         * @param strict302Handling strict handling
         * @return this
         * @since 1.7.2
         */
        public RxHttpClient.Builder setStrict302Handling(boolean strict302Handling) {
            configBuilder.setStrict302Handling(strict302Handling);
            return this;
        }

        /**
         * Set the maximum time in millisecond connection can be added to the pool for further reuse
         * <p>
         * <p> Default is -1 (no TTL set)</p>
         *
         * @param connectionTTL the maximum time in millisecond connection can be added to the pool for further reuse
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setConnectionTTL(int connectionTTL) {
            configBuilder.setConnectionTTL(connectionTTL);
            return this;
        }

        /**
         * Set the USER_AGENT header value
         *
         * @param userAgent the USER_AGENT header value
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setUserAgent(String userAgent) {
            configBuilder.setUserAgent(userAgent);
            return this;
        }

        /**
         * Set to true to enable HTTP redirect
         *
         * @param followRedirect@return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setFollowRedirect(boolean followRedirect) {
            configBuilder.setFollowRedirect(followRedirect);
            return this;
        }



//        /**
//         * Add an {@link com.ning.http.client.filter.RequestFilter} that will be invoked before {@link com.ning.http.client.AsyncHttpClient#executeObservably(com.ning.http.client.Request)}
//         *
//         * @param requestFilter {@link com.ning.http.client.filter.RequestFilter}
//         * @return this
//         */
//        public RestClient.Builder addRequestFilter(RequestFilter requestFilter) {
//            configBuilder.addRequestFilter(requestFilter);
//            return this;
//        }

//        /**
//         * Add an {@link com.ning.http.client.filter.IOExceptionFilter} that will be invoked when an {@link java.io.IOException}
//         * occurs during the download/upload operations.
//         *
//         * @param ioExceptionFilter an {@link com.ning.http.client.filter.ResponseFilter}
//         * @return this
//         */
//        public RestClient.Builder addIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
//            configBuilder.addIOExceptionFilter(ioExceptionFilter);
//            return this;
//        }

        /**
         * Disable automatic url escaping
         *
         * @param disableUrlEncodingForBoundedRequests disables the url encoding if set to true
         * @return this Builder
         */
        public RxHttpClient.Builder setDisableUrlEncodingForBoundedRequests(boolean disableUrlEncodingForBoundedRequests) {
            configBuilder.setDisableUrlEncodingForBoundedRequests(disableUrlEncodingForBoundedRequests);
            return this;
        }

        /**
         * Set the maximum time in millisecond an {@link com.ning.http.client.AsyncHttpClient} waits until the response is completed.
         *
         * @param requestTimeout the maximum time in millisecond an {@link com.ning.http.client.AsyncHttpClient} waits until the response is completed.
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setRequestTimeout(int requestTimeout) {
            configBuilder.setRequestTimeout(requestTimeout);
            return this;
        }

        /**
         * Set the {@link javax.net.ssl.SSLContext} for secure connection.
         *
         * @param sslContext the {@link javax.net.ssl.SSLContext} for secure connection
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setSSLContext(SSLContext sslContext) {
            configBuilder.setSSLContext(sslContext);
            return this;
        }

        /**
         * Enforce HTTP compression.
         *
         * @param compressionEnforced true if compression is enforced
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setCompressionEnforced(boolean compressionEnforced) {
            configBuilder.setCompressionEnforced(compressionEnforced);
            return this;
        }

        /**
         * Set the maximum time in millisecond an {@link com.ning.http.client.AsyncHttpClient} will keep connection
         * idle in pool.
         * <p>
         * <p>Default is 60000 millis (1 min.)</p>
         *
         * @param pooledConnectionIdleTimeout@return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setPooledConnectionIdleTimeout(int pooledConnectionIdleTimeout) {
            configBuilder.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
            return this;
        }

//        /**
//         * Remove an {@link com.ning.http.client.filter.IOExceptionFilter} tthat will be invoked when an {@link java.io.IOException}
//         * occurs during the download/upload operations.
//         *
//         * @param ioExceptionFilter an {@link com.ning.http.client.filter.ResponseFilter}
//         * @return this
//         */
//        public RestClient.Builder removeIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
//            configBuilder.removeIOExceptionFilter(ioExceptionFilter);
//            return this;
//        }

        /**
         * Set the maximum time in millisecond an {@link com.ning.http.client.AsyncHttpClient} can stay idle.
         *
         * @param readTimeout the maximum time in millisecond an {@code RestClient} can stay idle.
         * @return a {@link RxHttpClient.Builder}
         */
        public RxHttpClient.Builder setReadTimeout(int readTimeout) {
            configBuilder.setReadTimeout(readTimeout);
            return this;
        }

        public Builder setAwsEndPoint(AwsService service, AwsRegion region) {
            if (service == null || region == null) {
                throw new IllegalArgumentException("No null arguments allowed");
            }
            String url = AwsServiceEndPoint.UrlFor(service, region);
            this.setBaseUrl(url);
            this.awsRegion = region;
            this.awsService = service;
            this.isAws = true;
            return this;
        }

        public Builder setAwsCredentialsProvider(AwsCredentialsProvider provider) {
            this.awsCredentialsProvider = provider;
            return this;
        }
    }

    static private class BuildValidation {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        void addError(String errMsg) {
            errors.add(errMsg);
        }

        void addWarning(String warningMsg) {
            warnings.add(warningMsg);
        }

        String getErrorMessage() {
            StringBuilder builder = new StringBuilder();
            for (String msg : errors) {
                builder.append(msg).append("\n");
            }
            return chop(builder.toString());
        }

        boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        boolean hasErrors() {
            return !errors.isEmpty();
        }

        void logWarnings(Logger logger) {
            for (String msg : warnings) {
                logger.warn(msg);
            }
        }

        private String chop(String s) {
            if (s == null || s.isEmpty()) return s;
            return s.substring(0, s.length() - 1);
        }

    }

}
