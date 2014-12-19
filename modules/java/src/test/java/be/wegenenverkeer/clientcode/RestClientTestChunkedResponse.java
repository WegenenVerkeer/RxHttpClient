package be.wegenenverkeer.clientcode;

import be.wegenenverkeer.rest.*;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static junit.framework.Assert.assertEquals;

/**
 * Tests that demonstrate processing a long and slow chunked response.
 *
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */

public class RestClientTestChunkedResponse {

    RestClient client = new RestClient.Builder()
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
        Observable<ServerResponseElement> observable = client.sendRequest(request);

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
        Observable<ServerResponseElement> observable = client.sendRequest(request);

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
        Observable<ServerResponseElement> observable = client.sendRequest(request);

        CollectingSubscriber<String> subscriber = new CollectingSubscriber<>((bytes) -> new String(bytes));
        observable.subscribe(subscriber);

        Thread.sleep(500);
        System.out.println("Unsubscribing!!");
        List<String> received = subscriber.collectImmediately();
        assertEquals(6, received.size());

    }



}
