package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.*;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Supports the same set of tests as {@link RxHttpClientDesignTests}, but uses the executeObservably(ServerRequest) method.
 * * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public class RxHttpClientObservableOfServerElementsTests extends UsingWireMock{

    @Test
    public void GETHappyPath() throws InterruptedException {
        //set up stub
        String expectBody = "{ 'contacts': [1,2,3] }";
        stubFor(get(urlPathEqualTo("/contacts"))
                .withQueryParam("q", equalTo("test"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(REQUEST_TIME_OUT / 3)
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

        Observable<ServerResponseElement> observable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> sub = new TestSubscriber<>();
        observable.subscribe(sub);

        sub.awaitTerminalEvent(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertNoErrors();
        sub.assertTerminalEvent();
        for (ServerResponseElement el : sub.getOnNextEvents()) {
            if (el instanceof ServerResponseStatus) {
                assertEquals(200, ((ServerResponseStatus) el).getStatusCode());
            } else if (el instanceof ServerResponseBodyPart) {
                assertEquals(expectBody, new String(((ServerResponseBodyPart) el).getBodyPartBytes()));
            } else if (el instanceof ServerResponseHeaders) {
                assertEquals("application/json", ((ServerResponseHeaders)el).getContentType().get() );
            } else {
                fail("Unknown Server Response element: " + el.getClass().getCanonicalName());
            }
        }


    }

    @Test
    public void testHttp4xxResponseOnGET() throws InterruptedException {
        //no stub set-up so we always get a 404 response.

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Observable<ServerResponseElement> observable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> testsubscriber = new TestSubscriber<>();
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


        Observable<ServerResponseElement> observable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> testsubscriber = new TestSubscriber<>();
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
                .willReturn(aResponse().withFixedDelay(REQUEST_TIME_OUT * 2)
                        .withStatus(200)));

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Observable<ServerResponseElement> observable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> testsubscriber = new TestSubscriber<>();
        observable.subscribe(testsubscriber);

        testsubscriber.awaitTerminalEvent(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        List onErrorEvents = testsubscriber.getOnErrorEvents();
        assertFalse(onErrorEvents.isEmpty());
        assertTrue(onErrorEvents.get(0) instanceof TimeoutException);

    }

}