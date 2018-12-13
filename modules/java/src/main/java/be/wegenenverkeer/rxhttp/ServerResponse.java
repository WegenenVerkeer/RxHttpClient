package be.wegenenverkeer.rxhttp;

import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.Response;
import org.asynchttpclient.uri.Uri;
import org.asynchttpclient.util.HttpUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Wraps an Async Response delegate.
 * <p>
 * <p>This wrapper makes it easier to work with lambda expressions because the
 * checked IOExceptions are wrapped in RuntimeExceptions.
 * </p>
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class ServerResponse implements ServerResponseStatus, ServerResponseHeaders, ServerResponseBodyPart {

    final private Response response;

    public static ServerResponse wrap(Response response) {
        return new ServerResponse(response);
    }

    public ServerResponse(Response response) {
        this.response = response;
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public Optional<String> getStatusText() {
        return Optional.ofNullable(response.getStatusText());
    }


    public String getResponseBody(Charset charset) {
        return response.getResponseBody(charset);
    }

    public String getResponseBody() {

        Optional<Charset> parsedCharset =
                Optional
                        .ofNullable(response.getContentType()) // content-type can be null
                        .flatMap(contentType ->
                                // parseCharset can also return null
                                Optional.ofNullable(HttpUtils.extractContentTypeCharsetAttribute(contentType))
                        );

        Charset charset = parsedCharset.orElse(StandardCharsets.UTF_8);
        return getResponseBody(charset);
    }

    public byte[] getResponseBodyAsBytes() {
        return response.getResponseBodyAsBytes();
    }

    public InputStream getResponseBodyAsStream() {
        return response.getResponseBodyAsStream();
    }

    public Map<String, List<String>> getHeaders() {
        return
                CompatUtilities.headersToMap(response.getHeaders());
    }

    public boolean hasResponseHeaders() {
        return response.hasResponseHeaders();
    }

    public boolean hasResponseStatus() {
        return response.hasResponseStatus();
    }

    public List<Cookie> getCookies() {
        return response.getCookies();
    }

    public boolean isRedirected() {
        return response.isRedirected();
    }

    public ByteBuffer getResponseBodyAsByteBuffer() {
        return response.getResponseBodyAsByteBuffer();
    }

    public Optional<String> getContentType() {
        return Optional.ofNullable(response.getContentType());
    }

    public List<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    public boolean hasResponseBody() {
        return response.hasResponseBody();
    }

    public Uri getUri() {
        return response.getUri();
    }

    public String getResponseBodyExcerpt(int maxLength) {
        return CompatUtilities.bodyExcerpt(response, maxLength);
    }

    public String getResponseBodyExcerpt(int maxLength, String charset) {
        return CompatUtilities.bodyExcerpt(response, maxLength, charset);

    }

    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(response.getHeader(name));
    }

    @Override
    public byte[] getBodyPartBytes() {
        return getResponseBodyAsBytes();
    }


    @Override
    public <T> T match(
            Function<ServerResponseStatus, T> matchStatus,
            Function<ServerResponseHeaders, T> matchHeaders,
            Function<ServerResponseBodyPart, T> matchBodyPart,
            Function<ServerResponse, T> matchServerResponse) {
        return matchServerResponse.apply(this);
    }

}
