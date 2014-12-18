package be.wegenenverkeer.rest;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/12/14.
 */
public interface ServerResponseBodyPart extends ServerResponseElement {

    public abstract byte[] getBodyPartBytes();

}
