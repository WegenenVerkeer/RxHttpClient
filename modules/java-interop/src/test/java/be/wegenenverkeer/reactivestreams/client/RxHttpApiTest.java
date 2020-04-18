package be.wegenenverkeer.reactivestreams.client;


import be.wegenenverkeer.rxhttp.ClientRequest;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * These tests ensure that the code compiles and provides the required
 *  interface.
 *
 *  Functionality is tested in the RxJava (base) module.
 *
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class RxHttpApiTest extends UsingWiremockRxStreams {

    @Test
    public void smokeTest(){
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

        Publisher<String> publisher = client.executeObservably(request, bytes -> new String(bytes, Charset.forName("UTF8")));
        TestSubscriber<String> testSubscriber = TestSubscriber.create();
        publisher.subscribe(testSubscriber);

        testSubscriber.awaitDone(DEFAULT_TIME_OUT, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertValues(expectBody);


    }

}
