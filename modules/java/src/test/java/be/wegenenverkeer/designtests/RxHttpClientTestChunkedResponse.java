package be.wegenenverkeer.designtests;

import be.wegenenverkeer.UsingWireMockRxJava;
import be.wegenenverkeer.rxhttp.ClientRequest;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;

/**
 * Tests that demonstrate processing a long and slow chunked response.
 * <p>
 * Notice that these tests require running the node server.js test server.That is why they are checked-in as @ignored
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */

public class RxHttpClientTestChunkedResponse extends UsingWireMockRxJava {

    private final static int SIZE = 1_000_000;
    static File tmp;
    static File tmpDir;

    static {
        //Generate a large test file to text chunked transfer encoding
        try {
            Path td = Files.createTempDirectory("wireMock");
            Path files = Files.createDirectory(Path.of(td.toString(), "__files"));
            Path outF = Files.createTempFile(files, "wm", ".bin");

            tmp = outF.toFile();
            tmp.deleteOnExit();
            tmpDir = td.toFile();
            tmpDir.deleteOnExit();

            try(Writer out = Files.newBufferedWriter(outF)) {
                for(long i=0 ; i < SIZE; i++){
                    out.write("Event in output nr. " + i + "\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected FileSource fileRoot() {
        return new SingleRootFileSource(tmpDir);
    }

    @Test
    public void testChunkedTransfer() throws InterruptedException {
        stubFor(
                get(urlPathEqualTo("/sse"))
                        .willReturn(aResponse()
                                .withBodyFile(tmp.getName())
                                .withChunkedDribbleDelay(300_000, 2000)
                        )
        );

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/sse")
                .build();

        Flowable<String> flowable = client.executeAndDechunk(request, "\n");

        TestSubscriber<String> subscriber = flowable.test();
        subscriber.awaitDone(20_000, TimeUnit.MILLISECONDS);


        subscriber.assertValueCount(SIZE);

    }


    @Test
    public void testCancellation() throws InterruptedException {
        stubFor(
                get(urlPathEqualTo("/sse"))
                        .willReturn(aResponse()
                                .withBodyFile("sse-output.txt")
                                .withChunkedDribbleDelay(50, 60000)
                        )
        );

        ClientRequest request = client.requestBuilder()
                .setMethod("GET")
                .setUrlRelativetoBase("/sse")
                .build();
        Flowable<String> flowable = client.executeAndDechunk(request, "\n");

        TestSubscriber<String> subscriber = flowable.test();

        Thread.sleep(50);
        subscriber.cancel();
        assertTrue(subscriber.isCancelled());
        subscriber.assertValueCount(0);


    }

    //TODO verify that cancellation works properly
    //TODO verify error handling


}
