package news.exception.http;

public class ApiKeyIsMissingException extends RuntimeException {
    public ApiKeyIsMissingException(String message) {
        super(message);
    }

    public ApiKeyIsMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
