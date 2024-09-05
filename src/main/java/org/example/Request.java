package org.example;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final BufferedReader body;
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, InputStream body) {
        this.method = method;
        this.body = new BufferedReader(new InputStreamReader(body));
        String[] parts = path.split("\\?", 2);
        this.path = parts[0];
        if (parts.length > 1) {
            this.queryParams = URLEncodedUtils.parse(parts[1], StandardCharsets.UTF_8);
        } else {
            this.queryParams = List.of(); // Создаем пустой список, если нет параметров
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public BufferedReader getBody() {
        return body;
    }


    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream().filter(p -> p.getName().equals(name)).collect(Collectors.toList());

    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }
}
