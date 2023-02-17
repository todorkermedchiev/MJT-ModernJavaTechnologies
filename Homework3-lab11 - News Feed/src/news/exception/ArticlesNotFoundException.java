package news.exception;

public class ArticlesNotFoundException extends RuntimeException {
    public ArticlesNotFoundException(String message) {
        super(message);
    }

    public ArticlesNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
