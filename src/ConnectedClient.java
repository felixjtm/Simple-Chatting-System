import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ConnectedClient extends Thread {

    private Socket socket;
    private PrintWriter write;
    private BufferedReader read;
    private String receivedLine;
    private static List<ConnectedClient> clients;
    private volatile boolean running;
    private String name;

    public ConnectedClient(Socket s, List<ConnectedClient> c, int id) {
        socket = s;
        clients = c;
        running = true;
        name = "Client " + id;
        System.out.println("New client created!");

        try {
            write = new PrintWriter(socket.getOutputStream(), true);
            read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(Exception e) {
            System.out.println("Error setting up client: " + e.toString());
        }

    }

    @Override
    public void run() {
        write.println("Server: Welcome! Enter EXIT to disconnect from the server.");

        sendToAllOtherClients(name + " has connected to the server.");
        while (running) {
            try {

                receivedLine = read.readLine();
                if (receivedLine != null) {
                    if (receivedLine.equals("EXIT")) {
                        endConnection();
                    } else {
                        sendToAllOtherClients(name + ": " + receivedLine);
                    }
                }

            } catch (Exception e) {
                if (running) {
                    System.out.println(name + " has lost connection to the server.");
                    endConnection();
                    running = false;
                }
            }
        }
    }

    public synchronized void sendToAllOtherClients(String message) {
        for (ConnectedClient client : clients) {
            if (client != this) {
                client.write.println(message);
            }
        }
        System.out.println(message);
    }

    private void endConnection()  {
        clients.remove(this);
        sendToAllOtherClients(name + " has disconnected from the server.");
        running = false;
        try {
            socket.close();
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public synchronized void serverShutdown() {
        try {
            running = false;
            write.println("EXIT");
            socket.close();
            System.out.println(name + " has disconnected from the server.");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
