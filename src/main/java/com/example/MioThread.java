package com.example;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class MioThread extends Thread {
    private final Socket socket;
    private final ConcurrentHashMap<String, MioThread> clients;
    private String nomeUtente;
    private BufferedReader in;
    private DataOutputStream out;

    public MioThread(Socket socket, ConcurrentHashMap<String, MioThread> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            // Ricevi username
            while (true) {
                nomeUtente = in.readLine();
                if (nomeUtente == null || nomeUtente.trim().isEmpty() || clients.containsKey(nomeUtente)) {
                    out.writeBytes("!Username not valid\n");
                } else {
                    clients.put(nomeUtente, this);
                    broadcast("Join:" + nomeUtente);
                    break;
                }
            }

            // Gestione messaggi
            String messaggio;
            while ((messaggio = in.readLine()) != null) {
                if (messaggio.startsWith("Mess:")) {
                    broadcast("Mess:" + nomeUtente + ":" + messaggio.substring(5));
                } else if (messaggio.startsWith("MessP:")) {
                    String[] parts = messaggio.split(":", 3);
                    if (parts.length == 3) {
                        String destinatario = parts[1];
                        String msg = parts[2];
                        inviaPrivato(destinatario, "MessP:" + nomeUtente + ":" + msg);
                    }
                } else if (messaggio.equals("Disc")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(nomeUtente + " si è disconnesso.");
        } finally {
            disconnetti();
        }
    }

    private void broadcast(String messaggio) {
        clients.values().forEach(client -> {
            try {
                client.out.writeBytes(messaggio + "\n");
            } catch (IOException e) {
                System.err.println("!Errore nell'invio: " + e.getMessage());
            }
        });
    }

    private void inviaPrivato(String destinatario, String messaggio) {
        MioThread client = clients.get(destinatario);
        if (client != null) {
            try {
                client.out.writeBytes(messaggio + "\n");
            } catch (IOException e) {
                System.err.println("!Errore nell'invio privato: " + e.getMessage());
            }
        } else {
            try {
                out.writeBytes("!Destinatario non trovato\n");
            } catch (IOException e) {
                System.err.println("!Errore nella notifica: " + e.getMessage());
            }
        }
    }

    private void disconnetti() {
        if (nomeUtente != null) {
            clients.remove(nomeUtente);
            broadcast("Left:" + nomeUtente);
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("!Errore nella chiusura del socket: " + e.getMessage());
        }
    }
}