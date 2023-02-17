package bg.sofia.uni.fmi.mjt.cocktail.server.command;

public record Command(CommandType type, String... arguments) {
}
