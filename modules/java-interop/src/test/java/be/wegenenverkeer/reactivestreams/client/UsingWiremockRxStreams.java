package be.wegenenverkeer.reactivestreams.client;

import be.wegenenverkeer.UsingWiremock;
import be.wegenenverkeer.rxhttp.Builder;
import be.wegenenverkeer.rxhttp.RxHttpClient;
import be.wegenenverkeer.rxhttp.rxstreams.RxStreamsHttpClient;
import org.junit.After;
import org.junit.Before;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWiremockRxStreams extends UsingWiremock {

    @Override
    public Builder getBuilder() {
        return new RxStreamsHttpClient.Builder();
    }
}
