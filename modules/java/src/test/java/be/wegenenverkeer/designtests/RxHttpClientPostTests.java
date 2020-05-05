package be.wegenenverkeer.designtests;

import be.wegenenverkeer.UsingWireMockRxJava;
import be.wegenenverkeer.rxhttp.ClientRequest;
import be.wegenenverkeer.rxhttp.HttpClientError;
import be.wegenenverkeer.rxhttp.ServerResponse;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Test for POST requests.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
public class RxHttpClientPostTests extends UsingWireMockRxJava {

    //this is a post that creates a contact entity, and then
    //retrieves it back from the server.
    @Test
    public void POSTHappyPath() {
        //set up stub
        String expectBody = "{ 'name': 'John Doe }";
        stubFor(post(urlPathEqualTo("/contacts/new"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(DEFAULT_REQUEST_TIME_OUT / 3)
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/contacts/123"))
        );
        stubFor(get(urlPathEqualTo("/contacts/123"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(DEFAULT_REQUEST_TIME_OUT / 3)
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

        Flowable<String> flowable =
                client.executeToCompletion(request, resp -> resp.getHeader("Location").get())
                        .flatMap(url ->
                                client.executeToCompletion(
                                        client.requestBuilder().setMethod("GET").setUrlRelativetoBase(url).build(),
                                        ServerResponse::getResponseBody
                                )
                        );


        TestSubscriber<String> sub = flowable.test();

        sub.awaitDone(getTimeOut(), TimeUnit.MILLISECONDS);
        sub.assertNoErrors();
        sub.assertValues(expectBody);

    }

    @Test
    public void POSTFailure() {
        //set up stub
        String expectFailedBody = "{ \"message\": \"unauthorized\" }";
        stubFor(post(urlPathEqualTo("/contacts/new"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(DEFAULT_REQUEST_TIME_OUT / 3)
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectFailedBody))
        );

        //set up use case
        String path = "/contacts/new";
        ClientRequest request = client.requestBuilder()
                .setMethod("POST")
                .setUrlRelativetoBase(path)
                .build();

        Flowable<String> flowable = client.executeToCompletion(request, ServerResponse::getResponseBody);

        TestSubscriber<String> sub = flowable.test();

        sub.awaitDone(getTimeOut(), TimeUnit.MILLISECONDS);

        sub.assertError(t -> {
            if (t instanceof HttpClientError) {
                HttpClientError hte = (HttpClientError) t;
                ServerResponse sr = hte.getResponse().get();
                return hte.getStatusCode() == 403 && sr.getResponseBody().equals(expectFailedBody)
                        && sr.getContentType().get().equals("application/json");
            } else return false;
        });
    }
}
