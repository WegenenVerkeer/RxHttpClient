package be.wegenenverkeer.rxhttp;

import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseStatus extends ServerResponseElement {

    public int getStatusCode();

    public Optional<String>  getStatusText();

    @Override
    default <T> T match(Function<ServerResponseStatus, T> matchStatus,
                Function<ServerResponseHeaders, T> matchHeaders,
                Function<ServerResponseBodyPart, T> matchBodyPart,
                Function<ServerResponse, T> matchServerResponse) {
        return matchStatus.apply(this);
    }
}
