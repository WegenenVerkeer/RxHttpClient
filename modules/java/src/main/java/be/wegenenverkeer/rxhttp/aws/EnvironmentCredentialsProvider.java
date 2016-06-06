package be.wegenenverkeer.rxhttp.aws;

/**
 * An {@code AwsCredentialsProvider} that uses the {@code Environment} to look up credentials
 *
 * Created by Karel Maesen, Geovise BVBA on 22/07/16.
 */
public class EnvironmentCredentialsProvider implements AwsCredentialsProvider {

    final private static String KEY_ID = "AWS_ACCESS_KEY_ID";
    final private static String SECRET = "AWS_SECRET_ACCESS_KEY";


    final private Environment env;

    /**
     * Constructs an instance using the default {@code Environment}
     */
    public EnvironmentCredentialsProvider() {
        env = Environment.DEFAULT;
    }

    /**
     * Constructs an instance using the specified {@code Environment}
     * @param env
     */
    public EnvironmentCredentialsProvider(Environment env) {
        this.env = env;
    }

    @Override
    public AwsCredentials getAwsCredentials() {
        String key = env.getEnvironment(KEY_ID);
        String secret = env.getEnvironment(SECRET);
        return new AwsCredentials(key, secret);
    }
}
