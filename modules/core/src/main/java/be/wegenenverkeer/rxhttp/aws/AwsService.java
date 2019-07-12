package be.wegenenverkeer.rxhttp.aws;

/**
 * AWS Services
 *
 * Created by Karel Maesen, Geovise BVBA on 24/06/16.
 */
public enum AwsService {

    EC2("ec2"), S3("s3"), ECR("ecr"), ECS("ecs"), ES("es"), HOST("host")  ;

    final private String prefix;

    AwsService(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Returns the common prefix for this AWS Service
     * @return the common prefix for this AWS Service
     */
    public String prefix() {
        return this.prefix;
    }

    public String toString() {
        return this.prefix;
    }

}
