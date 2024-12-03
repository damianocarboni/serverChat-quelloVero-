package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) {
        System.out.println("Server in ascolto...");
        ConcurrentHashMap<String, MioThread> clients = new ConcurrentHashMap<>();

        try (ServerSocket serverSocket = new ServerSocket(3000)) {
            while (true) {
                Socket socket = serverSocket.accept();
                MioThread clientThread = new MioThread(socket, clients);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("!Errore del server: " + e.getMessage());
        }
    }
}