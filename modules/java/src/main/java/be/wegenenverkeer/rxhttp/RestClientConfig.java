package be.wegenenverkeer.rxhttp;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-12.
 */
public class RestClientConfig {

    private String baseUrl = "";
    private String Accept = "application/json";

    private boolean throttling = false;
    private int throttlingMaxWait = 0;
    private int maxConnections = -1;

    public void enableThrottling() {
        this.throttling = true;
    }

    public void setThrottlingMaxWait(int throttlingMaxWait) {
        this.throttlingMaxWait = throttlingMaxWait;
    }

    public void setMaxConnections(int maxConn) {
        this.maxConnections = maxConn;
    }

    public boolean isThrottling() {
        return throttling;
    }

    public int getThrottlingMaxWait() {
        return throttlingMaxWait;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = chopLastForwardSlash(baseUrl);
    }

    String getBaseUrl() {
        return baseUrl;
    }

    String getAccept() {
        return Accept;
    }

    void setAccept(String accept) {
        Accept = accept;
    }

    private static String chopLastForwardSlash(String url) {
        if (url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

}

