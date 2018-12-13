package be.wegenenverkeer.rxhttp;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Param;
import org.asynchttpclient.Request;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_LENGTH;

/**
 * Wraps a {@link Request} into a more limited interface.
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 06/12/14.
 */
public class ClientRequest {

    final private Request request;

    ClientRequest(Request request) {
        this.request = request;
    }

    public String getMethod() {
        return request.getMethod();
    }

    public long getContentLength() {
        return request.getHeaders().getInt(CONTENT_LENGTH);
    }

    public Map<String, List<String>> getHeaders() {
        HttpHeaders headers = request.getHeaders();
        return CompatUtilities.headersToMap(headers);
    }

    public Map<String, List<String>> getQueryParams() {
        List<Param> queryParams = request.getQueryParams();
        Map<String, List<String>> result = new HashMap<>();
        for (Param p : queryParams) {
            String name = p.getName();
            String val = p.getValue();
            if (result.get(name) == null) {
                List<String> vals = new ArrayList<>();
                vals.add(val);
                result.put(name, vals);
            } else {
                result.get(name).add(val);
            }
        }
        return result;
    }


    public File getFile() {
        return request.getFile();
    }

    public String getStringData() {
        return request.getStringData();
    }

    public String getVirtualHost() {
        return request.getVirtualHost();
    }

    public Boolean getFollowRedirect() {
        return request.getFollowRedirect();
    }

//    public Realm getRealm() {
//        return request.getRealm();
//    }
//
//    public ConnectionPoolPartitioning getConnectionPoolPartitioning() {
//        return request.getConnectionPoolPartitioning();
//    }
//
//    public InetAddress getInetAddress() {
//        return request.getInetAddress();
//    }
//
//    public List<Param> getFormParams() {
//        return request.getFormParams();
//    }
//
//    public List<Param> getQueryParams() {
//        return request.getQueryParams();
//    }
//
//    public BodyGenerator getBodyGenerator() {
//        return request.getBodyGenerator();
//    }
//
//    public ProxyServer getProxyServer() {
//        return request.getProxyServer();
//    }

    public long getRangeOffset() {
        return request.getRangeOffset();
    }

//    public Uri getUri() {
//        return request.getUri();
//    }


    public byte[] getByteData() {
        return request.getByteData();
    }

    public int getRequestTimeout() {
        return request.getRequestTimeout();
    }

    public InputStream getStreamData() {
        return request.getStreamData();
    }

//    public InetAddress getLocalAddress() {
//        return request.getLocalAddress();
//    }

    public String getUrl() {
        return request.getUrl();
    }

    public List<byte[]> getCompositeByteData() {
        return request.getCompositeByteData();
    }

//    public List<Part> getParts() {
//        return request.getParts();
//    }

    //TODO -- method should return charset
    public String getBodyEncoding() {
        return request.getCharset().name();
    }

    Request unwrap() {
        return this.request;
    }

    public String toString() {
        return this.request.toString();
    }

    //provided to inject headers after request is built
    public void addHeader(String header, String value) {
        List<String> hv = new ArrayList<>();
        hv.add(value);
        this.getHeaders().put(header, hv);
    }

}
