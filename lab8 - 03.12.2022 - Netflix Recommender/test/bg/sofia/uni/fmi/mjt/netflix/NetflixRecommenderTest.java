package bg.sofia.uni.fmi.mjt.netflix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class NetflixRecommenderTest {
    private NetflixRecommender recommender;

    private static final String testCSVString = """
            id,title,type,description,release_year,runtime,genres,seasons,imdb_id,imdb_score,imdb_votes
            1,Movie 1,MOVIE,Description 1.,1976,114,['drama'; 'crime'],-1,123,8.2,12.0
            2,Movie 2,MOVIE,Description 2,1972,109,['drama'; 'action'; 'thriller'; 'european'],-1,234,7.7,23.0
            3,Show 3,SHOW,Description 3,1975,91,['fantasy'; 'action'; 'comedy'],-1,345,8.2,34.0
            4,Movie 4,MOVIE,Description 4,1967,150,['war'; 'action'],-1,456,7.7,45.0
            5,Show 5,SHOW,Description 5,1969,30,['comedy'; 'european'],1,567,8.8,56.0
            """;

    @BeforeEach
    void initializeNetflixRecommender() {
        recommender = new NetflixRecommender(new StringReader(testCSVString));
    }

    @Test
    void testGetAllContent() {
        List<Content> expected = new ArrayList<>();
        expected.add(new Content("1","Movie 1",ContentType.MOVIE,"Description 1.",1976,
                114, Arrays.asList("drama", "crime"),-1,"123",8.2,12.0));
        expected.add(new Content("2","Movie 2",ContentType.MOVIE,"Description 2",1972,109,
                Arrays.asList("drama", "action", "thriller", "european"),-1,"234",7.7,23.0));
        expected.add(new Content("3","Show 3",ContentType.SHOW,"Description 3",1975,91,
                Arrays.asList("fantasy", "action", "comedy"),-1,"345",8.2,34.0));
        expected.add(new Content("4","Movie 4",ContentType.MOVIE,"Description 4",1967,150,
                Arrays.asList("war", "action"),-1,"456",7.7,45.0));
        expected.add(new Content("5","Show 5",ContentType.SHOW,"Description 5",1969,30,
                Arrays.asList("comedy", "european"),1,"567",8.8,56.0));

        List<Content> actual = recommender.getAllContent();

        assertIterableEquals(expected, actual, "Movies not added properly");
    }

    @Test
    void testGetLongestMovie() {
        Content expected = new Content("4","Movie 4",ContentType.MOVIE,"Description 4",1967,150,
                Arrays.asList("war", "action"),-1,"456",7.7,45.0);

        Content actual = recommender.getTheLongestMovie();

        assertEquals(expected, actual, "Longest movie is not properly returned");
    }

    @Test
    void testGroupContentsByType() {
        Set<Content> movies = new HashSet<>();
        Set<Content> shows = new HashSet<>();

        movies.add(new Content("1","Movie 1",ContentType.MOVIE,"Description 1.",1976,
                114, Arrays.asList("drama", "crime"),-1,"123",8.2,12.0));
        movies.add(new Content("2","Movie 2",ContentType.MOVIE,"Description 2",1972,109,
                Arrays.asList("drama", "action", "thriller", "european"),-1,"234",7.7,23.0));
        shows.add(new Content("3","Show 3",ContentType.SHOW,"Description 3",1975,91,
                Arrays.asList("fantasy", "action", "comedy"),-1,"345",8.2,34.0));
        movies.add(new Content("4","Movie 4",ContentType.MOVIE,"Description 4",1967,150,
                Arrays.asList("war", "action"),-1,"456",7.7,45.0));
        shows.add(new Content("5","Show 5",ContentType.SHOW,"Description 5",1969,30,
                Arrays.asList("comedy", "european"),1,"567",8.8,56.0));

        Map<ContentType, Set<Content>> expected = new HashMap<>();
        expected.put(ContentType.MOVIE, movies);
        expected.put(ContentType.SHOW, shows);

        Map<ContentType, Set<Content>> actual = recommender.groupContentByType();

        assertEquals(expected, actual, "Contents not grouped properly");
    }

    @Test
    void testGetTopNRatedContent() {
        List<Content> expected = new ArrayList<>();

        expected.add(new Content("5","Show 5",ContentType.SHOW,"Description 5",1969,30,
                Arrays.asList("comedy", "european"),1,"567",8.8,56.0));
        expected.add(new Content("3","Show 3",ContentType.SHOW,"Description 3",1975,91,
                Arrays.asList("fantasy", "action", "comedy"),-1,"345",8.2,34.0));

        List<Content> actual = recommender.getTopNRatedContent(2);

        assertEquals(expected, actual, "Top n rated content not determined properly");
    }


    @Test
    void testGetSimilarContent() {
        List<Content> expected = new ArrayList<>();

        expected.add(new Content("3","Show 3",ContentType.SHOW,"Description 3",1975,91,
                Arrays.asList("fantasy", "action", "comedy"),-1,"345",8.2,34.0));
        expected.add(new Content("5","Show 5",ContentType.SHOW,"Description 5",1969,30,
                Arrays.asList("comedy", "european"),1,"567",8.8,56.0));

        List<Content> actual = recommender.getSimilarContent(new Content("3","Show 3",ContentType.SHOW,
                "Description 3",1975,91, Arrays.asList("fantasy", "action", "comedy"),
                -1,"345",8.2,34.0));

        assertEquals(expected, actual, "Similar content not properly determined");
    }

    @Test
    void testGetContentByKeywords() {
        Set<Content> expected = new HashSet<>();
        expected.add(new Content("1","Movie 1",ContentType.MOVIE,"Description 1.",1976,
                114, Arrays.asList("drama", "crime"),-1,"123",8.2,12.0));
        expected.add(new Content("2","Movie 2",ContentType.MOVIE,"Description 2",1972,109,
                Arrays.asList("drama", "action", "thriller", "european"),-1,"234",7.7,23.0));
        expected.add(new Content("3","Show 3",ContentType.SHOW,"Description 3",1975,91,
                Arrays.asList("fantasy", "action", "comedy"),-1,"345",8.2,34.0));
        expected.add(new Content("4","Movie 4",ContentType.MOVIE,"Description 4",1967,150,
                Arrays.asList("war", "action"),-1,"456",7.7,45.0));
        expected.add(new Content("5","Show 5",ContentType.SHOW,"Description 5",1969,30,
                Arrays.asList("comedy", "european"),1,"567",8.8,56.0));

        Set<Content> actual = recommender.getContentByKeywords("description");

        assertEquals(expected, actual, "Content returned does not match the keywords");
    }
}
