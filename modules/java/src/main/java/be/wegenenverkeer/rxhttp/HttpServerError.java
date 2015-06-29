package be.wegenenverkeer.rxhttp;

import com.ning.http.client.Response;

/**
 * Unchecked Exception for HTTP Client errors (4xx status code).
 *
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class HttpServerError extends HttpError{

    public HttpServerError(int statusCode, ServerResponse response) {
        super(statusCode, response);
    }

    public HttpServerError(int statusCode, ServerResponse response, String message) {
        super(statusCode, response, message);
    }

}
