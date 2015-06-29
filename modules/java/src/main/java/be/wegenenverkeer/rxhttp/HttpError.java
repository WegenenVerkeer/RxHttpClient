package be.wegenenverkeer.rxhttp;

import com.ning.http.client.Response;

import java.util.Optional;

/**
 * Abstract Exception class for HTTP Error status codes.
 *
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
abstract public class HttpError extends RuntimeException {

    final private int statusCode;
    final private Optional<ServerResponse> response;

    public HttpError(int statusCode, ServerResponse response) {
        this.statusCode = statusCode;
        this.response = Optional.ofNullable(response);
    }

    public HttpError(int statusCode, ServerResponse response, String message) {
        super(message);
        this.statusCode = statusCode;
        this.response = Optional.ofNullable(response);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Optional<ServerResponse> getResponse() {
        return response;
    }
}
