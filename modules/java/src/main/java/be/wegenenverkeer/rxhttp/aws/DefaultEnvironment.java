package be.wegenenverkeer.rxhttp.aws;

import java.util.Map;

/**
 * The default implementation of this system's {@code Environment}
 *
 * Created by Karel Maesen, Geovise BVBA on 22/07/16.
 */
class DefaultEnvironment implements Environment {

    @Override
    public Map<String, String> getEnvironment() {
        return System.getenv();
    }

    @Override
    public String getEnvironment(String key) {
        return System.getenv(key);
    }
}
