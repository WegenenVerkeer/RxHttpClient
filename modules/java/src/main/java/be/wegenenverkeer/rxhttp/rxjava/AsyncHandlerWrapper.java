package be.wegenenverkeer.rxhttp.rxjava;

import be.wegenenverkeer.rxhttp.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.subjects.BehaviorSubject;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

/**
 * A {@link AsyncHandler} that pushes received items to a specified {@link BehaviorSubject}
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
class AsyncHandlerWrapper implements AsyncHandler<Boolean> {

    //Note: we don't use the StreamedAsyncHandler because on very small responses, the Publisher gets shut down before it can start emitting
    //values (or so it seems).
    // See also this message: https://groups.google.com/forum/#!searchin/asynchttpclient/streamedasynchandler%7Csort:date/asynchttpclient/h5E98f50Zco/b6fgEtTbAQAJ

    final private static Logger logger = LoggerFactory.getLogger(AsyncHandlerWrapper.class);
    final private BehaviorProcessor<ServerResponseElement> subject;

    /**
     * Constructs an instance.
     *
     * @param subject BehaviorSubject wrapped by this instance
     */
    AsyncHandlerWrapper(BehaviorProcessor<ServerResponseElement> subject) {
        this.subject = subject;
    }

    /**
     * Invoked when an unexpected exception occurs during the processing of the response. The exception may have been
     * produced by implementation of onXXXReceived method invocation.
     *
     * @param t a {@link Throwable}
     */
    @Override
    public void onThrowable(Throwable t) {
        subject.onError(t);
    }

    /**
     * Invoked as soon as some response body part are received. Could be invoked many times.
     *
     * @param bodyPart response's body part.
     * @return a {@link org.asynchttpclient.AsyncHandler.State} telling to CONTINUE or ABORT the current processing.
     */
    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) {
        if(!subject.hasSubscribers()) {
            trace("No observers: Aborting.");
            //bodyPart.markUnderlyingConnectionAsToBeClosed();
            onCompleted(); //send the uncompleted message
            return State.ABORT;
        }
        subject.onNext(new ServerResponseBodyPartImpl(bodyPart.getBodyPartBytes(), bodyPart.isLast()));

        return State.CONTINUE;
    }

    private String toUtf8String(ServerResponseBodyPart bodyPart) {
        try {
            return new String(bodyPart.getBodyPartBytes(), "UTF8");
        } catch (UnsupportedEncodingException e) {
           return "<<< binary body part >>>";
        }
    }

    //we don't check for hasObservers in the onStatusReceived() and onHeadersReceived(). This guarantees that
    //processing continues until some response body parts are received, after which the connection can be
    //marked as to be closed.

    /**
     * Invoked as soon as the HTTP status line has been received
     *
     * @param responseStatus the status code and test of the response
     * @return a {@link org.asynchttpclient.AsyncHandler.State} telling to CONTINUE or ABORT the current processing.
     */
    @Override
    public State onStatusReceived(HttpResponseStatus responseStatus) {
        final int statuscode = responseStatus.getStatusCode();

        if (statuscode >= 400 && statuscode < 500) {
            subject.onError(new HttpClientError(statuscode, null, responseStatus.getStatusText()));
        } else if (statuscode >= 500) {
            subject.onError(new HttpServerError(statuscode, null, responseStatus.getStatusText()));
        }

        subject.onNext(new ServerResponseStatus() {
            @Override
            public int getStatusCode() {
                return statuscode;
            }

            @Override
            public Optional<String> getStatusText() {
                return Optional.ofNullable(responseStatus.getStatusText());
            }

            public String toString(){
                return String.format("Server response: %d", statuscode);
            }
        });
        return State.CONTINUE;
    }

    /**
     * Invoked as soon as the HTTP headers has been received. Can potentially be invoked more than once if a broken server
     * sent trailing headers.
     *
     * @param headers the HTTP headers.
     * @return a {@link org.asynchttpclient.AsyncHandler.State} telling to CONTINUE or ABORT the current processing.
     */
    @Override
    public State onHeadersReceived(HttpHeaders headers) {
        subject.onNext(new ServerResponseHeadersImpl(headers));
        return State.CONTINUE;
    }

    /**
     * Invoked once the HTTP response processing is finished.
     * <p>
     * <p>
     * Gets always invoked as last callback method.
     *
     * @return T Value that will be returned by the associated {@link java.util.concurrent.Future}
     */
    @Override
    public Boolean onCompleted() {
        trace("Completed");
        subject.onComplete();
        return true;
    }

    private static void trace(String msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(msg);
        }
    }

}