package todoist.command;

public enum ParameterName {
    USERNAME("username"),
    PASSWORD("password"),
    NAME("name"),
    DATE("date"),
    DUE_DATE("due-date"),
    DESCRIPTION("description"),
    COMPLETED("completed"),
    COLLABORATION("collaboration"),
    USER("user"),
    TASK("task");

    public final String name;

    private ParameterName(String name) {
        this.name = name;
    }
}
