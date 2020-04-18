package be.wegenenverkeer;

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
abstract public class UsingWiremockRxJava extends UsingWiremock {

        public static RxJavaHttpClient client;

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
