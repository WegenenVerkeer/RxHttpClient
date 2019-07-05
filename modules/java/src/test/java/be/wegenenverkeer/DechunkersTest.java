package be.wegenenverkeer;

import be.wegenenverkeer.rxhttp.Dechunker;
import be.wegenenverkeer.rxhttp.Dechunkers;
import be.wegenenverkeer.rxhttp.ServerResponseBodyPart;
import be.wegenenverkeer.rxhttp.ServerResponseElement;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static be.wegenenverkeer.TestSRBP.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-05.
 */
public class DechunkersTest {



    @Test
    public void testStringDechunker(){
        assertEquals(Arrays.asList("first", "second"), receivedOnInput(
                bp("fi"),
                bp("rst"),
                bp("\n"),
                bp("sec"),
                last("ond")
                ));
    }

    @Test
    public void testStringDechunkerEmptySeparator(){
        assertEquals(Arrays.asList("first", "second"), receivedOnInput(
                bp("fi"),
                bp("rst"),
                bp(""),
                bp("sec"),
                last("ond")
        ));
    }

    @Test
    public void testStringDechunkerSingleEl(){
        assertEquals(Arrays.asList("first", "second"), receivedOnInput(
                last("first\nsecond")
        ));
    }

    @Test
    public void testStringDechunkerLastCombined(){
        assertEquals(Arrays.asList("first", "second", "third"), receivedOnInput(
                bp("first\n"),
                last("second\nthird")
        ));
    }




    public List<String> receivedOnInput(TestSRBP... chunks) {
        Dechunker<String> dechunker = Dechunkers.mkStringDechunker("\n", true);
        TestSubscriber<String> subscriber = new TestSubscriber<>();
        Observable.from(chunks)
                .concatMap(dechunker::dechunk)
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent(60, TimeUnit.MILLISECONDS);
        return subscriber.getOnNextEvents();
    }
}

class TestSRBP implements ServerResponseBodyPart {

    static TestSRBP bp(String body){
        return new TestSRBP(body, false);
    }

    static TestSRBP last(String body){
        return new TestSRBP(body, true);
    }


    byte[] bytes;
    boolean isLast;
    TestSRBP(String input, boolean last) {
        bytes = input.getBytes(Charset.forName("UTF8"));
        this.isLast = last;
    }


    @Override
    public byte[] getBodyPartBytes() {
        return bytes;
    }

    public boolean isLast(){
        return this.isLast;
    }

    @Override
    public String toString() {
        return "TestSRBP{" +
                "bytes=" + new String(bytes) +
                ", isLast=" + isLast +
                '}';
    }
}
