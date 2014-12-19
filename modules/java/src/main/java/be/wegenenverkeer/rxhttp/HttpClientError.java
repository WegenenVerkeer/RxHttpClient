package be.wegenenverkeer.rxhttp;

/**
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

