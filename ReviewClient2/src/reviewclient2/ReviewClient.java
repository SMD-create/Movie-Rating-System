
package reviewclient2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReviewClient {
    private static final int SERVER_PORT = 8888;
    private static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) {
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getLocalHost();

            // Send PING request
            sendRequest(socket, serverAddress, "PING SERVER");

            // Receive PING response
            String response = receiveResponse(socket);
            System.out.println("Response: " + response);

            // Send a custom request
            sendRequest(socket, serverAddress, "PING SERVER");

            // Receive the response
            response = receiveResponse(socket);
            System.out.println("Response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static void sendRequest(DatagramSocket socket, InetAddress serverAddress, String request) throws IOException {
        byte[] requestData = request.getBytes();
        DatagramPacket packet = new DatagramPacket(requestData, requestData.length, serverAddress, SERVER_PORT);
        socket.send(packet);
    }

    private static String receiveResponse(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength()).trim();
    }
}
