package be.wegenenverkeer.rxhttp.rxjava;

import io.netty.buffer.ByteBuf;
import io.reactivex.rxjava3.core.Flowable;
import org.asynchttpclient.request.body.Body;
import org.asynchttpclient.request.body.generator.BodyGenerator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A BodyGenerator that subscribes to an Observable and creates a com.ning.http.client.Body from it.
 * You can specify the capacity of the underlying blocking queue.
 * By default the subscription happens from the code that calls the getBody() method.
 * This can lead to deadlocks with respect to the queueCapacity, because when the queue is full the onNext call will block.
 * To avoid this call subscribeOn before passing in the Observable with a Scheduler specific for your use case, f.e. Schedulers.io()
 * Otherwise this is fully back-pressured.
 */
public class FlowableBodyGenerator implements BodyGenerator {

    private static final Logger logger = LoggerFactory.getLogger(FlowableBodyGenerator.class);

    private static final int DEFAULT_CAPACITY = 10;

    private final Flowable<byte[]> observable;
    private Subscription subscription;

    private BlockingQueue<BodyPart> queue;
    private volatile Throwable throwable = null;

    public FlowableBodyGenerator(Flowable<byte[]> observable) {
        this(observable, DEFAULT_CAPACITY);
    }

    public FlowableBodyGenerator(Flowable<byte[]> observable, int queueCapacity) {
        this.observable = observable;
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
    }

    @Override
    public Body createBody() {
        this.observable.subscribe(new Subscriber<byte[]>() {


            @Override
            public void onComplete() {
                try {
                    queue.put(new BodyPart(new byte[0], true)); //blocks until space available in queue
                } catch (InterruptedException e) {
                    FlowableBodyGenerator.logger.warn("Interrupted", e);
                }
            }

            @Override
            public void onError(Throwable t) {
                FlowableBodyGenerator.logger.warn("Unable to read", t);
                FlowableBodyGenerator.this.throwable = t;
            }

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                subscription.request(queue.remainingCapacity());
            }

            @Override
            public void onNext(byte[] bytes) {
                try {
                    if (bytes.length > 0) {
                        queue.put(new BodyPart(bytes, false)); //blocks until space available in queue
                    }
                    subscription.request(Math.max(queue.remainingCapacity(), 1)); // always request at least one
                } catch (InterruptedException e) {
                    FlowableBodyGenerator.logger.warn("Interrupted", e);
                }
            }
        });
        return new ObservableBody();
    }

    protected class ObservableBody implements Body {

        @Override
        public long getContentLength() {
            return -1;
        }

        @Override
        public BodyState transferTo(ByteBuf targetBuf) throws IOException {
            if (throwable != null) {
                throw new IOException("observable onError was called", throwable);
            }

            BodyPart nextPart = queue.peek();

            //no data available
            if (nextPart == null) {
                return BodyState.SUSPEND;
            }

            if (nextPart.isLast) {
                return BodyState.STOP;
            }

            // no more bytes available in nextPart
            if (nextPart.buffer.remaining() == 0) {
                queue.remove();
                transferTo(targetBuf);
            }

            // there is data available
            int size = Math.min(nextPart.buffer.remaining(), targetBuf.writableBytes());
            int position = nextPart.buffer.position();
            if (size > 0) {
                targetBuf.writeBytes(nextPart.buffer.array(), 0, size);
                nextPart.buffer.position(position + size);
            }

            // if all data was read, remove it from the queue
            if (!nextPart.buffer.hasRemaining()) {
                queue.remove();
            }

            if (size == -1) {
                System.out.println("Oops, returning -1");
            }

            return BodyState.CONTINUE;
        }

        @Override
        public void close() {
            FlowableBodyGenerator.this.subscription.cancel();
        }
    }

    private class BodyPart {

        private final ByteBuffer buffer;
        private final boolean isLast;

        private BodyPart(byte[] bytes, boolean isLast) {
            this.buffer = ByteBuffer.wrap(bytes);
            this.isLast = isLast;
        }


    }

}
