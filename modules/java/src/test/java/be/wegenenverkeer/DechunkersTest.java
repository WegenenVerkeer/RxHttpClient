package be.wegenenverkeer;

import be.wegenenverkeer.rxhttp.rxjava.Dechunker;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-05.
 */
public class DechunkersTest {

    private static Charset UTF8 = Charset.forName("UTF8");

    @Test
    public void testStringDechunker() {
        assertEquals(Arrays.asList("first", "second"), receivedOnInput(
                "fi",
                "rst",
                "\n",
                "sec",
                "ond"
        ));
    }


    @Test
    public void testStringDechunkerSingleEl() {
        assertEquals(Arrays.asList("first", "second"), receivedOnInput(
                "first\nsecond"
        ));
    }

    @Test
    public void testStringDechunkerLastCombined() {
        assertEquals(Arrays.asList("first", "second", "third"), receivedOnInput(
                "first\n",
                "second\nthird"
        ));
    }


    @Test
    public void testStringDechunkerCombinedThenEmpty() {
        assertEquals(Arrays.asList("first", "second", "third"), receivedOnInput(
                "first\nsecond", "\n",
                "third"
        ));
    }

    @Test
    public void testStringDechunkerSeparatorAsChunk() {
        assertEquals(Arrays.asList("first", "second", "third"), receivedOnInput(
                "fi", "rst\n", "\n", "sec", "ond\n", "\n",
                "third"
        ));
    }


    public List<String> receivedOnInput(String... chunks) {

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        Flowable.fromArray(chunks)
                .lift(new Dechunker("\n"))
                .subscribe(subscriber);
        subscriber.awaitDone(10, TimeUnit.MILLISECONDS);
        return subscriber.values();
    }
}

