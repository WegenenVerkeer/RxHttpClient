package be.wegenenverkeer.rxhttpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-12.
 */
public class BuildValidation {

    private static Logger logger = LoggerFactory.getLogger(BuildValidation.class);
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    void addError(String errMsg) {
        errors.add(errMsg);
    }

    void addWarning(String warningMsg) {
        warnings.add(warningMsg);
    }

    String getErrorMessage() {
        StringBuilder builder = new StringBuilder();
        for (String msg : errors) {
            builder.append(msg).append("\n");
        }
        return chop(builder.toString());
    }

    boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    void logWarnings() {
        for (String msg : warnings) {
            logger.warn(msg);
        }
    }

    private String chop(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, s.length() - 1);
    }
}