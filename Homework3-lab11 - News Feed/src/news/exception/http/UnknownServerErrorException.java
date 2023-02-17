package news.exception.http;

public class UnknownServerErrorException extends RuntimeException {
    public UnknownServerErrorException(String message) {
        super(message);
    }

    public UnknownServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
