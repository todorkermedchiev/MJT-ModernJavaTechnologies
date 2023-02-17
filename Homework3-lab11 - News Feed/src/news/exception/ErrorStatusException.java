package news.exception;

public class ErrorStatusException extends RuntimeException {
    public ErrorStatusException(String message) {
        super(message);
    }

    public ErrorStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
