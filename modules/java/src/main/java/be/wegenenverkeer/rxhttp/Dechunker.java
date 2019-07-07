package be.wegenenverkeer.rxhttp;

import rx.Observable;
import rx.Subscriber;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * An operator that "de-chunks" the Observerable of <code>ServerResponseElement</code>s in an Observable of messages.
 *
 * Created by Karel Maesen, Geovise BVBA on 2019-07-05.
 *
 *
 */
public class Dechunker implements Observable.Operator<String, ServerResponseElement> {

    private final String separator;
    private final boolean emptyIsSeparator;
    private final Charset charset;


    /**
     * Creates an instance
     * @param separator the separator character(s)
     * @param emptyIsSeparator if set to true, empty chunks are treated as separators
     * @param charset the character set of the message stream
     */
    public Dechunker(String separator, boolean emptyIsSeparator, Charset charset) {
        this.separator = separator;
        this.emptyIsSeparator = emptyIsSeparator;
        this.charset = charset;
    }

    @Override
    public Subscriber<ServerResponseElement> call(Subscriber<? super String> subscriber) {
    return new Subscriber<ServerResponseElement>(subscriber){

        private String previous = "";

        @Override
        public void onCompleted() {
            if (!subscriber.isUnsubscribed()) {
                if (!previous.isEmpty()) {
                    subscriber.onNext(previous);
                }
                subscriber.onCompleted();
            }
        }

        @Override
        public void onError(Throwable e) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        }

        @Override
        public void onNext(ServerResponseElement element) {
            if ( element instanceof ServerResponseBodyPart ) {
                ServerResponseBodyPart sbp = (ServerResponseBodyPart) element;
                if( emitOnEmpty(sbp) ) {
                    subscriber.onNext(previous);
                    previous = "";
                }
                String[] events = toChunks(bytesToString(sbp));
                for( String ev : events) {
                    if(!ev.isEmpty())  {
                        subscriber.onNext(ev);
                    }
                }
            }
        }


        private boolean emitOnEmpty(ServerResponseBodyPart sbp) {
            return emptyIsSeparator && sbp.isEmpty() && !previous.isEmpty();
        }

        private String bytesToString(ServerResponseBodyPart part) {
            return new String(part.getBodyPartBytes(), charset);
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

    };


    }
}
