package be.wegenenverkeer.reactor.client;

import be.wegenenverkeer.UsingWiremock;
import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.RxHttpClient;
import be.wegenenverkeer.rxhttp.reactor.ReactorHttpClient;
import be.wegenenverkeer.rxhttp.rxstreams.RxStreamsHttpClient;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.*;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWiremockReactor extends UsingWiremock<ReactorHttpClient> {

    @Override
    public Builder<ReactorHttpClient, ?> getBuilder() {
        return new ReactorHttpClient.Builder();
    }
}
