package be.wegenenverkeer.rxhttp.aws;

/**
 * AWS Credentials to use in request
 *
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
public class AwsCredentials {

    private final String secretKey;
    private final String keyId;

    AwsCredentials(String keyId, String secretKey) {
        this.keyId = keyId;
        this.secretKey = secretKey;
    }

    /**
     * Returns the AWS Key Id
     * @return the AWS Key Id
     */
    public String getAWSAccessKeyId() {
        return keyId;
    }

    /**
     * Returns the AWS secret access key
     *
     * @return The AWS secret access key
     */
    public String getAWSSecretKey(){
        return secretKey;
    }

}
