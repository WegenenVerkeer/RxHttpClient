package be.wegenenverkeer.rxhttp;

import java.util.function.Function;

/**
 * An element of the HTTP response.
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseElement {

    /**
     * Apply a function on this instance, depending on its type.
     * <p/>
     * This is a condensed form of the Visitor pattern.
     *
     * @param matchStatus the function to invoke when this instance is a {@code ServerResponseStatus}
     * @param matchHeaders the function to invoke when this instance is a {@code ServerResponseHeaders}
     * @param matchBodyPart the function to invoke when this instance is a {@code ServerResponseBodyPart}
     * @param matchServerResponse the function to invoke when this instance is a {@code ServerResponse}
     * @param <T> the return type of all matcher functions
     * @return the result of the matcher function that matches this instance type
     */
    public abstract <T> T match(Function<ServerResponseStatus, T> matchStatus,
                                Function<ServerResponseHeaders, T> matchHeaders,
                                Function<ServerResponseBodyPart, T> matchBodyPart,
                                Function<ServerResponse, T> matchServerResponse
    );
}
