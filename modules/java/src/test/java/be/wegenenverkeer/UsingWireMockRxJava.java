package be.wegenenverkeer;

import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
public class UsingWireMockRxJava extends UsingWireMock<RxJavaHttpClient> {

    @Override
    public Builder<RxJavaHttpClient, ?> getBuilder() {
        return new RxJavaHttpClient.Builder();
    }
}
