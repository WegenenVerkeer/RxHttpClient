package be.wegenenverkeer.rxhttp.rxstreams;

import be.wegenenverkeer.rxhttp.*;
import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import org.asynchttpclient.AsyncHttpClient;
import org.reactivestreams.Publisher;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class RxStreamsHttpClient implements RxHttpClient {

    private final RxJavaHttpClient delegate;

    private RxStreamsHttpClient(RxJavaHttpClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public <F> CompletableFuture<F> execute(ClientRequest request, Function<ServerResponse, F> transformer) {
        return delegate.execute(request, transformer);
    }

    @Override
    public <F> Publisher<F> executeToCompletion(ClientRequest request, Function<ServerResponse, F> transformer) {
        return delegate.executeToCompletion(request, transformer);
    }

    @Override
    public Publisher<ServerResponseElement> executeObservably(ClientRequest request) {
        return delegate.executeObservably(request);
    }

    @Override
    public Publisher<String> executeAndDechunk(ClientRequest request, String separator) {
        return delegate.executeAndDechunk(request, separator);
    }

    @Override
    public Publisher<String> executeAndDechunk(ClientRequest request, String separator, Charset charset) {
        return delegate.executeAndDechunk(request, separator, charset);
    }

    @Override
    public <F> Publisher<F> executeObservably(ClientRequest request, Function<byte[], F> transform) {
        return delegate.executeObservably(request, transform);
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
        return  delegate.toLogMessage(request);
    }

    @Override
    public AsyncHttpClient inner() {
        return delegate.inner();
    }

    @Override
    public int getMaxConnections() {
        return delegate.getMaxConnections();
    }


    public static class Builder extends be.wegenenverkeer.rxhttp.Builder<RxStreamsHttpClient, RxStreamsHttpClient.Builder> {

        @Override
        public RxStreamsHttpClient build() {
            return super.build();
        }

        @Override
        public RxStreamsHttpClient createClient(AsyncHttpClient innerClient, RestClientConfig rcConfig, ClientRequestLogFormatter logFmt, RequestSigner... signers) {
            RxJavaHttpClient delegate = new RxJavaHttpClient.Builder().createClient(innerClient, rcConfig, logFmt, signers);
            return new RxStreamsHttpClient(delegate);
        }
    }
}
