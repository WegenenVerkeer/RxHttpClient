package be.wegenenverkeer.rest;

import com.ning.http.client.Param;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class ClientRequest {

    final private HttpMethod method;

    final private String urlRelativePath;

    final private Optional<String> bodyText;

    final private List<Param> headers;

    final private List<Param> queryParameters;

    ClientRequest(
            HttpMethod method,
            String urlRelativePath,
            String bodyText,
            List<Param> headers,
            List<Param> params
    ) {
        if (method == null) throw new IllegalStateException("Missing HTTP method");
        this.method = method;
        if (urlRelativePath == null) throw new IllegalStateException("Missing (relative) URL path");
        this.urlRelativePath = urlRelativePath;
        this.bodyText = Optional.ofNullable(bodyText);
        this.headers = headers;
        this.queryParameters = params;
    }

    public HttpMethod method() {
        return this.method;
    }

    public String urlRelativePath() {
        return this.urlRelativePath;
    }

    public Optional<String> bodyText() {
        return this.bodyText;
    }

    public List<Map.Entry<String, String>> headers() {
        return toEntries(headers);
    }

    public List<Map.Entry<String, String>> queryParameters() {
        return toEntries(queryParameters);
    }

    private List<Map.Entry<String,String>> toEntries(List<Param> params) {
        List<Map.Entry<String, String>> list = new ArrayList<>();
        for (Param p : params) {
            list.add(new ParamEntry(p.getName(), p.getValue()));
        }
        return list;
    }

    Request toAsyncRequest() {
        RequestBuilder builder = new RequestBuilder(this.method.toString());
        return null;
    }

    static class ParamEntry implements Map.Entry<String, String> {
        final private String name;
        final private String value;
        ParamEntry(String name, String value){
            this.name = name;
            this.value = value;
        }
        @Override
        public String getKey() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException();
        }
    }
}
