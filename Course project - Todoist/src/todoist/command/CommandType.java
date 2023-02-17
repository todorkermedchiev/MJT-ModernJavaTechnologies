package todoist.command;

public enum CommandType {
    REGISTER("register"),
    LOGIN("login"),
    LOGOUT("logout"),
    ADD_TASK("add-task"),
    UPDATE_TASK("update-task"),
    DELETE_TASK("delete-task"),
    GET_TASK("get-task"),
    LIST_TASKS("list-tasks"),
    LIST_DASHBOARD("list-dashboard"),
    FINISH_TASK("finish-task"),

    ADD_COLLABORATION("add-collaboration"),
    DELETE_COLLABORATION("delete-collaboration"),
    LIST_COLLABORATIONS("list-collaborations"),
    ADD_USER("add-user"),
    ASSIGN_TASK("assign-task"),
    LIST_USERS("list-users"),
    HELP("help"),
    DISCONNECT("disconnect"),

    UNKNOWN("unknown");

    public final String name;

    private CommandType(String name) {
        this.name = name;
    }

    public static CommandType getTypeByName(String name) {
        CommandType[] types = CommandType.values();
        for (CommandType currentType : types) {
            if (currentType.name.equalsIgnoreCase(name)) {
                return currentType;
            }
        }

        return UNKNOWN;
    }
}
