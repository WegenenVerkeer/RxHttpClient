package be.wegenenverkeer.rxhttpclient.rxjava;

import be.wegenenverkeer.rxhttpclient.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.reactivex.rxjava3.core.Emitter;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Karel Maesen, Geovise BVBA on 05/05/2020.
 */
public class QueueingAsyncHandler implements AsyncHandler<Boolean> {


    final private static Logger logger = LoggerFactory.getLogger(QueueingAsyncHandler.class);

    private static void trace(String msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(Thread.currentThread().getName() +" - " +  msg);
        }
    }

    //TODO -- make the queue size configurable
    final private BlockingQueue<Notification> queue = new ArrayBlockingQueue<>(1024);
    final AtomicBoolean isCancelled = new AtomicBoolean(false);

    @Override
    public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        final int statuscode = responseStatus.getStatusCode();

        if (statuscode >= 400 && statuscode < 500) {
            queue.put( new ErrorNotification(new HttpClientError(statuscode, null, responseStatus.getStatusText())));
            return State.CONTINUE;
        } else if (statuscode >= 500) {
            queue.put( new ErrorNotification(new HttpServerError(statuscode, null, responseStatus.getStatusText())));
            return State.CONTINUE;
        }

        queue.put( new NextNotification(new ServerResponseStatus() {
            @Override
            public int getStatusCode() {
                return statuscode;
            }

            @Override
            public Optional<String> getStatusText() {
                return Optional.ofNullable(responseStatus.getStatusText());
            }

            public String toString() {
                return String.format("Server response: %d", statuscode);
            }
        }));
        return State.CONTINUE;
    }

    @Override
    public State onHeadersReceived(HttpHeaders headers) throws Exception {
        queue.put(new NextNotification(new ServerResponseHeadersImpl(headers)));
        return State.CONTINUE;
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {

        trace("Receiving body");
        if(isCancelled.get()) {
            trace("Aborting because cancelled");
            return State.ABORT;
        }
        trace("Putting value");
        queue.put(new NextNotification(new ServerResponseBodyPartImpl(bodyPart.getBodyPartBytes(), bodyPart.isLast())));
        return State.CONTINUE;
    }

    @Override
    public void onThrowable(Throwable t) {
        try {
            queue.put(new ErrorNotification(t));
        } catch (InterruptedException e) {
            logger.error("Interrupt exception when trying to enqueue throwable", t);
        }
    }

    @Override
    public Boolean onCompleted() throws Exception {
        queue.put(new CompletionNotification());
        return true;
    }

    public void cancel(){
        trace("Cancel() invoked");
        isCancelled.set(true);
        //we need to drain the queue so that the thread from the RxHttpClient is no longer blocked in case the
        //queue has already filled to capacity.
        drainQueue();
    }

    private void drainQueue() {
        queue.drainTo(new ArrayList<>());
    }

    public void emitTo(Emitter<ServerResponseElement> emitter){

        try {
            Notification n = queue.take();
            trace("Emitting next notification");
            if(n.isNext()){
                emitter.onNext(((NextNotification)n).element);
                return;
            }
            if (n.isCompletion()){
                emitter.onComplete();
            }
            if(n.isError()){
                emitter.onError( ((ErrorNotification)n).error);
            }
        } catch (InterruptedException e) {
            emitter.onError(e);
        }
    }

    static abstract class Notification{
        abstract boolean isError();
        abstract boolean isCompletion();
        abstract boolean isNext();
    }



    static class ErrorNotification extends Notification {

        final Throwable error;

        ErrorNotification(Throwable t){
            error=t;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public boolean isCompletion() {
            return false;
        }

        @Override
        public boolean isNext() {
            return false;
        }

    }

    static class NextNotification extends Notification{

        final ServerResponseElement element;

        public NextNotification(ServerResponseElement element) {
            this.element = element;
        }

        @Override
        boolean isError() {
            return false;
        }

        @Override
        boolean isCompletion() {
            return false;
        }

        @Override
        boolean isNext() {
            return true;
        }
    }

    static class CompletionNotification extends Notification{

        @Override
        boolean isError() {
            return false;
        }

        @Override
        boolean isCompletion() {
            return true;
        }

        @Override
        boolean isNext() {
            return false;
        }
    }
}
