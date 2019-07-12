package be.wegenenverkeer.designtests;

import be.wegenenverkeer.rxhttp.RxHttpClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
abstract public class UsingWireMock {


    static final int REQUEST_TIME_OUT = 500;
    static final int DEFAULT_TIME_OUT = REQUEST_TIME_OUT * 5;

    static int port = 8089;

    static RxHttpClient client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(port).useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE));

    @BeforeClass
    public static void setUpAndStartServer() {

        client = new RxHttpClient.Builder()
                .setRequestTimeout(REQUEST_TIME_OUT)
                .setMaxConnections(3)
                .setAccept("application/json")
                .setBaseUrl("http://localhost:" + port)
                .build();
    }

    @SuppressWarnings("unchecked")
    public <V> List<V> items(V... v) {
        return Arrays.asList(v);
    }


}
