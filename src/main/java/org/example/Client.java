package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Client {
    private final Socket SOCKET;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private String username;
    private static CustomTableModel model;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");

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
            errorReport("No server on port " + port + " at host '" + hostname + "'.", "No Server");
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
        chatWindow();
        System.exit(0);
    }

    void nameQuery() {
        AtomicBoolean finished = new AtomicBoolean(false);
        JFrame frame = new JFrame("Username");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(200, 100);
        frame.setResizable(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(username == null) {
                    try {
                        send(new Message(Constants.MESSAGE_FLAGS.LEAVE, null, "[[Unnamed User]]", ZonedDateTime.now()));
                    } catch (IOException ignore) {}
                    System.exit(0);
                }
            }
        });

        JTextField input = new JTextField(12);

        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Pattern.matches("[a-z-A-Z0-9_\\-]{2,12}", input.getText())) {
                    try {
                        send(new Message(Constants.MESSAGE_FLAGS.USERNAME_CHECK, null, input.getText(), null));
                        Message response;
                        while((response = (Message) in.readObject()) == null);
                        if(response.FLAG == Constants.MESSAGE_FLAGS.USERNAME_CHECK_FAIL) {
                            errorReport("Username '" + input.getText() + "' is already in use.", "Username in use");
                        } else {
                            username = input.getText();
                            frame.setVisible(false);
                            frame.dispose();
                            finished.set(true);
                        }
                    } catch (IOException ex) {
                        errorReport("Connection to server terminated.", "No Connection");
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    errorReport("Username '" + input.getText() + "' is an invalid username.", "Malformed Input");
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

    private static class CustomTableModel extends DefaultTableModel {
        public CustomTableModel(String... columns) {
            super(null, columns);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static class CustomColumnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(column != 1) return l;
            if(table.getModel().getValueAt(row, 2) == Constants.MESSAGE_FLAGS.LEAVE) {
                l.setForeground(Color.RED);
            } else if(table.getModel().getValueAt(row, 2) == Constants.MESSAGE_FLAGS.JOIN) {
                l.setForeground(Color.GREEN);
            }
            int fontHeight = this.getFontMetrics(this.getFont()).getHeight() / 3;
            int textLength = this.getText().length();
            int lines = textLength / table.getColumnCount() +1;//+1, cause we need at least 1 row.
            int height = fontHeight * lines;
            table.setRowHeight(row, height);

            return l;
        }
    }

    void chatWindow() {
        JFrame frame = new JFrame("Chatter: " + username);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    send(new Message(Constants.MESSAGE_FLAGS.LEAVE, null, username, ZonedDateTime.now()));
                } catch (IOException ignore) {}
            }
        });

        frame.setSize(500, 500);
        model = new CustomTableModel("Time", "Message", "Flags");
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(1).setCellRenderer(new CustomColumnRenderer());
        table.removeColumn(table.getColumnModel().getColumn(2));
        frame.getContentPane().add(BorderLayout.CENTER, table);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        model.addRow(new Object[]{"< " + formatter.format(ZonedDateTime.now()) + " >", "test", Constants.MESSAGE_FLAGS.JOIN});

        while(true);
    }

    void send(Message m) throws IOException {
        out.writeObject(m);
        out.flush();
    }

    static void errorReport(String text, String title) {
        JOptionPane.showMessageDialog(null, text, title, JOptionPane.ERROR_MESSAGE);
    }
}
