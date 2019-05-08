package be.wegenenverkeer.rxhttp;

import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Param;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.oauth.ConsumerKey;
import org.asynchttpclient.oauth.OAuthSignatureCalculator;
import org.asynchttpclient.oauth.RequestToken;
import org.asynchttpclient.request.body.generator.BodyGenerator;
import org.asynchttpclient.request.body.multipart.ByteArrayPart;
import org.asynchttpclient.request.body.multipart.FilePart;
import org.asynchttpclient.request.body.multipart.StringPart;
import org.asynchttpclient.util.Utf8UrlEncoder;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static org.asynchttpclient.util.HttpConstants.Methods.GET;

/**
 * A Builder for Client Requests.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class ClientRequestBuilder {

    final private BoundRequestBuilder inner;
    final private RxHttpClient client;

    //mutable state to check conformity to policy
    private boolean hasAcceptHeader;

    private boolean isOAuth1 = false;
    private String clientKey;
    private String clientSecret;
    private String requestToken;
    private String requestSecret;

    ClientRequestBuilder(RxHttpClient client) {
        inner = new BoundRequestBuilder( client.inner(), GET, false);
        this.client = client;
    }

    public ClientRequest build() {
        sanitize();
        if (isOAuth1) {
            ConsumerKey consumerKey = new ConsumerKey(clientKey, clientSecret);
            RequestToken token = new RequestToken(requestToken, requestSecret);
            OAuthSignatureCalculator calc = new OAuthSignatureCalculator(consumerKey, token);
            inner.setSignatureCalculator(calc);
        }
        ClientRequest request = new ClientRequest(inner.build());
        signRequest(request);
        return request;
    }

    private void signRequest(ClientRequest request) {
        client.getRequestSigners().forEach(s -> s.sign(request));
    }

    private void sanitize() {
        if (!hasAcceptHeader && client.getAccept() != null) {
            addHeader("Accept", client.getAccept());
        }
    }

    /**
     * Adds a byte array as part in a multi-part request
     *
     * @param name             The name of the part, or <code>null</code>
     * @param bytes            the content of the part
     * @param contentType      The content type, or <code>null</code>
     * @param charset          The character encoding, or <code>null</code>
     * @param filename         The filename, or <code>null</code>
     * @param contentId        The content id, or <code>null</code>
     * @param transferEncoding The transfer encoding, or <code>null</code>
     * @return this {@code ClientRequestBuilder}
     */
    public ClientRequestBuilder addByteArrayBodyPart(String name, byte[] bytes, String contentType, Charset charset, String filename, String contentId, String transferEncoding) {
        inner.addBodyPart(new ByteArrayPart(name, bytes, contentType, charset, filename, contentId, transferEncoding));
        return this;
    }

    /**
     * Adds the content of a {@code File} as part in a multi-part request
     *
     * @param name             The name of the part, or <code>null</code>
     * @param file             the file containing the content of the part
     * @param contentType      The content type, or <code>null</code>
     * @param charset          The character encoding, or <code>null</code>
     * @param contentId        The content id, or <code>null</code>
     * @param transferEncoding The transfer encoding, or <code>null</code>
     * @return this {@code ClientRequestBuilder}
     */
    public ClientRequestBuilder addFileBodyPart(String name, File file, String contentType, Charset charset, String fileName, String contentId, String transferEncoding) {
        inner.addBodyPart(new FilePart(name, file, contentType, charset, fileName, contentId, transferEncoding));
        return this;
    }

    /**
     * Adds the specified {@code String} as part in a multi-part request
     *
     * @param name             The name of the part, or <code>null</code>
     * @param value            the content of the part
     * @param contentType      The content type, or <code>null</code>
     * @param charset          The character encoding, or <code>null</code>
     * @param contentId        The content id, or <code>null</code>
     * @param transferEncoding The transfer encoding, or <code>null</code>
     * @return this {@code ClientRequestBuilder}
     */
    public ClientRequestBuilder addStringBodyPart(String name, String value, String contentType, Charset charset, String contentId, String transferEncoding) {
        inner.addBodyPart(new StringPart(name, value, contentType, charset, contentId, transferEncoding));
        return this;
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
        return setUrlRelativetoBase(url, false);
    }

    public ClientRequestBuilder setUrlRelativetoBase(String url, boolean urlEncode) {
        if (urlEncode) {
            inner.setUrl(Utf8UrlEncoder.encodePath(toFullPath(url)));
        } else {
            inner.setUrl(toFullPath(url));
        }
        return this;
    }


    public ClientRequestBuilder setBody(List<byte[]> data) {
        inner.setBody(data);
        return this;
    }

    public ClientRequestBuilder setOAuth1(String clientKey, String clientSecret, String requestToken, String requestSecret) {
        this.isOAuth1 = true;
        this.clientKey = clientKey;
        this.clientSecret = clientSecret;
        this.requestToken = requestToken;
        this.requestSecret = requestSecret;
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

    public ClientRequestBuilder addQueryParams(List<Param> queryParams) {
        inner.addQueryParams(queryParams);
        return this;
    }

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

    public ClientRequestBuilder setFormParams(Map<String, List<String>> params) {
        inner.setFormParams(params);
        return this;
    }

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

    //TODO -- method should take Charset parameter
    public ClientRequestBuilder setBodyEncoding(String charset) {
        inner.setCharset(Charset.forName(charset));
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
        inner.setHeader(CONTENT_LENGTH, Integer.toString(length));
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

    public ClientRequestBuilder setBody(BodyGenerator bodyGenerator) {
        inner.setBody(bodyGenerator);
        return this;
    }

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

    public ClientRequestBuilder setFollowRedirects(boolean followRedirect) {
        inner.setFollowRedirect(followRedirect);
        return this;
    }

    private String toFullPath(String path) {
        String p = chopFirstForwardSlash(path);
        return client.getBaseUrl() + "/" + p;
    }

    private static String chopFirstForwardSlash(String url) {
        if (url.charAt(0) == '/') {
            url = url.substring(1);
        }
        return url;
    }


}
