package be.wegenenverkeer.rxhttp;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.*;

/**
 * Base implementation of the ServerResponseHeaders interface. Only used internally.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
class ServerResponseHeadersImpl implements ServerResponseHeaders {

    Map<String, List<String>> headers = new HashMap<>();

    public ServerResponseHeadersImpl(HttpHeaders rh) {
        if (rh != null) {
            headers = CompatUtilities.headersToMap(rh);
        }
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public Optional<String> getContentType() {
        return getHeader("Content-type");
    }

    @Override
    public List<String> getHeaders(String name) {
        List<String> list = headers.get(name);
        if (list == null) return new ArrayList<>();
        return list;
    }

    @Override
    public Optional<String> getHeader(String name) {
        List<String> l = getHeaders(name);
        if (l.isEmpty()) return Optional.empty();
        return Optional.ofNullable(l.get(0));
    }

    public String toString(){
        return headers.toString();
    }
}
