package be.wegenenverkeer.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
abstract public class MutableResponseProcessor {

    private int statusCode;

    private Optional<String> statusText;

    private Map<String, List<String>>  headers;

    public MutableResponseProcessor process(ServerResponseElement el,
                                                          MutableResponseProcessor processor) {
        return el.match(

                (status) -> {
                    processor.statusCode = status.getStatusCode();
                    processor.statusText = status.getStatusText();
                    return processor;
                },

                (header) -> {
                    processor.headers = header.getHeaders();
                    return processor;
                },

                (part) -> {
                    processor.processPart(part.getBodyPartBytes());
                    return processor;
                },

                (response) -> {
                    processor.statusCode = response.getStatusCode();
                    processor.statusText = response.getStatusText();
                    processor.headers = response.getHeaders();
                    processor.processPart(response.getResponseBodyAsBytes());
                    return processor;
                }

        );
    }

    abstract void processPart(byte[] bytes);

    public int getStatusCode() {
        return statusCode;
    }

    public Optional<String> getStatusText() {
        return statusText;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

}
