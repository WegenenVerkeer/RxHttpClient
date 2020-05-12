package be.wegenenverkeer.rxhttpclient;

import java.util.Optional;
import java.util.function.Function;

/**
 * The HTTP Response Status
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseStatus extends ServerResponseElement {

    /**
     * Returns the status code of the response
     * @return the status code of the response
     */
    public int getStatusCode();

    /**
     * Returns the option status text
     * @return the option status text
     */
    public Optional<String>  getStatusText();

    /**
     * {@inheritDoc}
     */
    @Override
    default <T> T match(Function<ServerResponseStatus, T> matchStatus,
                Function<ServerResponseHeaders, T> matchHeaders,
                Function<ServerResponseBodyPart, T> matchBodyPart,
                Function<ServerResponse, T> matchServerResponse) {
        return matchStatus.apply(this);
    }
}
