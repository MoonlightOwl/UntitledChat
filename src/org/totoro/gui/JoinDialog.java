package org.totoro.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JoinDialog extends JDialog {
    private static final String PATTERN =
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private final JLabel lServer = new JLabel("Server IP");
    private final JLabel lNickname = new JLabel("Nickname");

    private final JTextField fServer = new JTextField(15);
    private final JTextField fNickname = new JTextField(15);

    private final JButton bOk = new JButton("Join");
    private final JButton bCancel = new JButton("Cancel");

    private final JLabel lStatus = new JLabel(" ");

    private LoginListener listener;

    public JoinDialog(JFrame owner, String defServerIP, String defNickname) {
        super(owner, "Join to");

        fServer.setText(defServerIP);
        fNickname.setText(defNickname);

        JPanel p3 = new JPanel(new GridLayout(2, 1));
        p3.add(lServer);
        p3.add(lNickname);

        JPanel p4 = new JPanel(new GridLayout(2, 1));
        p4.add(fServer);
        p4.add(fNickname);

        JPanel p1 = new JPanel();
        p1.add(p3);
        p1.add(p4);

        JPanel p2 = new JPanel();
        p2.add(bOk);
        p2.add(bCancel);

        JPanel p5 = new JPanel(new BorderLayout());
        p5.add(p2, BorderLayout.CENTER);
        p5.add(lStatus, BorderLayout.NORTH);
        lStatus.setForeground(Color.RED);
        lStatus.setHorizontalAlignment(SwingConstants.CENTER);

        setLayout(new BorderLayout());
        add(p1, BorderLayout.CENTER);
        add(p5, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });


        bOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(validate(fServer.getText())){
                    if(!fNickname.getText().isEmpty()) {
                        listener.authorized(fServer.getText(), fNickname.getText());
                        setVisible(false);
                    } else {
                        lStatus.setText("Empty nickname field");
                    }
                } else {
                    lStatus.setText("Invalid IP address");
                }
            }
        });
        bCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    public static boolean validate(final String ip){
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    public void setLoginListener(LoginListener listener){
        this.listener = listener;
    }
}