package be.wegenenverkeer.rxhttp;

import org.asynchttpclient.AsyncHttpClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A Reactive HTTP Client
 * Created by Karel Maesen, Geovise BVBA on 05/12/14.
 */
public abstract class BaseRxHttpClient implements RxHttpClient {

    final private static Logger logger = LoggerFactory.getLogger(BaseRxHttpClient.class);
    final private static Charset UTF8 = Charset.forName("UTF8");

    final private AsyncHttpClient innerClient;
    final private RestClientConfig config;
    final private List<RequestSigner> requestSigners;
    final private ClientRequestLogFormatter logFormatter;

    protected BaseRxHttpClient(AsyncHttpClient innerClient, RestClientConfig config, ClientRequestLogFormatter logFmt, RequestSigner... requestSigners) {
        this.innerClient = innerClient;
        this.config = config;
        this.requestSigners = List.of(requestSigners);
        this.logFormatter = logFmt;
    }

    /**
     * * Executes a request and returns an Observable for the complete response.
     */
    @Override
    public <F> CompletableFuture<F> execute(ClientRequest request, Function<ServerResponse, F> transformer) {
        logger.info("Sending Request: " + toLogMessage(request));
        //Note: we don't use Observable.toBlocking().toFuture()
        //because we need a CompletableFuture so that interop with Scala is possible
        final CompletableFuture<F> future = new CompletableFuture<>();

        return innerClient.executeRequest(request.unwrap()).toCompletableFuture()
                .thenApply( ServerResponse::wrap )
                .thenApply( transformer );
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
    @Override
    public Publisher<String> executeAndDechunk(ClientRequest request, String separator) {
        return executeAndDechunk(request, separator, Charset.forName("UTF8"));
    }


    /**
     * Returns the base URL that this client connects to.
     *
     * @return the base URL that this client connects to.
     */
    @Override
    public String getBaseUrl() {
        return this.config.getBaseUrl();
    }

    /**
     * Returns the configured default ACCEPT header for requests created using this instance's
     * {@code ClientRequestBuilder}s.
     *
     * @return the configured default ACCEPT header for requests created using this instance's {@code ClientRequestBuilder}s.
     */
    @Override
    public String getAccept() {
        return config.getAccept();
    }

    @Override
    public List<RequestSigner> getRequestSigners() {
        return requestSigners;
    }

    /**
     * Closes the underlying connection
     */
    @Override
    public void close() {
        try {
            this.innerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a new {@code ClientRequestBuilder}.
     *
     * @return a new {@code ClientRequestBuilder}.
     */
    @Override
    public ClientRequestBuilder requestBuilder() {
        return new ClientRequestBuilder(this);
    }

    @Override
    public String toLogMessage(ClientRequest request) {
        return this.logFormatter.toLogMessage(request);
    }


    @Override
    public AsyncHttpClient inner() {
        return this.innerClient;
    }



}
