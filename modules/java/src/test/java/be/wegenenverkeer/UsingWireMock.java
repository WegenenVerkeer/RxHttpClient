package be.wegenenverkeer;

import be.wegenenverkeer.rxhttpclient.Builder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/04/2020.
 */
public class UsingWireMock< C extends Closeable> {

    public final static int DEFAULT_REQUEST_TIME_OUT = 5000;

    public C client;

    private static File wireMockRootDir;

    private static void createTemporaryRoot() throws IOException {
        Path td = Files.createTempDirectory("wireMock");
        Path files = Files.createDirectory(Path.of(td.toString(), "__files"));
        wireMockRootDir = td.toFile();
        wireMockRootDir.deleteOnExit();
        files.toFile().deleteOnExit();
    }

    static {
        try {
            createTemporaryRoot();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected File getWireMockRootDir(){
        return wireMockRootDir;
    }

    protected FileSource fileRoot() {
        return new SingleRootFileSource("src/test/resources");
    }

    protected int getRequestTimeOut(){
        return DEFAULT_REQUEST_TIME_OUT;
    }

    protected int getTimeOut() {
        return DEFAULT_REQUEST_TIME_OUT * 5;
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options()
            .dynamicPort()
            .fileSource(fileRoot())
            .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
    );

    protected int port() {
        return wireMockRule.port();
    }
    ;

    @Before
    public void setUpAndStartServer() {
        client = getBuilder()
                .setRequestTimeout(getRequestTimeOut())
                .setMaxConnections(3)
                .setAccept("application/json")
                .setBaseUrl("http://localhost:" + port())
                .build();
    }

    @After
    public void stopServer() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Builder<C, ?> getBuilder() { return null;}

    protected File generateWireMockTestFile(long size) {
        try {

            Path files = Path.of(getWireMockRootDir().toString(), "__files");
            Path outF = Files.createTempFile(files, "wm", ".bin");

            File tmp = outF.toFile();
            tmp.deleteOnExit();

            try(Writer out = Files.newBufferedWriter(outF)) {
                for(long i=0 ; i < size; i++){
                    out.write("Event in output nr. " + i + "\n");
                }
            }

            return tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
