package bg.sofia.uni.fmi.mjt.netflix;

import java.util.ArrayList;
import java.util.List;

public record Content(String id, String title, ContentType type, String description, int releaseYear, int runtime,
                      List<String> genres, int seasons, String imdbId, double imdbScore, double imdbVotes) {

    private static final String DELIMITER = ",";

    public static Content of(String line) {
        final String[] tokens = line.split(DELIMITER);

        int index = 0;
        String id = tokens[index++];
        String title = tokens[index++];
        ContentType type = (tokens[index++].equalsIgnoreCase("MOVIE") ? ContentType.MOVIE : ContentType.SHOW);
        String description = tokens[index++];
        int releaseYear = Integer.parseInt(tokens[index++]);
        int runtime = Integer.parseInt(tokens[index++]);
        List<String> genres = createListFromCSVString(tokens[index++]);
        int seasons = Integer.parseInt(tokens[index++]);
        String imdbId = tokens[index++];
        double imdbScore = Double.parseDouble(tokens[index++]);
        double imdbVotes = Double.parseDouble(tokens[index]);

        return new Content(id, title, type, description, releaseYear, runtime, genres, seasons,
                imdbId, imdbScore, imdbVotes);
    }

    // ['drama'; 'action'; 'thriller'; 'european']
    private static List<String> createListFromCSVString(String string) {
        List<String> toReturn = new ArrayList<>();

        if (!string.startsWith("[") || !string.endsWith("]")) {
            throw new IllegalArgumentException("The string is not in correct format");
        }

        string = string.substring(1, string.length() - 1);

        String[] genres = string.split(";");
        for (String genre : genres) {
            genre = genre.trim();
            toReturn.add(genre.substring(1, genre.length() - 1).trim());
        }

        return toReturn;
    }
}