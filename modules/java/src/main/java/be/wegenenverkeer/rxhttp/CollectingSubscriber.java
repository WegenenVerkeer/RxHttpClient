package be.wegenenverkeer.rxhttp;

import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 *
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
public class CollectingSubscriber<T> extends Subscriber<ServerResponseElement> {


    final private CompletableFuture<List<T>> cfuture = new CompletableFuture<>();
    final private List<T> accumulator = new ArrayList<>();

    final private MutableResponseProcessor processor;

    public CollectingSubscriber(Function<byte[], T> handlePart){
        processor = new MutableResponseProcessor() {
            @Override
            void processPart(byte[] bytes) {
                try {
                    accumulator.add(handlePart.apply(bytes));
                } catch(Throwable t) {
                    cfuture.completeExceptionally(t);
                }
            }
        };
    }

    /**
     * Notifies the Observer that the {@code Observable} has finished sending push-based notifications.
     * <p>
     * The {@code Observable} will not call this method if it calls {@link #onError}.
     */
    @Override
    public void onCompleted() {
        cfuture.complete(accumulator);
    }

    /**
     * Notifies the Observer that the {@code Observable} has experienced an error condition.
     * <p>
     * If the {@code Observable} calls this method, it will not thereafter call {@link #onNext} or
     * {@link #onCompleted}.
     *
     * @param e the exception encountered by the Observable
     */
    @Override
    public void onError(Throwable e) {
        cfuture.completeExceptionally(e);
    }

    /**
     * Provides the Observer with a new item to observe.
     * <p>
     * The {@code Observable} may call this method 0 or more times.
     * <p>
     * The {@code Observable} will not call this method again after it calls either {@link #onCompleted} or
     * {@link #onError}.
     *
     * @param serverResponseElement the item emitted by the Observable
     */
    @Override
    public void onNext(ServerResponseElement serverResponseElement) {
        processor.process(serverResponseElement, processor);
    }

    /**
     * Unsubscribes from the observer, and returns the items already received.
     *
     * @return the items already received.
     */
    public List<T> collectImmediately(){
        this.unsubscribe();
        return new ArrayList<>(accumulator);
    }

    public Future<List<T>> collect() {
        return cfuture;
    }

}
