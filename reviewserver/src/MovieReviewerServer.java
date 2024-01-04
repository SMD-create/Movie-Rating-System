

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MovieReviewerServer {
    private static final int PORT = 8888;
    private static final int BUFFER_SIZE = 1024;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int CLIENT_PORT = 9999;
    
    private static Connection getConnection() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/moviereviews?useSSL=false";
    String username = "root";
    String password = "msql";
    return DriverManager.getConnection(url, username, password);
}

    public static void main(String[] args) {
        Map<String, Double> movieRatings = new HashMap<>();
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket(PORT);
            byte[] buffer = new byte[BUFFER_SIZE];
            
            System.out.println("Movie Reviewer Server is running...");
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength());

                String[] requestTokens = request.split(" ", 2); // Split into two tokens at the first space

                String response;

                if (requestTokens.length == 2) {
                    String command = requestTokens[0];
                    String requestData = requestTokens[1];

                if (command.equals("RATE")) {
                    int spaceIndex = requestData.lastIndexOf(' ');
                if (spaceIndex != -1) {
                    String movieTitle = requestData.substring(0, spaceIndex);
                    double rating = Double.parseDouble(requestData.substring(spaceIndex + 1));
                    // Inside the RATE command block
try (Connection connection = getConnection()) {
    String insertQuery = "INSERT INTO ratings (movie_title, rating) VALUES (?, ?)";
    PreparedStatement statement = connection.prepareStatement(insertQuery);
    statement.setString(1, movieTitle);
    statement.setDouble(2, rating);
    statement.executeUpdate();
    response = "Rating for " + movieTitle + " has been recorded as " + rating;
} catch (SQLException e) {
    response = "Error: Failed to record rating for " + movieTitle;
    e.printStackTrace();
}

                    movieRatings.put(movieTitle, rating);
                
                    response = "Rating for " + movieTitle + " has been recorded as " + rating;
                } else {
                    response = "Invalid RATE command. Format: RATE movie_title rating";
                }
            } else if (command.equals("GET")) {
                String movieTitle = requestData;
                Double rating = movieRatings.get(movieTitle);
            
                if (rating != null) {
                    response = "Rating for " + movieTitle + " is " + rating;
                } else {
                    response = "No rating found for " + movieTitle;
                }
            } else {
                response = "Invalid command";
            }
            } else {
                response = "Invalid request";
            }
                
                byte[] responseData = response.getBytes();
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                        clientAddress, clientPort);
                socket.send(responsePacket);
                
                System.out.println("Request: " + request + ", Response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}