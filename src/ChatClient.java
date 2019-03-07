import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Defines the client for a client-server chat system.
 */
public class ChatClient {

    //Defines variables for use in connecting to a server
    private int port;
    private Socket socket;
    private String hostName;
    private PrintWriter write;
    private BufferedReader serverRead;

    //Defines variables for use in multithreading
    private Thread listener;
    private static volatile boolean running;

    private BufferedReader clientRead;

    /**
     * Constructor. Establishes the connection to the server and sets up input and output
     *
     * @param p
     *  The port used to connect to the server
     * @param host
     *  The hostname used to connect to the server
     */
    private ChatClient(int p, String host) {
        port = p;
        hostName = host;
        running = true;

        clientRead = new BufferedReader(new InputStreamReader(System.in));
        setupServerConnection();
    }

    /**
     * Establishes connection to a server using details provided
     */
    private void setupServerConnection() {
        try {
            System.out.println("Connecting to server " + hostName + " through port " + port + "...");
            socket = new Socket(hostName, port); //Connects to the server
            System.out.println("Connected! Setting up communication...");

            //Used to write to the server
            write = new PrintWriter(socket.getOutputStream(), true);

            //Used to read from the server
            serverRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            listener = new Thread() {
                /**
                 * Thread for listening to messages from the server
                 */
                public void run() {
                    runListener();
                }
            };
            listener.start();

            runClient();

        } catch(Exception e) {
            //The server could not be found
            System.out.println("Error connecting to server: " + e.toString());
        }
    }

    /**
     * Listens to messages from the server
     */
    private void runListener() {
        String inputLine;
        while (running) {
            try {
                //Gets input from the server, if there is nothing then the value is null
                inputLine = serverRead.readLine();
                if (inputLine != null) {
                    //If the server has shut down then disconnect, otherwise output the message
                    if (inputLine.equals("EXIT")) {
                        System.out.println("The server has shut down.");
                        disconnect();
                    } else {
                        System.out.println(inputLine);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error attempting to listen to server: " + e.toString());
                running = false;
            }
        }
    }

    /**
     * Sends messages from the client to the server
     */
    private void runClient() {
        String inputLine;

        while (running) {
            try {
                //Gets the next line and removes any leading or trailing spaces
                inputLine = clientRead.readLine().trim();
                //If the message is not null then send it to the server
                if (inputLine != null && !inputLine.equals("")) {
                    write.println(inputLine);
                    if (inputLine.equals("EXIT")) {
                        disconnect();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error attempting to write to server: " + e.toString());
            }
        }
    }

    /**
     * Disconnects from the server and closes the program
     */
    private void disconnect() {
        try {
            running = false;
            socket.close(); //Closes the connection with the server
            System.out.println("Disconnected.");
            System.exit(0); //Closes the program
        } catch(Exception e) {
            System.out.println("Error disconnecting from server: " + e.toString());
        }
    }

    /**
     *Begins the program and gets passes the port and host address to the program
     *
     * @param args
     *  The arguments passed from the command line
     */
    public static void main(String[] args) {
        int port = 14001;
        String address = "localhost";

        if (args.length > 1) {
            if (args[0].equals("-ccp")) {
                port = Integer.parseInt(args[1]);
            } else if(args[0].equals("-cca")) {
                address = args[1];
            }
            if (args.length > 3) {
                if (args[2].equals("-ccp")) {
                    port = Integer.parseInt(args[3]);
                } else if(args[2].equals("-cca")) {
                    address = args[3];
                }
            }
        }

        ChatClient client = new ChatClient(port, address);

    }
}
