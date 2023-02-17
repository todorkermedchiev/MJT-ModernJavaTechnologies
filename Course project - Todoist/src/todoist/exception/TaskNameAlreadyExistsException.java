package todoist.exception;

public class TaskNameAlreadyExistsException extends Exception {
    public TaskNameAlreadyExistsException(String message) {
        super(message);
    }

    public TaskNameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
