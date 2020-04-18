package be.wegenenverkeer.rxhttp;

import java.util.Optional;

/**
 * Abstract Exception class for HTTP Error status codes.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
abstract public class HttpError extends RuntimeException {

    final private int statusCode;
    final private ServerResponse response;

    public HttpError(int statusCode, ServerResponse response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public HttpError(int statusCode, ServerResponse response, String message) {
        super(message);
        this.statusCode = statusCode;
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Optional<ServerResponse> getResponse() {
        return Optional.ofNullable(response);
    }
}
