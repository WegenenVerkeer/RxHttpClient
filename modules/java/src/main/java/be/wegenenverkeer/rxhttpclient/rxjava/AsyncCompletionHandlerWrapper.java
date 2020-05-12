package be.wegenenverkeer.rxhttpclient.rxjava;

import be.wegenenverkeer.rxhttpclient.ServerResponse;
import io.reactivex.rxjava3.exceptions.ProtocolViolationException;
import io.reactivex.rxjava3.processors.AsyncProcessor;
import io.reactivex.rxjava3.subjects.AsyncSubject;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static be.wegenenverkeer.rxhttpclient.ServerResponse.wrap;
import static be.wegenenverkeer.rxhttpclient.rxjava.CompleteResponseHandler.withCompleteResponse;

/**
 * A {@link AsyncCompletionHandler} that pushes received items to a specified {@link AsyncSubject}
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
class AsyncCompletionHandlerWrapper<F> extends AsyncCompletionHandler<F> {

    final private static Logger logger = LoggerFactory.getLogger(AsyncCompletionHandlerWrapper.class);

    final private AsyncProcessor<? super F> subject;
    final private Function<ServerResponse, F> handler;


    /**
     * Constructs a new instance with specified subject and response transform
     *
     * @param subject   the subject that receives the ServerResponse, after transformation
     * @param transform the transformation function
     */
    AsyncCompletionHandlerWrapper(AsyncProcessor<? super F> subject, Function<ServerResponse, F> transform) {
        if (subject == null || transform == null) throw new IllegalArgumentException();
        this.subject = subject;
        this.handler = transform;
    }

    @Override
    public F onCompleted(Response response) {
        try {
            withCompleteResponse(
                    response,
                    (r) -> {
                        F value = handler.apply(wrap(r));
                        if (value != null) subject.onNext(value);
                        subject.onComplete();
                    },
                    subject::onError,
                    subject::onError
            );
        } catch (Throwable t) {
            //TODO Should this logging not be done in the global onError handler? See Class RxJavaErrorHandler
            if (t instanceof ProtocolViolationException) {
                logger.error("Protocol Violation Exception: " + t.getMessage(), t);
            }
            subject.onError(t);
        }
        return null;
    }

    @Override
    public void onThrowable(Throwable t) {
        subject.onError(t);
    }


}
