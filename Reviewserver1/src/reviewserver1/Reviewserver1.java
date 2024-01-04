package reviewserver1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.MulticastSocket;
import java.util.zip.CRC32;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Reviewserver1 {
    private static final int PORT = 8888;
    private static final int BUFFER_SIZE = 2048;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int CLIENT_PORT = 9999;
    private static final String MULTICAST_ADDRESS = "224.0.0.1";
    private static final int MULTICAST_PORT = 7777;

    // MySQL database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviereviews";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "msql";

    private static final int SOCKET_TIMEOUT = 60000; // Socket timeout in milliseconds

private static String rateMovieField(String movieTitle, String fieldName, double rating, Connection conn) {
    // Check if the movie exists in the database or CSV file
    boolean movieExistsInDatabase = checkIfMovieExistsInDatabase(movieTitle, conn);
    boolean movieExistsInCSV = checkIfMovieExistsInCSV(movieTitle);

    if (movieExistsInDatabase || movieExistsInCSV) {
        // Update the rating in the corresponding column
        if (fieldName.equals("plot_rating")) {
            // Update plot_rating column in the database
            updateRatingInDatabase(movieTitle, "plot_rating", rating, conn);
        } else if (fieldName.equals("director_rating")) {
            // Update director_rating column in the database
            updateRatingInDatabase(movieTitle, "director_rating", rating, conn);
        } else if (fieldName.equals("music_rating")) {
            // Update music_rating column in the database
            updateRatingInDatabase(movieTitle, "music_rating", rating, conn);
        } else {
            return "Invalid field name.";
        }

        return "Rating updated successfully.";
    } else {
        return "Movie not found.";
    }
}




    private static class Movie {
        private final String title;
        private final double rating;
        private final String genre;
        private final String plot;
        private final String director;
        private final String music;
        private final String runtime;
        private final String released;
        private final String certificate;
        private final String star1;
        private final String star2;
        private final String star3;

        public Movie(String title, double rating, String genre, String plot, String director, String music,
                     String runtime, String released, String certificate, String star1, String star2, String star3) {
            this.title = title;
            this.rating = rating;
            this.genre = genre;
            this.plot = plot;
            this.director = director;
            this.music = music;
            this.runtime = runtime;
            this.released = released;
            this.certificate = certificate;
            this.star1 = star1;
            this.star2 = star2;
            this.star3 = star3;
            
            
        }

        public String getTitle() {
            return title;
        }

        public double getRating() {
            return rating;
        }

        public String getGenre() {
            return genre;
        }

        public String getPlot() {
            return plot;
        }

        public String getDirector() {
            return director;
        }

        public String getMusic() {
            return music;
        }

        public String getRuntime() {
            return runtime;
        }

        public String getReleased() {
            return released;
        }

        public String getCertificate() {
            return certificate;
        }

        public String getStar1() {
            return star1;
        }

        public String getStar2() {
            return star2;
        }

        public String getStar3() {
            return star3;
        }
        
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        ServerUI serverUI = new ServerUI();
        serverUI.setVisible(true);

        // Example usage
        serverUI.appendLog("Server started...");
        serverUI.appendLog("Waiting for incoming connections...");
    });
        
        Map<String, Movie> movieRatings = new HashMap<>();
        DatagramSocket socket = null;
        MulticastSocket multicastSocket = null;

        try {
            socket = new DatagramSocket(PORT);
            multicastSocket = new MulticastSocket();
            socket.setSoTimeout(60000); // Set the socket timeout
            byte[] buffer = new byte[BUFFER_SIZE];

            System.out.println("Movie Review Server is running...");

            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection to the MySQL database
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                // Load the IMDB ratings from the CSV file
                String csvFilePath = "C:\\Users\\Daksinayogam\\Documents\\SEM 4\\ML\\IMDB(1)\\imdb1.csv"; // Replace with the actual file path
                movieRatings = readCSV(csvFilePath);

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                socket.receive(packet);
                analyzePacket(packet);
                // Display packet information
                System.out.println("Received packet from: " + packet.getAddress() + ":" + packet.getPort());

                // Process the received packet and generate a response
                

                    String request = new String(packet.getData(), 0, packet.getLength());
                    

                    String[] requestTokens = request.split(" ", 2);

                    String response ;
                    
                    
                    int totalBitsPassed = 0;
                    
                    calccheck(request);
                    
                    if (requestTokens.length == 2) {
                        String command = requestTokens[0];
                        String requestData = requestTokens[1];
                        
                        long checksum = calccheck(request);

                        // Print the checksum
                        System.out.println("Checksum: " + checksum);
                        
                        totalBitsPassed += packet.getLength() * 8;

                        // Display total bits passed
                        System.out.println("Total Bits Passed: " + totalBitsPassed);
                        
                        String movieTitle = null;
                        String fieldName = null;
                        double rating = 0.0;
                        Scanner scanner=new Scanner(System.in);
if (command.equals("RATE")) {
    // Prompt for movie details
    System.out.print("Enter the movie title: ");
    String newMovieTitle = scanner.nextLine();
    
    // Prompt for ratings
    System.out.print("Enter the director rating: ");
    String directorRatingStr = scanner.nextLine();
    System.out.print("Enter the music rating: ");
    String musicRatingStr = scanner.nextLine();
    System.out.print("Enter the plot rating: ");
    String plotRatingStr = scanner.nextLine();
    
    try {
        double directorRating = Double.parseDouble(directorRatingStr);
        double musicRating = Double.parseDouble(musicRatingStr);
        double plotRating = Double.parseDouble(plotRatingStr);
        
        response = rateMovie(newMovieTitle, directorRating, musicRating, plotRating, conn);
    } catch (NumberFormatException e) {
        response = "Invalid rating format.";
    }


                        } else if (command.equals("GET")) {
                            try {
                                response = getRating(movieRatings, conn, requestData);
                            } catch (SQLException e) {
                                response = "Error occurred while retrieving rating.";
                            }
                        } else if (command.equals("GETGENRERATINGS")) {
                            try {
                                response = getGenreRatings(movieRatings, conn, requestData);
                            } catch (SQLException e) {
                                response = "Error occurred while retrieving genre ratings.";
                            }
                        
                        }else if (command.equals("PING")) {
                            response = "PONG";
                        } else {
                            response = "Unknown command: " + request;
                        }
                    } else {
                        response = "Invalid request format.";
                    }

                    byte[] responseData = response.getBytes();
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();
                    packet = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    socket.send(packet);
                    
                    // Multicast the response
                    byte[] multicastData = response.getBytes();
                    InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
                    DatagramPacket multicastPacket = new DatagramPacket(multicastData, multicastData.length, multicastAddress, MULTICAST_PORT);
                    multicastSocket.send(multicastPacket);
                
                    System.out.println("Response sent: " + response);
                }
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
    private static void analyzePacket(DatagramPacket packet) {
    byte[] data = packet.getData();
    int length = packet.getLength();

    System.out.println("Packet analysis:");
    System.out.println("Source IP: " + packet.getAddress());
    System.out.println("Source Port: " + packet.getPort());
    System.out.println("Data Length: " + length);

    // Print the packet data as a string
    String packetData = new String(data, 0, length);
    System.out.println("Packet Data:");
    System.out.println(packetData);

    // Further analysis of the packet can be performed here
    // For example, parsing the packet data or extracting specific information

    System.out.println("------------");
}
    private static long calccheck(String data)
    {
        CRC32 checksum= new CRC32();
        checksum.update(data.getBytes());
        return checksum.getValue();
    }
    
    private static int calculateTotalBitsPassed(int dataPackets, int packetSize) {
        return dataPackets * packetSize * 8;
    }

    private static int calculateBitsPassedPerPacket(int totalBitsPassed, int dataPackets) {
        if (dataPackets > 0) {
            return totalBitsPassed / dataPackets;
        } else {
            return 0;
        }
    }
    
    private static void updateRatingInDatabase(String movieTitle, String fieldName, double rating, Connection conn) {
    try {
        // Create a prepared statement to update the rating in the specified field
        String updateQuery = "UPDATE ratings SET %s = ? WHERE movie_title = ?";
        PreparedStatement statement = conn.prepareStatement(String.format(updateQuery, fieldName));
        statement.setDouble(1, rating);
        statement.setString(2, movieTitle);

        // Execute the update
        statement.executeUpdate();

        // Close the statement
        statement.close();
    } catch (SQLException e) {
        System.out.println("Error updating rating in the database: " + e.getMessage());
    }
}

