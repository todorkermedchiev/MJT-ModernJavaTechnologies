package bg.sofia.uni.fmi.mjt.mail.exceptions;

public class InvalidEmailMetadataException extends RuntimeException {
    public InvalidEmailMetadataException(String message) {
        super(message);
    }

    public InvalidEmailMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
