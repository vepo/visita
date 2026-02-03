package dev.vepo.infra;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;

public class StaticServer implements AutoCloseable {
    public static StaticServer serverFor(String path) {
        return new StaticServer(StaticServer.class.getClassLoader().getResource(path));
    }

    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final String content;
    private final String contentType;
    private volatile boolean running;
    private Thread serverThread;

    private StaticServer(URL resource) {
        // Read the content from the resource
        try (InputStream inputStream = resource.openStream();
                ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            this.content = result.toString(StandardCharsets.UTF_8);

            // Determine content type based on file extension
            String path = resource.getPath();
            if (path.endsWith(".html")) {
                this.contentType = "text/html; charset=utf-8";
            } else if (path.endsWith(".css")) {
                this.contentType = "text/css; charset=utf-8";
            } else if (path.endsWith(".js")) {
                this.contentType = "application/javascript; charset=utf-8";
            } else if (path.endsWith(".json")) {
                this.contentType = "application/json; charset=utf-8";
            } else if (path.endsWith(".xml")) {
                this.contentType = "application/xml; charset=utf-8";
            } else {
                this.contentType = "text/plain; charset=utf-8";
            }

            // Start server on a random available port
            this.serverSocket = new ServerSocket(0);
            this.threadPool = Executors.newFixedThreadPool(10);
            this.running = true;

            // Start the server thread
            startServer();
        } catch (IOException ioe) {
            Assertions.fail("Cannot start test server!", ioe);
            throw new RuntimeException("Cannot start test server!", ioe);
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public String getServerURL() {
        return "http://localhost:%d/".formatted(getPort());
    }

    @Override
    public void close() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        if (serverThread != null) {
            try {
                serverThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startServer() {
        serverThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Read the request line
            String requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }

            // Parse the request path
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendBadRequest(out);
                return;
            }

            String path = requestParts[1];

            // Only serve the root path "/"
            if (path.equals("/") || path.equals("/index.html") || path.equals("/index.htm")) {
                sendSuccess(out, content, contentType);
            } else {
                sendNotFound(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSuccess(PrintWriter out, String content, String contentType) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + content.getBytes(StandardCharsets.UTF_8).length);
        out.println("Connection: close");
        out.println();
        out.println(content);
    }

    private void sendNotFound(PrintWriter out) {
        String notFoundContent = "<html><body><h1>404 Not Found</h1></body></html>";
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html; charset=utf-8");
        out.println("Content-Length: " + notFoundContent.getBytes(StandardCharsets.UTF_8).length);
        out.println("Connection: close");
        out.println();
        out.println(notFoundContent);
    }

    private void sendBadRequest(PrintWriter out) {
        String badRequestContent = "<html><body><h1>400 Bad Request</h1></body></html>";
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: text/html; charset=utf-8");
        out.println("Content-Length: " + badRequestContent.getBytes(StandardCharsets.UTF_8).length);
        out.println("Connection: close");
        out.println();
        out.println(badRequestContent);
    }

}