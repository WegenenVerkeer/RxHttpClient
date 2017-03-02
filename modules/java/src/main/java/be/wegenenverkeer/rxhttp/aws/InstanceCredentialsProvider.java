package be.wegenenverkeer.rxhttp.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

/**
 * An {@code AwsCredentialsProvider} that retrieves (temporary) credentials from the EC2 instance metadata service.
 * <p>
 * The provider will retrieve and cache the credentials. Before each invocation of {@code InstanceCredentialsProvier#getAwsCredentials()},
 * the service will check whether the credentials need to be refreshed.
 * </p>
 * <p>
 * See the <a href="http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/iam-roles-for-amazon-ec2.html#instance-metadata-security-credentials">Retrieving Security Credentials from Instance Metadata</a>, and
 * <a href="http://docs.aws.amazon.com/general/latest/gr/sigv4-add-signature-to-request.html">AWS Request Signing</a> documentation.
 * </p>
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 07/02/17.
 */
public class InstanceCredentialsProvider implements AwsCredentialsProvider {

    final private static String METADATA_URL_BASE = "http://169.254.169.254/latest/meta-data/iam/security-credentials/";
    private String roleName;
    final private static Logger logger = LoggerFactory.getLogger(InstanceCredentialsProvider.class);

    final private Parser parser = new Parser();

    /**
     * Constructs an instance with the security credentials for the specified IAM role
     *
     * @param roleName the name of the IAM role to retrieve
     */
    public InstanceCredentialsProvider(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Constructs an instance
     * <p>
     *     When constructing this instance the instance metadata services will be queried for available role names and the first
     *     role will be selected. This is same behavior as the AWS SDK.
     * </p>
     */
    public InstanceCredentialsProvider() {
        try {
            logger.debug("Retrieving role names from " + METADATA_URL_BASE);
            String roleNamesResponse = retrieveMetadata(METADATA_URL_BASE);
            logger.debug("Response is: " + roleNamesResponse);
            String[] roles = roleNamesResponse.trim().split("\n");
            if (roles.length == 0  ) {
                throw new RuntimeException("No role names found in response");
            }
            roleName = roles[0];
            logger.info("Using role " + roleName + " as Instance Credentials");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Returns the AwsCredentials for this provider
     *
     * @return the AwsCredentials
     */
    @Override
    public AwsCredentials getAwsCredentials() {
        if (parser.needsRefresh()) {
            retrieveKeys();
        }
        return new AwsCredentials(parser.accessKeyId, parser.secretAccessKey, parser.token, parser.expiration);
    }

    private void retrieveKeys() {
        String metadata_url = METADATA_URL_BASE + roleName;
        logger.info("Retrieving temporary instance credentials from " + metadata_url);
        try {
            String meta = retrieveMetadata(metadata_url);
            parser.parse(meta);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve metadata", e);
        }

    }

    private String retrieveMetadata(String url) throws IOException {
        Scanner s = null;
        try {
            s = new Scanner(new URL(url).openStream(), "UTF-8");
            return s.useDelimiter("\\A").next(); // the "\\A" pattern is the "beginning of input" boundary matcher pattern
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Throwable t) {
                } //nothing to do
            }
        }

    }

    static class Parser {

        final private ObjectMapper mapper = new ObjectMapper();
        final private int EXPIRATION_MARGIN_MINUTES = 1;
        String accessKeyId;
        String secretAccessKey;
        String token;
        OffsetDateTime expiration;

        boolean needsRefresh() {
            return token == null || (OffsetDateTime.now().isAfter(expiration.minus(EXPIRATION_MARGIN_MINUTES, ChronoUnit.MINUTES)));
        }

        void parse(String input) {
            try {
                JsonNode rootNode = mapper.readValue(input, JsonNode.class);
                accessKeyId = rootNode.findPath("AccessKeyId").asText();
                secretAccessKey = rootNode.findPath("SecretAccessKey").asText();
                token = rootNode.findPath("Token").asText();
                expiration = asTimeStamp(rootNode.findPath("Expiration"));

            } catch (IOException e) {
                throw new RuntimeException("Failed to parse metadata response", e);
            }
        }

        private OffsetDateTime asTimeStamp(JsonNode node) {
            return OffsetDateTime.parse(node.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

}
