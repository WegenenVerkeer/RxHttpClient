package be.wegenenverkeer.reactor.client;

import be.wegenenverkeer.UsingWiremock;
import be.wegenenverkeer.rxhttp.RxHttpClient;
import be.wegenenverkeer.rxhttp.reactor.ReactorHttpClient;
import be.wegenenverkeer.rxhttp.rxstreams.RxStreamsHttpClient;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
abstract public class UsingWiremockReactor{

    //TODO -- this is a hack to solve issue with port-in-use conflict (Wiremock).
    public static final int REQUEST_TIME_OUT = 5000;
    public static final int DEFAULT_TIME_OUT = REQUEST_TIME_OUT * 5;
    public static int port = 8088;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(port)
            .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
    );

    public static ReactorHttpClient client;

    @BeforeClass
    public static void setUpAndStartServer() {
        client = new ReactorHttpClient.Builder()
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
