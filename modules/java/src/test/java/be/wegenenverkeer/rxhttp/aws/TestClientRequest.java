package be.wegenenverkeer.rxhttp.aws;

import be.wegenenverkeer.rxhttp.ClientRequest;
import be.wegenenverkeer.rxhttp.RxHttpClient;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-24.
 */
public class TestClientRequest {

    private RxHttpClient client = new RxHttpClient
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
