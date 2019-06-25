package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests that demonstrate processing a long and slow chunked response.
 *
 * Notice that these tests require running the node server.js test server.That is why they are checked-in as @ignored
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */

public class RxHttpClientTestChunkedResponse {

    RxHttpClient client = new RxHttpClient.Builder()
            .setRequestTimeout(6000)
            .setMaxConnections(10)
            .setAccept("application/json")
            .setBaseUrl("http://localhost:9000")
            .build();


    @Ignore //because this requires for the moment that the node server.js runs.
    @Test
    public void testChunkedTransfer(){

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/10")
                .build();
        Observable<ServerResponseElement> observable = client.executeObservably(request);

        TestSubscriber<ServerResponseElement> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent(6000, TimeUnit.MILLISECONDS);

        List<String> received = new ArrayList<>();
        for (ServerResponseElement el : subscriber.getOnNextEvents()) {
            if (el instanceof ServerResponseBodyPart) {
                received.add( new String (((ServerResponseBodyPart)el).getBodyPartBytes()) );
            }
        }

        System.out.println(received);

    }

    @Ignore //because this requires for the moment that the node server.js runs.
    @Test
    public void testChunkedTransferCollectingSubscriber() throws InterruptedException, ExecutionException, TimeoutException {


        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/10")
                .build();
        Observable<ServerResponseElement> observable = client.executeObservably(request);

        CollectingSubscriber<String> subscriber = new CollectingSubscriber<>((bytes) -> new String(bytes));
        observable.subscribe(subscriber);

        Future<List<String>> fResult = subscriber.collect();
        List<String> received = fResult.get(15, TimeUnit.SECONDS);
        assertEquals(10, received.size());

    }

    @Ignore //because this requires for the moment that the node server.js runs.
    @Test
    public void testCancellation() throws InterruptedException, ExecutionException, TimeoutException {


        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/1000")
                .build();
        Observable<ServerResponseElement> observable = client.executeObservably(request);

        CollectingSubscriber<String> subscriber = new CollectingSubscriber<>((bytes) -> new String(bytes));
        observable.subscribe(subscriber);

        Thread.sleep(500);

        List<String> received = subscriber.collectImmediately();

        //note that this depends on the server emitting at 100 millis intervals.
        assertEquals(5, received.size());

    }


    @Ignore
    @Test
    public void testObvervableComposition() throws InterruptedException, ExecutionException, TimeoutException {


        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/10")
                .build();

        Observable<String> observable = client.executeObservably(request, (bytes) -> new String(bytes));

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent(6000, TimeUnit.MILLISECONDS);

        subscriber.assertNoErrors();
        List<String> events = subscriber.getOnNextEvents();
        assertEquals(10, events.size());


    }


    @Ignore
    @Test
    public void testObvervableCompositionWithErrors() throws InterruptedException, ExecutionException, TimeoutException {


        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/100")
                .build();

        final AtomicInteger counter = new AtomicInteger(0);
        Observable<String> observable = client.executeObservably(request, (bytes) -> {
                    if (counter.incrementAndGet() > 4) {
                        throw new RuntimeException("FORCED ERROR");
                    }
                    return new String(bytes);
                }
        );

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent(6000, TimeUnit.MILLISECONDS);

        subscriber.assertTerminalEvent();
        assertEquals(1, subscriber.getOnErrorEvents().size());
        assertEquals("FORCED ERROR", subscriber.getOnErrorEvents().get(0).getMessage());
        assertEquals(4, subscriber.getOnNextEvents().size());



    }

    // verify that cancellation works properly on filterd and mapped observable
    @Ignore //because this requires for the moment that the node server.js runs.
    @Test
    public void testCancellationOnComposed() throws InterruptedException, ExecutionException, TimeoutException {


        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/1000")
                .build();
        Observable<String> observable = client.executeObservably(request, (bytes) -> new String(bytes));


        TestSubscriber<String> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        Thread.sleep(500);

        subscriber.unsubscribe();

    }


    // verify that cancellation works properly on filterd and mapped observable
    @Ignore //because this requires for the moment that the node server.js runs.
    @Test
    public void testCancellationOnComposedTakeWhile() throws InterruptedException, ExecutionException, TimeoutException {


        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/1000")
                .build();

        //request 1000 objects, but only take 10.
        //the connection to the server is immediately closed.
        Observable<String> observable =
                client.executeObservably(request, (bytes) -> new String(bytes))
                        .take(10);

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent(6000, TimeUnit.MILLISECONDS);
        subscriber.assertNoErrors();
        List<String> events = subscriber.getOnNextEvents();
        assertEquals(10, events.size());

    }



}
