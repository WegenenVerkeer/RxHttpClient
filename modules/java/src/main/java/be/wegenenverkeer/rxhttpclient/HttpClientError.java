package be.wegenenverkeer.rxhttpclient;

/**
 * Unchecked Exception for HTTP Client errors (4xx status code).
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class HttpClientError extends HttpError {

    public HttpClientError(int statusCode) {
        super(statusCode, null);
    }

    public HttpClientError(int statusCode, ServerResponse response) {
        super(statusCode, response);
    }

    public HttpClientError(int statusCode, ServerResponse response, String message) {
        super(statusCode, response, message);
    }

}

