
package multicastclient1.java;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class MulticastClient1Java {
    private static final int PORT = 5555;
    private static final String MULTICAST_ADDRESS = "230.0.0.1";

    public static void main(String[] args) {
        MulticastSocket socket = null;
        Scanner scanner = new Scanner(System.in);

        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket = new MulticastSocket(PORT);
            socket.joinGroup(group);

            System.out.println("Multicast Client is running...");

            byte[] buffer = new byte[2048];

            while (true) {
                System.out.print("Enter a command (RATE/GET movie_title): ");
                String command = scanner.nextLine();
                
                byte[] requestData = command.getBytes();
                InetAddress serverAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length,serverAddress, PORT);
                socket.send(requestPacket);
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Process the received packet
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received packet: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.leaveGroup(InetAddress.getByName(MULTICAST_ADDRESS));
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
