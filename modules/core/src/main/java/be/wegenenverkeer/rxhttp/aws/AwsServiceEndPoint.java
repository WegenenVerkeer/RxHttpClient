package be.wegenenverkeer.rxhttp.aws;

/**
 * Utility class to determine Endpoints for service/region combination
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 24/06/16.
 */
public class AwsServiceEndPoint {

    static private String AWS_DOMAIN = ".amazonaws.com";
    static public AwsRegion DEFAULT_REGION = AwsRegion.EU_WEST;

    final private AwsRegion region;
    final private String domain;
    final private AwsService service;

    /**
     * Creates an endpoint
     *
     * @param service the AWS Service for this endpoint
     * @param region the AWS Region to use
     * @param domain the domain name (host name) of the service
     */
    public AwsServiceEndPoint(AwsService service, AwsRegion region, String domain) {
        this.region = region;
        this.service = service;
        this.domain = domain;
    }

    public String endPointUrl() {
        return "https://" + domain;
    }

    public AwsRegion getRegion() {
        return region;
    }

    public String getDomain() {
        return domain;
    }

    public AwsService getService() {
        return service;
    }

    static public AwsServiceEndPoint defaultFor(AwsService service, AwsRegion region) {
        return new AwsServiceEndPoint(service, region, defaultHostFor(service, region));

    }

    static public AwsServiceEndPoint defaultFor(AwsService service) {
        return defaultFor(service, DEFAULT_REGION);
    }

    static public String defaultHostFor(AwsService service, AwsRegion region) {
        if (service == AwsService.S3) {
            return service.prefix() + "-" + region + AWS_DOMAIN;
        }
        return service.prefix() + "." + region + AWS_DOMAIN;
    }
}
