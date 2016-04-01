package be.wegenenverkeer.rxhttp;

import be.wegenenverkeer.rxhttp.RxHttpClient;
import org.junit.Test;

/**
 * Created by Karel Maesen, Geovise BVBA on 31/03/16.
 */
public class TestBuilder {


    @Test(expected = IllegalStateException.class)
    public void testBuilderThrowsIllegalArgumentExceptionOnMissingBaseUrl(){
        new RxHttpClient.Builder().build();
    }


}
