package be.wegenenverkeer.rxhttp.aws;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 10/02/17.
 */
public class TestUrlEncodedUrls {

    AwsSignature4Signer signer = Fixture.signer(true);
    Aws4TestSuite suite = new Aws4TestSuite();

    @Before
    public void setup() {
        suite.addTestCase("PUT", "/dc-prx/sad-schadedossier/dc%3A%2F%2Fsad-schadedossier%2F693%2FSV-17-214-00005", null, null, null);
    }

    @Test
    public void testCanonicalRequests() {
        for (Aws4TestCase tc : suite) {
            String creq = signer.canonicalRequest(tc.getRequest());
            assertEquals(expectedCReq, creq);
        }
    }

//    @Test
//    public void testStringToSign() {
//        for (Aws4TestCase tc : Fixture.testSuite) {
//            String creq = signer.canonicalRequest(tc.getRequest());
//
//        }
//    }

    String expectedCReq = "PUT\n/dc-prx/sad-schadedossier/dc%253A%252F%252Fsad-schadedossier%252F693%252FSV-17-214-00005\n\n\n\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
}
