package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private static ExecutorService pool;

    public Server(int poolSize) {
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public void startingServer() {
        try (final ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                pool.submit(() -> {
                    try {
                        requestProcessing(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
                 final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final String requestLine = in.readLine();
                final String[] parts = requestLine.split(" ");

                if (parts.length != 3) {
                    // just close socket
                    out.write((
                            "HTTP/1.1 400 Bad Request\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    socket.close();
                }

                final String path = parts[1];
                if (!validPaths.contains(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    continue;
                }

                final Path filePath = Path.of(".", "public", path);
                final String mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
                    final String template = Files.readString(filePath);
                    final byte[] content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                    continue;
                }

                final long length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
