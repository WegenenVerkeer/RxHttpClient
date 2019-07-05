package be.wegenenverkeer.rxhttp;

import rx.Observable;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-05.
 */
public class Dechunkers {

    /**
     * Dechunks by combining parts and splitting on the specified separator
     *
     * @param separator seperator value
     * @param emptyIsSeparator treat empty chunks as a separator
     * @param charset Charset to use when converting bytes to String
     * @return an Observable of strings in the specified Charset (without the separator values)
     */
    public static Dechunker<String> mkStringDechunker(String separator, boolean emptyIsSeparator, Charset charset) {
        return new StringDechunker(separator, emptyIsSeparator, charset);
    }

    /**
     * Dechunks by combining parts and splitting on the specified separator
     *
     * @param separator seperator value
     * @param emptyIsSeparator treat empty chunks as a separator
     * @return an Observable of UTF8 strings in the specified Charset (without the separator values)
     */
    public static Dechunker<String> mkStringDechunker(String separator, boolean emptyIsSeparator) {
        return mkStringDechunker(separator, emptyIsSeparator, Charset.forName("UTF8"));
    }

}


class StringDechunker implements Dechunker<String> {


    private final String separator;
    private final boolean emptyIsSeparator;
    private final Charset charset;

    private String previous = "";

    StringDechunker(String separator, boolean emptyIsSeparator, Charset charset) {
        this.separator = separator;
        this.emptyIsSeparator = emptyIsSeparator;
        this.charset = charset;
    }

    @Override
    public Observable<String> dechunk(ServerResponseElement element) {
        if ( element instanceof ServerResponseBodyPart ) {
            ServerResponseBodyPart sbp = (ServerResponseBodyPart) element;
            if( emitOnEmpty(sbp) ) {
                Observable<String> observable = Observable.just(previous);
                previous = "";
                return observable;
            }
            return toChunks(bytesToString(sbp), sbp.isLast());
        }
        return Observable.empty();
    }


    private boolean emitOnEmpty(ServerResponseBodyPart sbp) {
        return emptyIsSeparator && sbp.isEmpty() && !previous.isEmpty();
    }

    private String bytesToString(ServerResponseBodyPart part) {
        return new String(part.getBodyPartBytes(), charset);
    }

    private Observable<String> toChunks(String chunk, boolean lastChunk) {
        String withPrevious = previous + chunk;
        String[] parts = withPrevious.split(separator);
        if (chunk.endsWith(separator) || lastChunk) {
            previous = "";
            return Observable.from(parts);
        } else {
            previous = parts[parts.length - 1];
            return Observable.from(Arrays.copyOfRange(parts,0, parts.length - 1));
        }
    }
}

