package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.RxHttpClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
abstract public class UsingWireMock {


    static final int REQUEST_TIME_OUT = 5000;
    static final int DEFAULT_TIME_OUT = REQUEST_TIME_OUT * 5;

    static int port = 8089;

    static WireMockServer server;

    //use one Client for all tests.
    static RxHttpClient client;


    @BeforeClass
    public static void setUpAndStartServer() {
        server = new WireMockServer(wireMockConfig().port(port));
        server.start();
        configureFor("localhost", port);

        client = new RxHttpClient.Builder()
                .setRequestTimeout(REQUEST_TIME_OUT)
                .setMaxConnections(3)
                .setAccept("application/json")
                .setBaseUrl("http://localhost:" + port)
                .build();
    }

    @AfterClass
    public static void shutDownServer() throws InterruptedException {
        server.shutdown();
        Thread.sleep(1000); //allow the Jetty to shutdown.
    }

    @Before
    public void resetServer() {
        WireMock.resetToDefault();
    }

    public <V> List<V> items(V... v) {
        return Arrays.asList(v);
    }


}
