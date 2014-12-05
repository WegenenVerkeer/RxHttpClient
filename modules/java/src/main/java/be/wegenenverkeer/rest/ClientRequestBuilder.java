package be.wegenenverkeer.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class ClientRequestBuilder {

    private HttpMethod method;

    private String urlRelativePath;

    private String bodyText;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> queryParameters = new HashMap<>();

    public ClientRequest build() {
        return new ClientRequest(
                this.method,
                this.urlRelativePath,
                this.bodyText,
                this.headers,
                this.queryParameters);
    }

    public ClientRequestBuilder method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public ClientRequestBuilder urlRelativePath(String url) {
        this.urlRelativePath = url;
        return this;
    }

    public ClientRequestBuilder bodyText(String body) {
        this.bodyText = body;
        return this;
    }

    public ClientRequestBuilder addheader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public ClientRequestBuilder addQueryParameter(String key, String value) {
        this.queryParameters.put(key, value);
        return this;
    }

}
