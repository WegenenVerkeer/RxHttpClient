package be.wegenenverkeer.rxhttp;

import rx.Subscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * A {@link Subscriber} that collects all parts of a response body, after transformation, into a List.
 *
 *  <p>CollectingSubscribers are definitely not thread-safe</p>
 *
 * @param <T> the Type to which each response body part is transformed to.
 *
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
public class CollectingSubscriber<T> extends Subscriber<ServerResponseElement> {


    final private CompletableFuture<List<T>> cfuture = new CompletableFuture<>();
    final private List<T> accumulator = new ArrayList<>();

    final private MutableResponseProcessor processor;

    /**
     * Constructs an instance that transforms each chunk into a value of T
     *
     * @param transformPart the function that transforms the bytes of each response part into an object of type {@code T}
     */
    public CollectingSubscriber(Function<byte[], T> transformPart){
        processor = new MutableResponseProcessor() {
            @Override
            void processPart(byte[] bytes) {
                try {
                    accumulator.add(transformPart.apply(bytes));
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
        cfuture.complete(Collections.unmodifiableList(accumulator));
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
        processor.process(serverResponseElement);
    }

    /**
     * Unsubscribes from the observer and returns the items already received.
     *
     * @return the items already received.
     */
    public List<T> collectImmediately(){
        this.unsubscribe();
        return new ArrayList<>(accumulator);
    }

    /**
     * Returns the future list of response parts, after transformation
     *
     * @return the future list of response parts, after transformation
     */
    public Future<List<T>> collect() {
        return cfuture;
    }

}
