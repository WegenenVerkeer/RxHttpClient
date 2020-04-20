package be.wegenenverkeer.designtests;

import be.wegenenverkeer.UsingWireMockRxJava;
import be.wegenenverkeer.rxhttp.ClientRequest;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

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


    @Test
    public void testChunkedTransfer() {
        stubFor(
                get(urlPathEqualTo("/sse"))
                        .willReturn(aResponse()
                                .withBodyFile("sse-output.txt")
                                .withChunkedDribbleDelay(50, 30)
                        )
        );

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/sse")
                .build();

        Flowable<String> flowable = client.executeAndDechunk(request, "\n");

        TestSubscriber<String> subscriber = flowable.test();
        subscriber.awaitDone(200, TimeUnit.MILLISECONDS);

        subscriber.assertValueCount(10);

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

    //TODO verify that cancellation works properly
    //TODO verify error handling


}
