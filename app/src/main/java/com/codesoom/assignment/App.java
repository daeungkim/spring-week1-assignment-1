package com.codesoom.assignment;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
    private static final int PORT = 8000;
    public static void main(String[] args) {

        InetSocketAddress address = new InetSocketAddress("localhost", PORT);

        try {
            HttpServer httpserver = HttpServer.create(address, 0);
            HttpHandler handler = new DemoHttpHandler();
            httpserver.createContext("/", handler);
            httpserver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
