package todoist.command;

public record Command(CommandType type, String... arguments) {
}
