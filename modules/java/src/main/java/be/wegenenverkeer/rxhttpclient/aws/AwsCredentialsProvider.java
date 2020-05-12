package be.wegenenverkeer.rxhttpclient.aws;

/**
 * Provider for {@code AwsCredentials}
 *
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
public interface AwsCredentialsProvider {

    /**
     * Returns the AwsCredentials for this provider
     * @return the AwsCredentials
     */
    AwsCredentials getAwsCredentials();

}
