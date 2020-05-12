package be.wegenenverkeer.rxhttpclient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-25.
 */
public class DefaultClientRequestLogFormatter implements ClientRequestLogFormatter {

    private final List<String> headersToLog;
    private final List<String> formParamNamesToLog;

    @SuppressWarnings("unchecked")
    public DefaultClientRequestLogFormatter() {
        this(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public DefaultClientRequestLogFormatter(List<String> headersToLog, List<String> formParamNamesToLog) {
        this.headersToLog = headersToLog.stream().map( String::toLowerCase).collect(Collectors.toList());
        this.formParamNamesToLog = formParamNamesToLog.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public String toLogMessage(ClientRequest request) {
        StringBuilder sb = new StringBuilder(request.getMethod())
                .append(" ")
                .append(request.getUrl());

        List<String> headerLogs = headersToLog.stream().map(header ->
                header + ":" + String.join("; ", request.unwrap().getHeaders().getAll(header))
        ).collect(Collectors.toList());

        if (!headerLogs.isEmpty()) {
            sb.append("\theaders:");
            headerLogs.forEach(hl -> {
                sb.append("\t").append(hl);
            });
        }

        List<String> paramList = request.unwrap().getFormParams().stream()
                .filter(fparam -> formParamNamesToLog.contains(fparam.getName().toLowerCase()))
                .map(fparam -> fparam.getName() + ":" + fparam.getValue())
                .collect(Collectors.toList());

        if (!paramList.isEmpty()) {
            sb.append("\tformParams:");
            paramList.forEach(pl -> {
                sb.append("\t").append(pl);
            });
        }

        return sb.toString();
    }

}

