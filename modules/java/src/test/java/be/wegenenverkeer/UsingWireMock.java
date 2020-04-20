package be.wegenenverkeer;

import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.RxHttpClient;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;


import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWireMock<C extends RxHttpClient> {

    public final int REQUEST_TIME_OUT = 5000;
    public final int DEFAULT_TIME_OUT = REQUEST_TIME_OUT * 5;

    public C client;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .dynamicPort()
            .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
    );

    protected int port() {
        return wireMockRule.port();
    }
    ;

    @Before
    public void setUpAndStartServer() {
        client = getBuilder()
                .setRequestTimeout(REQUEST_TIME_OUT)
                .setMaxConnections(3)
                .setAccept("application/json")
                .setBaseUrl("http://localhost:" + port())
                .build();
    }

    @After
    public void stopServer() {
        client.close();
    }

    public Builder<C, ?> getBuilder() { return null;}

}
