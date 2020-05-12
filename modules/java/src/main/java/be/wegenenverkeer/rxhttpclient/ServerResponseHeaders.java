package be.wegenenverkeer.rxhttpclient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * The headers of an HTTP response
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseHeaders extends ServerResponseElement {

    /**
     * Returns all response headers
     *
     * @return all response headers
     */
    public Map<String, List<String>> getHeaders();

    /**
     * Returns some Content-Type if present in response header, None otherwise
     *
     * @return some Content-Type if present in response header, None otherwise
     */
    public Optional<String> getContentType();

    /**
     * Returns the HTTP Response header values for the specified header name.
     *
     * @param name the header name
     * @return the HTTP Response header values for the specified header name.
     */
    public List<String> getHeaders(String name);

    /**
     * Returns some value for the specified header name, or None if not present in the response headers.
     *
     * @param name header name to look up.
     * @return some value for the specified header name, or None if not present in the response headers.
     */
    public Optional<String> getHeader(String name);


    /**
     * {@inheritDoc}
     */
    @Override
    default <T> T match(Function<ServerResponseStatus, T> matchStatus,
                        Function<ServerResponseHeaders, T> matchHeaders,
                        Function<ServerResponseBodyPart, T> matchBodyPart,
                        Function<ServerResponse, T> matchServerResponse) {
        return matchHeaders.apply(this);
    }

}
