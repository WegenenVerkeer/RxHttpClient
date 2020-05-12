package be.wegenenverkeer.rxhttpclient.jdk;

import be.wegenenverkeer.rxhttpclient.*;
import be.wegenenverkeer.rxhttpclient.rxjava.RxJavaHttpClient;
import org.asynchttpclient.AsyncHttpClient;
import org.reactivestreams.FlowAdapters;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class FlowHttpClient implements Closeable {

    private final RxJavaHttpClient delegate;

    private FlowHttpClient(RxJavaHttpClient delegate) {
        this.delegate = delegate;
    }

    public <F> CompletableFuture<F> execute(ClientRequest request, Function<ServerResponse, F> transformer) {
        return delegate.execute(request, transformer);
    }


    public <F> Flow.Publisher<F> executeToCompletion(ClientRequest request, Function<ServerResponse, F> transformer) {
        return FlowAdapters.toFlowPublisher(delegate.executeToCompletion(request, transformer));
    }


    public Flow.Publisher<ServerResponseElement> executeObservably(ClientRequest request) {
        return FlowAdapters.toFlowPublisher(delegate.executeObservably(request));
    }


    public Flow.Publisher<String> executeAndDechunk(ClientRequest request, String separator) {
        return FlowAdapters.toFlowPublisher(delegate.executeAndDechunk(request, separator));
    }


    public Flow.Publisher<String> executeAndDechunk(ClientRequest request, String separator, Charset charset) {
        return FlowAdapters.toFlowPublisher(delegate.executeAndDechunk(request, separator, charset));
    }


    public <F> Flow.Publisher<F> executeObservably(ClientRequest request, Function<byte[], F> transform) {
        return FlowAdapters.toFlowPublisher(delegate.executeObservably(request, transform));
    }


    public String getBaseUrl() {
        return delegate.getBaseUrl();
    }


    public String getAccept() {
        return delegate.getAccept();
    }


    public List<RequestSigner> getRequestSigners() {
        return delegate.getRequestSigners();
    }


    public void close() {
        delegate.close();
    }


    public ClientRequestBuilder requestBuilder() {
        return delegate.requestBuilder();
    }


    public String toLogMessage(ClientRequest request) {
        return  delegate.toLogMessage(request);
    }


    public AsyncHttpClient inner() {
        return delegate.inner();
    }


    public int getMaxConnections() {
        return delegate.getMaxConnections();
    }


    public static class Builder extends be.wegenenverkeer.rxhttpclient.Builder<FlowHttpClient, FlowHttpClient.Builder> {

        @Override
        public FlowHttpClient build() {
            return super.build();
        }

        @Override
        public FlowHttpClient createClient(AsyncHttpClient innerClient, RestClientConfig rcConfig, ClientRequestLogFormatter logFmt, RequestSigner... signers) {
            RxJavaHttpClient delegate = new RxJavaHttpClient.Builder().createClient(innerClient, rcConfig, logFmt, signers);
            return new FlowHttpClient(delegate);
        }
    }
}
