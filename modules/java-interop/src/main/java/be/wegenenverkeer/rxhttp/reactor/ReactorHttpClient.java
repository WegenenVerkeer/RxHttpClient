package be.wegenenverkeer.rxhttp.reactor;

import be.wegenenverkeer.rxhttp.*;
import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import be.wegenenverkeer.rxhttp.rxstreams.RxStreamsHttpClient;
import io.reactivex.rxjava3.core.Completable;
import org.asynchttpclient.AsyncHttpClient;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class ReactorHttpClient implements RxHttpClient {

    private final RxJavaHttpClient delegate;

    private ReactorHttpClient(RxJavaHttpClient delegate) {
        this.delegate = delegate;
    }
    @Override
    public <F> CompletableFuture<F> execute(ClientRequest request, Function<ServerResponse, F> transformer) {
        return delegate.execute(request, transformer);
    }

    @Override
    public <F> Mono<F> executeToCompletion(ClientRequest request, Function<ServerResponse, F> transformer) {
        return Mono.fromFuture(delegate.execute(request, transformer));
    }

    @Override
    public Flux<ServerResponseElement> executeObservably(ClientRequest request) {
        return RxJava3Adapter.flowableToFlux(delegate.executeObservably(request));
    }

    @Override
    public Flux<String> executeAndDechunk(ClientRequest request, String separator) {
        return RxJava3Adapter.flowableToFlux(delegate.executeAndDechunk(request, separator));
    }

    @Override
    public Flux<String> executeAndDechunk(ClientRequest request, String separator, Charset charset) {
        return RxJava3Adapter.flowableToFlux(delegate.executeAndDechunk(request, separator, charset));
    }

    @Override
    public <F> Flux<F> executeObservably(ClientRequest request, Function<byte[], F> transform) {
        return RxJava3Adapter.flowableToFlux(delegate.executeObservably(request, transform));
    }

    @Override
    public String getBaseUrl() {
        return delegate.getBaseUrl();
    }

    @Override
    public String getAccept() {
        return delegate.getAccept();
    }

    @Override
    public List<RequestSigner> getRequestSigners() {
        return delegate.getRequestSigners();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public ClientRequestBuilder requestBuilder() {
        return delegate.requestBuilder();
    }

    @Override
    public String toLogMessage(ClientRequest request) {
        return delegate.toLogMessage(request);
    }

    @Override
    public AsyncHttpClient inner() {
        return delegate.inner();
    }

    @Override
    public int getMaxConnections() {
        return delegate.getMaxConnections();
    }

    public static class Builder extends be.wegenenverkeer.rxhttp.Builder<ReactorHttpClient, ReactorHttpClient.Builder> {

        @Override
        public ReactorHttpClient build() {
            return super.build();
        }

        @Override
        public ReactorHttpClient createClient(AsyncHttpClient innerClient, RestClientConfig rcConfig, ClientRequestLogFormatter logFmt, RequestSigner... signers) {
            RxJavaHttpClient delegate = new RxJavaHttpClient.Builder().createClient(innerClient, rcConfig, logFmt, signers);
            return new ReactorHttpClient(delegate);
        }
    }
}
