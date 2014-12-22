package be.wegenenverkeer.rxhttp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An abstract base class for implementations that process the {@code ServerResponseElement}s that are
 * returned by a HTTP request. Implementors should provide the processPart(byte[]) method.
 *
 * <p>Response status and headers are read whenever offered in the process() method, and the response body (in whole or
 * part by part) are processed by the processPart() method.</p>
 *
 * <p>This class is mutable, side-effecting and not thread-safe.</p>
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
abstract public class MutableResponseProcessor {

    private int statusCode;

    private Optional<String> statusText;

    private Map<String, List<String>>  headers;

    /**
     * Processes the specified {@code ServerResponseElement}
     *
     * @param el the {@code ServerResponseElement} to process
     */
    public void process(ServerResponseElement el) {
        el.match(

                (status) -> {
                    statusCode = status.getStatusCode();
                    statusText = status.getStatusText();
                    return true;
                },

                (header) -> {
                 headers = header.getHeaders();
                    return true;
                },

                (part) -> {
                    processPart(part.getBodyPartBytes());
                    return true;
                },

                (response) -> {
                    statusCode = response.getStatusCode();
                    statusText = response.getStatusText();
                    headers = response.getHeaders();
                    processPart(response.getResponseBodyAsBytes());
                    return true;
                }

        );
    }

    /**
     * Processes the response body part
     *
     * @param bytes the response body as a byte array
     */
    abstract void processPart(byte[] bytes);

    /**
     * Returns the HTTP Statuscode
     *
     * @return the HTTP Statuscode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the Status text, if any
     * @return Some status text, or None
     */
    public Optional<String> getStatusText() {
        return statusText;
    }

    /**
     * Returns the HTTP Response headers
     *
     * @return the HTTP Response headers
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

}
