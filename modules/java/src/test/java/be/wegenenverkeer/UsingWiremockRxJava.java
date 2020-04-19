package be.wegenenverkeer;

import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.*;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
public class UsingWiremockRxJava extends UsingWiremock<RxJavaHttpClient> {

    @Override
    public Builder<RxJavaHttpClient, ?> getBuilder() {
        return new RxJavaHttpClient.Builder();
    }
}
