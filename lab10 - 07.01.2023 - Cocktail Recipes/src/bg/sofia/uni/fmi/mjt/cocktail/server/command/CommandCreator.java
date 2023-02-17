package bg.sofia.uni.fmi.mjt.cocktail.server.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandCreator {

    private static final String CREATE = "create";
    private static final String GET = "get";
    private static final String GET_ALL = "all";
    private static final String GET_BY_NAME = "by-name";
    private static final String GET_BY_INGREDIENT = "by-ingredient";
    private static final String DISCONNECT = "disconnect";

    private static final String WHITESPACE_DELIMITER = "\s+";

    public static Command newCommand(String input) {
        String[] tokens = input.toLowerCase().split(WHITESPACE_DELIMITER);
        if (tokens.length == 0) {
            return new Command(CommandType.UNKNOWN);
        }

        String command = tokens[0];

        if (command.equalsIgnoreCase(CREATE)) {
            String[] arguments = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (Arrays.stream(arguments).skip(1).allMatch(arg -> arg.split("=").length == 2)) {
                return new Command(CommandType.CREATE, arguments);
            } else {
                return new Command(CommandType.UNKNOWN);
            }
        }

        if (command.equalsIgnoreCase(GET)) {
            if (tokens.length == 1) {
                return new Command(CommandType.UNKNOWN);
            }
            String getType = tokens[1];

            return switch (getType) {
                case GET_ALL -> new Command(CommandType.GET_ALL);
                case GET_BY_NAME -> new Command(CommandType.GET_BY_NAME,
                        Arrays.copyOfRange(tokens, 2, tokens.length));
                case GET_BY_INGREDIENT -> new Command(CommandType.GET_BY_INGREDIENT,
                        Arrays.copyOfRange(tokens, 2, tokens.length));
                default -> new Command(CommandType.UNKNOWN);
            };
        }

        if (command.equalsIgnoreCase(DISCONNECT)) {
            return new Command(CommandType.DISCONNECT);
        }

        return new Command(CommandType.UNKNOWN);
    }
}
