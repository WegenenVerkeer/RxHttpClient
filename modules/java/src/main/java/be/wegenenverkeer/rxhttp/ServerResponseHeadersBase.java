package be.wegenenverkeer.rxhttp;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base implementation of the ServerResponseHeaders interface. Only used internally.
 *
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
class ServerResponseHeadersBase implements ServerResponseHeaders {

    FluentCaseInsensitiveStringsMap headers = new FluentCaseInsensitiveStringsMap();

    public ServerResponseHeadersBase(HttpResponseHeaders rh) {
        if (rh.getHeaders() != null) {
            headers = rh.getHeaders();
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
}
