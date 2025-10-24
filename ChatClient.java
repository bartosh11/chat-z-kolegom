import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private PrintWriter out;
    private String username;

    public ChatClient(String host, int port) {
        setTitle("Java Czat — Klient");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Font font = new Font("Consolas", Font.PLAIN, 14);
        Color bgColor = new Color(30, 30, 30);
        Color fgColor = new Color(230, 230, 230);
        Color inputBg = new Color(50, 50, 50);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(font);
        chatArea.setBackground(bgColor);
        chatArea.setForeground(fgColor);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(chatScroll, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.setFont(font);
        inputField.setBackground(inputBg);
        inputField.setForeground(fgColor);
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(inputField, BorderLayout.SOUTH);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFont(font);
        userList.setBackground(new Color(40, 40, 40));
        userList.setForeground(fgColor);
        userList.setSelectionBackground(new Color(70, 130, 180));
        userList.setSelectionForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel("Użytkownicy");
        userLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        userPanel.setBackground(new Color(25, 25, 25));
        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        userPanel.setPreferredSize(new Dimension(150, 0));
        add(userPanel, BorderLayout.EAST);

        try {
            Socket socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            username = JOptionPane.showInputDialog(this, "Podaj login:");
            out.println(username);

            new Thread(() -> listen(socket)).start();

            inputField.addActionListener(e -> {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    String target = userList.getSelectedValue();
                    if (target == null || target.equals("ALL")) {
                        out.println("MESSAGE|" + username + "|ALL|" + text);
                    } else {
                        out.println("MESSAGE|" + username + "|" + target + "|" + text);
                    }
                    inputField.setText("");
                }
            });

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Nie można połączyć z serwerem: " + e.getMessage());
            System.exit(1);
        }
    }

    private void listen(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                final String message = line;
                SwingUtilities.invokeLater(() -> handleMessage(message));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> chatArea.append("Połączenie utracone.\n"));
        }
    }

    private void handleMessage(String line) {
        if (line.startsWith("USERS|")) {
            String[] users = line.substring(6).split(",");
            userListModel.clear();
            userListModel.addElement("ALL");
            Arrays.stream(users).forEach(userListModel::addElement);
        } else if (line.startsWith("MESSAGE|")) {
            String[] parts = line.split("\\|", 4);
            appendChat("[ALL] " + parts[1] + ": " + parts[3]);
        } else if (line.startsWith("PRIVATE|")) {
            String[] parts = line.split("\\|", 4);
            appendChat("[PRIV] " + parts[1] + " → " + parts[2] + ": " + parts[3]);
        } else if (line.startsWith("SERVER|")) {
            String[] parts = line.split("\\|", 4);
            appendChat("[SERVER] " + parts[3]);
        } else {
            appendChat(line);
        }
    }

    private void appendChat(String text) {
        chatArea.append(text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient("localhost", 5000).setVisible(true));
    }
}




