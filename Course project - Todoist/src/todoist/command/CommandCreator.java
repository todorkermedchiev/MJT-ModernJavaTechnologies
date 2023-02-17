package todoist.command;

import java.util.Arrays;

public class CommandCreator {
    private static final String DELIMITER = " --"; // \s+

    public static Command newCommand(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("The command string cannot be null, empty or blank");
        }

        String[] tokens = input.strip().split(DELIMITER);

        if (tokens.length == 0) {
            return new Command(CommandType.UNKNOWN);
        }

        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        return new Command(CommandType.getTypeByName(tokens[0].strip()), args);
    }
}
