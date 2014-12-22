package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Behavior Unit test
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class RxHttpClientDesignTests extends UsingWireMock{


    @Test
    public void GETHappyPath() throws InterruptedException {
        //set up stub
        String expectBody = "{ 'contacts': [1,2,3] }";
        stubFor(get(urlPathEqualTo("/contacts?q=test"))
                .withQueryParam("q", equalTo("test"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(200)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectBody)));

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase(path)
                .addQueryParam("q", "test")
                .build();

        Observable<String> observable = client.executeToCompletion(request, ServerResponse::getResponseBody);


        TestSubscriber<String> sub = new TestSubscriber<>();
        observable.subscribe(sub);

        sub.awaitTerminalEvent(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertNoErrors();

        sub.assertReceivedOnNext(items(expectBody));


    }

    @Test
    public void demonstrateComposableObservable() throws InterruptedException {
        //set up stubs
        String expectBody = "{ 'contacts': ['contacts/1','contacts/2','contacts/3'] }";
        stubFor(get(urlPathEqualTo("/contacts?q=test"))
                .withQueryParam("q", equalTo("test"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(10)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectBody)));
        stubFor(get(urlPathEqualTo("/contacts/1")).withHeader("Accept", equalTo("application/json")).willReturn(aResponse().withStatus(404).withBody("ONE")));
        stubFor(get(urlPathEqualTo("/contacts/2")).withHeader("Accept", equalTo("application/json")).willReturn(aResponse().withStatus(200).withBody("TWO")));
        stubFor(get(urlPathEqualTo("/contacts/3")).withHeader("Accept", equalTo("application/json")).willReturn(aResponse().withStatus(200).withBody("THREE")));



        //use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase(path)
                .addQueryParam("q", "test")
                .build();

        Function<String, Observable<String>> followLink  = (String contactUrl) -> {
            ClientRequest followUp = client.requestBuilder()
                    .setMethod("GET")
                    .setUrlRelativetoBase(contactUrl).build();
            return client
                    .executeToCompletion(followUp, ServerResponse::getResponseBody)
                    .onErrorResumeNext(Observable.just("ERROR"));
        };

        Observable<String> observable = client.executeToCompletion(request, ServerResponse::getResponseBody)
                .flatMap(body -> {
                    List<String> l = JsonPath.read(body, "$.contacts");
                    return Observable.from(l);
                }).flatMap(contactUrl -> followLink.apply(contactUrl));


        //verify behaviour
        TestSubscriber<String> sub = new TestSubscriber<>();
        observable.subscribe(sub);
        sub.awaitTerminalEvent(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        sub.assertNoErrors();
        assertEquals(new HashSet<String>(items("ERROR", "TWO", "THREE")), new HashSet<String>(sub.getOnNextEvents()));

    }

    @Test
    public void testHttp4xxResponseOnGET() throws InterruptedException {
        //no stub set-up so we always get a 404 response.

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Observable<String> observable = client.executeToCompletion(request, ServerResponse::getResponseBody);

        TestSubscriber<String> testsubscriber = new TestSubscriber<>();
        observable.subscribe(testsubscriber);

        testsubscriber.awaitTerminalEvent(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        List onErrorEvents = testsubscriber.getOnErrorEvents();
        assertFalse(onErrorEvents.isEmpty());
        if (onErrorEvents.get(0) instanceof HttpClientError) {
            HttpClientError hce = (HttpClientError) onErrorEvents.get(0);
            assertEquals(404, hce.getStatusCode());
        } else {
            fail("Didn't receive a HttpClientError");
        }

    }

    @Test
    public void testHttp5xxResponseOnGET() throws InterruptedException {
        //set up stub
        stubFor(get(urlEqualTo("/contacts"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(20)
                        .withStatus(500)));

        //set up use case
        String path = "/contacts";
        ClientRequest request = client
                .requestBuilder().setMethod("GET")
                .setUrlRelativetoBase(path)
                .build();

        Observable<String> observable = client.executeToCompletion(request, ServerResponse::getResponseBody);
        TestSubscriber<String> testsubscriber = new TestSubscriber<>();
        observable.subscribe(testsubscriber);

        testsubscriber.awaitTerminalEvent(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        List onErrorEvents = testsubscriber.getOnErrorEvents();
        assertFalse(onErrorEvents.isEmpty());
        if (onErrorEvents.get(0) instanceof HttpServerError) {
            HttpServerError hce = (HttpServerError) onErrorEvents.get(0);
            assertEquals(500, hce.getStatusCode());
        } else {
            fail("Didn't receive a HttpClientError");
        }

    }


    @Test
    public void testConnectionTimeOut() throws InterruptedException {
        //set up stub
        stubFor(get(urlEqualTo("/contacts"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(500)
                        .withStatus(200)));

        //set up client with very low connection time-out values
        RxHttpClient client = new RxHttpClient.Builder()
                .setRequestTimeout(100)
                .setMaxConnections(1)
                .setAccept("application/json")
                .setBaseUrl("http://localhost:" + port)
                .build();

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Observable<String> observable = client.executeToCompletion(request, ServerResponse::getResponseBody);

        TestSubscriber<String> testsubscriber = new TestSubscriber<>();
        observable.subscribe(testsubscriber);

        testsubscriber.awaitTerminalEvent(1000, TimeUnit.MILLISECONDS);

        List onErrorEvents = testsubscriber.getOnErrorEvents();
        assertFalse(onErrorEvents.isEmpty());
        assertTrue(onErrorEvents.get(0) instanceof TimeoutException);

    }

}
