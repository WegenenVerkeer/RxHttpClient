package be.wegenenverkeer.reactor.client;

import be.wegenenverkeer.rxhttp.ClientRequest;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.charset.Charset;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * These tests ensure that the code compiles and provides the required
 *  interface.
 *
 *  Functionality is tested in the RxJava (base) module.
 *
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class ReactorHttpTest extends UsingWireMockReactor {

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
        Flux<String> flux = client.executeObservably(request, bytes -> new String(bytes, Charset.forName("UTF8")));

        StepVerifier
                .create(flux)
                .expectNext(expectBody)
                .expectComplete()
                .verify();

    }

}
