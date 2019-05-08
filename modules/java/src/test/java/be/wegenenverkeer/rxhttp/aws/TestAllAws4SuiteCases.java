package be.wegenenverkeer.rxhttp.aws;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Runs Amazon's AWS V4 request signing Test Suite
 *
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
public class TestAllAws4SuiteCases {

    AwsSignature4Signer signer = Fixture.signer();

    @Test
    public void testCanonicalRequests() {
        for (Aws4TestCase tc : Fixture.testSuite) {
            String creq = signer.canonicalRequest(tc.getRequest());
            String expected = Fixture.getCanonicalRequest(tc.getName());
            test(true, tc, expected, creq);
        }
    }

    @Test
    public void testStringToSign() {
        for (Aws4TestCase tc : Fixture.testSuite) {
            String creq = signer.canonicalRequest(tc.getRequest());
            String sts = signer.stringToSign(creq, Fixture.dateStamp);
            String expected = Fixture.getStringToSign(tc.getName());
            test(false, tc, expected, sts);
        }
    }

    @Test
    public void testAuthorizationHeader() {
        for (Aws4TestCase tc : Fixture.testSuite) {
            String header = signer.authHeader(tc.getRequest(), Fixture.dateStamp);
            String expected = Fixture.getAuthorizationHeader(tc.getName());
            test(false, tc, expected, header);
        }
    }

    private static void test(boolean print, Aws4TestCase tc, String expected, String received) {
        if (print) {
            System.out.println("Test case: " + tc);
            System.out.println("========================================");
            System.out.println("Test case received: " + received);
            System.out.println("Test case expected: " + expected);
        }
        assertEquals("Error for " + tc.getName(), expected, received);
    }

}