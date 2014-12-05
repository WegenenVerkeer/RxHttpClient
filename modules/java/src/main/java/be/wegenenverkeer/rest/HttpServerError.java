package be.wegenenverkeer.rest;

/**
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
