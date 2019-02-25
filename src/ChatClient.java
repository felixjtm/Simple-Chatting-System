import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private int port;
    private Socket socket;
    private String hostName;
    private PrintWriter write;
    private BufferedReader serverRead;
    private BufferedReader clientRead;
    private Thread listener;
    private volatile boolean running;
    private String name;

    private ChatClient(int p, String host, String n) {
        port = p;
        hostName = host;
        running = true;
        name = n;
        try {
            System.out.println("Connecting to server " + hostName + " through port " + port + "...");
            socket = new Socket(hostName, port);
            System.out.println("Connected! Setting up communication...");

            write = new PrintWriter(socket.getOutputStream(), true);

            serverRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            clientRead = new BufferedReader(new InputStreamReader(System.in));

            write.println(name);

            listener = new Thread() {
                public void run() {
                    String inputLine;
                    while (running) {
                        try {
                            inputLine = serverRead.readLine();
                            if (inputLine != null) {
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
            };
            listener.start();

            runClient();

        } catch(Exception e) {
            System.out.println("Error connecting to server: " + e.toString());
        }
    }

    private void runClient() {
        String inputLine;

        while (running) {
            try {

                inputLine = clientRead.readLine();
                if (inputLine != null) {
                    write.println(inputLine);
                    if (inputLine.equals("EXIT")) {
                        disconnect();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error attempting to write to server: " + e.toString());
                running = false;
            }
        }
    }

    private void disconnect() {
        try {
            running = false;
            socket.close();
            System.out.println("Disconnected.");
            System.exit(0);
        } catch(Exception e) {
            System.out.println("Error disconnecting from server: " + e.toString());
        }
    }

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

        Scanner read = new Scanner(System.in);
        String name;
        System.out.print("Enter your name: ");
        name = read.nextLine();

        ChatClient client = new ChatClient(port, address, name);

    }
}
