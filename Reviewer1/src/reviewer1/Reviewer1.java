package reviewer1;

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

public class Reviewer1 {
    private static final int PORT = 8888;
    private static final int BUFFER_SIZE = 1024;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int CLIENT_PORT = 9999;

    // MySQL database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviereviews";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "msql";

    private static final int SOCKET_TIMEOUT = 60000; // Socket timeout in milliseconds

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
        Map<String, Movie> movieRatings = new HashMap<>();
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket(PORT);
            socket.setSoTimeout(SOCKET_TIMEOUT); // Set the socket timeout
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

                    // Display packet information
                    System.out.println("Received packet from: " + packet.getAddress() + ":" + packet.getPort());

                    String request = new String(packet.getData(), 0, packet.getLength());

                    String[] requestTokens = request.split(" ", 2);

                    String response = null;

                    if (requestTokens.length == 2) {
                        String command = requestTokens[0];
                        String requestData = requestTokens[1];

                        if (command.equals("RATE")) {
                            int spaceIndex = requestData.lastIndexOf(' ');
                            if (spaceIndex != -1) {
                                String movieTitle = requestData.substring(0, spaceIndex);
                                String ratingStr = requestData.substring(spaceIndex + 1);

                                try {
                                    double rating = Double.parseDouble(ratingStr);
                                    response = rateMovie(movieTitle, rating, conn);
                                } catch (NumberFormatException e) {
                                    response = "Invalid rating format.";
                                }
                            } else {
                                response = "Invalid request format.";
                            }
                        } else if (command.equals("GET")) {
                            try {
                                response = getRating(movieRatings, conn, requestData);
                            } catch (SQLException e) {
                                response = "Error occurred while retrieving rating.";
                            }
                        } else {
                            response = "Unknown command: " + command;
                        }
                    } else {
                        response = "Invalid request format.";
                    }

                    byte[] responseData = response.getBytes();
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();
                    packet = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    socket.send(packet);
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

    private static String rateMovie(String movieTitle, double rating, Connection conn) {
        String sql = "INSERT INTO ratings (movie_title, rating) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, movieTitle);
            stmt.setDouble(2, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed to rate the movie.";
        }

        return "Rating of " + rating + " for " + movieTitle + " has been saved.";
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
                double averageRating = totalRating / numRatings;

                List<String> genres = getGenres(movie.getGenre());

                String response = "Movie: " + movieTitle + "\n" + "Individual Ratings: " + ratingsList.toString()
                        + "\n" + "Average Rating: " + averageRating + "\n" + "IMDb Rating: "
                        + movie.getRating() + " / 10" + "\n" + "Genres: " + genres + "\n" + "Plot: "
                        + movie.getPlot() + "\n" + "Director: " + movie.getDirector() + "\n" + "Music: "
                        + movie.getMusic() + "\n" + "Runtime: " + movie.getRuntime() + "\n" + "Released: "
                        + movie.getReleased() + "\n" + "Certificate: " + movie.getCertificate() + "\n" + "Star1: "
                        + movie.getStar1() + "\n" + "Star2: " + movie.getStar2() + "\n" + "Star3: "
                        + movie.getStar3();

                return response;
            } else {
                List<String> genres = getGenres(movie.getGenre());

                return "No individual ratings found in the database for the specified movie."
                        + "\n" + "Movie: " + movieTitle + "\n"
                        + "IMDb Rating: "+ movie.getRating() + " / 10" + "\n" + "Genres: " + genres + "\n" + "Plot: "
                        + movie.getPlot() + "\n" + "Director: " + movie.getDirector() + "\n" + "Music: "
                        + movie.getMusic() + "\n" + "Runtime: " + movie.getRuntime() + "\n" + "Released: "
                        + movie.getReleased() + "\n" + "Certificate: " + movie.getCertificate() + "\n" + "Star1: "
                        + movie.getStar1() + "\n" + "Star2: " + movie.getStar2() + "\n" + "Star3: "
                        + movie.getStar3();
            }
        } else {
            return "Movie not found: " + movieTitle;
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

    private static List<String> getGenres(String genreString) {
        String[] genres = genreString.split(",");
        List<String> genreList = new ArrayList<>();
        for (String genre : genres) {
            genreList.add(genre.trim());
        }
        return genreList;
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
                double rating = Double.parseDouble(tokens[1]);
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

                Movie movie = new Movie(title, rating, genre, plot, director, music, runtime, released, certificate,
                        star1, star2, star3);
                movieRatings.put(title, movie);
            }
        }

        return movieRatings;
    }
}