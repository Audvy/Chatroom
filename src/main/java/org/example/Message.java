package org.example;
import java.io.Serializable;

/**
 * A class used for the messages sent by both the server and clients
 */
public class Message implements Serializable {
    /**
     * The flag denoting the type of message this object represents
     */
    final Constants.MESSAGE_FLAGS FLAG;
    /**
     * The message contained within this object
     * <p>Can be {@code null} for some message types</p>
     */
    final String MESSAGE;
    /**
     * The username of the client that sent the messages
     * <p>Can be {@code null} if either not relevant or sent by the server</p>
     */
    final String USERNAME;
    /**
     * The timestamp of the message
     */
    final long TIMESTAMP;

    /**
     * Creates a {@code Message} with the specified values
     *
     * @param flag The flag representing the message type
     * @param msg The message contained in this {@code Message}
     * @param usr The username of the client that sent this {@code Message}
     * @param time The timestamp of this {@code Message}
     */
    public Message(Constants.MESSAGE_FLAGS flag, String msg, String usr, long time) {
        FLAG = flag;
        MESSAGE = msg;
        USERNAME = usr;
        TIMESTAMP = time;
    }

    /**
     * Creates a {@code Message} with every other field set to {@code null}
     * @param flag The flag representing the message type
     * @param time The timestamp of this {@code Message}
     */

    public Message(Constants.MESSAGE_FLAGS flag, long time) {
        this(flag, null, null, time);
    }

    /**
     * Creates a {@code Message} with every other field set to {@code null}
     * @param flag The flag representing the message type
     */
    public Message(Constants.MESSAGE_FLAGS flag) {
        this(flag, null, null, -1);
    }
}

