package todoist.command;

import todoist.collaboration.Collaboration;
import todoist.exception.CollaborationAlreadyExistsException;
import todoist.exception.CollaborationNotFoundException;
import todoist.exception.InvalidCommandFormatException;
import todoist.exception.TaskAlreadyExistsException;
import todoist.exception.TaskNameAlreadyExistsException;
import todoist.exception.TaskNotFoundException;
import todoist.exception.UserAlreadyExistsException;
import todoist.exception.UserNotFoundException;
import todoist.exception.UserNotLoggedException;
import todoist.exception.WrongPasswordException;
import todoist.storage.Storage;
import todoist.task.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private static final int REGISTER_ARGUMENTS_COUNT = 2;
    private static final int LOGIN_ARGUMENTS_COUNT = 2;
    private static final int ADD_USER_ARGUMENTS_COUNT = 2;
    private static final int ASSIGN_TASK_ARGUMENTS_COUNT = 3;
    private static final int MIN_ARGUMENTS_COUNT = 1;

    private static final String KEY_VALUE_DELIMITER_REGEX = "=";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final int PARAMETER_TOKENS_COUNT = 2;

    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: command \"%s\" expects %s arguments.";
    private static final String INVALID_COMMAND_FORMAT_MESSAGE = "Invalid command format. "; // help

    private static final String HELP_MESSAGE = """
            Possible commands:
                << register --username=<username> --password=<password>
                << login --username=<username> --password=<password>
                << logout
                << add-task --name=<task name> --date=<date*> --due-date=<due-date*> --description=<description>
                << update-task --name=<task name> --date=<date*> --due-date=<due-date*> --description=<description>
                << delete-task --name=<task name>
                << delete-task --name=<task name> --date=<date*>
                << get-task --name=<task name>
                << get-task --name=<task name> --date=<date*>
                << list-tasks
                << list-tasks --completed=true
                << list-tasks --date=<date*>
                << list-tasks --collaboration=<collaboration name>
                << list-dashboard
                << finish-task --name=<name>
                << add-collaboration --name=<collaboration name>
                << delete-collaboration --name=<collaboration name>
                << list-collaborations
                << add-user --collaboration=<collaboration name> --user=<username>
                << assign-task --collaboration=<collaboration name> --user=<username> --task=<name>
                << assign-task --collaboration=<collaboration name> --user=<username> --task=<name> --date=<date*>
                << list-users --collaboration=<collaboration name>
                *date format: dd.MM.yyyy
            """; // todo
    private static final String UNKNOWN_COMMAND_MESSAGE = "Unknown command. Please enter valid command!";
    private static final String DISCONNECT_MESSAGE = "Disconnected from server.";
    private static final String RESULTS_SECTION_SEPARATOR = "##################################################";

    private final Storage storage;

    private final Map<Integer, String> loggedUsers;

    public CommandExecutor(Storage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null.");
        }

        this.storage = storage;
        this.loggedUsers = new HashMap<>();
    }

    public String execute(int clientId, Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        return switch (command.type()) {
            case REGISTER -> register(command.arguments());
            case LOGIN -> login(clientId, command.arguments());
            case LOGOUT -> logout(clientId);
            case ADD_TASK -> addTask(clientId, command.arguments());
            case UPDATE_TASK -> updateTask(clientId, command.arguments());
            case DELETE_TASK -> deleteTask(clientId, command.arguments());
            case GET_TASK -> getTask(clientId, command.arguments());
            case LIST_TASKS -> listTasks(clientId, command.arguments());
            case LIST_DASHBOARD -> listDashboard(clientId);
            case FINISH_TASK -> finishTask(clientId, command.arguments());
            case ADD_COLLABORATION -> addCollaboration(clientId, command.arguments());
            case DELETE_COLLABORATION -> deleteCollaboration(clientId, command.arguments());
            case LIST_COLLABORATIONS -> listCollaborations(clientId);
            case ADD_USER -> addUserToCollaboration(clientId, command.arguments());
            case ASSIGN_TASK -> assignTask(clientId, command.arguments());
            case LIST_USERS -> listUsers(clientId, command.arguments());
            case DISCONNECT -> disconnect(clientId);
            case HELP -> HELP_MESSAGE;
            case UNKNOWN -> UNKNOWN_COMMAND_MESSAGE;
        };
    }

    private String register(String... arguments) {
        if (arguments.length != REGISTER_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "register", REGISTER_ARGUMENTS_COUNT);
        }

        String username;
        String password;

        try {
            username = parseArgument(ParameterName.USERNAME, arguments);
            password = parseArgument(ParameterName.PASSWORD, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE;
        }

        try {
            storage.addUser(username, password);
            return String.format("User \"%s\" added successfully!", username);
        } catch (UserAlreadyExistsException e) {
            return "User cannot be added. " + e.getMessage();
        }
    }

    private String login(int clientId, String... arguments) {
        if (arguments.length != LOGIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "login", LOGIN_ARGUMENTS_COUNT);
        }

        String username;
        String password;

        try {
            username = parseArgument(ParameterName.USERNAME, arguments);
            password = parseArgument(ParameterName.PASSWORD, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE;
        }

        try {
            if (loggedUsers.containsKey(clientId)) {
                return "There is already another logged user. Please log out first.";
            }
            storage.login(username, password);
            loggedUsers.put(clientId, username);
            return String.format("User \"%s\" logged successfully!", username);
        } catch (UserNotFoundException | WrongPasswordException e) {
            return "Cannot log in. " + e.getMessage();
        }
    }

    private String logout(int clientId) {
        try {
            String currentUser = getCurrentUser(clientId);
            loggedUsers.remove(clientId);
            return "User \"" + currentUser + "\" successfully logged out.";
        } catch (UserNotLoggedException e) {
            return "User cannot be logged out. " + e.getMessage();
        }
    }

    private String addTask(int clientId, String... arguments) {
        if (arguments.length < MIN_ARGUMENTS_COUNT) { // todo MAX_ARGUMENTS_COUNT
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-task",
                    "at least " + MIN_ARGUMENTS_COUNT);
        }

        Task task;

        try {
            task = parseTask(arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        try {
            storage.addTask(getCurrentUser(clientId), task);
            return String.format("Task \"%s\" successfully added!", task.getName());
        } catch (UserNotLoggedException | TaskNameAlreadyExistsException | UserNotFoundException e) {
            return "Task cannot be added. " + e.getMessage();
        }
    }

    private String updateTask(int clientId, String... arguments) {
        if (arguments.length < MIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "update-task",
                    "at least " + MIN_ARGUMENTS_COUNT);
        }

        Task newTask;
        try {
            newTask = parseTask(arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        try {
            storage.updateTask(getCurrentUser(clientId), newTask);
            return String.format("Task \"%s\" successfully updated!", newTask.getName());
        } catch (UserNotLoggedException | TaskNotFoundException | UserNotFoundException e) {
            return "Task cannot be updated. " + e.getMessage();
        }
    }

    private Task parseTask(String... arguments) throws InvalidCommandFormatException {
        String name = parseArgument(ParameterName.NAME, arguments);
        LocalDate date = parseDate(ParameterName.DATE, arguments);
        LocalDate dueDate = parseDate(ParameterName.DUE_DATE, arguments);
        String description = parseArgument(ParameterName.DESCRIPTION, arguments);

        if (name == null || name.isBlank()) {
            throw new InvalidCommandFormatException("\"name\" parameter not found.");
        }

        Task.TaskBuilder builder = Task.builder(name);

        if (date != null) {
            builder.setDate(date);
        }
        if (dueDate != null) {
            builder.setDueDate(dueDate);
        }
        if (description != null && !description.isBlank()) {
            builder.setDescription(description);
        }

        return builder.build();
    }

    private String deleteTask(int clientId, String... arguments) {
        if (arguments.length < MIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "delete-task",
                    "at least " + MIN_ARGUMENTS_COUNT);
        }

        String taskName;
        LocalDate date;

        try {
            taskName = parseArgument(ParameterName.NAME, arguments);
            date = parseDate(ParameterName.DATE, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (taskName == null || taskName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" parameter not found.";
        }

        try {
            String currentUser = getCurrentUser(clientId);

            if (date == null) {
                storage.deleteTask(currentUser, taskName);
            } else {
                storage.deleteTask(currentUser, taskName, date);
            }

            return String.format("Task \"%s\" deleted successfully!", taskName);

        } catch (UserNotLoggedException | TaskNotFoundException | UserNotFoundException e) {
            return "Task cannot be deleted. " + e.getMessage();
        }
    }

    private String getTask(int clientId, String... arguments) {
        if (arguments.length < MIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "get-task",
                    "at least " + MIN_ARGUMENTS_COUNT);
        }

        String taskName;
        LocalDate date;

        try {
            taskName = parseArgument(ParameterName.NAME, arguments);
            date = parseDate(ParameterName.DATE, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (taskName == null || taskName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" parameter not found."; // todo constant
        }

        try {
            String currentUser = getCurrentUser(clientId);
            Task task;

            if (date == null) {
                task = storage.getTask(currentUser, taskName);
            } else {
                task = storage.getTask(currentUser, taskName, date);
            }

            return task.toString();

        } catch (UserNotLoggedException | TaskNotFoundException | UserNotFoundException e) {
            return "Task cannot be shown. " + e.getMessage();
        }
    }

    private String listTasks(int clientId, String... arguments) {
        boolean completed;
        LocalDate date;
        String collaborationName;

        try {
            completed = Boolean.parseBoolean(parseArgument(ParameterName.COMPLETED, arguments));
            date = parseDate(ParameterName.DATE, arguments);
            collaborationName = parseArgument(ParameterName.COLLABORATION, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        boolean dateIsSet = date != null;
        boolean collaborationIsSet = collaborationName != null && !collaborationName.isBlank();

        Collection<Task> tasks;

        try {
            String currentUser = getCurrentUser(clientId);

            if (!completed && !dateIsSet && !collaborationIsSet) {
                tasks = storage.listTasks(currentUser);

            } else if (completed && !dateIsSet && !collaborationIsSet) {
                tasks = storage.listCompletedTasks(currentUser);

            } else if (!completed && dateIsSet && !collaborationIsSet) {
                tasks = storage.listTasks(currentUser, date);

            } else if (!completed && !dateIsSet && collaborationIsSet) {
                tasks = storage.listTasks(currentUser, collaborationName);

            } else {
                return INVALID_COMMAND_FORMAT_MESSAGE + "There are more than one set properties.";
            }
        } catch (UserNotLoggedException | TaskNotFoundException | CollaborationNotFoundException |
                 UserNotFoundException e) {

            return "Tasks cannot be listed. " + e.getMessage();
        }

        if (tasks.isEmpty()) {
            return "No tasks found!";
        }

        StringBuilder response = new StringBuilder(RESULTS_SECTION_SEPARATOR + System.lineSeparator());
        tasks.forEach(response::append);
        response.append(RESULTS_SECTION_SEPARATOR).append(System.lineSeparator());

        return response.toString();
    }

    private String listDashboard(int clientId) {
        Collection<Task> tasks;

        try {
            tasks = storage.listDashboard(getCurrentUser(clientId));
        } catch (UserNotLoggedException | TaskNotFoundException | UserNotFoundException e) {
            return "No tasks found. " + e.getMessage();
        }

        StringBuilder response = new StringBuilder(RESULTS_SECTION_SEPARATOR + System.lineSeparator());
        tasks.forEach(response::append);
        response.append(RESULTS_SECTION_SEPARATOR).append(System.lineSeparator());

        return response.toString();
    }

    private String finishTask(int clientId, String... arguments) {
        if (arguments.length != MIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "finish-task", MIN_ARGUMENTS_COUNT);
        }

        String name;

        try {
            name = parseArgument(ParameterName.NAME, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (name == null || name.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" argument not found";
        }

        try {
            storage.finishTask(getCurrentUser(clientId), name);
            return String.format("Task \"%s\" finished successfully!", name);
        } catch (UserNotLoggedException | TaskNotFoundException | UserNotFoundException e) {
            return "Task cannot be finished. " + e.getMessage();
        }
    }

    private String addCollaboration(int clientId, String... arguments) {
        if (arguments.length != MIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-collaboration", MIN_ARGUMENTS_COUNT);
        }

        String collaborationName;

        try {
            collaborationName = parseArgument(ParameterName.NAME, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (collaborationName == null || collaborationName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration name parameter is missing.";
        }

        try {
            storage.addCollaboration(getCurrentUser(clientId), collaborationName);
            return "Collaboration \"" + collaborationName + "\" added successfully";
        } catch (UserNotLoggedException | CollaborationAlreadyExistsException | UserNotFoundException e) {
            return "Collaboration cannot be created. " + e.getMessage();
        }
    }

    private String deleteCollaboration(int clientId, String... arguments) {
        if (arguments.length != MIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "delete-collaboration", MIN_ARGUMENTS_COUNT);
        }

        String collaborationName;

        try {
            collaborationName = parseArgument(ParameterName.COLLABORATION, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (collaborationName == null || collaborationName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration name parameter is missing.";
        }

        try {
            storage.deleteCollaboration(getCurrentUser(clientId), collaborationName);
            return "Collaboration \"" + collaborationName + "\" deleted successfully";
        } catch (UserNotLoggedException | CollaborationNotFoundException | UserNotFoundException e) {
            return "Collaboration cannot be deleted. " + e.getMessage();
        }
    }

    private String listCollaborations(int clientId) {
        StringBuilder response = new StringBuilder(RESULTS_SECTION_SEPARATOR).append(System.lineSeparator());

        try {
            Collection<Collaboration> collaborations = storage.getCollaborations(getCurrentUser(clientId));
            if (collaborations.isEmpty()) {
                return "No collaborations found!";
            }

            collaborations.forEach(coll -> response.append(coll.getName()).append(System.lineSeparator()));
            response.append(RESULTS_SECTION_SEPARATOR).append(System.lineSeparator());

            return response.toString();

        } catch (UserNotLoggedException | UserNotFoundException e) {
            return "Cannot list collaborations. " + e.getMessage();
        }
    }

    private String addUserToCollaboration(int clientId, String... arguments) {
        if (arguments.length != ADD_USER_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-user", ADD_USER_ARGUMENTS_COUNT);
        }

        String collaborationName;
        String username;

        try {
            collaborationName = parseArgument(ParameterName.COLLABORATION, arguments);
            username = parseArgument(ParameterName.USER, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (collaborationName == null || collaborationName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration parameter not found.";
        }
        if (username == null || username.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "Username parameter not found.";
        }

        try {
            storage.addUserToCollaboration(getCurrentUser(clientId), collaborationName, username);
            return String.format("User \"%s\" successfully added in collaboration \"%s\".", username,
                    collaborationName);

        } catch (UserNotLoggedException | CollaborationNotFoundException | UserNotFoundException |
                 UserAlreadyExistsException  e) {
            return "Cannot add user to collaboration. " + e.getMessage();
        }
    }

    private String assignTask(int clientId, String... arguments) {
        if (arguments.length < ASSIGN_TASK_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "assign-task",
                    "at least " + ASSIGN_TASK_ARGUMENTS_COUNT);
        }

        String collaborationName;
        String username;
        String taskName;
        LocalDate date;

        try {
            collaborationName = parseArgument(ParameterName.COLLABORATION, arguments);
            username = parseArgument(ParameterName.USER, arguments);
            taskName = parseArgument(ParameterName.TASK, arguments);
            date = parseDate(ParameterName.DATE, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (collaborationName == null || collaborationName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration parameter not found.";
        }
        if (username == null || username.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "User parameter not found.";
        }
        if (taskName == null || taskName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "Task parameter not found.";
        }

        try {
            String currentUser = getCurrentUser(clientId);

            if (date == null) {
                storage.assignTask(currentUser, collaborationName, username, taskName);
            } else {
                storage.assignTask(currentUser, collaborationName, username, taskName, date);
            }

            return String.format("Task \"%s\" successfully assigned with user \"%s\".", taskName, username);

        } catch (UserNotLoggedException | CollaborationNotFoundException | UserNotFoundException |
                 TaskNotFoundException | TaskAlreadyExistsException e) {
            return "Cannot assign task. " + e.getMessage();
        }
    }

    private String listUsers(int clientId, String... arguments) {
        if (arguments.length != MIN_ARGUMENTS_COUNT) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "list-users", MIN_ARGUMENTS_COUNT);
        }

        String collaborationName;

        try {
            collaborationName = parseArgument(ParameterName.COLLABORATION, arguments);
        } catch (InvalidCommandFormatException e) {
            return INVALID_COMMAND_FORMAT_MESSAGE + e.getMessage();
        }

        if (collaborationName == null || collaborationName.isBlank()) {
            return INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration name parameter is missing.";
        }

        try {
            Collection<String> users = storage.listUsersInCollaboration(getCurrentUser(clientId), collaborationName);
            if (users.isEmpty()) {
                return "No users found in this collaboration.";
            }

            StringBuilder response = new StringBuilder(RESULTS_SECTION_SEPARATOR).append(System.lineSeparator());
            users.forEach(user -> response.append(user).append(System.lineSeparator()));
            response.append(RESULTS_SECTION_SEPARATOR).append(System.lineSeparator());

            return response.toString();

        } catch (UserNotLoggedException | CollaborationNotFoundException | UserNotFoundException e) {
            return "Cannot list users in this collaboration. " + e.getMessage();
        }
    }

    private String disconnect(int clientId) {
        logout(clientId);
        return DISCONNECT_MESSAGE;
    }

    private String parseArgument(ParameterName parameterName, String... arguments)
            throws InvalidCommandFormatException {

        String argument = null;

        for (String currentArg : arguments) {
            String[] argumentTokens = currentArg.strip().split(KEY_VALUE_DELIMITER_REGEX);
            if (argumentTokens.length != PARAMETER_TOKENS_COUNT) {
                throw new InvalidCommandFormatException("Command expected in \"key=value\" format");
            }

            if (argumentTokens[0].equalsIgnoreCase(parameterName.name)) {
                argument = argumentTokens[1];
            }
        }

        return argument;
    }

    private LocalDate parseDate(ParameterName parameterName, String... arguments) throws InvalidCommandFormatException {
        String stringDate = parseArgument(parameterName, arguments);
        LocalDate date = null;

        if (stringDate != null) {
            try {
                date = LocalDate.parse(stringDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
            } catch (DateTimeParseException e) {
                throw new InvalidCommandFormatException("Unknown date format for the date provided.", e);
            }
        }

        return date;
    }

    private String getCurrentUser(int clientId) throws UserNotLoggedException {
        if (!loggedUsers.containsKey(clientId)) {
            throw new UserNotLoggedException("There is no logged user.");
        }

        return loggedUsers.get(clientId);
    }
}
