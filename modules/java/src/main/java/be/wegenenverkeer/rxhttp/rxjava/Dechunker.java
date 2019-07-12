package be.wegenenverkeer.rxhttp.rxjava;

import io.reactivex.FlowableOperator;
import io.reactivex.FlowableSubscriber;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.subscribers.DefaultSubscriber;
import io.reactivex.subscribers.DisposableSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * An operator that "de-chunks" the Observerable of <code>ServerResponseElement</code>s in an Observable of messages.
 *
 * Created by Karel Maesen, Geovise BVBA on 2019-07-05.
 *
 *
 */
public class Dechunker implements FlowableOperator<String, String> {

    private final String separator;


    public Dechunker(String sep) {
        this.separator = sep;

    }

    @Override
    public Subscriber<? super String> apply(Subscriber<? super String> subscriber) throws Exception {
        return new Op(subscriber, separator);
    }

    static final class Op implements FlowableSubscriber<String>, Subscription {
        final Subscriber<? super String> child;
        final String separator;

        private String previous = "";
        Subscription s;

        public Op(Subscriber<? super String> child, String separator) {
            this.child = child;
            this.separator = separator;
        }


        @Override
        public void onSubscribe(Subscription s) {
            this.s =s;
            child.onSubscribe(this);
        }

        @Override
        public void onNext(String str) {
            String[] events = toChunks(str);
            for( String ev : events) {
                if(!ev.isEmpty())  {
                    child.onNext(ev);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            child.onError(t);
        }

        @Override
        public void onComplete() {
            if (!previous.isEmpty()) {
                child.onNext(previous);
            }
        }

        @Override
        public void request(long n) {
            s.request(n);
        }

        @Override
        public void cancel() {
            s.cancel();
        }

        private String[] toChunks(String chunk) {
            String withPrevious = previous + chunk;
            String[] parts = withPrevious.split(separator);
            if (chunk.endsWith(separator)) {
                previous = "";
                return parts;
            } else {
                previous = parts[parts.length - 1];
                return Arrays.copyOfRange(parts,0, parts.length - 1);
            }
        }
    }




}
