package bg.sofia.uni.fmi.mjt.sentiment;

import bg.sofia.uni.fmi.mjt.sentiment.exception.SentimentTypeNotFoundException;

public enum SentimentType {
    NEGATIVE(0, "negative"),
    SOMEWHAT_NEGATIVE(1, "somewhat negative"),
    NEUTRAL(2, "neutral"),
    SOMEWHAT_POSITIVE(3, "somewhat positive"),
    POSITIVE(4, "positive");

    public final int rating;
    public final String name;

    private SentimentType(int rating, String name) {
        this.rating = rating;
        this.name = name;
    }

    public static SentimentType getSentimentTypeByRating(int rating) throws SentimentTypeNotFoundException {
        SentimentType[] types = SentimentType.values();

        for (SentimentType type : types) {
            if (type.rating == rating) {
                return type;
            }
        }

        throw new SentimentTypeNotFoundException("Sentiment type with rating " + rating + " not found!");
    }
}
