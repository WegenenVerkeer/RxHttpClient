package be.wegenenverkeer.rxhttp;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Karel Maesen, Geovise BVBA on 31/03/16.
 */
public class TestBuilder {


    @Test(expected = IllegalStateException.class)
    public void testBuilderThrowsIllegalArgumentExceptionOnMissingBaseUrl(){
        new RxHttpClient.Builder().build();
    }

    @Test
    public void testRequestSignersAreAdded(){
        RequestSigner requestSigner = clientRequest -> {};

        RxHttpClient client = new RxHttpClient.Builder().setBaseUrl("http://foo.com").addRequestSigner(requestSigner).build();
        Assert.assertTrue(client.getRequestSigners().contains(requestSigner));
    }
}
