package be.wegenenverkeer.rxhttp;

/**
 * Unchecked Exception for HTTP Client errors (4xx status code).
 *
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class HttpServerError extends HttpError{

    public HttpServerError(int statusCode) {
        super(statusCode);
    }

    public HttpServerError(int statusCode, String message) {
        super(statusCode, message);
    }

}
