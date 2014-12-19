package be.wegenenverkeer.rxhttp;

import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseBodyPart extends ServerResponseElement {

    public abstract byte[] getBodyPartBytes();

    @Override
    default <T> T match(Function<ServerResponseStatus, T> matchStatus,
                        Function<ServerResponseHeaders, T> matchHeaders,
                        Function<ServerResponseBodyPart, T> matchBodyPart,
                        Function<ServerResponse, T> matchServerResponse) {
        return matchBodyPart.apply(this);
    }

}
