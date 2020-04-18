package be.wegenenverkeer.rxhttp.aws;

/**
 * AWS Regions
 *
 * Created by Karel Maesen, Geovise BVBA on 24/06/16.
 */
public enum AwsRegion {

    EU_WEST("eu-west-1"), EU_CENTRAL("eu_central_1"), US_EAST("us-east-1"), US_WEST_1("us-west-1"), US_WEST_2("us-west-2");

    private final String regionString;

    AwsRegion(String regionString) {
        this.regionString = regionString;
    }

    public String toString() {
        return this.regionString;
    }

}