private static String rateMovie(String movieTitle, double directorRating, double musicRating, double plotRating, Connection conn) {
    String sql = "INSERT INTO ratings (movie_title, director_rating, music_rating, plot_rating) VALUES (?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, movieTitle);
        stmt.setDouble(2, directorRating);
        stmt.setDouble(3, musicRating);
        stmt.setDouble(4, plotRating);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
        return "Failed to rate the movie.";
    }

    return "Ratings for " + movieTitle + " have been saved.";
}

    
    // Method to check if a movie exists in the database
private static boolean checkIfMovieExistsInDatabase(String movieTitle, Connection conn) {
    try {
        // Assuming you have a table named "movies" in the database with a column named "movie_title"
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ratings WHERE movie_title = ?");
        stmt.setString(1, movieTitle);
        ResultSet rs = stmt.executeQuery();
        return rs.next(); // Returns true if a row with the movie title is found in the database
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

private static boolean checkIfMovieExistsInCSV(String movieTitle) {
    String csvFilePath = "C:\\Users\\Daksinayogam\\Documents\\SEM 4\\ML\\IMDB(1)\\imdb1.csv";
    try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] data = line.split("\t"); // Splitting by tab (\t) instead of comma (,)
            if (data.length > 0 && data[0].equals(movieTitle)) {
                return true; // Returns true if the movie title is found in the CSV file
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return false;
}


    private static String getRating(Map<String, Movie> movieRatings, Connection conn, String movieTitle)
        throws SQLException {
    Movie movie = movieRatings.get(movieTitle);
    if (movie != null) {
        List<Double> ratingsList = getRatingsList(movieTitle, conn);
        if (ratingsList != null && !ratingsList.isEmpty()) {
            double totalRating = 0.0;
            int numRatings = ratingsList.size();
            for (double rating : ratingsList) {
                totalRating += rating;
            }
            
            double plotRating = getRatingFromDatabase(movieTitle, "plot_rating", conn);
            double directorRating = getRatingFromDatabase(movieTitle, "director_rating", conn);
            double musicRating = getRatingFromDatabase(movieTitle, "music_rating", conn);

            double fieldAverageRating = (plotRating + directorRating + musicRating) / 3.0;
     
            // Calculate the overall average rating using the updated total rating
            double averageRating = fieldAverageRating;

            List<String> genres = getGenres(movie.getGenre());

            String response = "Movie: " + movieTitle + "\n" + "Individual Ratings: " + ratingsList.toString()
                    + "\n" + "Average Rating: " + averageRating + "\n"
                    + "Field Ratings:" + "\n"
                    + "Plot Rating: " + plotRating + "\n"
                    + "Director Rating: " + directorRating + "\n"
                    + "Music Rating: " + musicRating + "\n"
                    + "IMDb Rating: " + movie.getRating() + " / 10" + "\n"
                    + "Genres: " + genres + "\n"
                    + "Plot: " + movie.getPlot() + "\n"
                    + "Director: " + movie.getDirector() + "\n"
                    + "Music: " + movie.getMusic() + "\n"
                    + "Runtime: " + movie.getRuntime() + "\n"
                    + "Released: " + movie.getReleased() + "\n"
                    + "Certificate: " + movie.getCertificate() + "\n"
                    + "Star1: " + movie.getStar1() + "\n"
                    + "Star2: " + movie.getStar2() + "\n"
                    + "Star3: " + movie.getStar3();

            return response;
        } else {
            List<String> genres = getGenres(movie.getGenre());

            return "No individual ratings found in the database for the specified movie."
                    + "\n" + "Movie: " + movieTitle + "\n"
                    + "IMDb Rating: "+ movie.getRating() + " / 10" + "\n"
                    + "Genres: " + genres + "\n"
                    + "Plot: " + movie.getPlot() + "\n"
                    + "Director: " + movie.getDirector() + "\n"
                    + "Music: " + movie.getMusic() + "\n"
                    + "Runtime: " + movie.getRuntime() + "\n"
                    + "Released: " + movie.getReleased() + "\n"
                    + "Certificate: " + movie.getCertificate() + "\n"
                    + "Star1: " + movie.getStar1() + "\n"
                    + "Star2: " + movie.getStar2() + "\n"
                    + "Star3: " + movie.getStar3();
        }
    } else {
        return "Movie not found: " + movieTitle;
    }
}


private static double getRatingFromDatabase(String movieTitle, String fieldName, Connection conn) throws SQLException {
    double rating = 0.0;
    PreparedStatement stmt = conn.prepareStatement("SELECT " + fieldName + " FROM ratings WHERE movie_title = ?");
    stmt.setString(1, movieTitle);
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
        rating = rs.getDouble(fieldName);
    }
    rs.close();
    stmt.close();
    return rating;
}

    private static String getGenreRatings(Map<String, Movie> movieRatings, Connection conn, String genre)
            throws SQLException {
        List<String> moviesInGenre = new ArrayList<>();
        for (Movie movie : movieRatings.values()) {
            List<String> genres = getGenres(movie.getGenre());
            if (genres.contains(genre)) {
                moviesInGenre.add(movie.getTitle());
            }
        }

        if (!moviesInGenre.isEmpty()) {
            StringBuilder responseBuilder = new StringBuilder();
            for (String movieTitle : moviesInGenre) {
                List<Double> ratingsList = getRatingsList(movieTitle, conn);
                if (ratingsList != null && !ratingsList.isEmpty()) {
                    double totalRating = 0.0;
                    int numRatings = ratingsList.size();
                    for (double rating : ratingsList) {
                        totalRating += rating;
                    }
                    double averageRating = totalRating / numRatings;

                    Movie movie = movieRatings.get(movieTitle);
                    List<String> genres = getGenres(movie.getGenre());

                    String movieInfo = "Movie: " + movieTitle + "\n" + "Individual Ratings: " + ratingsList.toString()
                            + "\n" + "Average Rating: " + averageRating + "\n" + "IMDb Rating: "
                            + movie.getRating() + " / 10" + "\n" + "Genres: " + genres + "\n" + "Plot: "
                            + movie.getPlot() + "\n" + "Director: " + movie.getDirector() + "\n" + "Music: "
                            + movie.getMusic() + "\n" + "Runtime: " + movie.getRuntime() + "\n" + "Released: "
                            + movie.getReleased() + "\n" + "Certificate: " + movie.getCertificate() + "\n" + "Star1: "
                            + movie.getStar1() + "\n" + "Star2: " + movie.getStar2() + "\n" + "Star3: "
                            + movie.getStar3() + "\n\n";

                    responseBuilder.append(movieInfo);
                } else {
                    Movie movie = movieRatings.get(movieTitle);
                    List<String> genres = getGenres(movie.getGenre());

                    String movieInfo = "No individual ratings found in the database for the specified movie."
                            + "\n" + "Movie: " + movieTitle + "\n"
                            + "IMDb Rating: "+ movie.getRating() + " / 10" + "\n" + "Genres: " + genres + "\n" + "Plot: "
                            + movie.getPlot() + "\n" + "Director: " + movie.getDirector() + "\n" + "Music: "
                            + movie.getMusic() + "\n" + "Runtime: " + movie.getRuntime() + "\n" + "Released: "
                            + movie.getReleased() + "\n" + "Certificate: " + movie.getCertificate() + "\n" + "Star1: "
                            + movie.getStar1() + "\n" + "Star2: " + movie.getStar2() + "\n" + "Star3: "
                            + movie.getStar3() + "\n\n";

                    responseBuilder.append(movieInfo);
                }
            }

            return responseBuilder.toString();
        } else {
            return "No movies found in the specified genre.";
        }
    }

    private static List<Double> getRatingsList(String movieTitle, Connection conn) throws SQLException {
        List<Double> ratingsList = new ArrayList<>();
        String sql = "SELECT rating FROM ratings WHERE movie_title = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movieTitle);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double rating = rs.getDouble("rating");
                ratingsList.add(rating);
            }
        }

        return ratingsList;
    }

    


    private static List<String> getGenres(String genre) {
        return List.of(genre.split("\\|"));
    }

    private static Map<String, Movie> readCSV(String filePath) throws IOException {
    Map<String, Movie> movieRatings = new HashMap<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        String line;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            String title = tokens[0].replaceAll("^\"|\"$", "");
            double rating = Double.parseDouble(tokens[1]); // Update this line
            String genre = tokens[2].replaceAll("^\"|\"$", "");
            String plot = tokens[3].replaceAll("^\"|\"$", "");
            String director = tokens[4].replaceAll("^\"|\"$", "");
            String music = tokens[5].replaceAll("^\"|\"$", "");
            String runtime = tokens[6].replaceAll("^\"|\"$", "");
            String released = tokens[7].replaceAll("^\"|\"$", "");
            String certificate = tokens[8].replaceAll("^\"|\"$", "");
            String star1 = tokens[9].replaceAll("^\"|\"$", "");
            String star2 = tokens[10].replaceAll("^\"|\"$", "");
            String star3 = tokens[11].replaceAll("^\"|\"$", "");

            Movie movie = new Movie(title, rating, genre, plot, director, music, runtime, released, certificate,star1, star2, star3);
            movieRatings.put(title, movie);
        }
    }

    return movieRatings;
}}
