package todoist.exception;

public class InvalidCommandFormatException extends Exception {
    public InvalidCommandFormatException(String message) {
        super(message);
    }

    public InvalidCommandFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
