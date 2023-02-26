package todoist.exception;

public class InvalidTimeIntervalException extends Exception {
    public InvalidTimeIntervalException(String message) {
        super(message);
    }

    public InvalidTimeIntervalException(String message, Throwable cause) {
        super(message, cause);
    }
}
