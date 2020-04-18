package be.wegenenverkeer.rxhttp;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-12.
 */

import be.wegenenverkeer.rxhttp.aws.*;
import io.netty.handler.ssl.SslContext;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.filter.RequestFilter;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/**
 * A Builder for {@code RxHttpClient} builders.
 */
public abstract class Builder<T extends RxHttpClient, U extends Builder<T,U>> {

    final private static Logger logger = LoggerFactory.getLogger(BaseRxHttpClient.class);

    private DefaultAsyncHttpClientConfig.Builder configBuilder = new DefaultAsyncHttpClientConfig.Builder();
    final private RestClientConfig rcConfig = new RestClientConfig();

    private boolean isAws = false;
    private AwsServiceEndPoint awsServiceEndPoint;
    private AwsCredentialsProvider awsCredentialsProvider;

    protected List<RequestSigner> requestSigners = new LinkedList<>(); //TODO -- make this member private
    private List<String> headersToLog = new ArrayList<>();
    private ArrayList<String> formParmsToLog = new ArrayList<>();

    public T build() {
        addRestClientConfigsToConfigBuilder();
        DefaultAsyncHttpClientConfig config = configBuilder.build();

        BuildValidation validation = validate(config);

        validation.logWarnings();
        if (validation.hasErrors()) {
            throw new IllegalStateException(validation.getErrorMessage());
        }

        if (isAws && awsCredentialsProvider == null) {
            throw new IllegalStateException("Aws endpoint specified, but no CredentialsProvider set.");
        }

        AsyncHttpClient innerClient = asyncHttpClient(config);

        if (isAws) {
            requestSigners.add(new AwsSignature4Signer(this.awsServiceEndPoint, this.awsCredentialsProvider));
        }

        ClientRequestLogFormatter logFmt = new DefaultClientRequestLogFormatter(headersToLog, formParmsToLog);

        return createClient(innerClient, rcConfig, logFmt, requestSigners.toArray(new RequestSigner[0]));
    }

    //TODO -- make the createClient method protected or private. This implies a builder() factory method, rather than directly invoking a static inner class constructor
    // to get a Builder
    /**
     * This is for internal use only. Clients are advised NOT to use this methode.
     * @param innerClient
     * @param rcConfig
     * @param logFmt
     * @param signers
     * @return
     */
    public abstract T createClient(AsyncHttpClient innerClient, RestClientConfig rcConfig, ClientRequestLogFormatter logFmt, RequestSigner... signers);

    
            
