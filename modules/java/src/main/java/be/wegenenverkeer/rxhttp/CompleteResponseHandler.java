package be.wegenenverkeer.rxhttp;

import org.asynchttpclient.Response;

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
        } else if (status < 500) {
            handleClientError.accept(new HttpClientError(status, ServerResponse.wrap(response), "request failed with status = "+response.getStatusText()));
        } else {
            handleServerError.accept(new HttpServerError(status, ServerResponse.wrap(response), "request failed with status = "+response.getStatusText()));
        }
    }

}
