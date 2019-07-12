package be.wegenenverkeer.rxhttp;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-05.
 */
public class ServerResponseBodyPartImpl implements ServerResponseBodyPart{
    final private byte[] bytes;
    final private boolean isLast;

    public ServerResponseBodyPartImpl(byte[] bytes, boolean isLast) {
        this.bytes = bytes;
        this.isLast = isLast;
    }


    @Override
    public byte[] getBodyPartBytes() {
        return bytes;
    }

    @Override
    public boolean isLast() {
        return isLast;
    }
}
