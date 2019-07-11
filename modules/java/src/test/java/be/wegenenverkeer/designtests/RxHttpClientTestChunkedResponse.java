package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests that demonstrate processing a long and slow chunked response.
 *
 * Notice that these tests require running the node server.js test server.That is why they are checked-in as @ignored
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */

public class RxHttpClientTestChunkedResponse extends UsingWireMock{


    @Test
    public void testChunkedTransfer(){
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

        Observable<String> observable = client.executeAndDechunk(request, "\n");
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);
        subscriber.awaitTerminalEvent(120, TimeUnit.MILLISECONDS);

        assertEquals(10, subscriber.getOnNextEvents().size());

    }




    @Test
    public void testCancellation() throws InterruptedException, ExecutionException, TimeoutException {
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
        Observable<String> observable = client.executeAndDechunk(request, "\n");

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        Subscription subscription = observable.subscribe(subscriber);

        Thread.sleep(50);
        subscription.unsubscribe();
        assertEquals(0, subscriber.getOnNextEvents().size());

    }

    //TODO verify that cancellation works properly
    //TODO verify error handling


}
