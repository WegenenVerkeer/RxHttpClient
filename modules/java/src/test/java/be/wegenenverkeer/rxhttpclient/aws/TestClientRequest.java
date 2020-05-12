package be.wegenenverkeer.rxhttpclient.aws;

import be.wegenenverkeer.rxhttpclient.ClientRequest;
import be.wegenenverkeer.rxhttpclient.RxHttpClient;
import be.wegenenverkeer.rxhttpclient.rxjava.RxJavaHttpClient;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-24.
 */
public class TestClientRequest {

    private RxHttpClient client = new RxJavaHttpClient
            .Builder()
            .setBaseUrl("http://foo.com")
            .setMaxConnections(1)
            .build();

    @Test
    public void testAddHeaderAfterBuild() {
        ClientRequest request = client.requestBuilder().build();
        request.addHeader("Test-Header", "hasAValue");
        assertEquals(request.getHeaders().get("Test-Header"), Collections.singletonList("hasAValue"));

    }
}
