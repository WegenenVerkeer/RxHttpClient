package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
abstract public class UsingWireMock {


    static final int REQUEST_TIME_OUT = 5000;
    static final int DEFAULT_TIME_OUT = REQUEST_TIME_OUT * 5;
    static int port = 8089;
    static RxJavaHttpClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(port)
            .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
    );

    @BeforeClass
    public static void setUpAndStartServer() {

        client = new RxJavaHttpClient.Builder()
                .setRequestTimeout(REQUEST_TIME_OUT)
                .setMaxConnections(3)
                .setAccept("application/json")
                .setBaseUrl("http://localhost:" + port)
                .build();
    }

    @AfterClass
    public static void stopServer() {
        client.close();
    }
}
