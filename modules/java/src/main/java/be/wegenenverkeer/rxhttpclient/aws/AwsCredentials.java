package be.wegenenverkeer.rxhttpclient.aws;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * AWS Credentials to use in request
 *
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
public class AwsCredentials {

    private final String secretKey;
    private final String keyId;
    private final String token;
    private final OffsetDateTime expiration;

    AwsCredentials(String keyId, String secretKey) {
        this(keyId, secretKey, null, null);
    }

    AwsCredentials(String keyId, String secretKey, String token, OffsetDateTime expiration) {
        this.keyId = keyId;
        this.secretKey = secretKey;
        this.token = token;
        this.expiration = expiration;
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

    /**
     * Returns true if these credentials are temporary
     * @return true if these credentials are temporary
     */
    public boolean isTemporary() {
        return this.expiration != null;
    }


    /**
     * Returns the optional expiration date.
     *
     * This will be defined if the credentials are temporary
     * @return the optional expiration date
     */
    public Optional<OffsetDateTime> getExpiration(){
        return Optional.ofNullable(expiration);
    }

    /**
     * Returns the optional security token
     *
     * This will be defined if the credentials are temporary
     * @return the optional security token
     */
    public Optional<String> getToken(){
        return Optional.ofNullable(token);
    }

}
