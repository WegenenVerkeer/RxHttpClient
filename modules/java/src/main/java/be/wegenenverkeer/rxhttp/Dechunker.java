package be.wegenenverkeer.rxhttp;

import rx.Observable;

/**
 * De-chunks received <code>ServerResponseBodyPart</code>s into an list of messages
 *
 * Created by Karel Maesen, Geovise BVBA on 2019-07-05.
 *
 * @param <O> The type of Output chunk
 */
public interface Dechunker<O> {

    //TODO can we make this into an Operator??
    Observable<O> dechunk(ServerResponseElement e);



}
