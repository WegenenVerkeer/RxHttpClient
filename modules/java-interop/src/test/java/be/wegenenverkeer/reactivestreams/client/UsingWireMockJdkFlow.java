package be.wegenenverkeer.reactivestreams.client;

import be.wegenenverkeer.UsingWireMock;
import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.jdk.FlowHttpClient;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWireMockJdkFlow extends UsingWireMock<FlowHttpClient> {

    @Override
    public Builder getBuilder() {
        return new FlowHttpClient.Builder();
    }
}
