
package reviewclient4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ReviewClient4 extends JFrame {
    private static final int SERVER_PORT = 8888;
    private static final int BUFFER_SIZE = 2048;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int CLIENT_PORT = 9999;

    private DatagramSocket socket;
    private JTextArea logTextArea;
    private JTextField commandTextField;

    public ReviewClient4() {
        setTitle("Review Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());

        commandTextField = new JTextField();
        inputPanel.add(commandTextField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCommand();
            }
        });
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void sendCommand() {
        String command = commandTextField.getText();

        try {
            byte[] requestData = command.getBytes();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length,
                    serverAddress, SERVER_PORT);
            socket.send(requestPacket);

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket responsePacket = new DatagramPacket(buffer, BUFFER_SIZE);
            socket.receive(responsePacket);

            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            log(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        logTextArea.append(message + "\n");
    }

    public void startClient() {
        try {
            socket = new DatagramSocket(CLIENT_PORT);
            log("Client started on port " + CLIENT_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ReviewClient4 client = new ReviewClient4();
                client.setVisible(true);
                client.startClient();
            }
        });
    }
}


