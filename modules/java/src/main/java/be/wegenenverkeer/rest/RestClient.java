package be.wegenenverkeer.rest;

import com.ning.http.client.*;
import com.ning.http.client.date.TimeConverter;
import com.ning.http.client.filter.IOExceptionFilter;
import com.ning.http.client.filter.RequestFilter;
import com.ning.http.client.filter.ResponseFilter;

import rx.Observable;
import rx.subjects.AsyncSubject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * A REST API Client
 * Created by Karel Maesen, Geovise BVBA on 05/12/14.
 */
public class RestClient {

    final private AsyncHttpClient innerClient;
    final private RestClientConfig config;


    protected RestClient(AsyncHttpClient innerClient, RestClientConfig config) {
        this.innerClient = innerClient;
        this.config = config;
    }


    public <F> Observable<F> GET(String path, Function<Response, F> handler) {
        AsyncSubject<F> subject = AsyncSubject.create();
        innerClient
                .prepareGet(toFullPath(path))
                .addHeader("Accept", config.getAccept())
                .execute(new AsyncCompletionHandlerWrapper<>(subject, handler));
        return subject;
    }


    private String toFullPath(String path) {
        //TODO add testing to see if this is a genuine URL
        String p = chopFirstForwardSlash(path);
        return config.getBaseUrl() + "/" + p;
    }

    private static String chopLastForwardSlash(String url) {
        if (url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }


    private static String chopFirstForwardSlash(String url) {
        if (url.charAt(0) == '/') {
            url = url.substring(1, url.length());
        }
        return url;
    }


    class AsyncCompletionHandlerWrapper<F> extends AsyncCompletionHandler<F> {

        final private AsyncSubject<? super F> subject;
        final private Function<Response, F> handler;


        AsyncCompletionHandlerWrapper(AsyncSubject<? super F> subject, Function<Response, F> handler) {
            this.subject = subject;
            this.handler = handler;
        }

        @Override
        public F onCompleted(Response response) throws Exception {
            F value = null;
            int status = response.getStatusCode();
            try {
                //Everything above
                if (status < 400) {
                    value = handler.apply(response);
                    subject.onNext(value);
                    subject.onCompleted();
                } else if (status >= 400 && status < 500) {
                    subject.onError(new HttpClientError(status, response.getStatusText() + "\n" + response.getResponseBody()) );
                } else {
                    subject.onError(new HttpServerError(status, response.getStatusText() + "\n" + response.getResponseBody()) );
                }
            } catch (Throwable t) {
                subject.onError(t);
            }
            return value;
        }

        @Override
        public void onThrowable(Throwable t) {
            subject.onError(t);
        }


    }


    static class RestClientConfig {

        private String baseUrl = "http://localhost";
        private String Accept = "application/json";

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getAccept() {
            return Accept;
        }

        public void setAccept(String accept) {
            Accept = accept;
        }
    }


    static public class Builder {

        private AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
        final private RestClientConfig rcConfig = new RestClientConfig();

        public RestClient build() {
            return new RestClient(new AsyncHttpClient(configBuilder.build()), rcConfig);
        }


        public RestClient.Builder setAccept(String acceptHeaderValue) {
            rcConfig.setAccept(acceptHeaderValue);
            return this;
        }

        public RestClient.Builder setBaseUrl(String url) {
            url = chopLastForwardSlash(url);
            rcConfig.setBaseUrl(url);
            return this;
        }

        public RestClient.Builder setMaxConnections(int maxConnections) {
            configBuilder.setMaxConnections(maxConnections);
            return this;
        }

        public RestClient.Builder removeResponseFilter(ResponseFilter responseFilter) {
            configBuilder.removeResponseFilter(responseFilter);
            return this;
        }

        public RestClient.Builder setConnectionTTL(int connectionTTL) {
            configBuilder.setConnectionTTL(connectionTTL);
            return this;
        }

        public RestClient.Builder setExecutorService(ExecutorService applicationThreadPool) {
            configBuilder.setExecutorService(applicationThreadPool);
            return this;
        }

        public RestClient.Builder setUseProxyProperties(boolean useProxyProperties) {
            configBuilder.setUseProxyProperties(useProxyProperties);
            return this;
        }

        public RestClient.Builder setProxyServerSelector(ProxyServerSelector proxyServerSelector) {
            configBuilder.setProxyServerSelector(proxyServerSelector);
            return this;
        }

        public RestClient.Builder setDisableUrlEncodingForBoundedRequests(boolean disableUrlEncodingForBoundedRequests) {
            configBuilder.setDisableUrlEncodingForBoundedRequests(disableUrlEncodingForBoundedRequests);
            return this;
        }

        public RestClient.Builder setUserAgent(String userAgent) {
            configBuilder.setUserAgent(userAgent);
            return this;
        }

        public RestClient.Builder setIOThreadMultiplier(int multiplier) {
            configBuilder.setIOThreadMultiplier(multiplier);
            return this;
        }

        public RestClient.Builder setPooledConnectionIdleTimeout(int pooledConnectionIdleTimeout) {
            configBuilder.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout);
            return this;
        }

        public RestClient.Builder addRequestFilter(RequestFilter requestFilter) {
            configBuilder.addRequestFilter(requestFilter);
            return this;
        }

        public RestClient.Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            configBuilder.setHostnameVerifier(hostnameVerifier);
            return this;
        }

