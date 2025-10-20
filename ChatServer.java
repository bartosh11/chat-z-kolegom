import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Serwer czatu uruchomiony na porcie " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcastClientList() {
        String users = String.join(",", clients.keySet());
        for (ClientHandler ch : clients.values()) {
            ch.send("USERS|" + users);
        }
    }

    static void broadcast(String message) {
        for (ClientHandler ch : clients.values()) {
            ch.send(message);
        }
    }

    static void sendPrivate(String target, String message) {
        ClientHandler ch = clients.get(target);
        if (ch != null) {
            ch.send(message);
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private String username;
        private PrintWriter out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                out = new PrintWriter(socket.getOutputStream(), true);
                username = in.readLine();
                if (username == null || username.isEmpty()) return;

                clients.put(username, this);
                System.out.println("[SERVER] " + username + " dołączył.");
                broadcastClientList();
                broadcast("SERVER||ALL|" + username + " dołączył do czatu.");

                String line;
                while ((line = in.readLine()) != null) {
                    String[] parts = line.split("\\|", 4);
                    if (parts.length < 4) continue;

                    String type = parts[0];
                    String sender = parts[1];
                    String target = parts[2];
                    String msg = parts[3];

                    if ("MESSAGE".equals(type)) {
                        if ("ALL".equals(target)) {
                            System.out.println("[ALL] " + sender + ": " + msg);
                            broadcast("MESSAGE|" + sender + "|ALL|" + msg);
                        } else {
                            System.out.println("[PRIV] " + sender + " → " + target + ": " + msg);
                            sendPrivate(target, "PRIVATE|" + sender + "|" + target + "|" + msg);
                            sendPrivate(sender, "PRIVATE|" + sender + "|" + target + "|" + msg);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("[ERROR] " + username + " rozłączony.");
            } finally {
                if (username != null) {
                    clients.remove(username);
                    broadcast("SERVER||ALL|" + username + " wyszedł z czatu.");
                    broadcastClientList();
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        void send(String msg) {
            if (out != null) out.println(msg);
        }
    }
}
