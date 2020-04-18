package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.ClientRequest;
import be.wegenenverkeer.rxhttp.HttpClientError;
import be.wegenenverkeer.rxhttp.HttpServerError;
import be.wegenenverkeer.rxhttp.ServerResponse;
import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Behavior Unit test
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class RxHttpClientDesignTests extends UsingWireMock{


    @Test
    public void GETObservably() {
        //set up stub
        String expectBody = "{ 'contacts': [1,2,3] }";
        stubFor(get(urlPathEqualTo("/contacts"))
                .withQueryParam("q", equalTo("test"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
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

        Flowable<String> flowable = client.executeObservably(request, bytes -> new String(bytes, Charset.forName("UTF8")));
        TestSubscriber<String> sub = flowable.test();

        sub.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertNoErrors();
        sub.assertValues(expectBody);
    }

    @Test
    public void GETHappyPath() {
        //set up stub
        String expectBody = "{ 'contacts': [1,2,3] }";
        stubFor(get(urlPathEqualTo("/contacts"))
                .withQueryParam("q", equalTo("test"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
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

        Flowable<String> flowable = client.executeToCompletion(request, ServerResponse::getResponseBody);
        TestSubscriber<String> sub = flowable.test();

        sub.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertNoErrors();
        sub.assertValues(expectBody);
    }

    /**
     * Proves that UTF-8 charset is used as default.
     */
    @Test
    public void testCharsetEncodingDefaultsToUTF8() {

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

        Flowable<String> flowable = client.executeToCompletion(request, ServerResponse::getResponseBody);


        TestSubscriber<String> sub = flowable.test();
        sub.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertNoErrors();
        sub.assertValues(expectBody);
    }

    /**
     * Proves that charset when present in content type gets priority over default UTF-8
     */
    @Test
    public void testCharsetEncodingInContentType() {

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

        Flowable<String> flowable = client.executeToCompletion(request, ServerResponse::getResponseBody);
        TestSubscriber<String> sub = flowable.test();
        sub.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertNoErrors();

        try {
            sub.assertValues(expectBody);
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
    public void testHttp4xxResponseOnGET() {
        stubFor(get(urlPathEqualTo("/contacts")).willReturn(aResponse().withStatus(404)));

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Flowable<String> flowable = client.executeToCompletion(request, ServerResponse::getResponseBody);

        TestSubscriber<String> sub = flowable.test();

        sub.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        sub.assertError( t -> {
            if(t instanceof HttpClientError) {
                return ((HttpClientError)t).getStatusCode() == 404;
            }
            return false;
        });
    }

    @Test
    public void testHttp5xxResponseOnGET(){
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

        Flowable<String> flowable = client.executeToCompletion(request, ServerResponse::getResponseBody);
        TestSubscriber<String> testsubscriber = flowable.test();

        testsubscriber.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        testsubscriber.assertError( t -> {
            if(t instanceof HttpServerError) {
                return ((HttpServerError)t).getStatusCode() == 500;
            }
            return false;
        });

    }


    @Test
    public void testConnectionTimeOut(){
        //set up stub
        stubFor(get(urlEqualTo("/contacts"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(500)
                        .withStatus(200)));

        //set up client with very low connection time-out values
        RxJavaHttpClient client = new RxJavaHttpClient.Builder()
                .setRequestTimeout(100)
                .setMaxConnections(1)
                .setAccept("application/json")
                .setBaseUrl("http://localhost:" + port)
                .build();

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Flowable<String> flowable = client.executeToCompletion(request, ServerResponse::getResponseBody);

        TestSubscriber<String> testsubscriber = flowable.test();

        testsubscriber.awaitDone(1000, TimeUnit.MILLISECONDS);

        testsubscriber.assertError(TimeoutException.class);

    }

}
