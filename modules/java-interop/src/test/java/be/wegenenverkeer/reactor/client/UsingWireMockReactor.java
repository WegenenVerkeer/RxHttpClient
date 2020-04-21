package be.wegenenverkeer.reactor.client;

import be.wegenenverkeer.UsingWireMock;
import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.reactor.ReactorHttpClient;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWireMockReactor extends UsingWireMock<ReactorHttpClient> {

    @Override
    public Builder<ReactorHttpClient, ?> getBuilder() {
        return new ReactorHttpClient.Builder();
    }
}
