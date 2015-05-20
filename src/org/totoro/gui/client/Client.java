package org.totoro.gui.client;

import org.totoro.Const;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * untitled.Client
 * Created by MoonlightOwl on 11/14/2014.
 * ---
 */

public class Client {
    public static final int TIMEOUT = 5000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String nickname;

    private Receiver receiver;

    public Client(){
        socket = new Socket();
        receiver = new Receiver();
        nickname = "Anonymouse";
    }

    public void setNickname(String name){
        nickname = name;
    }
    public void setMessageListener(MessageListener listener){
        receiver.setListener(listener);
    }

    public boolean connect(String ip){
        // close old session
        close();
        // open new
        try {
            // try to connect
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, Const.Port), TIMEOUT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream());

            // start
            receiver.start();

            // send nickname
            out.println("nick:" + nickname);
            out.flush();

            return true;
        } catch (SocketTimeoutException e) {
            System.err.println("[Client] Connection failed!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void send(String message) {
        if (receiver.isWorking()) {
            out.println(message);
            out.flush();
        }
    }

    public void disconnect(){
        receiver.setStop();
    }

    public boolean close(){
        if(receiver.isWorking())
            disconnect();
        try {
            if(in != null) in.close();
            if(out != null) out.close();
            if(socket != null) socket.close();
            return true;
        } catch (IOException e){
            System.err.println("[Client] Streams were not closed properly!");
            e.printStackTrace();
        }
        return false;
    }

    private class Receiver extends Thread {
        private volatile boolean stoped = true;
        private MessageListener listener;

        public void setListener(MessageListener listener){
            this.listener = listener;
        }

        private void setStop(){ stoped = true; }
        private boolean isWorking(){ return !stoped; }

        @Override
        public void run(){
            stoped = false;
            try {
                while(!stoped){
                    String message = in.readLine();
                    if(message != null)
                        listener.messageReceived(MessageListener.MESSAGE, message);
                    else setStop();
                }
            } catch (IOException e) {
                System.err.println("[Client] Message receiving error! Connection lost.");
                e.printStackTrace();
                // feedback
                listener.messageReceived(MessageListener.ERROR, "Connection lost.");
                setStop();
                close();
            }
        }
    }
}
