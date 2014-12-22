package be.wegenenverkeer.rxhttp;

/**
 * Abstract Exception class for HTTP Error status codes.
 *
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
abstract public class HttpError extends RuntimeException {

    final private int statusCode;

    public HttpError(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpError(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
