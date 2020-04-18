package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.*;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Supports the same set of tests as {@link RxHttpClientDesignTests}, but uses the executeObservably(ServerRequest) method.
 * * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public class RxHttpClientObservableOfServerElementsTests extends UsingWireMock {

    @Test
    public void GETHappyPath(){
        //set up stub
        String expectBody = "{ 'contacts': [1,2,3] }";
        stubFor(get(urlPathEqualTo("/contacts"))
                //.withQueryParam("q", equalTo("test"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
//                        .withBodyFile("sse-output.txt")
                        .withBody(expectBody)
                )
        );

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase(path)
                //.addQueryParam("q", "test")
                .build();

        Flowable<ServerResponseElement> flowable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> sub = flowable.test();

        sub.awaitDone(DEFAULT_TIME_OUT, TimeUnit.HOURS);
        sub.assertNoErrors();
        sub.assertComplete();

        //we must receive at least 1 body part
        assertTrue( sub.values().stream().filter(t -> t instanceof ServerResponseBodyPart).count() > 0);

        for (ServerResponseElement el : sub.values()) {
            if (el instanceof ServerResponseStatus) {
                assertEquals(200, ((ServerResponseStatus) el).getStatusCode());
            } else if (el instanceof ServerResponseBodyPart) {
                assertEquals(expectBody, new String(((ServerResponseBodyPart) el).getBodyPartBytes()));
            } else if (el instanceof ServerResponseHeaders) {
                assertEquals(Optional.of("application/json"), ((ServerResponseHeaders) el).getContentType());
            } else {
                fail("Unknown Server Response element: " + el.getClass().getCanonicalName());
            }
        }


    }

    @Test
    public void testHttp4xxResponseOnGET() {
        stubFor(get(urlPathEqualTo("/contacts")).willReturn(aResponse().withStatus(404)));

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Flowable<ServerResponseElement> flowable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> testsubscriber = flowable.test();

        testsubscriber.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        testsubscriber.assertError(t -> {
            if (t instanceof HttpClientError) {
                return ((HttpClientError) t).getStatusCode() == 404;
            } else return false;
        });

    }

    @Test
    public void testHttp5xxResponseOnGET() {
        //set up stub
        stubFor(get(urlEqualTo("/contacts"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withStatus(500)));

        //set up use case
        String path = "/contacts";
        ClientRequest request = client
                .requestBuilder().setMethod("GET")
                .setUrlRelativetoBase(path)
                .build();


        Flowable<ServerResponseElement> flowable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> testsubscriber = flowable.test();

        testsubscriber.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        testsubscriber.assertError(t -> {
            if (t instanceof HttpServerError) {
                return ((HttpServerError) t).getStatusCode() == 500;
            } else return false;
        });


    }


    @Test
    public void testConnectionTimeOut() {
        //set up stub
        stubFor(get(urlEqualTo("/contacts"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse().withFixedDelay(REQUEST_TIME_OUT * 2)
                        .withStatus(200)));

        //set up use case
        String path = "/contacts";
        ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase(path).build();
        Flowable<ServerResponseElement> flowable = client.executeObservably(request);


        TestSubscriber<ServerResponseElement> testsubscriber = flowable.test();

        testsubscriber.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);

        testsubscriber.assertError(TimeoutException.class);



    }

}