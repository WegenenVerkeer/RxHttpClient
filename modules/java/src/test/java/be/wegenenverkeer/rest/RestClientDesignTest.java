package be.wegenenverkeer.rest;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.ning.http.client.Response;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.Observable;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Behavior Unit test
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class RestClientDesignTest {


    private static int port = 8089;
    public static WireMockServer server;

    //Use a countdown latch to have the unit test wait until async-callbacks have been handled
    private CountDownLatch lock = new CountDownLatch(1);



    @BeforeClass
    public static void setUpAndStartServer(){
        server = new WireMockServer( wireMockConfig().port(port));
        server.start();
        configureFor("localhost", port);
    }

    @AfterClass
    public static void shutDownServer(){
        server.shutdown();
    }

    @Before
    public void resetServer() {
//        WireMock.resetToDefault();
    }

    @Test
    public void testBasicGETUsage() throws InterruptedException {


        stubFor(get(urlEqualTo("/contacts"))
//                .withHeader("Accept", equalTo("text/xml"))
                .willReturn(aResponse().withFixedDelay(20)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ 'contacts': [1,2,3] }")));

        RestClient client = new RestClient.Builder()
                .setRequestTimeout(100)
                .setMaxConnections(10)
                .setBaseUrl("http://localhost:" + port)
                .build();


        String path = "/contacts";


        Observable<String> observable = client.GET(path, resp -> {try {
            return resp.getResponseBody();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }});

        observable.subscribe(
                (System.out::println),
                (Throwable t) -> { t.printStackTrace(); lock.countDown();},
                () -> { System.out.println("Completed!"); lock.countDown(); }

        );

        lock.await(2000, TimeUnit.MILLISECONDS);


    }
}
