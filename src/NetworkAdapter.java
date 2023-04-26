import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.Line;

/**
 * An abstraction of a TCP/IP socket, allowing two players to communicate
 * and play Omok games through the socket. It is assumed that a socket
 * connection has already been established between the peers.
 * <p>
 * This class handles different types of messages, each consisting of
 * a header and a body. The header identifies the message type and ends
 * with a ":", while the body holds the content of the message.
 * In case the body contains multiple elements, they are separated
 * by a ",". The five different message types are:
 * <ul>
 *   <li><strong>play:</strong> a request for a new play (game)</li>
 *   <li><strong>play_ack: m, n</strong> an acknowledgement of a play
 *       request, where m is the response (1 for accepted, 0 for rejected)
 *       and n is the turn (1 for the play requester plays first,
 *       0 for the play receiver plays first).</li>
 *   <li><strong>move: x, y</strong> a request to place a stone at the
 *       specified location (x and y are 0-based column and row indices,
 *       respectively).</li>
 *   <li><strong>move_ack: x, y</strong> an acknowledgement of
 *       a move message.</li>
 *   <li><strong>quit:</strong> a request to quit the play.</li>
 * </ul>
 *
 * The communication protocol is straightforward. The client requests
 * a new game from the server, and if the request is accepted, the server
 * determines the turn. The play then proceeds by sending and receiving
 * moves and move acknowledgements until the game ends or one of the
 * players quits.
 *
 * <pre>
 * 1. Client has the first turn.
 *  Client        Server
 *    |------------&gt;| play: - request for a new play
 *    |&lt;------------| play_ack:1,1 - ack the play request
 *    |------------&gt;| move:3,4 - client move
 *    |&lt;------------| move_ack:3,4 - server ack
 *    |&lt;------------| move:2,3 - server move
 *    |------------&gt;| move_ack:2,3 - client ack
 *    ...
 * </pre>
 *
 * <pre>
 *  2. Server has the first turn.
 *  Client        Server
 *    |------------&gt;| play: - request for a new play
 *    |&lt;------------| play_ack:1,0 - ack the play request
 *    |&lt;------------| move:3,3 - server move
 *    |------------&gt;| move_ack:3,3 - client ack
 *    |------------&gt;| move:2,3 - client move
 *    |&lt;------------| move_ack:2,3 - server ack
 *    ...
 * </pre>
 * 
 * <pre>
 *  3. A play request is declined.
 *  Client        Server
 *    |------------&gt;| play: - request for a new play
 *    |&lt;------------| play_ack:0,0 - reject the play request
 * </pre> 
 *
 * This class provides a simple way to send and receive messages
 * in the Omok game protocol. To receive messages from a peer,
 * a {@link MessageListener} must be registered by calling the
 * {@link #setMessageListener(MessageListener)} method and passing
 * an instance of a class that implements
 * the {@link MessageListener} interface.
 * Then, the {@link #}receiveMessagesAsync()} method should be called
 * to start a new thread that will receive messages asynchronously.
 *
 * <pre>
 *  Socket socket = ...;
 *  NetworkAdapter network = new NetworkAdapter(socket);
 *  network.setMessageListener(new NetworkAdapter.MessageListener() {
 *      public void messageReceived(NetworkAdapter.MessageType type, int x, int y) {
 *        switch (type) {
 *          case PLAY: ...
 *          case PLAY_ACK: ...
 *          case MOVE: ...
 *          case MOVE_ACK: ...
 *          ...
 *        }
 *      }
 *    });
 *
 *  // receive messages asynchronously
 *  network.receiveMessagesAsync();
 * </pre>
 * <p>
 * To send messages to a peer, use the writeXXX methods.
 * These methods, such as writePlay() or writeMove(x, y),
 * run asynchronously and send messages in the specified format of the
 * Omok game protocol. Finally, when the communication is finished,
 * call the close() method to close the socket.
 *
 * <pre>
 *  network.writePlay();
 *  network.writeMove(1,2);
 *  ...
 *  network.close();
 * </pre>
 *
 * @author cheon
 * @see MessageType
 * @see MessageListener
 */

public class NetworkAdapter {

    /** Different type of game messages. */
    public enum MessageType { 
        
        /** Exit the game. The format of this message is "quit:". */
        QUIT ("quit:"), 
        
        /** Request a new game. The format of this message is "play:". */
        PLAY ("play:"), 
        
