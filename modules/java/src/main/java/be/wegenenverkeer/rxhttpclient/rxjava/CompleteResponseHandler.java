package be.wegenenverkeer.rxhttpclient.rxjava;

import be.wegenenverkeer.rxhttpclient.HttpClientError;
import be.wegenenverkeer.rxhttpclient.HttpServerError;
import be.wegenenverkeer.rxhttpclient.ServerResponse;
import org.asynchttpclient.Response;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Karel Maesen, Geovise BVBA on 26/02/15.
 */
class CompleteResponseHandler {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    public static void withCompleteResponse(
            Response response,
            Consumer<Response> handleSuccess,
            Consumer<Throwable> handleClientError,
            Consumer<Throwable> handleServerError
    ) {
        int status = response.getStatusCode();
        executor.execute(() -> {
            if (status < 400) {
                handleSuccess.accept(response);
            } else if (status < 500) {
                handleClientError.accept(new HttpClientError(status, ServerResponse.wrap(response), "request failed with status = " + response.getStatusText()));
            } else {
                handleServerError.accept(new HttpServerError(status, ServerResponse.wrap(response), "request failed with status = " + response.getStatusText()));
            }
        });
    }

}
