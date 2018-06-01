package be.wegenenverkeer.rxhttp;

import com.ning.http.client.Body;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ObservableBodyGeneratorTest {

    private final Random random = new Random();
    private final int chunkSize = 1024 * 8;

    private byte[] sourceArray(int size) {
        final byte[] srcArray = new byte[size];
        random.nextBytes(srcArray);
        return srcArray;
    }

    @Test
    public void testSingleRead() throws IOException {
        final int srcArraySize = chunkSize - 1;
        final byte[] srcArray = sourceArray(srcArraySize);

        Observable<byte[]> observable = Observable.just(srcArray);
        ObservableBodyGenerator bodyGenerator = new ObservableBodyGenerator(observable);

        Body body = bodyGenerator.createBody();
        final ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);

        // should take 1 read to get through the srcArray
        assertEquals(body.read(chunkBuffer), srcArraySize);
        assertEquals("bytes read", srcArraySize, chunkBuffer.position());
        chunkBuffer.clear();

        assertEquals("body at EOF", -1, body.read(chunkBuffer));
    }

    @Test
    public void testMultipleReadsFromSingleSource() throws IOException {
        final int srcArraySize = (3 * chunkSize) + 42;
        final byte[] srcArray = sourceArray(srcArraySize);

        Observable<byte[]> observable = Observable.just(srcArray);
        ObservableBodyGenerator bodyGenerator = new ObservableBodyGenerator(observable);

        Body body = bodyGenerator.createBody();
        final ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);

        int reads = 0;
        int bytesRead = 0;
        while (body.read(chunkBuffer) != -1) {
            reads += 1;
            bytesRead += chunkBuffer.position();
            chunkBuffer.clear();
        }
        assertEquals("reads to drain generator", 4, reads);
        assertEquals("bytes read", srcArraySize, bytesRead);
    }

    @Test
    public void testMultipleReadsFromMultipleSource() throws IOException {
        final int srcArraySize = chunkSize;

        Observable<byte[]> observable = Observable.just(sourceArray(srcArraySize), sourceArray(srcArraySize), sourceArray(srcArraySize), sourceArray(42));
        ObservableBodyGenerator bodyGenerator = new ObservableBodyGenerator(observable);

        Body body = bodyGenerator.createBody();
        final ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);

        int reads = 0;
        int bytesRead = 0;
        while (body.read(chunkBuffer) != -1) {
            reads += 1;
            bytesRead += chunkBuffer.position();
            chunkBuffer.clear();
        }
        assertEquals("reads to drain generator", 4, reads);
        assertEquals("bytes read", srcArraySize * 3 + 42, bytesRead);
    }

    @Test
    public void testSlowProducer() throws IOException {
        final AtomicInteger size = new AtomicInteger();

        Observable<byte[]> observable = Observable
                .interval(50, TimeUnit.MILLISECONDS)
                .map(i -> {
                    int arraySize = chunkSize - 128 + random.nextInt(256);
                    size.addAndGet(arraySize);
                    return sourceArray(arraySize);
                })
                .take(20);

        ObservableBodyGenerator bodyGenerator = new ObservableBodyGenerator(observable, 1);
        Body body = bodyGenerator.createBody();
        final ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);

        int bytesRead = 0;
        while (body.read(chunkBuffer) != -1) {
            bytesRead += chunkBuffer.position();
            chunkBuffer.clear();
        }
        assertEquals("bytes read", size.get(), bytesRead);
    }

    @Test
    public void testSlowConsumer() throws IOException, InterruptedException {
        final AtomicInteger size = new AtomicInteger();

        Observable<byte[]> observable = Observable
                .fromCallable(() -> {
                    int arraySize = chunkSize - 128 + random.nextInt(256);
                    size.addAndGet(arraySize);
                    return sourceArray(arraySize);
                })
                .repeat(50)
                .observeOn(Schedulers.computation());

        ObservableBodyGenerator bodyGenerator = new ObservableBodyGenerator(observable, 2);
        Body body = bodyGenerator.createBody();
        final ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);

        int bytesRead = 0;
        while (body.read(chunkBuffer) != -1) {
            bytesRead += chunkBuffer.position();
            chunkBuffer.clear();
            Thread.sleep(50);
        }
        System.out.println(bytesRead);
        assertEquals("bytes read", size.get(), bytesRead);

    }

}
