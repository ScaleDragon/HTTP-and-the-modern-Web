package org.example;

import java.io.*;

public class Request {
    private final String method;
    private final String path;
    private final BufferedReader body;

    public Request(String method, String path, InputStream body) {
        this.method = method;
        this.path = path;
        this.body = new BufferedReader(new InputStreamReader(body));
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

}
