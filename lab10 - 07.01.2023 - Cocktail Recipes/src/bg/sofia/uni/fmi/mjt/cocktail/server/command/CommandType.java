package bg.sofia.uni.fmi.mjt.cocktail.server.command;

public enum CommandType {
    CREATE("create"),
    GET_ALL("get all"),
    GET_BY_NAME("get by-name"),
    GET_BY_INGREDIENT("get by-ingredient"),
    DISCONNECT("disconnect"),
    UNKNOWN("unknown");

    public final String name;

    private CommandType(String name) {
        this.name = name;
    }
}
