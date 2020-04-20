package be.wegenenverkeer.reactivestreams.client;

import be.wegenenverkeer.UsingWireMock;
import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.rxstreams.RxStreamsHttpClient;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWireMockRxStreams extends UsingWireMock {

    @Override
    public Builder getBuilder() {
        return new RxStreamsHttpClient.Builder();
    }
}
