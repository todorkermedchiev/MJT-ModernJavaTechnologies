package bg.sofia.uni.fmi.mjt.sentiment.review;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public record Review(int sentiment, List<String> words) {
    private static final String DELIMITER_REGEX = "[^\\w']+";

    public static Review of(String review, Set<String> stopwords) {
        final String[] tokens = review.split(DELIMITER_REGEX);
        int sentiment = Integer.parseInt(tokens[0]);

        List<String> words = Arrays.stream(tokens)
                .skip(1) // The first token is the sentiment
                .filter(str -> str.length() >= 2)
                .map(String::toLowerCase)
                .filter(Predicate.not(stopwords::contains))
                .toList();

        return new Review(sentiment, words);
    }
}
