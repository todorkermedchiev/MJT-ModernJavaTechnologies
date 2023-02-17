package news.exception;

public class KeywordsNotProvidedException extends RuntimeException {
    public KeywordsNotProvidedException(String message) {
        super(message);
    }

    public KeywordsNotProvidedException(String message, Throwable cause) {
        super(message, cause);
    }
}
