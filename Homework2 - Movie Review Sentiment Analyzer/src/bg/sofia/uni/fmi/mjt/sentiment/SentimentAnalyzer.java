package bg.sofia.uni.fmi.mjt.sentiment;

import java.util.List;

public interface SentimentAnalyzer {

    /**
     * @param review the text of the review
     * @return the review sentiment as a floating-point number in the interval [0.0,
     * 4.0] if known, and -1.0 if unknown.
     */
    double getReviewSentiment(String review);

    /**
     * @param review the text of the review
     * @return the review sentiment as a name: "negative", "somewhat negative",
     * "neutral", "somewhat positive", "positive" or "unknown".
     * As the review sentiment is a floating-point number, the sentiment name is defined
     * by the integer number closest to it, i.e. the sentiment is rounded to integer.
     */
    String getReviewSentimentAsName(String review);

    /**
     * @param word
     * @return the review sentiment of the word as a floating-point number in the
     * interval [0.0, 4.0] if known, and -1.0 if unknown. The word is considered case-insensitive.
     */
    double getWordSentiment(String word);

    /**
     * @param word
     * @return the number of all occurrences of the word in all reviews. Repeating occurrences of the word
     * in the same review are also counted for the frequency. The word is considered case-insensitive.
     * If {@code word} is a stopword, the result is undefined.
     */
    int getWordFrequency(String word);

    /**
     * Returns a list of the n most frequent words found in the reviews, sorted by frequency in decreasing order.
     * Repeating occurrences of a word in the same review are also counted for the frequency.
     * Words are considered case-insensitive.
     * Stopwords are ignored and should not be included in the result.
     *
     * @throws {@link IllegalArgumentException}, if n is negative
     */
    List<String> getMostFrequentWords(int n);

    /**
     * Returns a list of the n most positive words in the reviews, sorted by sentiment score in decreasing order.
     * Words are considered case-insensitive.
     *
     * @throws {@link IllegalArgumentException}, if n is negative
     */
    List<String> getMostPositiveWords(int n);

    /**
     * Returns a list of the n most negative words in the reviews, sorted by sentiment score in ascending order.
     * Words are considered case-insensitive.
     *
     * @throws {@link IllegalArgumentException}, if n is negative
     */
    List<String> getMostNegativeWords(int n);

    /**
     * Appends a review to the end of the data set.
     * Any information from the data set stored in memory should be automatically updated.
     *
     * @param review    The text part of the review. Note that it does not include the rating of the review.
     * @param sentiment the given rating
     * @return true if the operation was successful and false if an issue has occurred and the review is not stored
     * @throws {@link IllegalArgumentException}, if review is null, empty or blank,
     *                or if the sentiment is not in the [0.0, 4.0] range
     */
    boolean appendReview(String review, int sentiment);

    /**
     * Returns the total number of words with known sentiment score
     */
    int getSentimentDictionarySize();

    /**
     * Returns whether a word is a stopword. The word is considered case-insensitive.
     */
    boolean isStopWord(String word);

}