package be.wegenenverkeer.rxhttp.aws;

/**
 * An {@AwsCredentialProvider} that always returns the credentials provided during instance construction
 *
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
public class ConstantAwsCredentialProvider implements AwsCredentialsProvider {

    public final AwsCredentials constantCredentials;


    public ConstantAwsCredentialProvider(String keyId, String secretKey) {
        this.constantCredentials = new AwsCredentials(keyId, secretKey);
    }

    @Override
    public AwsCredentials getAwsCredentials() {
        return this.constantCredentials;
    }
}
