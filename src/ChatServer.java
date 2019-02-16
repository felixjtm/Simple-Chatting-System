import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class ChatServer {

    private final int port;
    private Socket socket;
    private ServerSocket serverSocket;
    private List<ConnectedClient> clients;
    private boolean running;

    public ChatServer(int p) {
        running = true;
        port = p;
        clients = new LinkedList<>();
        System.out.println("Setting up server...");

        try {
            System.out.println("Server started successfully with port " + port + ".");
            serverSocket = new ServerSocket(port);
            runServer();
        } catch(Exception e) {
            System.out.println("Error setting up server: " + e.toString());
        }

    }

    private void runServer() {
        Scanner read = new Scanner(System.in);

        Thread writeThread = new Thread() {
            public void run() {
                String line;
                while (running) {
                    line = read.nextLine();
                    if (line != null) {
                        if (line.equals("EXIT")) {
                            for (ConnectedClient client : clients) {
                                client.ServerShutdown();
                            }
                        } else {
                            if (!clients.isEmpty()) {
                                clients.get(0).SendToAllClients("Server: " + line);
                            }
                        }
                    }
                }
            }
        };
        writeThread.start();


        ConnectedClient client;
        try {
            while (running){
                System.out.println("Waiting for client...");
                socket = serverSocket.accept();
                System.out.println("Setting up client " + socket);

                client = new ConnectedClient(socket, clients);
                client.start();
                clients.add(client);
            }
        } catch(Exception e){
            System.out.println("Error accepting client: " + e.toString());
        }

    }


    public static void main(String[] args) {
        int port = 14001;

        if (args.length > 1) {
            if (args[0].equals("-csp")) {
                port = Integer.parseInt(args[1]);
            }
        }

        ChatServer server = new ChatServer(port);

    }
}
