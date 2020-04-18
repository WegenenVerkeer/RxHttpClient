package be.wegenenverkeer.rxhttp;

import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Karel Maesen, Geovise BVBA on 31/03/16.
 */
public class TestBuilder {


    @Test(expected = IllegalStateException.class)
    public void testBuilderThrowsIllegalArgumentExceptionOnMissingBaseUrl() {
        new RxJavaHttpClient.Builder().build();
    }

    @Test
    public void testRequestSignersAreAdded() {
        RequestSigner requestSigner = clientRequest -> {
        };

        RxHttpClient client = new RxJavaHttpClient.Builder().setBaseUrl("http://foo.com").addRequestSigner(requestSigner).build();
        Assert.assertTrue(client.getRequestSigners().contains(requestSigner));
    }
}
