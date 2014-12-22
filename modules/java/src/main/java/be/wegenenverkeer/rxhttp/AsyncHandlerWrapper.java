package be.wegenenverkeer.rxhttp;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import rx.subjects.BehaviorSubject;

import java.util.Optional;

/**
 *  A {@link AsyncHandler} that pushes received items to a specified {@link BehaviorSubject}
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
class AsyncHandlerWrapper implements AsyncHandler<Boolean> {

    final private BehaviorSubject<ServerResponseElement> subject;

    /**
     * Constructs an instance.
     *
     * @param subject
     */
    AsyncHandlerWrapper(BehaviorSubject<ServerResponseElement> subject) {
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
     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     * @throws Exception if something wrong happens
     */
    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        if (!subject.hasObservers()) {
            bodyPart.markUnderlyingConnectionAsToBeClosed();
            onCompleted(); //send the uncompleted message
            return STATE.ABORT;
        }
        subject.onNext(
                (ServerResponseBodyPart)( bodyPart::getBodyPartBytes )
            );
        return STATE.CONTINUE;
    }

    //we don't check for hasObservers in the onStatusReceived() and onHeadersReceived(). This guarantees that
    //processing continues until some response body parts are received, after which the connection can be
    //marked as to be closed.

    /**
     * Invoked as soon as the HTTP status line has been received
     *
     * @param responseStatus the status code and test of the response
     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     * @throws Exception if something wrong happens
     */
    @Override
    public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        final int statuscode = responseStatus.getStatusCode();

        if (statuscode >= 400 && statuscode < 500) {
            subject.onError(new HttpClientError(statuscode, responseStatus.getStatusText()));
        } else if (statuscode >= 500) {
            subject.onError(new HttpServerError(statuscode, responseStatus.getStatusText()));
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
        });
        return STATE.CONTINUE;
    }

    /**
     * Invoked as soon as the HTTP headers has been received. Can potentially be invoked more than once if a broken server
     * sent trailing headers.
     *
     * @param headers the HTTP headers.
     * @return a {@link com.ning.http.client.AsyncHandler.STATE} telling to CONTINUE or ABORT the current processing.
     * @throws Exception if something wrong happens
     */
    @Override
    public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        subject.onNext(new ServerResponseHeadersBase(headers));
        return STATE.CONTINUE;
    }

    /**
     * Invoked once the HTTP response processing is finished.
     * <p>
     * <p>
     * Gets always invoked as last callback method.
     *
     * @return T Value that will be returned by the associated {@link java.util.concurrent.Future}
     * @throws Exception if something wrong happens
     */
    @Override
    public Boolean onCompleted() throws Exception {
        subject.onCompleted();
        return true;
    }
}