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


