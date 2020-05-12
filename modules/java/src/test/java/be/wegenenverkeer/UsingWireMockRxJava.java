package be.wegenenverkeer;

import be.wegenenverkeer.rxhttpclient.Builder;
import be.wegenenverkeer.rxhttpclient.rxjava.RxJavaHttpClient;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
public class UsingWireMockRxJava extends UsingWireMock<RxJavaHttpClient> {

    @Override
    public Builder<RxJavaHttpClient, ?> getBuilder() {
        return new RxJavaHttpClient.Builder();
    }
}
