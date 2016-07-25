package be.wegenenverkeer.rxhttp.aws;

/**
 * Utility class to determine Endpoints for service/region combination
 *
 * Created by Karel Maesen, Geovise BVBA on 24/06/16.
 */
public class AwsServiceEndPoint {

    static public AwsRegion DEFAULT_REGION = AwsRegion.EU_WEST;
    static private String AWS_DOMAIN = ".amazonaws.com";

    static public String UrlFor(AwsService service, AwsRegion region) {
        return "https://" + hostFor(service, region);

    }

    static public String UrlFor(AwsService service) {
        return UrlFor(service, DEFAULT_REGION);
    }

    static public String hostFor(AwsService service, AwsRegion region) {
        if (service == AwsService.S3) {
            return service.prefix() + "-" + region + AWS_DOMAIN;
        }
        return service.prefix() + "." + region + AWS_DOMAIN;
    }

    static public String hostFor(AwsService service) {
        return hostFor(service, DEFAULT_REGION);
    }
}
