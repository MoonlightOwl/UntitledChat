package org.totoro;

import org.totoro.client.Client;
import org.totoro.gui.ChatFrame;
import org.totoro.server.Server;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class UntitledChat {

    public static void main(String[] args) {
        // console interface
        if(args.length > 0 && (args[0].equals("--console") || args[0].equals("-c"))) {
            String command;
            Scanner scan = new Scanner(System.in);

            System.out.println("Enter your command:");

            chat:
            while (true) {
                command = scan.next();
                switch (command) {
                    case "exit": case "q": break chat;
                    case "client": new Client(); break;
                    case "server": new Server(); break;
                    case "ip":
                        System.out.println(getCurrentIP());
                        break;
                    default:
                        System.out.println("Bad command!");
                        break;
                }
            }
        }
        // GUI
        else {
            SwingUtilities.invokeLater(ChatFrame::new);
        }
    }

    private static String getCurrentIP() {
        String result = null;
        try {
            BufferedReader reader = null;
            try {
                URL url = new URL("http://myip.by/");
                InputStream inputStream = url.openStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder allText = new StringBuilder();
                char[] buff = new char[1024];

                int count;
                while ((count = reader.read(buff)) != -1) {
                    allText.append(buff, 0, count);
                }
                // The string containing IP address is the following:
                // <a href="whois.php?127.0.0.1">whois 127.0.0.1</a>
                Integer indStart = allText.indexOf("\">whois ");
                Integer indEnd = allText.indexOf("</a>", indStart);

                String ipAddress = allText.substring(indStart + 8, indEnd);
                if (ipAddress.split("\\.").length == 4) { // minimal IP validation
                    result = ipAddress;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
