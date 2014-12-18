package be.wegenenverkeer.rest;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.uri.Uri;
import scala.Some;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wraps an Async Response delegate.
 *
 * <p>This wrapper makes it easier to work with lambda expressions because the
 * checked IOExceptions are wrapped in RuntimeExceptions.
 * </p>
 *
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


    public String getResponseBody(String charset) {
        try {
            return response.getResponseBody(charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResponseBody()  {
        try {
            return response.getResponseBody();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getResponseBodyAsBytes() {
        try {
            return response.getResponseBodyAsBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getResponseBodyAsStream() {
        try {
            return response.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, List<String>> getHeaders() {
        return response.getHeaders();
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
        try {
            return response.getResponseBodyAsByteBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        try {
            return response.getResponseBodyExcerpt(maxLength);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResponseBodyExcerpt(int maxLength, String charset) {
        try {
            return response.getResponseBodyExcerpt(maxLength, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(response.getHeader(name));
    }

    @Override
    public byte[] getBodyPartBytes() {
        return getResponseBodyAsBytes();
    }

}
