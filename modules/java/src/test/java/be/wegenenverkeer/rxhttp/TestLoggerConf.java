package be.wegenenverkeer.rxhttp;

import be.wegenenverkeer.rxhttp.rxjava.RxJavaHttpClient;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-25.
 */
public class TestLoggerConf {

    private RxHttpClient client = null;

    @Test
    public void testLoggerConfDefault(){
        client = new RxJavaHttpClient.Builder().setBaseUrl("http://foo.com").build();
        try {
            ClientRequest request = client.requestBuilder().setMethod("GET").setUrlRelativetoBase("/test").build();
            assertEquals("GET http://foo.com/test", client.toLogMessage(request));
        } finally {
            client.close();
        }
    }

    @Test
    public void testLoggerConfWithHeader(){
        client = new RxJavaHttpClient.Builder()
                .setBaseUrl("http://foo.com")
                .logHeaders(Arrays.asList("Test-Header"))
                .build();
        try {
            ClientRequest request = client
                    .requestBuilder()
                    .setMethod("GET")
                    .setUrlRelativetoBase("/test")
                    .setHeader("Test-Header", "testvalue")
                    .build();
            assertEquals("GET http://foo.com/test\theaders:\ttest-header:testvalue", client.toLogMessage(request));
        } finally {
            client.close();
        }
    }

    @Test
    public void testLoggerConfWithHeaderIsCaseInsentivie(){
        client = new RxJavaHttpClient.Builder()
                .setBaseUrl("http://foo.com")
                .logHeaders(Arrays.asList("Test-Header"))
                .build();
        try {
            ClientRequest request = client
                    .requestBuilder()
                    .setMethod("GET")
                    .setUrlRelativetoBase("/test")
                    .setHeader("test-Header", "testvalue")
                    .build();
            assertEquals("GET http://foo.com/test\theaders:\ttest-header:testvalue", client.toLogMessage(request));
        } finally {
            client.close();
        }
    }

    @Test
    public void testLoggerConfWithFParamIsCaseInsensitive(){
        client = new RxJavaHttpClient.Builder()
                .setBaseUrl("http://foo.com")
                .logFormParams(Arrays.asList("TestParam"))
                .build();
        try {
            ClientRequest request = client
                    .requestBuilder()
                    .setMethod("GET")
                    .setUrlRelativetoBase("/test")
                    .addFormParam("testparam", "testparamValue")
                    .build();
            assertEquals("GET http://foo.com/test\tformParams:\ttestparam:testparamValue", client.toLogMessage(request));
        } finally {
            client.close();
        }

    }

    @Test
    public void testLoggerConfWithFormParam(){
        client = new RxJavaHttpClient.Builder()
                .setBaseUrl("http://foo.com")
                .logFormParams(Arrays.asList("TestParam"))
                .build();
        try {
            ClientRequest request = client
                    .requestBuilder()
                    .setMethod("GET")
                    .setUrlRelativetoBase("/test")
                    .addFormParam("TestParam", "testparamValue")
                    .build();
            assertEquals("GET http://foo.com/test\tformParams:\tTestParam:testparamValue", client.toLogMessage(request));
        } finally {
            client.close();
        }

    }

}
