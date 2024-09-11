package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ExecutorService pool;
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int poolSize) {
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public void startingServer(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                pool.submit(() -> {
                    try {
                        requestProcessing(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();  // Лучше использовать logging
                        }
                    }
                });
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    private static void requestProcessing(Socket socket) throws IOException {
        while (true) {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
            ) {
                final String requestLine = in.readLine();
                if (requestLine == null) {
                    throw new IOException("Invalid request line");
                }
                final String[] parts = requestLine.split(" ");
                if (parts.length != 3) {
                    Server.badRequest(out);
                    return;
                }
                final String method = parts[0];
                final String path = parts[1];

                InputStream inputStreamBody = socket.getInputStream();
                Request request = new Request(method, path, inputStreamBody);

                Handler handler = handlers.getOrDefault(method, new ConcurrentHashMap<>()).get(request.getPath());
                if (handler == null) {
                    notFound(out);
                } else {
                    handler.handle(request, out);
                    out.flush();
                }
                System.out.println(request.getQueryParams());
            }
        }
    }

    protected void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n\r\n";
        out.write(response.getBytes());
    }

    private static void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 " + "404" + " " + "Not found" + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}
