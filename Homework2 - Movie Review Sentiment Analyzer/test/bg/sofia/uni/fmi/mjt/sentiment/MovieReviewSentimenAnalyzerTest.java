package bg.sofia.uni.fmi.mjt.sentiment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MovieReviewSentimenAnalyzerTest {

    private static final double DELTA = 0.001;
    private static final String STOPWORDS = """
            the
            this
            and
            a
            is
            but
            too
            don't
            or
            have
            stopword
            """;

    private static final String REVIEWS = """
            0 This movie is the worst film I have ever seen - negative.	
            1 I don't recommend .	
            2 Good, but too short and ... .	
            3 Good, this is a good movie  .	
            4 Excellent, this is a very good movie! Recommend! .	
            """;

    private MovieReviewSentimentAnalyzer analyzer;
    private Writer reviewsOut;

    @BeforeEach
    void initialize() {
        Reader stopwordsIn = new StringReader(STOPWORDS);
        Reader reviewsIn = new StringReader(REVIEWS);
        reviewsOut = new StringWriter();

        analyzer = new MovieReviewSentimentAnalyzer(stopwordsIn, reviewsIn, reviewsOut);
    }

    @Test
    void testCreateSentimentAnalyzerNullArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new MovieReviewSentimentAnalyzer(null, new StringReader(REVIEWS), new StringWriter()),
                "StopwordsIn Reader cannot be null - expected IllegalArgumentException");

        assertThrows(IllegalArgumentException.class,
                () -> new MovieReviewSentimentAnalyzer(new StringReader(STOPWORDS), null, new StringWriter()),
                "ReviewsIn Reader cannot be null - expected IllegalArgumentException");

        assertThrows(IllegalArgumentException.class,
                () -> new MovieReviewSentimentAnalyzer(new StringReader(STOPWORDS), new StringReader(REVIEWS), null),
                "ReviewsOut Writer cannot be null - expected IllegalArgumentException");
    }

    @Test
    void testGetReviewSentimentInvlaidString() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getReviewSentiment(null),
                "The string cannot be null");
        assertThrows(IllegalArgumentException.class, () -> analyzer.getReviewSentiment(""),
                "The string cannot be empty or blank");
    }

    @Test
    void testGetReviewSentimentUnknownWords() {
        double result = analyzer.getReviewSentiment("Some unknown words and stopwords");
        assertEquals(-1.0, result, "Expected -1.0 when the words in the review are unknown");
    }

    @Test
    void testGetReviewSentimentExistingWords() {
        double result = analyzer.getReviewSentiment("This is an excellent movie, it is very good - strongly recommend");
        assertEquals(3.166, result, DELTA, "The sentiment for this review not correctly " +
                "calculated, expected 3.166, but returned " + result);
    }

    @Test
    void testGetReviewSentimentAsNameInvlaidString() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getReviewSentimentAsName(null),
                "The string cannot be null");
        assertThrows(IllegalArgumentException.class, () -> analyzer.getReviewSentimentAsName(""),
                "The string cannot be empty or blank");
    }

    @Test
    void testGetReviewSentimentAsNameUnknownWords() {
        String result = analyzer.getReviewSentimentAsName("Some unknown words and stopwords");
        assertEquals("unknown", result, "Expected \"unknown\" when the words in the review are unknown");
    }

    @Test
    void testGetReviewSentimentAsNameExistingWords() {
        String result = analyzer.getReviewSentimentAsName("This is an excellent movie, it is very good - strongly recommend");
        assertEquals("somewhat positive", result, "The sentiment for this review not correctly " +
                "calculated or estimated as name, expected \"somewhat positive\", but returned " + result);
    }

    @Test
    void testGetWordSentimentInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getWordSentiment(null),
                "The string cannot be null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> analyzer.getWordSentiment(""),
                "The string cannot be empty or blank - expected IllegalArgumentException");
    }

    @Test
    void testGetWordSentimentUnknownWord() {
        assertEquals(-1.0, analyzer.getWordSentiment("unknown"),
                "The word sentiment for unknown word must be -1.0");
    }

    @Test
    void testGetWordSentimentExistingWord() {
        assertEquals(3.0, analyzer.getWordSentiment("good"),
                "Word sentiment not calculated properly - expected 3.0 to be returned");
    }

    @Test
    void testGetWordSentimentExistingWordIgnoreCase() {
        assertEquals(analyzer.getWordSentiment("movie"), analyzer.getWordSentiment("MoViE"), DELTA,
                "The sentiment for \"movie\" and \"MoViE\" must be equal");
    }

    @Test
    void testGetWordFrequencyInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getWordFrequency(null),
                "The string cannot be null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> analyzer.getWordFrequency(""),
                "The string cannot be empty or blank - expected IllegalArgumentException");
    }

    @Test
    void testGetWordFrequencyUnknownWord() {
        assertEquals(0, analyzer.getWordFrequency("unknown"),
                "The frequency for unknown word must be 0");
    }

    @Test
    void testGetWordFrequencyExistingWord() {
        assertEquals(4, analyzer.getWordFrequency("good"),
                "Word frequency not calculated properly - expected 4 to be returned");
    }

    @Test
    void testGetWordFrequencyExistingWordIgnoreCase() {
        assertEquals(analyzer.getWordFrequency("movie"), analyzer.getWordFrequency("MoViE"), DELTA,
                "The frequency for \"movie\" and \"MoViE\" must be equal");
    }

    @Test
    void testGetMostFrequentWordsNegativeNumber() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getMostFrequentWords(-5),
                "The number of most frequent words cannot be negative - expected IllegalArgumentException");
    }

    @Test
    void testGetMostFrequentWordsValidNumber() {
        List<String> expected = List.of("good", "movie", "recommend");
        assertEquals(expected, analyzer.getMostFrequentWords(3),
                "The most frequent words don't determined properly");
    }

    @Test
    void testGetMostPositiveWordsNegativeNumber() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getMostPositiveWords(-5),
                "The number of most positive words cannot be negative - expected IllegalArgumentException");
    }

    @Test
    void testGetMostPositiveWordsValidNumber() {
        List<String> expected = List.of("very", "excellent", "good", "recommend", "movie");
        assertEquals(expected, analyzer.getMostPositiveWords(5),
                "The most positive words don't determined properly");
    }

    @Test
    void testGetMostNegativeWordsNegativeNumber() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getMostNegativeWords(-5),
                "The number of most negative words cannot be negative - expected IllegalArgumentException");
    }

    @Test
    void testGetMostNegativeWordsValidNumber() {
        Set<String> expected = Set.of("seen", "film", "worst", "negative", "ever");
        List<String> actual = analyzer.getMostNegativeWords(5);

        assertEquals(5, actual.size(), "The size of the list returned not as expected");

        for (String word : actual) {
            assertTrue(expected.contains(word), "Unexpected word returned");
        }
    }

    @Test
    void testIsStopwordTrue() {
        assertTrue(analyzer.isStopWord("stopword"), "The word must be recognized as stop word, but it was not");
    }

    @Test
    void testIsStopwordFalse() {
        assertFalse(analyzer.isStopWord("excellent"), "The word must not be recognized as stop word, but it was");
    }

    @Test
    void testAppendReviewInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.appendReview(null, 2),
                "The review string cannot be null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> analyzer.appendReview("", 2),
                "The review string cannot be empty or blank - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> analyzer.appendReview("Some review text", -1),
                "The sentiment cannot be negative");
        assertThrows(IllegalArgumentException.class, () -> analyzer.appendReview("Some review text", 5),
                "The sentiment cannot be greater than 4");
    }

    @Test
    void testAppendReviewTheNewReviewIsWritten() throws IOException {
        reviewsOut.write(REVIEWS);
        analyzer.appendReview("Awesome movie - strongly recommend", 4);

        String expected = REVIEWS + String.valueOf(4) + " Awesome movie - strongly recommend" + System.lineSeparator();

        assertEquals(expected, reviewsOut.toString(), "The new review not properly appended");
    }

    @Test
    void testAppendReviewUpdatedInformation() {
        analyzer.appendReview("Awesome movie - strongly recommend", 4);

        assertEquals(2.75, analyzer.getWordSentiment("movie"), "The word sentiment not updated");
        assertEquals(4, analyzer.getWordFrequency("movie"), "The word frequency not updated");

        assertEquals(4.0, analyzer.getWordSentiment("awesome"), "The word sentiment not updated");
        assertEquals(1, analyzer.getWordFrequency("awesome"), "The word frequency not updated");

        assertEquals(4.0, analyzer.getWordSentiment("strongly"), "The word sentiment not updated");
        assertEquals(1, analyzer.getWordFrequency("strongly"), "The word frequency not updated");

        assertEquals(3.0, analyzer.getWordSentiment("recommend"), "The word sentiment not updated");
        assertEquals(3, analyzer.getWordFrequency("recommend"), "The word frequency not updated");
    }
}