    /**
     * Perform additional configBuilder build steps based on rcConfig settings
     */
    private void addRestClientConfigsToConfigBuilder() {
        if (rcConfig.getMaxConnections() > 0) {
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

    public U addRequestSigner(RequestSigner requestSigner) {
        if (requestSigner == null) {
            throw new IllegalArgumentException("No null argument allowed");
        }
        this.requestSigners.add(requestSigner);
        return (U)this;
    }

    /**
     * Sets the default Accept request-header for requests built using this instance.
     *
     * @param acceptHeaderValue the Media-range and accept-params to use as value for the Accept request-header field
     * @return (U)this Builder
     * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">W3C HTTP 1.1 specs</a>.
     */
    public U setAccept(String acceptHeaderValue) {
        rcConfig.setAccept(acceptHeaderValue);
        return (U)this;
    }

    /**
     * Sets the base URL for this client.
     * <p>
     * <p>The base url will be prepended to any relative URL path specified in the {@code RequestBuilder}</p>
     *
     * @param url the base URL for this instance
     * @return (U)this.Builder
     */
    public U setBaseUrl(String url) {
        if (this.isAws) throw new IllegalStateException("Not allowed to set Base URL on AWS EndPoint");
        rcConfig.setBaseUrl(url);
        return (U)this;
    }

    /**
     * Validates the Builder (used before building the client)
     *
     * @param config the AsyncHttpClient config object
     * @return a {@code BuildValidation} containing all Errors and Warnings
     */
    private BuildValidation validate(DefaultAsyncHttpClientConfig config) {
        BuildValidation bv = new BuildValidation();

        if (rcConfig.getBaseUrl().isEmpty()) {
            bv.addError("No baseURL is set");
        }

        try {
            new URL(rcConfig.getBaseUrl());
        } catch (MalformedURLException e) {
            bv.addError("Malformed URL: " + e.getMessage());
        }

        String messagePrefix = "RxHttpClient for " + rcConfig.getBaseUrl();

        if (!config.isKeepAlive()) {
            bv.addWarning(messagePrefix + " has ChannelPool (KeepAlive) support disabled!");
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

        if (config.getConnectionTtl() < 0 && config.getPooledConnectionIdleTimeout() < 0) {
            bv.addWarning(messagePrefix + " has no connection TTL or pool idle timeout set!");
        }

        return bv;
    }


    /**
     * Set the maximum number of connections an {@link org.asynchttpclient.AsyncHttpClient} can handle.
     *
     * @param maxConnections the maximum number of connections an {@link org.asynchttpclient.AsyncHttpClient} can handle.
     * @return a {@link Builder}
     */
    public U setMaxConnections(int maxConnections) {
        //we set this setting first on the rcConfig object, because we need this information together with the other throttling settings
        // to properly configure the RequestThrottler
        rcConfig.setMaxConnections(maxConnections);
        return (U)this;
    }

    /**
     * Set true if connection can be pooled by a ChannelPool. Default is true.
     *
     * @param allowPoolingConnections true if connection can be pooled by a ChannelPool
     * @return a {@link Builder}
     * @deprecated Use setKeepAlive(boolean) instead
     */
    @Deprecated
    public U setAllowPoolingConnections(boolean allowPoolingConnections) {
        configBuilder.setKeepAlive(allowPoolingConnections);
        return (U)this;
    }

    /**
     * Set true if connection can be pooled by a ChannelPool. Default is true.
     *
     * @param keepAlive true if connection can be pooled by a ChannelPool
     * @return a {@link Builder}
     */
    public U setKeepAlive(boolean keepAlive) {
        configBuilder.setKeepAlive(keepAlive);
        return (U)this;
    }

    /**
     * Throttles requests by blocking until connections in the pool become available, waiting for
     * the response to arrives before executing the next request.
     *
     * @param maxWait timeout in millisceconds
     * @return (U)this Builder
     */
    public U setThrottling(int maxWait) {
        rcConfig.enableThrottling();
        rcConfig.setThrottlingMaxWait(maxWait);
        return (U)this;
    }

    /**
     * Set the maximum time in millisecond an {@link org.asynchttpclient.AsyncHttpClient} can wait when connecting to a remote host
     *
     * @param connectTimeOut the maximum time in millisecond an {@link org.asynchttpclient.AsyncHttpClient} can wait when connecting to a remote host
     * @return a {@link Builder}
     */
    public U setConnectTimeout(int connectTimeOut) {
        configBuilder.setConnectTimeout(connectTimeOut);
        return (U)this;
    }

//        /**
//         * Set the {@link org.asynchttpclient.Realm}  that will be used for all requests.
//         *
//         * @param realm the {@link org.asynchttpclient.Realm}
//         * @return a {@link be.wegenenverkeer.rest.RestClient.Builder}
//         */
//        public RestClient.Builder setRealm(Realm realm) {
//            configBuilder.setRealm(realm);
//            return (U)this;
//        }

    /**
     * Set the {@link java.util.concurrent.ExecutorService} an {@link org.asynchttpclient.AsyncHttpClient} uses for handling
     * asynchronous response.
     * <p>
     *
     * @param threadFactory the {@code threadFactory} an {@link org.asynchttpclient.AsyncHttpClient} use for handling
     *                      asynchronous response.
     * @return a {@link Builder}
     */
    public U setThreadFactory(ThreadFactory threadFactory) {
        configBuilder.setThreadFactory(threadFactory);
        return (U)this;
    }

    /**
     * Set the number of time a request will be retried when an {@link java.io.IOException} occurs because of a Network exception.
     *
     * @param maxRequestRetry the number of time a request will be retried
     * @return (U)this
     */
    public U setMaxRequestRetry(int maxRequestRetry) {
        configBuilder.setMaxRequestRetry(maxRequestRetry);
        return (U)this;
    }

//        public RestClient.Builder setTimeConverter(TimeConverter timeConverter) {
//            configBuilder.setTimeConverter(timeConverter);
//            return (U)this;
//        }


    //TODO -- check https://github.com/AsyncHttpClient/async-http-client/issues/622 : is there a replacement for hostnameverifier??
//        /**
//         * Set the {@link javax.net.ssl.HostnameVerifier}
//         *
//         * @param hostnameVerifier {@link javax.net.ssl.HostnameVerifier}
//         * @return (U)this
//         */
//        public U setHostnameVerifier(HostnameVerifier hostnameVerifier) {
//            configBuilder.setHostnameVerifier(hostnameVerifier);
//            return (U)this;
//        }

//        /**
//         * Set an instance of {@link org.asynchttpclient.ProxyServerSelector} used by an {@link org.asynchttpclient.AsyncHttpClient}
//         *
//         * @param proxyServerSelector instance of {@link org.asynchttpclient.ProxyServerSelector}
//         * @return a {@link be.wegenenverkeer.rest.RestClient.Builder}
//         */
//        public RestClient.Builder setProxyServerSelector(ProxyServerSelector proxyServerSelector) {
//            configBuilder.setProxyServerSelector(proxyServerSelector);
//            return (U)this;
//        }

//        /**
//         * Remove an {@link org.asynchttpclient.filter.RequestFilter} that will be invoked before {@link org.asynchttpclient.AsyncHttpClient#executeObservably(org.asynchttpclient.Request)}
//         *
//         * @param requestFilter {@link org.asynchttpclient.filter.RequestFilter}
//         * @return (U)this
//         */
//        public RestClient.Builder removeRequestFilter(RequestFilter requestFilter) {
//            configBuilder.removeRequestFilter(requestFilter);
//            return (U)this;
//        }

    public U setEnabledProtocols(String[] enabledProtocols) {
        configBuilder.setEnabledProtocols(enabledProtocols);
        return (U)this;
    }

//        /**
//         * Set an instance of {@link org.asynchttpclient.ProxyServer} used by an {@link org.asynchttpclient.AsyncHttpClient}
//         *
//         * @param proxyServer instance of {@link org.asynchttpclient.ProxyServer}
//         * @return a {@link be.wegenenverkeer.rest.RestClient.Builder}
//         */
//        public RestClient.Builder setProxyServer(ProxyServer proxyServer) {
//            configBuilder.setProxyServer(proxyServer);
//            return (U)this;
//        }

    //TODO -- seems to be removed??
//        /**
//         * Configures this AHC instance to use relative URIs instead of absolute ones when talking with a SSL proxy or WebSocket proxy.
//         *
//         * @param useRelativeURIsWithConnectProxies use relative URIs with connect proxies
//         * @return (U)this
//         * @since 1.8.13
//         */
//        public U setUseRelativeURIsWithConnectProxies(boolean useRelativeURIsWithConnectProxies) {
//            configBuilder. setUseRelativeURIsWithConnectProxies(useRelativeURIsWithConnectProxies);
//            return (U)this;
//        }

//        /**
//         * Set the maximum number of connections per hosts an {@link org.asynchttpclient.AsyncHttpClient} can handle.
//         *
//         * @param maxConnectionsPerHost the maximum number of connections per host an {@link org.asynchttpclient.AsyncHttpClient} can handle.
//         * @return a {@link Builder}
//         */
//        public U setMaxConnectionsPerHost(int maxConnectionsPerHost) {
//            configBuilder.setMaxConnectionsPerHost(maxConnectionsPerHost);
//            return (U)this;
//        }

    public U setEnabledCipherSuites(String[] enabledCipherSuites) {
        configBuilder.setEnabledCipherSuites(enabledCipherSuites);
        return (U)this;
    }

    //TODO -- seems to be removed??
//        /**
//         * Set whether connections pooling is enabled.
//         * <p>
//         * <p>Default is set to true</p>
//         *
//         * @param allowPoolingSslConnections true if enabled
//         * @return (U)this
//         */
//        public U setAllowPoolingSslConnections(boolean allowPoolingSslConnections) {
//            configBuilder.setKConnections(allowPoolingSslConnections);
//            return (U)this;
//        }

//        /**
//         * Remove an {@link org.asynchttpclient.filter.ResponseFilter} that will be invoked as soon as the response is
//         * received, and before {@link org.asynchttpclient.AsyncHandler#onStatusReceived(org.asynchttpclient.HttpResponseStatus)}.
//         *
//         * @param responseFilter an {@link org.asynchttpclient.filter.ResponseFilter}
//         * @return (U)this
//         */
//        public RestClient.Builder removeResponseFilter(ResponseFilter responseFilter) {
//            configBuilder.removeResponseFilter(responseFilter);
//            return (U)this;
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
    public U setUseProxyProperties(boolean useProxyProperties) {
        configBuilder.setUseProxyProperties(useProxyProperties);
        return (U)this;
    }

    /**
     * Sets whether AHC should use the default JDK ProxySelector to select a proxy server.
     * <p>
     * See http://docs.oracle.com/javase/7/docs/api/java/net/ProxySelector.html
     *
     * @param useProxySelector whether AHC should use the default JDK ProxySelector to select a proxy server.
     */
    public U setUseProxySelector(boolean useProxySelector) {
        configBuilder.setUseProxySelector(useProxySelector);
        return (U)this;
    }

//        /**
//         * Add an {@link org.asynchttpclient.filter.ResponseFilter} that will be invoked as soon as the response is
//         * received, and before {@link org.asynchttpclient.AsyncHandler#onStatusReceived(org.asynchttpclient.HttpResponseStatus)}.
//         *
//         * @param responseFilter an {@link org.asynchttpclient.filter.ResponseFilter}
//         * @return (U)this
//         */
//        public RestClient.Builder addResponseFilter(ResponseFilter responseFilter) {
//            configBuilder.addResponseFilter(responseFilter);
//            return (U)this;
//        }

    /**
     * Set the maximum number of HTTP redirect
     *
     * @param maxRedirects the maximum number of HTTP redirect
     * @return a {@link Builder}
     */
    public U setMaxRedirects(int maxRedirects) {
        configBuilder.setMaxRedirects(maxRedirects);
        return (U)this;
    }

    //@Deprecated "Use setUserInsecureTrustaManager"
    public U setAcceptAnyCertificate(boolean acceptAnyCertificate) {
        configBuilder.setUseInsecureTrustManager(acceptAnyCertificate);
        return (U)this;
    }

    public U setUseInsecureTrustManager(boolean useInsecureTrustManager) {
        configBuilder.setUseInsecureTrustManager(useInsecureTrustManager);
        return (U)this;
    }

    /**
     * Configures this AHC instance to be strict in it's handling of 302 redirects
     * in a POST/Redirect/GET situation.
     *
     * @param strict302Handling strict handling
     * @return (U)this
     * @since 1.7.2
     */
    public U setStrict302Handling(boolean strict302Handling) {
        configBuilder.setStrict302Handling(strict302Handling);
        return (U)this;
    }

    /**
     * Set the maximum time in millisecond connection can be added to the pool for further reuse
     * <p>
     * <p> Default is -1 (no TTL set)</p>
     *
     * @param connectionTTL the maximum time in millisecond connection can be added to the pool for further reuse
     * @return a {@link Builder}
     */
    public U setConnectionTTL(int connectionTTL) {
        //TODO deprecate and change to camelcase
        configBuilder.setConnectionTtl(connectionTTL);
        return (U)this;
    }

    /**
     * Set the USER_AGENT header value
     *
     * @param userAgent the USER_AGENT header value
     * @return a {@link Builder}
     */
    public U setUserAgent(String userAgent) {
        configBuilder.setUserAgent(userAgent);
        return (U)this;
    }

    /**
     * Set to true to enable HTTP redirect
     *
     * @param followRedirect@return a {@link Builder}
     */
    public U setFollowRedirect(boolean followRedirect) {
        configBuilder.setFollowRedirect(followRedirect);
        return (U)this;
    }


//        /**
//         * Add an {@link org.asynchttpclient.filter.RequestFilter} that will be invoked before {@link org.asynchttpclient.AsyncHttpClient#executeObservably(org.asynchttpclient.Request)}
//         *
//         * @param requestFilter {@link org.asynchttpclient.filter.RequestFilter}
//         * @return (U)this
//         */
//        public RestClient.Builder addRequestFilter(RequestFilter requestFilter) {
//            configBuilder.addRequestFilter(requestFilter);
//            return (U)this;
//        }

//        /**
//         * Add an {@link org.asynchttpclient.filter.IOExceptionFilter} that will be invoked when an {@link java.io.IOException}
//         * occurs during the download/upload operations.
//         *
//         * @param ioExceptionFilter an {@link org.asynchttpclient.filter.ResponseFilter}
//         * @return (U)this
//         */
//        public RestClient.Builder addIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
//            configBuilder.addIOExceptionFilter(ioExceptionFilter);
//            return (U)this;
//        }

    /**
     * Disable automatic url escaping
     *
     * @param disableUrlEncodingForBoundedRequests disables the url encoding if set to true
     * @return (U)this Builder
     * @deprecated Use setDisableUrlEncodingForBoundRequests
     */
    @Deprecated
    public U setDisableUrlEncodingForBoundedRequests(boolean disableUrlEncodingForBoundedRequests) {
        configBuilder.setDisableUrlEncodingForBoundRequests(disableUrlEncodingForBoundedRequests);
        return (U)this;
    }

    /**
     * Disable automatic url escaping
     *
     * @param disableUrlEncodingForBoundedRequests disables the url encoding if set to true
     * @return (U)this Builder
     */
    public U setDisableUrlEncodingForBoundRequests(boolean disableUrlEncodingForBoundedRequests) {
        configBuilder.setDisableUrlEncodingForBoundRequests(disableUrlEncodingForBoundedRequests);
        return (U)this;
    }

    /**
     * Set the maximum time in millisecond an {@link org.asynchttpclient.AsyncHttpClient} waits until the response is completed.
     *
     * @param requestTimeout the maximum time in millisecond an {@link org.asynchttpclient.AsyncHttpClient} waits until the response is completed.
     * @return a {@link Builder}
     */
    public U setRequestTimeout(int requestTimeout) {
        configBuilder.setRequestTimeout(requestTimeout);
        return (U)this;
    }

    /**
     * Set the {@link io.netty.handler.ssl.SslContext} for secure connection.
     *
     * @param sslContext the SSLContext for secure connection
     * @return a {@link Builder}
     */
    public U setSslContext(SslContext sslContext) {
        //TODO Can we hide this context behind javax.net.ssl interface (as before)??
        configBuilder.setSslContext(sslContext);
        return (U)this;
    }

    /**
     * Enforce HTTP compression.
     *
     * @param compressionEnforced true if compression is enforced
     * @return a {@link Builder}
     */
    public U setCompressionEnforced(boolean compressionEnforced) {
        configBuilder.setCompressionEnforced(compressionEnforced);
        return (U)this;
    }

    /**
     * Set the maximum time in millisecond an {@link org.asynchttpclient.AsyncHttpClient} will keep connection
     * idle in pool.
     * <p>
     * <p>Default is 60000 millis (1 min.)</p>
     *
     * @param pooledConnectionIdleTimeout@return a {@link Builder}
     */
    public U setPooledConnectionIdleTimeout(int pooledConnectionIdleTimeout) {
        configBuilder.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
        return (U)this;
    }

//        /**
//         * Remove an {@link org.asynchttpclient.filter.IOExceptionFilter} tthat will be invoked when an {@link java.io.IOException}
//         * occurs during the download/upload operations.
//         *
//         * @param ioExceptionFilter an {@link org.asynchttpclient.filter.ResponseFilter}
//         * @return (U)this
//         */
//        public RestClient.Builder removeIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
//            configBuilder.removeIOExceptionFilter(ioExceptionFilter);
//            return (U)this;
//        }

    /**
     * Set the maximum time in millisecond an {@link org.asynchttpclient.AsyncHttpClient} can stay idle.
     *
     * @param readTimeout the maximum time in millisecond an {@code RestClient} can stay idle.
     * @return a {@link Builder}
     */
    public U setReadTimeout(int readTimeout) {
        configBuilder.setReadTimeout(readTimeout);
        return (U)this;
    }

    public U setAwsEndPoint(AwsService service, AwsRegion region) {
        return setAwsEndPoint(service, region, AwsServiceEndPoint.defaultHostFor(service, region));
    }

    public U setAwsEndPoint(AwsService service, AwsRegion region, String domain) {
        if (service == null || region == null) {
            throw new IllegalArgumentException("No null arguments allowed");
        }
        this.awsServiceEndPoint = new AwsServiceEndPoint(service, region, domain);
        logger.info("Overwriting Base URL to " + this.awsServiceEndPoint.endPointUrl());
        this.setBaseUrl(this.awsServiceEndPoint.endPointUrl());
        this.isAws = true;
        return (U)this;
    }

    public U setAwsCredentialsProvider(AwsCredentialsProvider provider) {
        this.awsCredentialsProvider = provider;
        return (U)this;
    }

    public U logHeaders(List<String> headerNames) {
        this.headersToLog = new ArrayList<>(headerNames);
        return (U)this;
    }

    public U logFormParams(List<String> formParameterNames) {
        this.formParmsToLog = new ArrayList<>(formParameterNames);
        return (U)this;
    }

}


