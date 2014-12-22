package be.wegenenverkeer.rxhttp;

import com.ning.http.client.*;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Wraps a {@link Request} into a more limited interface.
 *
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
        return request.getContentLength();
    }

    public Map<String, List<String>> getHeaders() {
        return request.getHeaders();
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

    public String getBodyEncoding() {
        return request.getBodyEncoding();
    }

    public Request unwrap() {
        return this.request;
    }

    public String toString() {
        return this.request.toString();
    }

}
