package be.wegenenverkeer.rxhttpclient.aws;

import java.util.Map;

/**
 * The system environment
 *
 * Created by Karel Maesen, Geovise BVBA on 22/07/16.
 */
public interface Environment {

    /**
     * The default implementation (equivalent to <code>System.getenv()</code>
     */
    Environment DEFAULT = new DefaultEnvironment();

    /**
     * Returns the environment variables as a <code>Map</code>
     * @return the environment variables as a <code>Map</code>
     */
    Map<String, String> getEnvironment();

    /**
     * Returns the environment variable specified by the key argument
     * @param key the environment variable name
     * @return the value for the specified environment variable
     */
    String getEnvironment(String key);

}
