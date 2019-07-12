package be.wegenenverkeer.rxhttp;

import java.util.function.Function;

/**
 * a part of the response body, either a chunk in a chunked-encoded response, or the whole response body.
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseBodyPart extends ServerResponseElement {

    /**
     * Returns the bytes of the response body.
     * @return the bytes of the response body.
     */
    byte[] getBodyPartBytes();

    boolean isLast();

    default boolean isEmpty() {
        return getBodyPartBytes().length == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <T> T match(Function<ServerResponseStatus, T> matchStatus,
                        Function<ServerResponseHeaders, T> matchHeaders,
                        Function<ServerResponseBodyPart, T> matchBodyPart,
                        Function<ServerResponse, T> matchServerResponse) {
        return matchBodyPart.apply(this);
    }

}