        /** 
         * Confirmation of a play request. The format of this message is
         * "play_ack: m, n", where m (response) and n (turn) are either
         * 0 or 1. If m is 1, the request is approved; if it's 0, it's rejected.
         * If n is 1, the client plays first; if it's 0, the server plays first.
         */
        PLAY_ACK ("play_ack:"), 
        
        /** 
         * Placement of a stone. The format of this message is "move: x, y",
         * with x and y being the 0-based column and row indices of
         * the position where the stone is placed.
         */
        MOVE ("move:"), 
        
        /** 
         * Confirmation of a player's move. The format of this message is
         * "move_ack: x, y", where x and y are the 0-based column
         * and row indices.
         */
        MOVE_ACK ("move_ack:"), 
        
        /** Disconnection. A notification to indicate the closing
         *  of the socket. */
        CLOSE (null), 
        
        /** Unknown message received. */
        UNKNOWN (null);
        
        /** Message header. */
        public final String header;
        
        MessageType(String header) {
            this.header = header;
        }

    }

    /** Callback function to trigger upon receipt of a message. */
    public interface MessageListener {

        /** 
         * A method to be activated upon receipt of a message.
         * The message type, along with any optional parameters
         * such as x and y, are passed as arguments to the function.
         */
        void messageReceived(MessageType type, int x, int y);
    }

    /** Listener to be called upon receipt of a message. */
    private MessageListener listener;
    
    /** Asynchronous message writer. */
    private final MessageWriter messageWriter;
    
    /** A reader connected to a peer for the purpose of reading
     * messages from it. */
    private BufferedReader in;
    
    /** A writer connected to a peer for the purpose of writing messages to it. */
    private PrintWriter out;
    
    /** If not null, log all messages sent and received. */
    private final PrintStream logger;

    /** Create a new network adapter for reading and writing messages
     * to and from the specified socket. */
    public NetworkAdapter(Socket socket) {
        this(socket, null);
    }
    
