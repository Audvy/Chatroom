package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Client {
    private final Socket SOCKET;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private String username;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: Client [hostname] port");
            System.exit(1);
        }
        try {
            if (args.length > 1) {
                new Client(new Socket(args[0], Integer.parseInt(args[1])));
            } else {
                new Client(new Socket((String) null, Integer.parseInt(args[0])));
            }
        } catch (IOException e) {
            String hostname = (args.length == 1) ? "localhost" : args[0];
            String port = (args.length == 1) ? args[0] : args[1];
            errorReport("No server on port " + port + " at host '" + hostname + "'", "No Server");
            System.exit(1);
        }
    }

    private Client(Socket s) throws IOException {
        System.out.println("Started");
        SOCKET = s;
        System.out.println("Started");
        out = new ObjectOutputStream(SOCKET.getOutputStream());
        in = new ObjectInputStream(SOCKET.getInputStream());

        nameQuery();
        System.exit(0);
    }

    void nameQuery() {
        AtomicBoolean finished = new AtomicBoolean(false);
        JFrame frame = new JFrame("Username");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(200, 100);
        frame.setResizable(false);

        JTextField input = new JTextField(12);

        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Pattern.matches("[a-z-A-Z0-9_\\-]*", input.getText())) {
                    try {
                        send(new Message(Constants.MESSAGE_FLAGS.USERNAME_CHECK, null, input.getText(), null));
                        Message response;
                        while((response = (Message) in.readObject()) == null);
                        if(response.FLAG == Constants.MESSAGE_FLAGS.USERNAME_CHECK_FAIL) {
                            errorReport("Username '" + input.getText() + "' is already in use", "Username in use");
                        } else {
                            username = input.getText();
                            frame.setVisible(false);
                            finished.set(true);
                        }
                    } catch (IOException ex) {
                        errorReport("Connection to server terminated", "No Connection");
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    errorReport("Username '" + input.getText() + "' contains invalid characters", "Malformed Input");
                }
            }
        };

        input.addActionListener(action);

        JButton confirm = new JButton("Confirm");
        confirm.addActionListener(action);

        frame.getContentPane().add(BorderLayout.CENTER, input);
        frame.getContentPane().add(BorderLayout.SOUTH, confirm);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        while(!finished.get());
    }

    void send(Message m) throws IOException {
        out.writeObject(m);
        out.flush();
    }

    static void errorReport(String text, String title) {
        JOptionPane.showMessageDialog(null, text, title, JOptionPane.ERROR_MESSAGE);
    }
}
