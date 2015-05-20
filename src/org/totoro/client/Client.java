package org.totoro.client;

import org.totoro.Const;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * untitled.Client
 * Created by MoonlightOwl on 11/14/2014.
 * ---
 */

public class Client {
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    public Client(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Input server IP, please: ");
        String ip = scan.nextLine();
        try {
            System.out.print("Try to connect... ");
            InetAddress ipAddress = InetAddress.getByName(ip);
            try {
                socket = new Socket(ipAddress, Const.Port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Connected!");

                Receiver receiver = new Receiver();
                receiver.start();

                System.out.print("Input your nickname: ");
                String nickname = scan.nextLine();
                out.println(nickname);

                String message = "";
                while(!message.equals("exit")){
                    System.out.print("# ");
                    message = scan.nextLine();
                    out.println(message);
                }
                receiver.setStop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }
        } catch (UnknownHostException e) {
            System.err.println("IP not found!");
            e.printStackTrace();
        }
    }

    private void close(){
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e){
            System.err.println("Client: Streams were not closed properly!");
            e.printStackTrace();
        }
    }

    private class Receiver extends Thread {
        private boolean stoped;

        private void setStop(){ stoped = true; }

        @Override
        public void run(){
            try {
                while(!stoped){
                    String message = in.readLine();
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Message receiving error!");
                e.printStackTrace();
            }
        }
    }
}
