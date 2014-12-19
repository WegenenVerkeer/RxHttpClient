package be.wegenenverkeer.rxhttp;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseHeaders extends ServerResponseElement {

    public Map<String, List<String>> getHeaders();

    public Optional<String> getContentType();

    public List<String> getHeaders(String name);

    public Optional<String> getHeader(String name);

    @Override
    default <T> T match(Function<ServerResponseStatus, T> matchStatus,
                        Function<ServerResponseHeaders, T> matchHeaders,
                        Function<ServerResponseBodyPart, T> matchBodyPart,
                        Function<ServerResponse, T> matchServerResponse) {
        return matchHeaders.apply(this);
    }

}
