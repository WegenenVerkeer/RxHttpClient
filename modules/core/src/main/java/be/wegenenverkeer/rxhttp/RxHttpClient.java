package be.wegenenverkeer.rxhttp;

import org.asynchttpclient.AsyncHttpClient;
import org.reactivestreams.Publisher;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-12.
 */
public interface RxHttpClient {
    <F> CompletableFuture<F> execute(ClientRequest request, Function<ServerResponse, F> transformer);

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
    <F> Publisher<F> executeToCompletion(ClientRequest request, Function<ServerResponse, F> transformer);

    /**
     * Returns a "cold" Observable for a stream of {@link ServerResponseElement}s.
     * <p>
     * The returned Observable is Cold, i.e. on each subscription a new HTTP request is made
     * and the response elements returned as a new Observable. So for each subscriber, a separate HTTP request will be made.
     *
     * @param request the request to send
     * @return a cold observable of ServerResponseElements
     */
    Publisher<ServerResponseElement> executeObservably(ClientRequest request);

    Publisher<String> executeAndDechunk(ClientRequest request, String separator);

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
    Publisher<String> executeAndDechunk(ClientRequest request, String separator, Charset charset);

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
     *
     */
    <F> Publisher<F> executeObservably(ClientRequest request, Function<byte[], F> transform);

    String getBaseUrl();

    String getAccept();

    List<RequestSigner> getRequestSigners();

    void close();

    ClientRequestBuilder requestBuilder();

    String toLogMessage(ClientRequest request);

    AsyncHttpClient inner();
}
