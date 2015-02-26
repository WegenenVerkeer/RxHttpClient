package be.wegenenverkeer.rxhttp;

import com.ning.http.client.Response;

import java.io.IOException;
import java.util.function.Consumer;

/**
 *
 * Created by Karel Maesen, Geovise BVBA on 26/02/15.
 */
class CompleteResponseHandler {

    public static void withCompleteResponse(
            Response response,
            Consumer<Response> handleSuccess,
            Consumer<Throwable> handleClientError,
            Consumer<Throwable> handleServerError
    ) {
        int status = response.getStatusCode();
        if (status < 400) {
            handleSuccess.accept(response);
        } else if (status >= 400 && status < 500) {
            handleClientError.accept(new HttpClientError(status, response.getStatusText() + "\n" + getResponseBody(response)));
        } else {
            handleClientError.accept(new HttpServerError(status, response.getStatusText() + "\n" + getResponseBody(response)));
        }
    }

    private static String getResponseBody(Response response){
        String msg = null;
        try {
            msg = response.getResponseBody();
        } catch (IOException e) {
            // drop exception (is only an error message)
        }
        return msg;
    }

}
