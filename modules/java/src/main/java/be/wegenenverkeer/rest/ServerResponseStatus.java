package be.wegenenverkeer.rest;

import java.util.Optional;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseStatus extends ServerResponseElement {

    public int getStatusCode();

    public Optional<String>  getStatusText();


}
