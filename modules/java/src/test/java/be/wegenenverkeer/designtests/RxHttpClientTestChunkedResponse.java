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
                        .withChunkedDribbleDelay(50, 1000)
                        )
                );


        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/sse")
                .build();
        Observable<ServerResponseElement> observable = client.executeObservably(request);

        TestSubscriber<ServerResponseElement> subscriber = new TestSubscriber<>();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent(600, TimeUnit.MILLISECONDS);

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

        CollectingSubscriber<String> subscriber = new CollectingSubscriber<>(String::new);
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

String body="data:{\"id\":804,\"versie\":3,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":1,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"BEZIG\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":4,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":5,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":1,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"BEZIG\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":6,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"GELUKT\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":7,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2715,\"versie\":1,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"UITVOERINGEN_KOPPELEN\",\"stapBericht\":\"Koppelen met contractjaar(en)\",\"status\":\"BEZIG\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":8,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2715,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"UITVOERINGEN_KOPPELEN\",\"stapBericht\":\"Koppelen met contractjaar(en)\",\"status\":\"GELUKT\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":9,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2715,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"UITVOERINGEN_KOPPELEN\",\"stapBericht\":\"Koppelen met contractjaar(en)\",\"status\":\"GELUKT\"},{\"id\":2716,\"versie\":1,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"UITVOERINGSSTATEN_IMPORTEREN\",\"stapBericht\":\"Uitvoeringsstaten (Posten) importeren\",\"status\":\"BEZIG\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":10,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2715,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"UITVOERINGEN_KOPPELEN\",\"stapBericht\":\"Koppelen met contractjaar(en)\",\"status\":\"GELUKT\"},{\"id\":2716,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"stap\":\"UITVOERINGSSTATEN_IMPORTEREN\",\"stapBericht\":\"Uitvoeringsstaten (Posten) importeren\",\"status\":\"GELUKT\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":11,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2715,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"UITVOERINGEN_KOPPELEN\",\"stapBericht\":\"Koppelen met contractjaar(en)\",\"status\":\"GELUKT\"},{\"id\":2716,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"stap\":\"UITVOERINGSSTATEN_IMPORTEREN\",\"stapBericht\":\"Uitvoeringsstaten (Posten) importeren\",\"status\":\"GELUKT\"},{\"id\":2717,\"versie\":1,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"stap\":\"BESTEK_OPSLAAN\",\"stapBericht\":\"Gegevens opslaan\",\"status\":\"BEZIG\"}],\"edeltaKey\":\"100000787\"}\n" +
        "\n" +
        "data:{\"id\":804,\"versie\":12,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"status\":\"BEZIG\",\"eDeltaKey\":\"100000787\",\"stappen\":[{\"id\":2713,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:07\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"stap\":\"OPDRACHT_KOPPELEN\",\"stapBericht\":\"Bestek koppelen met eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2714,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:08\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"OPDRACHT_DETAILS_OPHALEN\",\"stapBericht\":\"Ophalen van details eDelta opdracht\",\"status\":\"GELUKT\"},{\"id\":2715,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"stap\":\"UITVOERINGEN_KOPPELEN\",\"stapBericht\":\"Koppelen met contractjaar(en)\",\"status\":\"GELUKT\"},{\"id\":2716,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:12\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"stap\":\"UITVOERINGSSTATEN_IMPORTEREN\",\"stapBericht\":\"Uitvoeringsstaten (Posten) importeren\",\"status\":\"GELUKT\"},{\"id\":2717,\"versie\":2,\"creationEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"lastUpdateEvent\":{\"naam\":\"Van der Auwera\",\"voornaam\":\"Joachim\",\"ldapId\":\"saaac474\",\"voId\":\"e99b29a7-c3c6-46ee-8c13-90c3daf9ec85\",\"voornaamNaam\":\"Joachim Van der Auwera\",\"naamVoornaam\":\"Van der Auwera Joachim\",\"tijdstip\":\"2019-07-04T14:02:15\"},\"stap\":\"BESTEK_OPSLAAN\",\"stapBericht\":\"Gegevens opslaan\",\"status\":\"GELUKT\"}],\"edeltaKey\":\"100000787\"}";
}