        public RestClient.Builder setAcceptAnyCertificate(boolean acceptAnyCertificate) {
            configBuilder.setAcceptAnyCertificate(acceptAnyCertificate);
            return this;
        }

        public RestClient.Builder removeIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
            configBuilder.removeIOExceptionFilter(ioExceptionFilter);
            return this;
        }

        public RestClient.Builder setMaxRequestRetry(int maxRequestRetry) {
            configBuilder.setMaxRequestRetry(maxRequestRetry);
            return this;
        }

        public RestClient.Builder setAllowPoolingSslConnections(boolean allowPoolingSslConnections) {
            configBuilder.setAllowPoolingSslConnections(allowPoolingSslConnections);
            return this;
        }

        public RestClient.Builder setMaxRedirects(int maxRedirects) {
            configBuilder.setMaxRedirects(maxRedirects);
            return this;
        }

        public RestClient.Builder setEnabledProtocols(String[] enabledProtocols) {
            configBuilder.setEnabledProtocols(enabledProtocols);
            return this;
        }

        public RestClient.Builder setCompressionEnforced(boolean compressionEnforced) {
            configBuilder.setCompressionEnforced(compressionEnforced);
            return this;
        }

        public RestClient.Builder addResponseFilter(ResponseFilter responseFilter) {
            configBuilder.addResponseFilter(responseFilter);
            return this;
        }

        public RestClient.Builder setFollowRedirect(boolean followRedirect) {
            configBuilder.setFollowRedirect(followRedirect);
            return this;
        }

        public RestClient.Builder setConnectTimeout(int connectTimeOut) {
            configBuilder.setConnectTimeout(connectTimeOut);
            return this;
        }

        public RestClient.Builder setStrict302Handling(boolean strict302Handling) {
            configBuilder.setStrict302Handling(strict302Handling);
            return this;
        }

        public RestClient.Builder setRequestTimeout(int requestTimeout) {
            configBuilder.setRequestTimeout(requestTimeout);
            return this;
        }

        public RestClient.Builder setUseRelativeURIsWithConnectProxies(boolean useRelativeURIsWithConnectProxies) {
            configBuilder.setUseRelativeURIsWithConnectProxies(useRelativeURIsWithConnectProxies);
            return this;
        }

        public RestClient.Builder setTimeConverter(TimeConverter timeConverter) {
            configBuilder.setTimeConverter(timeConverter);
            return this;
        }

        public RestClient.Builder addIOExceptionFilter(IOExceptionFilter ioExceptionFilter) {
            configBuilder.addIOExceptionFilter(ioExceptionFilter);
            return this;
        }

        public RestClient.Builder setWebSocketTimeout(int webSocketTimeout) {
            configBuilder.setWebSocketTimeout(webSocketTimeout);
            return this;
        }

        public RestClient.Builder setProxyServer(ProxyServer proxyServer) {
            configBuilder.setProxyServer(proxyServer);
            return this;
        }

        public RestClient.Builder setRealm(Realm realm) {
            configBuilder.setRealm(realm);
            return this;
        }

        public RestClient.Builder setEnabledCipherSuites(String[] enabledCipherSuites) {
            configBuilder.setEnabledCipherSuites(enabledCipherSuites);
            return this;
        }

        public RestClient.Builder setUseProxySelector(boolean useProxySelector) {
            configBuilder.setUseProxySelector(useProxySelector);
            return this;
        }

        public RestClient.Builder setSSLContext(SSLContext sslContext) {
            configBuilder.setSSLContext(sslContext);
            return this;
        }

        public RestClient.Builder setMaxConnectionsPerHost(int maxConnectionsPerHost) {
            configBuilder.setMaxConnectionsPerHost(maxConnectionsPerHost);
            return this;
        }

        public RestClient.Builder setAllowPoolingConnections(boolean allowPoolingConnections) {
            configBuilder.setAllowPoolingConnections(allowPoolingConnections);
            return this;
        }

        public RestClient.Builder setRemoveQueryParamsOnRedirect(boolean removeQueryParamOnRedirect) {
            configBuilder.setRemoveQueryParamsOnRedirect(removeQueryParamOnRedirect);
            return this;
        }

        public RestClient.Builder setAsyncHttpClientProviderConfig(AsyncHttpProviderConfig<?, ?> providerConfig) {
            configBuilder.setAsyncHttpClientProviderConfig(providerConfig);
            return this;
        }

        public RestClient.Builder removeRequestFilter(RequestFilter requestFilter) {
            configBuilder.removeRequestFilter(requestFilter);
            return this;
        }

        public RestClient.Builder setReadTimeout(int readTimeout) {
            configBuilder.setReadTimeout(readTimeout);
            return this;
        }
    }


}
