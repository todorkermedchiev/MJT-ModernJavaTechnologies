package news.exception;

public class NewsFeedClientException extends RuntimeException {
    public NewsFeedClientException(String message) {
        super(message);
    }

    public NewsFeedClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
