package be.wegenenverkeer.clientcode;

import be.wegenenverkeer.rest.ClientRequest;
import be.wegenenverkeer.rest.RestClient;
import be.wegenenverkeer.rest.ServerResponseBodyPart;
import be.wegenenverkeer.rest.ServerResponseElement;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
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
                .setUrlRelativetoBase("/")
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

}
