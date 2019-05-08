package be.wegenenverkeer.rxhttp.aws;

import be.wegenenverkeer.rxhttp.ClientRequest;
import be.wegenenverkeer.rxhttp.RequestSigner;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Signs Requests to AWS Services, using the v4 Signature protocol
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
public class AwsSignature4Signer implements RequestSigner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AwsServiceEndPoint endPoint;
    private final AwsCredentialsProvider credentialsProvider;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final boolean doubleUrlEnocde;


    /**
     * Constructs an instance
     * <p>
     * See also:  <a href="https://github.com/aws/aws-sdk-js/issues/853"> https://github.com/aws/aws-sdk-js/issues/853</a> for why
     * we need the doubleUrlEncode parameter, and why by default it is set to true.
     * </p>
     *
     * @param serviceEndPoint     the service Endpoint
     * @param credentialsProvider the credentials provider
     * @param doubleUrlEncode     whether canonical request URL encode already URL encoded paths
     */
    public AwsSignature4Signer(AwsServiceEndPoint serviceEndPoint, AwsCredentialsProvider credentialsProvider, boolean doubleUrlEncode) {
        this.endPoint = serviceEndPoint;
        this.credentialsProvider = credentialsProvider;
        this.doubleUrlEnocde = doubleUrlEncode;
    }

    @Override
    public void sign(ClientRequest clientRequest) {
        AwsCredentials credentials = getCredentials();
        String timeStamp = df.format(ZonedDateTime.now(ZoneOffset.UTC));
        clientRequest.addHeader("x-amz-date", timeStamp);
        clientRequest.addHeader("host", awsHost());
        Optional<String> securityToken = credentials.getToken();
        securityToken.ifPresent(s -> clientRequest.addHeader("x-amz-security-token", s));
        clientRequest.addHeader("Authorization", authHeader(clientRequest, timeStamp, credentials));
    }

    /**
     * Constructs an instance that double encodes URL paths
     *
     * @param serviceEndPoint     the service Endpoint
     * @param credentialsProvider the credentials provider
     */
    public AwsSignature4Signer(AwsServiceEndPoint serviceEndPoint, AwsCredentialsProvider credentialsProvider) {
        this(serviceEndPoint, credentialsProvider, true);
    }

    public String canonicalRequest(ClientRequest request) {
        return canonicalRequest(
                request.getMethod(),
                request.getUrl(),
                request.getQueryParams(),
                request.getHeaders(),
                request.getStringData());
    }

    public String canonicalRequest(String method, String uri, Map<String, List<String>> queryParams, Map<String, List<String>> headers, String body) {
        return method.toUpperCase()
                + "\n"
                + canonicalUri(uri)
                + "\n"
                + canonicalQueryString(queryParams)
                + "\n"
                + canonicalHeaders(headers)
                + "\n"
                + signedHeaders(headers)
                + "\n"
                + digest(body == null ? "" : body);
    }

    public AwsCredentials getCredentials() {
        return this.credentialsProvider.getAwsCredentials();
    }


    public String awsHost() {
        return endPoint.getDomain();
    }


    public String authHeader(ClientRequest request, String timestamp) {
        return authHeader(request, timestamp, this.getCredentials());
    }

    public String authHeader(ClientRequest request, String timestamp, AwsCredentials credentials) {
        String creq = canonicalRequest(request);
        logger.debug("Canonical Request: " + creq);
        String sts = stringToSign(creq, timestamp);
        logger.debug("String to sign: " + sts);
        String signature = signature(sts, timestamp, credentials);
        return formatAuthHeader(signature, timestamp, signedHeaders(request.getHeaders()), credentials);
    }

    private String canonicalUri(String uri) {
        if (uri.isEmpty()) return "/";
        try {
            return doubleUrlEnocde ? UrlEncoder.urlEncode(new URI(uri).normalize().getRawPath(), true) : new URI(uri).normalize().getRawPath();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String signedHeaders(Map<String, List<String>> headers) {
        List<String> keys = collectSorted(headers);
        return join(keys, ";");
    }

    private List<String> collectSorted(Map<String, List<String>> headers) {
        List<String> keys = headers
                .keySet()
                .stream()
                .map(s -> s.toLowerCase(Locale.ENGLISH))
                .collect(Collectors.toList());
        Collections.sort(keys);
        return keys;
    }

    private String canonicalHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        List<String> keys = collectSorted(headers);
        List<String> reqElems = new ArrayList<>();

        for (String key : keys) {
            List<String> sortedVals = headers.get(key).stream().map(AwsSignature4Signer::trimAll).collect(Collectors.toList());
            Collections.sort(sortedVals);
            String vals = join(sortedVals, ",");
            reqElems.add(key.trim() + ":" + vals);

        }
        return join(reqElems, "\n") + "\n";
    }


    private String SignedHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        List<String> keys = collectSorted(headers);
        return join(keys, ";");
    }

    private String canonicalQueryString(Map<String, List<String>> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        List<String> keys = new ArrayList<>();
        keys.addAll(queryParams.keySet());
        Collections.sort(keys);
        List<String> reqElems = new ArrayList<>();
        for (String key : keys) {
            List<String> vals = queryParams.get(key);
            Collections.sort(vals);
            reqElems.addAll(vals.stream().map(val -> key + "=" + val).collect(Collectors.toList()));
        }
        return join(reqElems, "&");
    }

    public String stringToSign(String canonicalRequest, String timestamp) {
        String hashedCanonicalRequest = digest(canonicalRequest);
        StringBuilder buf = new StringBuilder("AWS4-HMAC-SHA256\n")
                .append(timestamp).append("\n");

        appendCredentialScope(buf, timestamp)
                .append("\n")
                .append(hashedCanonicalRequest);

        return buf.toString();
    }

    public String signature(String stringToSign, String timestamp, AwsCredentials credentials) {
        try {
            byte[] signatureKey = getSignatureKey(
                    credentials.getAWSSecretKey(),
                    timestamp.substring(0, 8),
                    endPoint.getRegion().toString(),
                    endPoint.getService().prefix()
            );
            return hexEncode(hmacSHA256(stringToSign, signatureKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String formatAuthHeader(String signature, String timestamp, String SignedHeaders, AwsCredentials credentials) {
        StringBuilder builder = new StringBuilder("AWS4-HMAC-SHA256 ")
                .append("Credential=")
                .append(credentials.getAWSAccessKeyId())
                .append("/");

        appendCredentialScope(builder, timestamp)
                .append(", SignedHeaders=")
                .append(SignedHeaders)
                .append(", ")
                .append("Signature=")
                .append(signature);

        return builder.toString();
    }


    private StringBuilder appendCredentialScope(StringBuilder builder, String timestamp) {
        builder.append(timestamp.substring(0, 8))
                .append("/")
                .append(endPoint.getRegion())
                .append("/")
                .append(endPoint.getService())
                .append("/")
                .append("aws4_request");
        return builder;
    }

    private static String digest(String data) {//
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return hexEncode(digest.digest(data.getBytes()));
    }


    public static String hexEncode(byte[] digest) {
        return Hex.encodeHexString(digest);
    }


    public static byte[] hmacSHA256(String data, byte[] key) throws Exception {
        String algorithm = "hmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName)
            throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = hmacSHA256(dateStamp, kSecret);
        byte[] kRegion = hmacSHA256(regionName, kDate);
        byte[] kService = hmacSHA256(serviceName, kRegion);
        return hmacSHA256("aws4_request", kService);
    }


    private String timeStamp() {
        return this.df.format(
                java.time.Instant.now().atOffset(java.time.ZoneOffset.UTC)
        );
    }

    private static String join(Collection<String> collection, String separator) {
        StringBuilder builder = new StringBuilder();
        for (Iterator elements = collection.iterator(); elements.hasNext(); builder.append(elements.next())) {
            if (builder.length() != 0) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    private static String trimAll(String str) {
        return str.trim().replaceAll(" +", " ");
    }
}
