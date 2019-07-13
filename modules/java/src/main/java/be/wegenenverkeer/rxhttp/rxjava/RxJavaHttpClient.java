package be.wegenenverkeer.rxhttp.rxjava;

import be.wegenenverkeer.rxhttp.*;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.processors.AsyncProcessor;
import io.reactivex.processors.BehaviorProcessor;
import org.asynchttpclient.AsyncHttpClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A Reactive HTTP Client
 * Created by Karel Maesen, Geovise BVBA on 05/12/14.
 */
public class RxJavaHttpClient extends BaseRxHttpClient implements RxHttpClient {

    final private static Logger logger = LoggerFactory.getLogger(RxHttpClient.class);
    final private static Charset UTF8 = Charset.forName("UTF8");


    protected RxJavaHttpClient(AsyncHttpClient innerClient, RestClientConfig config, ClientRequestLogFormatter logFmt, RequestSigner... requestSigners) {
        super(innerClient, config, logFmt, requestSigners);
    }

    /**
     * * Executes a request and returns an Observable for the complete response.
     */
    public <F> CompletableFuture<F> execute(ClientRequest request, Function<ServerResponse, F> transformer) {
        logger.info("Sending Request: " + toLogMessage(request));
        //Note: we don't use Observable.toBlocking().toFuture()
        //because we need a CompletableFuture so that interop with Scala is possible
        final CompletableFuture<F> future = new CompletableFuture<>();

        return inner().executeRequest(request.unwrap()).toCompletableFuture()
                .thenApply( ServerResponse::wrap )
                .thenApply( transformer );
    }

    /**
     * Executes a request and returns an Observable for the complete response.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     * </p>
     *
     * @param request     the request to send
     * @param transformer a function that transforms the {@link ServerResponse} to a value of F
     * @param <F>         the type of return value
     * @return An Observable that returns the transformed server response.
     */
    public <F> Flowable<F> executeToCompletion(ClientRequest request, Function<ServerResponse, F> transformer) {
        return Flowable.defer(() -> {
            logger.info("Sending Request: " + toLogMessage(request));
            AsyncProcessor<F> subject = AsyncProcessor.create();
            inner().executeRequest(request.unwrap(), new AsyncCompletionHandlerWrapper<>(subject, transformer));
            return subject;
        });
    }

    /**
     * Returns a "cold" Observable for a stream of {@link ServerResponseElement}s.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     *
     * @param request the request to send
     * @return a cold observable of ServerResponseElements
     * @see Observable#defer
     */
    public Flowable<ServerResponseElement> executeObservably(ClientRequest request) {
        return Flowable.defer(() -> {
            BehaviorProcessor<ServerResponseElement> subject = BehaviorProcessor.create();
            inner().executeRequest(request.unwrap(), new AsyncHandlerWrapper(subject));
            return subject;
        });
    }

    /**
     * Returns a "cold" Observable for a stream of messages.
     * <p>
     * All <code>ServerResponseElement</code>s are filtered out, <code>ResponseBodyPart</code>s are turned into (UTF8) <code>String</code>s,
     * and the chunks are combined and split at the specified separator characters.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     *
     * @param request the request to send
     * @param separator the separator
     * @return a cold Flowable of messages (UTF8 Strings)

     */
    public Flowable<String> executeAndDechunk(ClientRequest request, String separator) {
        return executeAndDechunk(request, separator, Charset.forName("UTF8"));
    }

    /**
     * Returns a "cold" Observable for a stream of messages.
     * <p>
     * All <code>ServerResponseElement</code>s are filtered out, <code>ResponseBodyPart</code>s are turned into <code>String</code>s in
     * the specified charset, and the chunks are combined and split at the separator characters.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     *
     * @param request the request to send
     * @param separator the separator
     * @param charset the character set of the messages
     * @return a cold Flowable of messages (Strings in the specified Charset)
     */
    public Flowable<String> executeAndDechunk(ClientRequest request, String separator, Charset charset) {
        return executeObservably(request)
                .filter(sre -> sre instanceof ServerResponseBodyPart)
                .map( sre ->  new String(((ServerResponseBodyPart)sre).getBodyPartBytes(), charset))
                .lift(new Dechunker(separator));
    }


    /**
     * Returns a "cold" Observable for a stream of {@code T}.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     *
     * @param request   the request to send
     * @param transform the function that transforms the response body (chunks) into objects of type F
     * @param <F>       return type of the transform
     * @return a cold observable of ServerResponseElements
     * @see Observable#defer
     */
    public <F> Flowable<F> executeObservably(ClientRequest request, Function<byte[], F> transform) {
        return executeObservably(request)
                    .filter(el -> el.match(e -> false, e -> false, e -> true, e -> true))
                    .map(el -> el.match(
                            e -> null, //won't happen, is filtered
                            e -> null, //won't happen, is filtered
                            e -> transform.apply(e.getBodyPartBytes()),
                            e -> transform.apply(e.getResponseBodyAsBytes())));
    }


    /**
     * A Builder for {@code RxHttpClient} builders.
     */
    static public class Builder extends be.wegenenverkeer.rxhttp.Builder<RxJavaHttpClient, Builder>{

        @Override
        public RxJavaHttpClient build(){
            return super.build();
        }

        @Override
        protected RxJavaHttpClient createClient(AsyncHttpClient innerClient, RestClientConfig rcConfig, ClientRequestLogFormatter logFmt, RequestSigner... signers) {
            return new RxJavaHttpClient(innerClient, rcConfig, logFmt, requestSigners.toArray(new RequestSigner[0]));
        }

    }

}
