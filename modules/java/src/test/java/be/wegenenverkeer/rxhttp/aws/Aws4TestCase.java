package be.wegenenverkeer.rxhttp.aws;

import be.wegenenverkeer.rxhttp.ClientRequest;
import be.wegenenverkeer.rxhttp.ClientRequestBuilder;
import be.wegenenverkeer.rxhttp.RxHttpClient;

/**
 * A Test case in the AWS Request Signing Test Suite
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
class Aws4TestCase {

    private String name;
    private RxHttpClient client;
    private ClientRequestBuilder builder;
    private ClientRequest request;

    ClientRequest getRequest() {
        if (this.request == null) {
            this.request = builder.build();
        }
        return this.request;
    }

    Aws4TestCase(RxHttpClient client) {
        this.client = client;
        this.builder = client.requestBuilder();
    }

    void setName(String name) {
        this.name = name;
    }

    void setMethod(String method) {
        this.builder.setMethod(method);
    }


    void addHeader(String key, String val) {
        this.builder.addHeader(key, val);
    }


    void setBody(String body) {
        this.builder.setBody(body);
    }

    String getName() {
        return name;
    }

    void setUri(String uri) {
        this.builder.setUrlRelativetoBase(uri);
    }


    void addQueryParam(String key, String value) {
        this.builder.addQueryParam(key, value);
    }

    @Override
    public String toString() {
        return "Aws4TestCase{" +
                "name='" + name + '\'' +
                ", ClientRequest='" + request +
                '}';
    }

}