    /** 
     * Create a new network adapter. Messages are to be read from and 
     * written to the given socket. All incoming and outgoing 
     * messages will be logged on the given logger. */
    public NetworkAdapter(Socket socket, PrintStream logger) {
        this.logger = logger;
        messageWriter = new MessageWriter();
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            // ignore
        }
    }
    
    /** Close the IO streams of this adapter. Note that the socket
     * to which the streams are attached is not closed by
     * this method. */
    public void close() {
        try {
            // close "out" first to break the circular dependency
            // between peers.
            out.close();  
            in.close();
            messageWriter.stop();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Register the given messageListener to be notified when a message
     * is received.
     *
     * @see MessageListener
     * @see #receiveMessages()
     * @see #receiveMessagesAsync()
     */
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    /**
     * Start accepting messages from this network adapter and
     * notifying them to the registered listener. This method blocks
     * the caller. To receive messages synchronously, use the
     * {@link #receiveMessagesAsync()} method that creates a new
     * background thread.
     *
     * @see #setMessageListener(MessageListener)
     * @see #receiveMessagesAsync()
     */
    public void receiveMessages() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (logger != null) {
                    logger.format(" < %s\n", line);
                    System.out.println("< "+line);
                }
                parseMessage(line);
            }
        } catch (IOException e) {
            // ignore
        }
        notifyMessage(MessageType.CLOSE);
    }
    
    /**
     * Start accepting messages asynchronously from this network
     * adapter and notifying them to the registered listener.
     * This method doesn't block the caller. Instead, a new
     * background thread is created to read incoming messages.
     * To receive messages synchronously, use the
     * {@link #receiveMessages()} method.
     *
     * @see #setMessageListener(MessageListener)
     * @see #receiveMessages()
     */
    public void receiveMessagesAsync() {
        new Thread(this::receiveMessages).start();
    }

    /** Parse the given message and notify to the registered listener. */
    private void parseMessage(String msg) {
        if (msg.startsWith(MessageType.QUIT.header)) {
                notifyMessage(MessageType.QUIT);
        } else if (msg.startsWith(MessageType.PLAY_ACK.header)) {
            parsePlayAckMessage(msgBody(msg));
        } else if (msg.startsWith(MessageType.PLAY.header)) {
            notifyMessage(MessageType.PLAY);
        } else if (msg.startsWith(MessageType.MOVE_ACK.header)) {
            parseMoveMessage(MessageType.MOVE_ACK, msgBody(msg));
        } else if (msg.startsWith(MessageType.MOVE.header)){
            parseMoveMessage(MessageType.MOVE, msgBody(msg));
        } else {
            notifyMessage(MessageType.UNKNOWN);
        }
    }

    /** Parse and return the body of the given message. */
    private String msgBody(String msg) {
        int i = msg.indexOf(':');
        if (i > -1) {
            msg = msg.substring(i + 1);
        }
        return msg;
    }

    /** Parse and notify the given play_ack message body. */
    private void parsePlayAckMessage(String msgBody) {
        String[] m = msgBody.split(",");
        int response = parseInt(m[0].trim()) == 0 ? 0 : 1;
        int turn = 0;
        if (response == 1) {
            turn = parseInt(m[1].trim()) == 0 ? 0 : 1;
        }
        notifyMessage(MessageType.PLAY_ACK, response, turn);
    }

    /**
     * Attempt to parse the input string as an integer.
     * Returns -1 if the string is not a valid representation of an integer.
     */
    private int parseInt(String txt) {
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /** Parse and notify the given move or move_ack message. */
    private void parseMoveMessage(MessageType type, String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 2) {
            int x = parseInt(parts[0].trim());
            int y = parseInt(parts[1].trim());
            notifyMessage(type, x, y);
        } else {
            notifyMessage(MessageType.UNKNOWN);
        }
    }

    /** Send the given message asynchronously. */
    public void writeMsg(String msg) {
        messageWriter.write(msg);
    }
    
    /**
     * Send a play message asynchronously.
     *
     * @see #writePlayAck(boolean, boolean)
     */
    public void writePlay() {
        writeMsg(MessageType.PLAY.header);
    }

    /**
     * Send a play_ack message asynchronously. The response parameter
     * indicates whether the play request was accepted or not.
     *
     * @see #writePlay()
     */
    public void writePlayAck(boolean response, boolean turn) {
        writeMsg(MessageType.PLAY_ACK.header
                 + toInt(response) + "," + toInt(turn));
    }

    /**
     * Convert the given boolean value to its integer representation.
     * Returns 1 for `true` and 0 for `false`.
     */
    private int toInt(boolean flag) {
        return flag ? 1: 0;
    }


    /**
     * Asynchronously send a move message. The message includes
     * the coordinates of the place where a stone is placed, specified
     * as 0-based column and row indices.
     *
     * @see #writeMoveAck(int, int)
     */
    public void writeMove(int x, int y) {
        writeMsg(MessageType.MOVE.header + x + "," + y);
    }

    /**
     * Send a move_ack message asynchronously. The message includes
     * the coordinates of the place where a stone is placed, specified
     * as 0-based column and row indices.
     *
     * @see #writeMove(int, int)
     */
    public void writeMoveAck(int x, int y) {
        writeMsg(MessageType.MOVE_ACK.header + x + "," + y);
    }
    
    /** Send a quit (gg) message asynchronously. */
    public void writeQuit() {
        writeMsg(MessageType.QUIT.header);
    }

    /**
     * Notify the listener of the receipt of a specific message type.
     */
    private void notifyMessage(MessageType type) {
        listener.messageReceived(type, 0, 0);
    }

    /** Notify the listener of the receipt of the given message. */
    private void notifyMessage(MessageType type, int x, int y) {
        listener.messageReceived(type, x, y);
    }

    /**
     * A class for writing messages asynchronously.
     * This class employs a single background thread to write messages
     * in a first-in, first-out manner. To halt the background thread,
     * call the @{link #stop()} method.
     */
    private class MessageWriter {
        
        /** Background thread to write messages asynchronously. */
        private Thread writerThread;
        
        /** Queue containing messages to be written asynchronously. */
        private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        /**
         * Writes the specified message asynchronously, if required,
         * by creating a new thread.
         */
        public void write(final String msg) {
            if (writerThread == null) {
                writerThread = new Thread(() -> {
                    while (true) {
                        try {
                            String m = messages.take();
                            out.println(m);
                            out.flush();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                });
                writerThread.start();
            }
            synchronized (messages) {
                try {
                    messages.put(msg);
                    if (logger != null) {
                        logger.format(" > %s\n", msg);
                        System.out.println("> "+msg);

                    }
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        
        /** Halt this message writer. */
        public void stop() {
            if (writerThread != null) {
                writerThread.interrupt();
            }
        }
    }
}
