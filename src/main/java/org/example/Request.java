package org.example;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final BufferedReader body;
    private final List<NameValuePair> queryParams;
    private List<NameValuePair> postParams;
    private Map<String, String> headers;

    public Request(String method, String path, InputStream body) {
        this.method = method;
        this.body = new BufferedReader(new InputStreamReader(body));
        String[] parts = path.split("\\?");
        this.path = parts[0];
        if (parts.length > 1) {
            this.queryParams = URLEncodedUtils.parse(parts[1], StandardCharsets.UTF_8);
        } else {
            this.queryParams = List.of(); // Создаем пустой список, если нет параметров
        }
    }

    public List<NameValuePair> getPostParam(String name) {
        return this.postParams.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getPostParam() {
        return Collections.unmodifiableList(this.postParams);
    }

    public List<NameValuePair> getPostParams() {
        return postParams;
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

    private void parseBody() throws IOException {
        if ("POST".equalsIgnoreCase(method)) {
            String contentType = getHeader("Content-Type");
            if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                StringBuilder bodyBuilder = new StringBuilder();
                String line;
                while ((line = body.readLine()) != null) {
                    bodyBuilder.append(line);
                }
                String body = bodyBuilder.toString();
                this.postParams = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
            } else {
                this.postParams = Collections.emptyList();
            }
        } else {
            this.postParams = Collections.emptyList();
        }
    }

    private String getHeader(String headerName) {
        return headers.getOrDefault(headerName.toLowerCase(), null);
    }
}
