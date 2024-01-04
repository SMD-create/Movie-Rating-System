package moviereviewclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class MulticastClient {
    private static final int SERVER_PORT = 8888;
    private static final int MULTICAST_PORT = 7777;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your request: ");
        String request = scanner.nextLine();

        try {
            InetAddress serverAddress = InetAddress.getByName("localhost");
            InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);

            // Create a unicast socket
            DatagramSocket socket = new DatagramSocket();

            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);

            byte[] buffer = new byte[BUFFER_SIZE];

            // Send request to the server
            byte[] requestData = request.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverAddress, SERVER_PORT);
            socket.send(requestPacket);

            // Receive response from the server
            DatagramPacket responsePacket = new DatagramPacket(buffer, BUFFER_SIZE);
            socket.receive(responsePacket);
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("Response from server: " + response);

            // Join the multicast group
            multicastSocket.joinGroup(multicastAddress);

            // Receive multicast messages
            System.out.println("Waiting for multicast messages...");
            while (true) {
                DatagramPacket multicastPacket = new DatagramPacket(buffer, BUFFER_SIZE);
                multicastSocket.receive(multicastPacket);
                String multicastMessage = new String(multicastPacket.getData(), 0, multicastPacket.getLength());
                System.out.println("Multicast message received: " + multicastMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
