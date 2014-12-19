package be.wegenenverkeer.rxhttp;

import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseElement {

    public abstract <T> T match(Function<ServerResponseStatus, T> matchStatus,
                                Function<ServerResponseHeaders, T> matchHeaders,
                                Function<ServerResponseBodyPart, T> matchBodyPart,
                                Function<ServerResponse, T> matchServerResponse
    );
}
