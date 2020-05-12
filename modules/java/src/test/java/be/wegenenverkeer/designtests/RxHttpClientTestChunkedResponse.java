package be.wegenenverkeer.designtests;

import be.wegenenverkeer.UsingWireMockRxJava;
import be.wegenenverkeer.rxhttpclient.ClientRequest;
import be.wegenenverkeer.rxhttpclient.ServerResponseElement;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;

/**
 * Tests that demonstrate processing a long and slow chunked response.
 * <p>
 * Notice that these tests require running the node server.js test server.That is why they are checked-in as @ignored
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */

public class RxHttpClientTestChunkedResponse extends UsingWireMockRxJava {

    private final static int SIZE = 1_000_000;
    private final File tmp;

    public RxHttpClientTestChunkedResponse(){
        //Generate a large test file to text chunked transfer encoding
        tmp = generateWireMockTestFile(SIZE);
    }


    @Override
    protected int getRequestTimeOut(){
        return 20_000;
    }

    @Override
    protected int getTimeOut(){
        return getRequestTimeOut() + 10_000;
    }

    @Override
    protected FileSource fileRoot() {
        return new SingleRootFileSource(getWireMockRootDir());
    }

    @Test
    public void testChunkedTransfer() throws InterruptedException {
        stubFor(
                get(urlPathEqualTo("/sse"))
                        .willReturn(aResponse()
                                .withBodyFile(getNameOfGeneratedFile())
                                .withChunkedDribbleDelay(300_000, 15_000)
                        )
        );

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/sse")
                .build();

        Flowable<ServerResponseElement> flowable = client.executeObservably(request);

        TestSubscriber<ServerResponseElement> subscriber = flowable.test();

        subscriber.awaitDone(getRequestTimeOut(), TimeUnit.MILLISECONDS);

        subscriber.assertComplete();
        subscriber.assertNoErrors();
    }

    private String getNameOfGeneratedFile() {
        return tmp.getName();
    }


    @Test
    public void testChunkedTransferWithDechunk() throws InterruptedException {
        stubFor(
                get(urlPathEqualTo("/sse"))
                        .willReturn(aResponse()
                                .withBodyFile(getNameOfGeneratedFile())
                                .withChunkedDribbleDelay(300_000, 2000)
                        )
        );

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/sse")
                .build();

        Flowable<String> flowable = client.executeAndDechunk(request, "\n");

        TestSubscriber<String> subscriber = flowable.test();
        subscriber.awaitDone(20_000, TimeUnit.MILLISECONDS);

        subscriber.assertValueCount(SIZE);
    }


    @Test
    public void testCancellation() throws InterruptedException {
        stubFor(
                get(urlPathEqualTo("/sse"))
                        .willReturn(aResponse()
                                .withBodyFile("sse-output.txt")
                                .withChunkedDribbleDelay(50, 60000)
                        )
        );

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/sse")
                .build();
        Flowable<String> flowable = client.executeAndDechunk(request, "\n");

        TestSubscriber<String> subscriber = flowable.test();

        Thread.sleep(50);
        subscriber.cancel();
        assertTrue(subscriber.isCancelled());
        subscriber.assertValueCount(0);


    }


    //TODO verify error handling


}
