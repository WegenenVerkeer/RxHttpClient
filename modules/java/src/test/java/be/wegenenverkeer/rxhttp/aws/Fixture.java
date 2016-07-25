package be.wegenenverkeer.rxhttp.aws;

import com.google.common.io.CharSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * AWS Request signing test suite fixture
 *
 * Created by Karel Maesen, Geovise BVBA on 09/06/16.
 */
class Fixture {


    static Aws4TestSuite testSuite;
    static private File sourceDirectory;
    static private AwsCredentialsProvider provider = new ConstantAwsCredentialProvider("AKIDEXAMPLE", "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY");
    static String dateStamp = "20110909T233600Z";

    static {
        URL url = Thread.currentThread().getContextClassLoader().getResource("aws4_testsuite");
        sourceDirectory = new File(url.getFile());
        testSuite = new Aws4TestSuite(sourceDirectory);
    }

    static AwsSignature4Signer signer() {
        return new AwsSignature4Signer(AwsRegion.US_EAST, AwsService.HOST, provider.getAwsCredentials());
    }

    public static Aws4TestSuite testSuite(){
        return testSuite;
    }

    static String getCanonicalRequest(String name) {
        return getExpected(name, ".creq");
    }

    static String getStringToSign(String name) {
        return getExpected(name, ".sts");
    }

    static String getAuthorizationHeader(String name) {
        return getExpected(name, ".authz");
    }

    public static String getSignedRequest(String name) {
        return getExpected(name, ".sreq");
    }


    static private String getExpected(String name, String ext) {
        File creq = new File(sourceDirectory, name + ext);
        CharSource cs = Files.asCharSource(creq, Charset.forName("UTF-8"));
        try{
            return cs.read().replaceAll("\\r", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
