package be.wegenenverkeer.rxhttp;

import com.ning.http.client.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Builder for Client Requests.
 *
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class ClientRequestBuilder {

    final private RequestBuilder inner;
    final private RxHttpClient client;

    //mutable state to check conformity to policy
    private boolean hasAcceptHeader;

    ClientRequestBuilder(RxHttpClient client) {
        inner = new RequestBuilder();
        this.client = client;
    }

    public ClientRequest build() {
        sanitizeConfiguration();
        return new ClientRequest(inner.build());
    }

    private void sanitizeConfiguration(){
        if(!hasAcceptHeader) {
            addHeader("Accept", client.getAccept());
        }
    }

//    public RequestBuilder addBodyPart(Part part) {
//        return inner.addBodyPart(part);
//    }
//
//    public RequestBuilder setQueryParams(List<Param> params) {
//        return inner.setQueryParams(params);
//    }
//
//    /**
//     * Deprecated - Use setBody(new InputStreamBodyGenerator(inputStream)).
//     *
//     * @param stream - An {@link InputStream}
//     * @return a {@link com.ning.http.client.RequestBuilder}
//     * @throws IllegalArgumentException
//     * @see #setBody(BodyGenerator) InputStreamBodyGenerator(inputStream)
//     * @see com.ning.http.client.generators.InputStreamBodyGenerator
//     * @deprecated {@link #setBody(BodyGenerator)} setBody(new InputStreamBodyGenerator(inputStream))
//     */
//    @Deprecated
//    public RequestBuilder setBody(InputStream stream) {
//        return inner.setBody(stream);
//    }

    public ClientRequestBuilder setBody(File file) {
        inner.setBody(file);
        return this;
    }

//    public ClientRequestBuilder setConnectionPoolKeyStrategy(ConnectionPoolPartitioning connectionPoolKeyStrategy) {
//        inner.setConnectionPoolKeyStrategy(connectionPoolKeyStrategy);
//    return this;}
//
//    public ClientRequestBuilder setSignatureCalculator(SignatureCalculator signatureCalculator) {
//        inner.setSignatureCalculator(signatureCalculator);
//    return this;}

    public ClientRequestBuilder setUrlRelativetoBase(String url) {
        inner.setUrl(toFullPath(url));
        return this;
    }

    public ClientRequestBuilder setBody(List<byte[]> data) {
        inner.setBody(data);
        return this;
    }

    public void resetQuery() {
        inner.resetQuery();
    }

//    public ClientRequestBuilder setInetAddress(InetAddress address) {
//        inner.setInetAddress(address);
//        return this;
//    }

//    public ClientRequestBuilder setHeaders(FluentCaseInsensitiveStringsMap headers) {
//        inner.setHeaders(headers);
//        return this;
//    }

//    public void resetCookies() {
//        inner.resetCookies();
//    }

//    public ClientRequestBuilder setCookies(Collection<Cookie> cookies) {
//        inner.setCookies(cookies);
//        return this;
//    }

//    public ClientRequestBuilder addQueryParams(List<Param> queryParams) {
//        inner.addQueryParams(queryParams);
//        return this;
//    }

    public ClientRequestBuilder addQueryParam(String name, String value) {
        inner.addQueryParam(name, value);
        return this;
    }

    public void resetNonMultipartData() {
        inner.resetNonMultipartData();
    }

//    public ClientRequestBuilder setUri(Uri uri) {
//        inner.setUri(uri);
//        return this;
//    }

//    public ClientRequestBuilder setFormParams(Map<String, List<String>> params) {
//        inner.setFormParams(params);
//        return this;
//    }

    public ClientRequestBuilder setBody(byte[] data) {
        inner.setBody(data);
        return this;
    }

    public ClientRequestBuilder setBody(String data) {
        inner.setBody(data);
        return this;
    }

    public ClientRequestBuilder setVirtualHost(String virtualHost) {
        inner.setVirtualHost(virtualHost);
        return this;
    }

    public ClientRequestBuilder addFormParam(String key, String value) {
        inner.addFormParam(key, value);
        return this;
    }

    public ClientRequestBuilder setHeaders(Map<String, Collection<String>> headers) {
        inner.setHeaders(headers);
        return this;
    }

    public ClientRequestBuilder setQueryParams(Map<String, List<String>> params) {
        inner.setQueryParams(params);
        return this;
    }

    public ClientRequestBuilder setBodyEncoding(String charset) {
        inner.setBodyEncoding(charset);
        return this;
    }

//    public ClientRequestBuilder setRealm(Realm realm) {
//        inner.setRealm(realm);
//        return this;
//    }

    public ClientRequestBuilder setMethod(String method) {
        inner.setMethod(method);
        return this;
    }

    public ClientRequestBuilder setContentLength(int length) {
        inner.setContentLength(length);
        return this;
    }

    public ClientRequestBuilder setRequestTimeout(int requestTimeout) {
        inner.setRequestTimeout(requestTimeout);
        return this;
    }

//    public ClientRequestBuilder setProxyServer(ProxyServer proxyServer) {
//        inner.setProxyServer(proxyServer);
//        return this;
//    }

    public ClientRequestBuilder setRangeOffset(long rangeOffset) {
        inner.setRangeOffset(rangeOffset);
        return this;
    }

//    public ClientRequestBuilder setBody(BodyGenerator bodyGenerator) {
//        inner.setBody(bodyGenerator);
//        return this;
//    }

    public ClientRequestBuilder addHeader(String name, String value) {
        if (name.equalsIgnoreCase("Accept")) {
            hasAcceptHeader = true;
        }
        inner.addHeader(name, value);
        return this;
    }

    public void resetFormParams() {
        inner.resetFormParams();
    }

    public ClientRequestBuilder setHeader(String name, String value) {
        inner.setHeader(name, value);
        return this;
    }

    public void resetMultipartData() {
        inner.resetMultipartData();
    }

//    public ClientRequestBuilder setLocalInetAddress(InetAddress address) {
//        inner.setLocalInetAddress(address);
//        return this;
//    }

//    public ClientRequestBuilder setFormParams(List<Param> params) {
//        inner.setFormParams(params);
//        return this;
//    }
//
//    public ClientRequestBuilder addCookie(Cookie cookie) {
//        inner.addCookie(cookie);
//        return this;
//    }

//    public ClientRequestBuilder addOrReplaceCookie(Cookie c) {
//        inner.addOrReplaceCookie(c);
//        return this;
//    }

    public ClientRequestBuilder setFollowRedirects(boolean followRedirects) {
        inner.setFollowRedirects(followRedirects);
        return this;
    }

    private String toFullPath(String path) {
        String p = chopFirstForwardSlash(path);
        return client.getBaseUrl() + "/" + p;
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


}
