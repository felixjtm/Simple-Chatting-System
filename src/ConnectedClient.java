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
    private boolean running;
    private String name;

    public ConnectedClient(Socket s, List<ConnectedClient> c) {
        socket = s;
        clients = c;
        running = true;
        name = socket.toString();
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
        try {
            String nameLine = read.readLine();
            while (nameLine == null) {
                nameLine = read.readLine();
            }
            name = nameLine;
        } catch(Exception e) {
            System.out.println("Error receiving name: " + e.toString());
        }

        SendToAllClients(name + " has connected to the server.");
        while (running) {
            try {

                receivedLine = read.readLine();
                if (receivedLine != null) {
                    if (receivedLine.equals("EXIT")) {
                        EndConnection();
                    } else {
                        SendToAllClients(name + ": " + receivedLine);
                    }
                }

            } catch (Exception e) {
                System.out.println(name + " has lost connection to the server.");
                EndConnection();
                running = false;
            }
        }
    }

    public synchronized void SendToAllClients(String message) {
        for (ConnectedClient client : clients) {
            client.write.println(message);
        }
        System.out.println(message);
    }

    private void EndConnection()  {
        clients.remove(this);
        SendToAllClients(name + " has disconnected from the server.");
        running = false;
        try {
            socket.close();
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public void ServerShutdown() {
        try {
            running = false;
            write.println("EXIT");
            socket.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
