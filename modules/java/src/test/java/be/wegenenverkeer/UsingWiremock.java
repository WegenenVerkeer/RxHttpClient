package be.wegenenverkeer;

import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
abstract public class UsingWiremock {

    public static final int REQUEST_TIME_OUT = 5000;
    public static final int DEFAULT_TIME_OUT = REQUEST_TIME_OUT * 5;
    public static int port = 8089;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .port(port)
            .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
    );


}
