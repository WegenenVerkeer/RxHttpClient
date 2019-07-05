package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
        stubFor(get(urlPathEqualTo("/contacts"))
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

    /**
     * Proves that UTF-8 charset is used as default.
     * @throws InterruptedException
     */
    @Test
    public void testCharsetEncodingDefaultsToUTF8() throws InterruptedException {

        //set up stub
        String expectBody = "{ 'contacts': 'žẽūș' }"; // with chars only available in UTF-8

        stubFor(get(urlPathEqualTo("/contacts"))
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

    /**
     * Proves that charset when present in content type gets priority over default UTF-8
     * @throws InterruptedException
     */
    @Test
    public void testCharsetEncodingInContentType() throws InterruptedException {

        //set up stub
        String expectBody = "{ 'contacts': 'žẽūș' }"; // with chars only available in UTF-8

        stubFor(get(urlPathEqualTo("/contacts"))
                .withQueryParam("q", equalTo("test"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(200)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=ISO-8859-1") // explicitly picking Latin-1
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

        try {
            sub.assertReceivedOnNext(items(expectBody));
            fail("Expecting wrongly parsed body");
        } catch (AssertionError exp) {
             // failure expected
        }


    }

    @Test
    public void GETHappyPathWithFuture() throws InterruptedException, TimeoutException, ExecutionException {
        //set up stub
        String expectBody = "{ 'contacts': [1,2,3] }";
        stubFor(get(urlPathEqualTo("/contacts"))
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

        Future<String> f = client.execute(request, ServerResponse::getResponseBody);
        String result = f.get(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        assertEquals(expectBody, result);

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
