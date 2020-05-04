package be.wegenenverkeer;

import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.RxHttpClient;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;


import java.io.Closeable;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWireMock< C extends Closeable> {

    public final int REQUEST_TIME_OUT = 5000;
    public final int DEFAULT_TIME_OUT = REQUEST_TIME_OUT * 5;

    public C client;

    protected FileSource fileRoot() {
        return new SingleRootFileSource("src/test/resources");
    }
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .dynamicPort()
            .fileSource(fileRoot())
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
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Builder<C, ?> getBuilder() { return null;}

}
