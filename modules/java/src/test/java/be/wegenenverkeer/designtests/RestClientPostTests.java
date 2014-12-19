package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rest.ClientRequest;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import static be.wegenenverkeer.rest.HTTPStatusCode.*;

/**
 * Test for POST requests.
 *
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
public class RestClientPostTests extends UsingWireMock{

    //this is a post that creates a contact entity, and then
    //retrieves it back from the server.
    @Test
    public void POSTHappyPath() throws InterruptedException {
        //set up stub
        String expectBody = "{ 'name': 'John Doe }";
        stubFor(post(urlPathEqualTo("/contacts/new"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(REQUEST_TIME_OUT / 3)
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/contacts/123"))
                        );
        stubFor(get(urlPathEqualTo("/contacts/123"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(aResponse().withFixedDelay(REQUEST_TIME_OUT / 3)
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(expectBody))
        );

        //set up use case
        String path = "/contacts/new";
        ClientRequest request = client.requestBuilder()
                .setMethod("POST")
                .setUrlRelativetoBase(path)
                .build();

        Observable<String> observable =
                client.executeToCompletion(request, resp -> resp.getHeader("Location").get())
                        .flatMap( url ->
                                client.executeToCompletion(
                                    client.requestBuilder().setMethod("GET").setUrlRelativetoBase(url).build(),
                                        resp -> resp.getResponseBody()
                                )
                        );


        TestSubscriber<String> sub = new TestSubscriber<>();
        observable.subscribe(sub);

        sub.awaitTerminalEvent(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertNoErrors();
        sub.assertReceivedOnNext(items(expectBody));

    }

}
