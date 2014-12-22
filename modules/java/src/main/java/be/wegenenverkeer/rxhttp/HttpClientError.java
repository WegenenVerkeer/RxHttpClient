package be.wegenenverkeer.rxhttp;

/**
 * Unchecked Exception for HTTP Client errors (4xx status code).
 *
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class HttpClientError  extends HttpError{

    public HttpClientError(int statusCode) {
        super(statusCode);
    }

    public HttpClientError(int statusCode, String message) {
        super(statusCode, message);
    }

}

