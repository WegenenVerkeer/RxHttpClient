package be.wegenenverkeer.rxhttp.aws;

import be.wegenenverkeer.rxhttp.RxHttpClient;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Amazon's Request v4 Signing Test suite
 * <p>
 * Created by Karel Maesen, Geovise BVBA on 06/06/16.
 */
public class Aws4TestSuite implements Iterable<Aws4TestCase> {

    private final RxHttpClient client;
    private final File sourceDirectory;
    private final List<Aws4TestCase> cases;

    Aws4TestSuite(File sourceDir) {

        this.client = new RxHttpClient.Builder()
                .setRequestTimeout(6000)
                .setMaxConnections(3)
                .setBaseUrl("http://localhost/")
                .setAccept(null) //force that we don't set an accept-header!!
                .build();

        this.sourceDirectory = sourceDir;
        if (sourceDirectory != null) {
            cases = loadTestCases();
        } else {
            cases = new ArrayList<>();
        }
    }

    Aws4TestSuite() {
        this(null);
    }

    private List<Aws4TestCase> loadTestCases() {
        return Stream.of(sourceDirectory.list((File dir, String name) -> name.endsWith(".req")))
                //TODO -- remove this when PR #1601 is accepted in async upstream!!!
                .filter(fn -> !fn.startsWith("get-vanilla-query-unreserved"))
                .map(this::parseRequest).collect(Collectors.toList());
    }

    void clear() {
        this.cases.clear();
    }

    void addTestCase(String method, String uri, String body, Map<String, String> headers, Map<String, String> queryParams) {
        Aws4TestCase tc = new Aws4TestCase(client);
        tc.setMethod(method);
        tc.setUri(uri);
        tc.setBody(body);
        if (headers != null) {
            for (Map.Entry<String, String> kv : headers.entrySet()) {
                tc.addHeader(kv.getKey(), kv.getValue());
            }
        }
        if (queryParams != null) {
            for (Map.Entry<String, String> kv : queryParams.entrySet()) {
                tc.addQueryParam(kv.getKey(), kv.getValue());
            }
        }
        cases.add(tc);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Aws4TestCase> iterator() {
        return cases.iterator();
    }

    private Aws4TestCase parseRequest(String file) {
        File reqFile = new File(sourceDirectory, file);
        List<String> lines;
        try {
            lines = Files.readLines(reqFile, Charset.forName("UTF-8"));
            String name = file.substring(0, file.length() - 4);
            return new Parser(name, lines, client).parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    static private class Parser {
        int counter = 0;
        List<String> lines;
        Aws4TestCase testcase;

        Parser(String name, List<String> lines, RxHttpClient client) {
            this.testcase = new Aws4TestCase(client);
            testcase.setName(name);
            this.lines = lines;
        }

        Aws4TestCase parse() {
            parseMethodAndPath();
            parseHeaders();
            parseBody();
            return testcase;
        }

        private void parseBody() {
            if (counter >= lines.size()) return;
            String str = lines.get(counter++);
            StringBuilder bodyBuf = new StringBuilder(str);
            boolean start = true;
            while (!str.isEmpty() && counter < lines.size()) {
                bodyBuf.append(str);
                if (start) {
                    start = false;
                } else {
                    bodyBuf.append("\n");
                }
                str = lines.get(counter++);
            }
            testcase.setBody(bodyBuf.toString());
        }

        private void parseHeaders() {
            String str = lines.get(counter++);
            while (!str.isEmpty() && counter < lines.size()) {
                String[] elems = str.split(":");
                String k = elems[0];
                String v = collectNonFirstElems(elems, ":");
                testcase.addHeader(k, v);
                str = lines.get(counter++);
            }
        }

        private String collectNonFirstElems(String[] elems, String sep) {
            String v = elems[1];
            for (int idx = 2; idx < elems.length; idx++) {
                v += sep + elems[idx];
            }
            return v;
        }

        private void parseMethodAndPath() {
            String[] elems = lines.get(counter++).split(" ");
            testcase.setMethod(elems[0]);
            String path = elems[1];
            String[] pathElems = path.split("\\?");
            testcase.setUri(pathElems[0]);
            if (pathElems.length > 1) {
                //Replace '+' by a space in query string.
                //TODO -- check if '+' in URL should not be encoded as a space
                parseQueryString(collectNonFirstElems(pathElems, "?").replaceAll("\\+", " "));
            }
        }

        //TODO -- remove code duplication!!
        private void parseQueryString(String qStr) {
            String[] queryParams = qStr.split("&");
            Map<String, List<String>> params = new HashMap<>();
            for (String qp : queryParams) {
                String[] qpe = qp.split("=");
                String name = qpe[0];
                String val = qpe.length == 2 ? qpe[1] : "";
                this.testcase.addQueryParam(name, val);
            }

        }

    }


}



