package be.wegenenverkeer.rest;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.exceptions.OnErrorFailedException;
import rx.subjects.AsyncSubject;

import java.util.function.Function;

import static be.wegenenverkeer.rest.ServerResponse.wrap;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
class AsyncCompletionHandlerWrapper<F> extends AsyncCompletionHandler<F> {

    final private static Logger logger = LoggerFactory.getLogger(AsyncCompletionHandlerWrapper.class);

    final private AsyncSubject<? super F> subject;
    final private Function<ServerResponse, F> handler;


    AsyncCompletionHandlerWrapper(AsyncSubject<? super F> subject, Function<ServerResponse, F> handler) {
        this.subject = subject;
        this.handler = handler;
    }

    @Override
    public F onCompleted(Response response) throws Exception {
        F value = null;
        try {
            try {
                int status = response.getStatusCode();
                if (status < 400) {
                    value = handler.apply(wrap(response));
                    subject.onNext(value);
                    subject.onCompleted();
                } else if (status >= 400 && status < 500) {
                    subject.onError(new HttpClientError(status, response.getStatusText() + "\n" + response.getResponseBody()));
                } else {
                    subject.onError(new HttpServerError(status, response.getStatusText() + "\n" + response.getResponseBody()));
                }
            } catch (Throwable t) {
                //TODO Should this logging not be done in the global onError handler? See Class RxJavaErrorHandler
                if (t instanceof OnErrorFailedException) {
                    logger.error("onError handler failed: " + t.getMessage(), t);
                }
                subject.onError(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public void onThrowable(Throwable t) {
        subject.onError(t);
    }


}
