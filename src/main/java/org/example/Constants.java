package org.example;

import java.time.format.DateTimeFormatter;

public final class Constants {

    public static boolean checkMention(MESSAGE_FLAGS flag) {
        return flag == MESSAGE_FLAGS.MESSAGE || flag == MESSAGE_FLAGS.SERVER_MESSAGE;
    }

    /** Formatter for the message timestamps */
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");

    /** Do not instantiate */
    private Constants() {}

    private static final int NONE = 0;
    /** Message type should be shown on the client's table */
    private static final int SHOW = 1;
    /** Message type should be logged by the server */
    private static final int LOG = 1 << 1;
    /** Requires special handling by the server */
    private static final int SPECIAL_HANDLING = 1 << 2;

    /** An enum representing the possible message types that can be sent */
    public enum MESSAGE_FLAGS {
        SERVER_MESSAGE(SHOW | LOG),
        /** Denotes a message sent by a client */
        MESSAGE(SHOW | LOG),
        /** Denotes a message showing a client has successfully joined */
        JOIN(SHOW | LOG),
        /** Denotes a message showing a client has disconnected */
        LEAVE(SHOW | LOG),
        /**
         * Requests the accompanying username be checked to guarantee that it is unique
         *
         * @implNote Should only be sent once a valid username is entered
         */
        USERNAME_CHECK(SPECIAL_HANDLING),
        /**
         * Username supplied by {@link Constants.MESSAGE_FLAGS#USERNAME_CHECK} is
         * already being used by another user
         */
        USERNAME_CHECK_FAIL,
        /**
         * Username supplied by {@link Constants.MESSAGE_FLAGS#USERNAME_CHECK} is
         * not being used by another user
         */
        USERNAME_CHECK_SUCCEED,
        /**
         * Denotes a message sent by the server to all clients when it is shut
         * down, forcing them to leave
         */
        FORCE_LEAVE(SHOW);

        /** Whether the message type is shown in the client's table */
        final boolean show;
        /** Whether the message type is put into the server's log */
        final boolean log;
        /** Whether the message has to be handled in a non-standard manner */
        final boolean special;

        MESSAGE_FLAGS() {
            this(NONE);
        }
        MESSAGE_FLAGS(int flag) {
            show = (flag & SHOW) != 0;
            log = (flag & LOG) != 0;
            special = (flag & SPECIAL_HANDLING) != 0;
        }
    }
}
