package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(64);
        try {
            server.addHandler("GET", "/messages", new Handler() {
                public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                    String body = "method = GET , path = massages";
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + "text/plain" + "\r\n" +
                                    "Content-Length: " + body.length() + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    responseStream.write(body.getBytes());
                    responseStream.flush();
                }
            });
            server.addHandler("GET", "/hello", new Handler() {
                public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                    String body = "method = GET , path = hello";
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + "text/plain" + "\r\n" +
                                    "Content-Length: " + body.length() + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    responseStream.write(body.getBytes());
                    responseStream.flush();
                }
            });
            server.addHandler("POST", "/messages", new Handler() {
                public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                    String body = "method = POST , path = massages";
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + "text/plain" + "\r\n" +
                                    "Content-Length: " + body.length() + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    responseStream.write(body.getBytes());
                    responseStream.flush();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        server.startingServer(9999);
    }
}