package bg.sofia.uni.fmi.mjt.sentiment.exception;

public class SentimentTypeNotFoundException extends Exception {
    public SentimentTypeNotFoundException(String message) {
        super(message);
    }

    public SentimentTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
