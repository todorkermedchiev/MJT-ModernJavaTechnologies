package bg.sofia.uni.fmi.mjt.sentiment;

import bg.sofia.uni.fmi.mjt.sentiment.exception.SentimentTypeNotFoundException;
import bg.sofia.uni.fmi.mjt.sentiment.review.Review;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MovieReviewSentimentAnalyzer implements SentimentAnalyzer {
    private final Map<String, Double> wordsScore;
    private final Map<String, Integer> wordsOccurrences;
    private final Map<String, Integer> wordsOccurrencesWithoutRepeating; // How many reviews contain this word
    private Set<String> stopwords;
    private final Writer reviewsOut;

    private static final String DELIMITER_REGEX = "[^\\w']+";

    public MovieReviewSentimentAnalyzer(Reader stopwordsIn, Reader reviewsIn, Writer reviewsOut) {
        validateNullObject(stopwordsIn);
        validateNullObject(reviewsIn);
        validateNullObject(reviewsOut);

        this.reviewsOut = reviewsOut;
        this.wordsScore = new HashMap<>();
        this.wordsOccurrences = new HashMap<>();
        this.wordsOccurrencesWithoutRepeating = new HashMap<>();

        readStopwords(stopwordsIn);
        readReviews(reviewsIn);
    }

    @Override
    public double getReviewSentiment(String review) {
        final double invalidReturnValue = -1.0;
        validateString(review);

        int wordsCount = 0;
        double sum = 0.0;

        List<String> words = extractWordsFromReview(review);
        for (String word : words) {
            if (!wordsScore.containsKey(word)) {
                continue;
            }

            ++wordsCount;
            sum += wordsScore.get(word);
        }

        if (wordsCount == 0) {
            return invalidReturnValue;
        }

        return sum / wordsCount;
    }

    @Override
    public String getReviewSentimentAsName(String review) {
        validateString(review);
        final String invalidReturnValue = "unknown";

        double sentiment = getReviewSentiment(review);

        try {
            return SentimentType.getSentimentTypeByRating((int) Math.round(sentiment)).name;
        } catch (SentimentTypeNotFoundException e) {
            return invalidReturnValue;
        }
    }

    @Override
    public double getWordSentiment(String word) {
        validateString(word);
        final double invalidReturnValue = -1.0;

        if (!wordsScore.containsKey(word.toLowerCase())) {
            return invalidReturnValue;
        }
        return wordsScore.get(word.toLowerCase());
    }

    @Override
    public int getWordFrequency(String word) {
        validateString(word);

        Integer frequency = wordsOccurrences.get(word.toLowerCase());
        return (frequency != null ? frequency : 0);
    }

    @Override
    public List<String> getMostFrequentWords(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The number of most frequent words cannot be negative");
        }

        return wordsOccurrences.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public List<String> getMostPositiveWords(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The number of most positive words cannot be negative");
        }

        return wordsScore.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public List<String> getMostNegativeWords(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The number of most negative words cannot be negative");
        }

        return wordsScore.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public boolean appendReview(String review, int sentiment) {
        final int maxSentimentValue = 4;
        final int minSentimentValue = 0;

        validateString(review);
        if (sentiment < minSentimentValue || sentiment > maxSentimentValue) {
            throw new IllegalArgumentException("Sentiment must be in the [0.0, 4.0] range");
        }

        try (var writer = new BufferedWriter(reviewsOut)) {
            writer.append(String.valueOf(sentiment))
                  .append(" ")
                  .append(review)
                  .append(System.lineSeparator())
                  .flush();
        } catch (IOException e) {
            return false;
        }

        // Update the current information
        updateInfo(new Review(sentiment, extractWordsFromReview(review)));

        return true;
    }

    @Override
    public int getSentimentDictionarySize() {
        return wordsScore.size();
    }

    @Override
    public boolean isStopWord(String word) {
        validateString(word);
        return stopwords.contains(word);
    }

    private void readStopwords(Reader stopwordsIn) {
        try (var reader = new BufferedReader(stopwordsIn)) {
            stopwords = reader.lines().map(String::toLowerCase).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from the file", e);
        }
    }

    private void readReviews(Reader reviewsIn) {
        final List<Review> reviews;
        try (var reader = new BufferedReader(reviewsIn)) {
            reviews = reader.lines()
                    .map(String::toLowerCase)
                    .map(review -> Review.of(review, this.stopwords))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from the file", e);
        }

        calculateWordsScore(reviews);
    }

    private void calculateWordsScore(List<Review> reviews) {
        for (Review review : reviews) {
            updateInfo(review);
        }
    }

    private void updateInfo(Review review) {
        review.words().forEach(this::updateFrequency);
        review.words().stream().distinct().forEach(word -> updateScore(word, review.sentiment()));
    }

    private void updateFrequency(String word) {
        Integer currentFrequency = wordsOccurrences.get(word);

        if (currentFrequency == null) {
            currentFrequency = 0;
        }
        wordsOccurrences.put(word, currentFrequency + 1);
    }

    private void updateScore(String word, int sentiment) {
        Integer reviewsContainingThisWordCount = wordsOccurrencesWithoutRepeating.get(word);
        if (reviewsContainingThisWordCount == null) {
            reviewsContainingThisWordCount = 0;
        }

        wordsOccurrencesWithoutRepeating.put(word, reviewsContainingThisWordCount + 1);

        Double currentScore = wordsScore.get(word);
        if (currentScore == null) {
            currentScore = 0.0;
        }

        double newScore = (currentScore * reviewsContainingThisWordCount + sentiment) /
                       (reviewsContainingThisWordCount + 1);

        wordsScore.put(word, newScore);
    }

    private List<String> extractWordsFromReview(String review) {
        return Arrays.stream(review.split(DELIMITER_REGEX))
                .filter(str -> str.length() >= 2)
                .map(String::toLowerCase)
                .filter(Predicate.not(this::isStopWord))
                .toList();
    }

    private void validateNullObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("This object cannot be null");
        }
    }

    private void validateString(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException("The string cannot be null, empty or blank");
        }
    }

}
