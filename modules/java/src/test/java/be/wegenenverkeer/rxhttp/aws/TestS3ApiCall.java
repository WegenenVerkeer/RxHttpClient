package be.wegenenverkeer.rxhttp.aws;

import be.wegenenverkeer.rxhttp.ClientRequest;
import be.wegenenverkeer.rxhttp.RxHttpClient;
import be.wegenenverkeer.rxhttp.ServerResponse;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Karel Maesen, Geovise BVBA on 24/06/16.
 */
public class TestS3ApiCall {

//    @Ignore("Only run when we have correct environment set up")
    @Test
    public void testS3ApiCall(){

        AwsCredentialsProvider provider = new EnvironmentCredentialsProvider();
        RxHttpClient client = new RxHttpClient.Builder()
                .setRequestTimeout(6000)
                .setMaxConnections(3)
                .setAwsEndPoint(AwsService.S3, AwsRegion.EU_WEST)
                .setAwsCredentialsProvider(provider)
                .setAccept("application/json")
                .build();

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/geovise-aws-api-test/test_file.txt")
                //note that we need to sign the content (even if empty) in case of S3
                .addHeader("x-amz-content-sha256", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .build();

        Observable<String> observable = client.executeToCompletion(request, ServerResponse::getResponseBody);

        TestSubscriber<String> sub = new TestSubscriber<>();
        observable.subscribe(sub);

        sub.awaitTerminalEvent(6000, TimeUnit.MILLISECONDS);
        sub.getOnErrorEvents().forEach( t -> System.out.println(t.getMessage()));
        sub.assertNoErrors();

        List<String>  expectedItems = new ArrayList<String>();
        expectedItems.add("This is a test\n");
        sub.assertReceivedOnNext(expectedItems);
    }

}
