package be.wegenenverkeer.rxhttp.aws;

import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 07/02/17.
 */
public class TestInstanceCredentialsProvider {

    String responseText = "{\n" +
            "  \"Code\" : \"Success\",\n" +
            "  \"LastUpdated\" : \"2017-02-07T16:22:45Z\",\n" +
            "  \"Type\" : \"AWS-HMAC\",\n" +
            "  \"AccessKeyId\" : \"ACCESSKEY\",\n" +
            "  \"SecretAccessKey\" : \"VERYVERYSECRET\",\n" +
            "  \"Token\" : \"thisisatoken=\",\n" +
            "  \"Expiration\" : \"2017-02-07T22:41:36Z\"\n" +
            "}";

    @Test
    public void testParse() {

        InstanceCredentialsProvider.Parser parser = new InstanceCredentialsProvider.Parser();

        parser.parse(responseText);

        assertEquals("ACCESSKEY", parser.accessKeyId);
        assertEquals("VERYVERYSECRET", parser.secretAccessKey);
        assertEquals("thisisatoken=", parser.token);
        assertEquals(OffsetDateTime.of(2017, 2, 7, 22, 41, 36, 0, ZoneOffset.UTC), parser.expiration);

    }
}
