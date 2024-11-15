package org.example;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

class ClientHandler {
    protected final Socket SOCKET;
    protected String username;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;

    /**
     * Create a {@code ClientHandler} relating to the specified socket
     * @param sock The socket this {@code ClientHandler} is dealing without
     */
    public ClientHandler(Socket sock) {
        SOCKET = sock;
        try {
            in = new ObjectInputStream(sock.getInputStream());
            out = new ObjectOutputStream(sock.getOutputStream());
            System.out.println(UUID.randomUUID());
        } catch(EOFException ignored) {
            System.out.println("fuck");
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

class SendMessage extends Thread {
    /** The message to be sent */
    final Message MESSAGE;

    /**
     * Create the {@code SendMessage} thread with the supplied message
     * @param msg The message to send to connected clients
     */
    public SendMessage(Message msg) {
        MESSAGE = msg;
    }

    @Override
    public void run() {
        synchronized (ServerBackend.clients) {
            for (ClientHandler handler : ServerBackend.clients) {
                if (!handler.SOCKET.isClosed()) {
                    try {
                        handler.out.writeObject(MESSAGE);
                        handler.out.flush();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }
}

class ClientThread extends Thread {
    /** The client this thread deals with */
    private final ClientHandler CLIENT;
    /**
     * Creates a thread to read from the specified {@code ClientHandler}'s input stream
     * @param client The {@code ClientHandler} that this thread is related to
     */
    public ClientThread(ClientHandler client) {
        CLIENT = client;
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        Message buffer;
        while(true) {
            try {
                if((buffer = (Message) CLIENT.in.readObject()) != null) {
                    if(buffer.FLAG.log) ServerBackend.connection.addMessage(buffer);

                    if(!buffer.FLAG.special){
                        new SendMessage(buffer).start();
                        if(buffer.FLAG == Constants.MESSAGE_FLAGS.LEAVE) {
                            System.out.println(buffer.USERNAME + " has left");
                            ServerBackend.clients.remove(CLIENT);
                            if(CLIENT.username != null){
                                ServerBackend.usernames.remove(CLIENT.username);
                            }
                            return;
                        }
                    } else {
                        if (buffer.FLAG == Constants.MESSAGE_FLAGS.USERNAME_CHECK) {
                            checkUsername(buffer.USERNAME);
                        }
                        if(buffer.FLAG == Constants.MESSAGE_FLAGS.BACKLOG_REQUEST) {
                            System.out.println("started");
                            List<Message> messages = ServerBackend.connection.backlog();
                            for(Message m : messages) {
                                CLIENT.out.writeObject(m);
                            }
                            System.out.println("done");
                            CLIENT.out.writeObject(new Message(Constants.MESSAGE_FLAGS.BACKLOG_COMPLETE));
                        }
                    }
                }
            } catch(EOFException | SocketException e) {
                break;
            } catch(IOException | ClassNotFoundException exc) {
                exc.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void checkUsername(String name) throws IOException {
        if(ServerBackend.usernames.contains(name)) {
            CLIENT.out.writeObject(new Message(Constants.MESSAGE_FLAGS.USERNAME_CHECK_FAIL));
            CLIENT.out.flush();
        } else {
            CLIENT.username = name;
            ServerBackend.usernames.add(name);
            CLIENT.out.writeObject(new Message(Constants.MESSAGE_FLAGS.USERNAME_CHECK_SUCCEED));
        }
    }
}

class ServerBackend extends Thread {
    static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    public static ServerSocket SOCK;
    static HashSet<String> usernames = new HashSet<>();

    static DBConnection connection;

    public ServerBackend(String[] args) {
        try {
            if(args.length == 1) {
                SOCK = new ServerSocket(Integer.parseInt(args[0]));
                this.setDaemon(true);
                this.start();
            } else {
                System.err.println("Usage: <port>");
                System.exit(1);
            }
            connection = new DBConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                Socket client = SOCK.accept();
                System.out.println("got one");
                ClientHandler c = new ClientHandler(client);
                clients.add(c);
                new ClientThread(c);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}

public class ServerMain {
    public static void main(String[] args) {
        new ServerBackend(args);

        while(true);
    }
}