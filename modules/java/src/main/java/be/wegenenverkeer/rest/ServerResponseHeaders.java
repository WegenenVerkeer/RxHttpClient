package be.wegenenverkeer.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseHeaders extends ServerResponseElement {

    public Map<String, List<String>> getHeaders();

    public Optional<String> getContentType();

    public List<String> getHeaders(String name);

    public Optional<String> getHeader(String name);

}
