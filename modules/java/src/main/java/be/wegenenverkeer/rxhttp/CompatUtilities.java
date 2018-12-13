package be.wegenenverkeer.rxhttp;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Response;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * A set of utility functions to assist in the migrations from
 * AHC 1.9 to AHC 2.6
 * Created by Karel Maesen, Geovise BVBA on 2018-12-13.
 */
public class CompatUtilities {


    /**
     * Returns the headers as an immutable map
     * @param headers
     * @return
     */
    public static Map<String, List<String>> headersToMap(HttpHeaders headers) {
        throw new NotImplementedException();
    }

    public static int remaining(ByteBuf buffer) {
        throw new NotImplementedException();
    }

    public static void put(ByteBuf buffer, byte[] array, int i, int size) {
        throw new NotImplementedException();
    }

    public static String bodyExcerpt(Response response, int maxLength) {
        return bodyExcerpt(response, maxLength, "UTF-8");
    }

    public static String bodyExcerpt(Response response, int maxLength, String charset) {
        //response.getResponseBodyExcerpt(maxLength, charset);
        throw new NotImplementedException();
    }
}
