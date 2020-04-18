package be.wegenenverkeer.rxhttp;

/**
 * A request signer can be used to customize requests with authorization headers.
 */
public interface RequestSigner {
    void sign(ClientRequest clientRequest);
}
