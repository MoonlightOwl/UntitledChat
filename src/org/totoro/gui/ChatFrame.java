package org.totoro.gui;

import org.totoro.Const;
import org.totoro.gui.client.Client;
import org.totoro.gui.client.MessageListener;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class ChatFrame extends JFrame {
    private JTextPane textPane;
    private JTextArea textArea;
    private JScrollPane scrollTextPane, scrollTextArea;
    private JSplitPane splitPane;

    private StyledDocument chatLog;
    private SimpleAttributeSet aSet;

    private MessageListener messageListener;

    private JoinDialog joinDialog;
    private String serverIP = Const.IP;
    private String nickname = Const.Nickname;
    private Client client;

    public ChatFrame(){
        setTitle("Untitled Chat [0]");
        setSize(800, 600); setMinimumSize(new Dimension(400, 260));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        // App icon
        ImageIcon img = new ImageIcon("icon.png");
        setIconImage(img.getImage());
        // Gnome title fix :/
        try {
            Toolkit xToolkit = Toolkit.getDefaultToolkit();
            java.lang.reflect.Field awtAppClassNameField =
                    xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit, "[Untitled Chat]");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Generate UI
        textPane = new JTextPane();
        textPane.setText("Session started");
        textPane.setEditable(false);
        textPane.setBackground(Color.DARK_GRAY);
        textPane.setForeground(Color.WHITE);
        scrollTextPane = new JScrollPane(textPane);
        scrollTextPane.setMinimumSize(new Dimension(400, 200));

        textArea = new JTextArea();
        scrollTextArea = new JScrollPane(textArea);
        scrollTextArea.setMinimumSize(new Dimension(400, 60));

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTextPane, scrollTextArea);
        splitPane.setResizeWeight(1.0);

        // Chat log init
        chatLog = textPane.getStyledDocument();
        aSet = new SimpleAttributeSet();
        StyleConstants.setForeground(aSet, Color.BLACK);
        StyleConstants.setBackground(aSet, Color.WHITE);

        // Set login dialog
        joinDialog = new JoinDialog(this, serverIP, nickname);

        // Init
        client = new Client();

        // Set action listeners
        setActionListeners();

        // Show
        add(splitPane);
        setVisible(true);
        joinDialog.setVisible(true);
    }

    private void setActionListeners(){
        // Exit by Escape
        Object escapeActionKey = new Object();
        splitPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), escapeActionKey);
        splitPane.getActionMap().put(escapeActionKey, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        // Send message by Enter
        Object sendActionKey = new Object();
        textArea.getInputMap().put(
                KeyStroke.getKeyStroke("ENTER"),
                sendActionKey);
        textArea.getActionMap().put(sendActionKey, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(!textArea.getText().isEmpty()){
                sendMessage(textArea.getText());
                textArea.setText(null);}
            }
        });
        // Add new line by Shift+Enter
        Object newLineActionKey = new Object();
        textArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.event.InputEvent.SHIFT_DOWN_MASK),
                newLineActionKey);
        textArea.getActionMap().put(newLineActionKey, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                textArea.append("\n");
            }
        });

        // Login action
        joinDialog.setLoginListener(new LoginListener() {
            @Override
            public void authorized(String serverIP, String nickname) {
                // close old session
                client.close();
                // set data
                ChatFrame.this.nickname = nickname;
                ChatFrame.this.serverIP = serverIP;
                // login
                client.setNickname(nickname);
                if(!client.connect(serverIP)){
                    messageListener.messageReceived(MessageListener.ERROR, "Connecting failed... ");
                } else {
                    messageListener.messageReceived(MessageListener.INFO, "Connected. ");
                }
            }
        });

        // Reconnect
        Object reconnectActionKey = new Object();
        textArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("F1"),
                reconnectActionKey);
        textArea.getActionMap().put(reconnectActionKey, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                joinDialog.setVisible(true);
            }
        });

        // Chat messages
        messageListener = new MessageListener() {
            @Override
            public void messageReceived(int type, String message) {
                switch(type) {
                    case MESSAGE:
                        String[] data = message.split("\\s*:\\s*");
                        if(data.length > 1){
                            if(data[0].equals("Server"))
                                printMessage("\n [ Server ] ", Color.WHITE, Color.DARK_GRAY);
                            else if(data[0].equals(nickname))
                                printMessage("\n [ " + data[0] + " ] ", Color.ORANGE, Color.DARK_GRAY);
                            else
                                printMessage("\n [ " + data[0] + " ] ", Color.MAGENTA, Color.DARK_GRAY);
                            printMessage(" " + data[1], Color.WHITE, Color.DARK_GRAY);
                        } else {
                            printMessage("\n" + message, Color.WHITE, Color.DARK_GRAY);
                        }
                        break;
                    case ERROR:
                        printMessage("\n [ Chat ] ", Color.RED, Color.BLACK);
                        printMessage(" "+message, Color.BLACK, Color.RED);
                        break;
                    case INFO:
                        printMessage("\n [ Chat ] ", Color.GREEN, Color.BLACK);
                        printMessage(" "+message, Color.BLACK, new Color(99, 145, 21));
                        break;
                }
            }
        };
        client.setMessageListener(messageListener);
    }

    private void printMessage(String message, Color fore, Color back){
        try {
            StyleConstants.setForeground(aSet, fore);
            StyleConstants.setBackground(aSet, back);
            chatLog.insertString(chatLog.getLength(), message, aSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    private void sendMessage(String message){
        //messageListener.messageReceived(MessageListener.MESSAGE, nickname+": "+message);
        client.send(message);
    }

    private void exit(){
        client.send("command:exit");
        client.close();
        System.out.println("All streams were closed correctly.");
        System.exit(0);
    }
}
