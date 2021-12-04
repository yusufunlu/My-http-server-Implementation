package com.yusufu.httpserver;

public class HttpServerTest {

    private static final String CONTEXT = "/app";
    private static final int PORT = 8000;

    public static void main(String[] args) throws Exception {

        // Create a new SimpleHttpServer
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(PORT, CONTEXT, new HttpRequestHandler());

        // Start the server
        simpleHttpServer.start();
        System.out.println("Server is started and listening on port "+ PORT);
    }

}