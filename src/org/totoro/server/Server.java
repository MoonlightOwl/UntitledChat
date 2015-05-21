package org.totoro.server;

import org.totoro.Const;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.nio.charset.StandardCharsets;

/**
 * untitled.Server
 * Created by MoonlightOwl on 11/14/2014.
 * ---
 */

public class Server {
    private final List<Connection> connections;
    private ServerSocket server;

    public Server() {
        connections = Collections.synchronizedList(new ArrayList<Connection>());

        System.out.print("Creating server socket... ");
        try {
            server = new ServerSocket(Const.Port);
            System.out.println("Done. \nWaiting for clients.");

            while(true){
                Socket client = server.accept();

                System.out.println("New connection: " + client.getInetAddress().getHostAddress());
                Connection connection = new Connection(client);
                connections.add(connection);
                connection.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.print("Closing server streams...");
            closeAll();
            System.out.println(" Done.");
        }
    }

    private void closeAll(){
        try {
            server.close();

            synchronized (connections){
                for (Connection connection : connections) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Server: streams were not closed properly!");
            e.printStackTrace();
        }
    }

    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        private String nickname = "";

        public Connection(Socket socket){
            this.socket = socket;

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                close();
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            try {
                // default client nickname
                nickname = "Anonymouse";

                String message;
                boolean fromServer = false;
                while(true) {
                    message = in.readLine();
                    if(message.equals("command:exit")) break;
                    else if(message.contains(":")){
                        String[] data = message.split("\\s*:\\s*");
                        if(data.length > 0){
                            if(data[0].equals("nick")){
                                nickname = data[1];
                                message = nickname+" cames now!";
                                fromServer = true;
                            }
                        }
                    }

                    if(fromServer)
                        System.out.println("Server: "+message);
                    else
                        System.out.println(nickname+": "+message);

                    synchronized (connections) {
                        for(Connection connection: connections){
                            if(fromServer){
                                connection.out.println("Server: "+message);
                            }
                            else
                                connection.out.println(nickname+": "+message);
                        }
                    }
                }

                System.out.println("Server: "+nickname+" has left.");

                // send info about disconnection
                synchronized (connections){
                    for(Connection connection: connections){
                        connection.out.println("Server: "+nickname+" has left.");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }

        private void close(){
            try {
                in.close();
                out.close();
                socket.close();

                connections.remove(this);
                if(connections.size() == 0){
                    Server.this.closeAll();
                    System.exit(0);
                }
            } catch (Exception e) {
                System.err.println("Server connection: streams were not closed properly!");
                e.printStackTrace();
            }
        }
    }
}
