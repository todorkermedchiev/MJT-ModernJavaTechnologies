package todoist.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import todoist.collaboration.Collaboration;
import todoist.exception.CollaborationAlreadyExistsException;
import todoist.exception.CollaborationNotFoundException;
import todoist.exception.TaskAlreadyExistsException;
import todoist.exception.TaskNameAlreadyExistsException;
import todoist.exception.TaskNotFoundException;
import todoist.exception.UserAlreadyExistsException;
import todoist.exception.UserNotFoundException;
import todoist.exception.WrongPasswordException;
import todoist.storage.Storage;
import todoist.task.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandExecutorTest {
    private static final String UNKNOWN_COMMAND_MESSAGE = "Unknown command. Please enter valid command!";
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: command \"%s\" expects %s arguments.";
    private static final String INVALID_COMMAND_FORMAT_MESSAGE = "Invalid command format. ";
    private static final String RESULTS_SECTION_SEPARATOR = "##################################################";

    private static final Command LOGIN_COMMAND = CommandCreator.newCommand("login --username=username " +
            "--password=password");

    @Mock
    private Storage storageMock;

    @InjectMocks
    private CommandExecutor executor;

    @Test
    void testExecuteNullCommand() {
        assertThrows(IllegalArgumentException.class, () -> executor.execute(0, null),
                "Unexpected response returned when command is null. Expected IllegalArgumentException to be thrown");
    }

    @Test
    void testExecuteUnknownCommand() {
        Command cmd = CommandCreator.newCommand("command");
        String response = executor.execute(0, cmd);

        assertEquals(UNKNOWN_COMMAND_MESSAGE, response, "Unexpected response returned when command is unknown.");
    }

    @Test
    void testRegisterLessArguments() {
        Command cmd = CommandCreator.newCommand("register --username=username");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "register", 2), response,
                "Unexpected response returned when arguments count is not as needed.");
    }

    @Test
    void testRegisterMoreArguments() {
        Command cmd = CommandCreator.newCommand("register --arg1=val1 --arg2=val2 --arg3=val3");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "register", 2), response,
                "Unexpected response returned when arguments count is not as needed.");
    }

    @Test
    void testRegisterMissingArgument() {
        Command cmd = CommandCreator.newCommand("register --username=username --argument=value");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE, response,
                "Unexpected response returned when an argument is missing.");
    }

    @Test
    void testRegisterInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("register --username: username --password-password");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when arguments are in invalid formant.");
    }

    @Test
    void testRegisterExistingUser() throws UserAlreadyExistsException {
        doThrow(new UserAlreadyExistsException("User already exists."))
                .when(storageMock).addUser("username", "password");

        Command cmd = CommandCreator.newCommand("register --username=username --password=password");
        String response = executor.execute(0, cmd);

        assertEquals("User cannot be added. User already exists.", response,
                "Unexpected response returned when user exists.");

        verify(storageMock).addUser("username", "password");
    }

    @Test
    void testRegisterNewUser() throws UserAlreadyExistsException {
        doNothing().when(storageMock).addUser("username", "password");

        Command cmd = CommandCreator.newCommand("register --username=username --password=password");
        String response = executor.execute(0, cmd);

        assertEquals("User \"username\" added successfully!", response,
                "Unexpected response returned when user does not exists by now.");

        verify(storageMock).addUser("username", "password");
    }

    @Test
    void testLoginLessArguments() {
        Command cmd = CommandCreator.newCommand("login --username=username");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "login", 2), response,
                "Unexpected response returned when arguments count is not as needed.");
    }

    @Test
    void testLoginMoreArguments() {
        Command cmd = CommandCreator.newCommand("login --arg1=val1 --arg2=val2 --arg3=val3");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "login", 2), response,
                "Unexpected response returned when arguments count is not as needed.");
    }

    @Test
    void testLoginMissingArgument() {
        Command cmd = CommandCreator.newCommand("login --username=username --argument=value");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE, response,
                "Unexpected response returned when an argument is missing.");
    }

    @Test
    void testLoginInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("login --username: username --password-password");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when arguments are in invalid formant.");
    }

    @Test
    void testLoginMissingUser() throws UserNotFoundException, WrongPasswordException {
        doThrow(new UserNotFoundException("User not found."))
                .when(storageMock).login("username", "password");

        Command cmd = CommandCreator.newCommand("login --username=username --password=password");
        String response = executor.execute(0, cmd);

        assertEquals("Cannot log in. User not found.", response,
                "Unexpected response returned when user is not found.");

        verify(storageMock).login("username", "password");
    }

    @Test
    void testLoginWrongPassword() throws UserNotFoundException, WrongPasswordException {
        doThrow(new WrongPasswordException("Wrong password."))
                .when(storageMock).login("username", "password");

        Command cmd = CommandCreator.newCommand("login --username=username --password=password");
        String response = executor.execute(0, cmd);

        assertEquals("Cannot log in. Wrong password.", response,
                "Unexpected response returned when the password is wrong.");

        verify(storageMock).login("username", "password");
    }

    @Test
    void testLoginAlreadyLoggedUser() {
        Command cmd = CommandCreator.newCommand("login --username=username --password=password");
        executor.execute(0, cmd);
        String response = executor.execute(0, cmd);

        assertEquals("There is already another logged user. Please log out first.", response,
                "Unexpected response returned when there is another logged user.");
    }

    @Test
    void testLoginExistingUser() throws UserNotFoundException, WrongPasswordException {
        doNothing().when(storageMock).login("username", "password");

        Command cmd = CommandCreator.newCommand("login --username=username --password=password");
        String response = executor.execute(0, cmd);

        assertEquals("User \"username\" logged successfully!", response,
                "Unexpected response returned when user does not exists by now.");

        verify(storageMock).login("username", "password");
    }

    @Test
    void testLogoutUserNotLogged() {
        Command cmd = CommandCreator.newCommand("logout");
        String response = executor.execute(0, cmd);

        assertEquals("User cannot be logged out. There is no logged user.", response,
                "Unexpected response returned when there is not logged user");
    }

    @Test
    void testLogoutLoggedUser() throws UserNotFoundException, WrongPasswordException {
        doNothing().when(storageMock).login("username", "password");

        Command loginCmd = CommandCreator.newCommand("login --username=username --password=password");
        Command logoutCmd = CommandCreator.newCommand("logout");

        executor.execute(0, loginCmd);
        String response = executor.execute(0, logoutCmd);

        assertEquals("User \"username\" successfully logged out.", response,
                "Unexpected response returned when try to logout logged user. Expected success message");

        verify(storageMock).login("username", "password");
    }

    @Test
    void testAddTaskLessArguments() {
        Command cmd = CommandCreator.newCommand("add-task");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-task", "at least 1"), response,
                "Unexpected response returned when addTask() is called and argument count is less than required");
    }

    @Test
    void testAddTaskInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("add-task --name: task1");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when addTask() is called and key-value format is invalid");
    }

    @Test
    void testAddTaskNameArgumentMissing() {
        Command cmd = CommandCreator.newCommand("add-task --argument=value");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" parameter not found.", response,
                "Unexpected response returned when addTask() is called and name argument is missing");
    }

    @Test
    void testAddTaskWithDateInvalidDateFormat() {
        Command cmd = CommandCreator.newCommand("add-task --name=task --date=12/02/23");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Unknown date format for the date provided.", response,
                "Unexpected response returned when addTask() is called and date is in invalid format");
    }

    @Test
    void testAddTaskWhenUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("add-task --name=task --date=12.02.2023");
        String response = executor.execute(0, cmd);

        assertEquals("Task cannot be added. There is no logged user.", response,
                "Unexpected response returned when addTask() is called and there is no logged user.");
    }

    @Test
    void testAddTaskWhenUserIsLoggedAndTaskAlreadyExists()
            throws TaskNameAlreadyExistsException, UserNotFoundException, WrongPasswordException {

        Task task = Task.builder("task").build();

        doThrow(new TaskNameAlreadyExistsException("Task already exists."))
                .when(storageMock).addTask("username", task);
        doNothing().when(storageMock).login("username", "password");

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command addTaskCommand = CommandCreator.newCommand("add-task --name=task");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, addTaskCommand);

        assertEquals("Task cannot be added. Task already exists.", response,
                "Unexpected response returned when addTask() is called and task with this name and date already exists");

        verify(storageMock).addTask("username", task);
        verify(storageMock).login("username", "password");
    }

    @Test
    void testAddTaskWithDateWhenUserIsLoggedAndTaskAlreadyExists()
            throws TaskNameAlreadyExistsException, UserNotFoundException, WrongPasswordException {

        Task task = Task.builder("task").setDate(LocalDate.parse("2023-02-12")).build();

        doThrow(new TaskNameAlreadyExistsException("Task already exists."))
                .when(storageMock).addTask("username", task);
        doNothing().when(storageMock).login("username", "password");

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command addTaskCommand = CommandCreator.newCommand("add-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, addTaskCommand);

        assertEquals("Task cannot be added. Task already exists.", response,
                "Unexpected response returned when addTask() is called and task with this name and date already exists");

        verify(storageMock).addTask("username", task);
        verify(storageMock).login("username", "password");
    }

    @Test
    void testAddTaskWithDateWhenTaskSuccessfullyAdded()
            throws TaskNameAlreadyExistsException, UserNotFoundException, WrongPasswordException {

        Task task = Task.builder("task").setDate(LocalDate.parse("2023-02-12")).build();

        doNothing().when(storageMock).addTask("username", task);
        doNothing().when(storageMock).login("username", "password");

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command addTaskCommand = CommandCreator.newCommand("add-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, addTaskCommand);

        assertEquals(String.format("Task \"%s\" successfully added!", task.getName()), response,
                "Unexpected response returned when addTask() is called for valid task");

        verify(storageMock).addTask("username", task);
        verify(storageMock).login("username", "password");
    }

    @Test
    void testUpdateTaskLessArguments() {
        Command cmd = CommandCreator.newCommand("update-task");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "update-task", "at least 1"), response,
                "Unexpected response returned when updateTask() is called and argument count is less than required");
    }

    @Test
    void testUpdateTaskInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("update-task --name: task1");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when updateTask() is called and key-value format is invalid");
    }

    @Test
    void testUpdateTaskNameArgumentMissing() {
        Command cmd = CommandCreator.newCommand("update-task --argument=value");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" parameter not found.", response,
                "Unexpected response returned when updateTask() is called and name argument is missing");
    }

    @Test
    void testUpdateTaskWithDateInvalidDateFormat() {
        Command cmd = CommandCreator.newCommand("update-task --name=task --date=12/02/23");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Unknown date format for the date provided.", response,
                "Unexpected response returned when updateTask() is called and date is in invalid format");
    }

    @Test
    void testUpdateTaskWhenUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("update-task --name=task --date=12.02.2023");
        String response = executor.execute(0, cmd);

        assertEquals("Task cannot be updated. There is no logged user.", response,
                "Unexpected response returned when updateTask() is called and there is no logged user.");
    }

    @Test
    void testUpdateTaskWhenUserIsLoggedAndTaskDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        Task task = Task.builder("task").build();

        doThrow(new TaskNotFoundException("Task not found."))
                .when(storageMock).updateTask("username", task);
        doNothing().when(storageMock).login("username", "password");

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command updateTaskCommand = CommandCreator.newCommand("update-task --name=task");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, updateTaskCommand);

        assertEquals("Task cannot be updated. Task not found.", response,
                "Unexpected response returned when updateTask() is called and task with this name and date does not exist");

        verify(storageMock).updateTask("username", task);
        verify(storageMock).login("username", "password");
    }

    @Test
    void testUpdateTaskWithDateWhenUserIsLoggedAndTaskDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        Task task = Task.builder("task").setDate(LocalDate.parse("2023-02-12")).build();

        doThrow(new TaskNotFoundException("Task not found."))
                .when(storageMock).updateTask("username", task);

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command updateTaskCommand = CommandCreator.newCommand("update-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, updateTaskCommand);

        assertEquals("Task cannot be updated. Task not found.", response,
                "Unexpected response returned when updateTask() is called and task with this name and date does not exist");

        verify(storageMock).updateTask("username", task);
        verify(storageMock).login("username", "password");
    }

    @Test
    void testUpdateTaskWhenTaskSuccessfullyUpdated()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        Task task = Task.builder("task").setDate(LocalDate.parse("2023-02-12")).build();

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command updateTaskCommand = CommandCreator.newCommand("update-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, updateTaskCommand);

        assertEquals(String.format("Task \"%s\" successfully updated!", task.getName()), response,
                "Unexpected response returned when updateTask() is called for valid task");

        verify(storageMock).updateTask("username", task);
        verify(storageMock).login("username", "password");
    }

    @Test
    void testDeleteTaskLessArguments() {
        Command cmd = CommandCreator.newCommand("delete-task");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "delete-task", "at least 1"), response,
                "Unexpected response returned when deleteTask() is called and argument count is less than required");
    }

    @Test
    void testDeleteTaskInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("update-task --name: task1");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when deleteTask() is called and key-value format is invalid");
    }

    @Test
    void testDeleteTaskNameArgumentMissing() {
        Command cmd = CommandCreator.newCommand("delete-task --argument=value");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" parameter not found.", response,
                "Unexpected response returned when deleteTask() is called and name argument is missing");
    }

    @Test
    void testDeleteTaskWithDateInvalidDateFormat() {
        Command cmd = CommandCreator.newCommand("delete-task --name=task --date=12/02/23");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Unknown date format for the date provided.", response,
                "Unexpected response returned when deleteTask() is called and date is in invalid format");
    }

    @Test
    void testDeleteTaskWhenUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("delete-task --name=task --date=12.02.2023");
        String response = executor.execute(0, cmd);

        assertEquals("Task cannot be deleted. There is no logged user.", response,
                "Unexpected response returned when deleteTask() is called and there is no logged user.");
    }

    @Test
    void testDeleteTaskWhenUserIsLoggedAndTaskDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        doThrow(new TaskNotFoundException("Task not found."))
                .when(storageMock).deleteTask("username", "task");

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command deleteTaskCommand = CommandCreator.newCommand("delete-task --name=task");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, deleteTaskCommand);

        assertEquals("Task cannot be deleted. Task not found.", response,
                "Unexpected response returned when deleteTask() is called and task with this name does not exist");

        verify(storageMock).deleteTask("username", "task");
        verify(storageMock).login("username", "password");
    }

    @Test
    void testDeleteTaskWithDateWhenUserIsLoggedAndTaskDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        doThrow(new TaskNotFoundException("Task not found."))
                .when(storageMock).deleteTask("username", "task", LocalDate.parse("2023-02-12"));

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command deleteTaskCommand = CommandCreator.newCommand("delete-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, deleteTaskCommand);

        assertEquals("Task cannot be deleted. Task not found.", response,
                "Unexpected response returned when deleteTask() is called and task with this name and date does not exist");

        verify(storageMock).deleteTask("username", "task", LocalDate.parse("2023-02-12"));
        verify(storageMock).login("username", "password");
    }

    @Test
    void testDeleteTaskWhenTaskSuccessfullyDeleted()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command deleteTaskCommand = CommandCreator.newCommand("delete-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, deleteTaskCommand);

        assertEquals("Task \"task\" deleted successfully!", response,
                "Unexpected response returned when deleteTask() is called for valid task");

        verify(storageMock).deleteTask("username", "task", LocalDate.parse("2023-02-12"));
        verify(storageMock).login("username", "password");
    }

    @Test
    void testGetTaskLessArguments() {
        Command cmd = CommandCreator.newCommand("get-task");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "get-task", "at least 1"), response,
                "Unexpected response returned when getTask() is called and argument count is less than required");
    }

    @Test
    void testGetTaskInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("get-task --name: task1");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when getTask() is called and key-value format is invalid");
    }

    @Test
    void testGetTaskNameArgumentMissing() {
        Command cmd = CommandCreator.newCommand("get-task --argument=value");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" parameter not found.", response,
                "Unexpected response returned when getTask() is called and name argument is missing");
    }

    @Test
    void testGetTaskWithDateInvalidDateFormat() {
        Command cmd = CommandCreator.newCommand("delete-task --name=task --date=12/02/23");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Unknown date format for the date provided.", response,
                "Unexpected response returned when getTask() is called and date is in invalid format");
    }

    @Test
    void testGetTaskWhenUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("delete-task --name=task --date=12.02.2023");
        String response = executor.execute(0, cmd);

        assertEquals("Task cannot be deleted. There is no logged user.", response,
                "Unexpected response returned when getTask() is called and there is no logged user.");
    }

    @Test
    void testGetTaskWhenUserIsLoggedAndTaskDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        when(storageMock.getTask("username", "task"))
                .thenThrow(new TaskNotFoundException("Task not found."));

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command getTaskCommand = CommandCreator.newCommand("get-task --name=task");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, getTaskCommand);

        assertEquals("Task cannot be shown. Task not found.", response,
                "Unexpected response returned when getTask() is called and task with this name does not exist");

        verify(storageMock).getTask("username", "task");
        verify(storageMock).login("username", "password");
    }

    @Test
    void testGetTaskWithDateWhenUserIsLoggedAndTaskDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        when(storageMock.getTask("username", "task", LocalDate.parse("2023-02-12")))
                .thenThrow(new TaskNotFoundException("Task not found."));

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command getTaskCommand = CommandCreator.newCommand("get-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, getTaskCommand);

        assertEquals("Task cannot be shown. Task not found.", response,
                "Unexpected response returned when getTask() is called and task with this name and date does not exist");

        verify(storageMock).getTask("username", "task", LocalDate.parse("2023-02-12"));
        verify(storageMock).login("username", "password");
    }

    @Test
    void testGetTaskWhenTaskIsFound()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        LocalDate date = LocalDate.parse("2023-02-12");
        Task task = Task.builder("task").setDate(date).build();
        when(storageMock.getTask("username", "task", date)).thenReturn(task);

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command getTaskCommand = CommandCreator.newCommand("get-task --name=task --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, getTaskCommand);

        assertEquals(task.toString(), response,
                "Unexpected response returned when getTask() is called for valid task");

        verify(storageMock).getTask("username", "task", LocalDate.parse("2023-02-12"));
        verify(storageMock).login("username", "password");
    }

    @Test
    void testListTasksInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("list-tasks --arg->val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when listTasks() is called and command is in invalid key-value " +
                        "format");
    }

    @Test
    void testListTasksInvalidDateFormat() {
        Command cmd = CommandCreator.newCommand("list-tasks --date=12/02/23");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Unknown date format for the date provided.", response,
                "Unexpected response returned when listTasks() is called and date is in invalid format");
    }

    @Test
    void testListTasksWhenUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("list-tasks");
        String response = executor.execute(0, cmd);

        assertEquals("Tasks cannot be listed. There is no logged user.", response,
                "Unexpected response returned when listTasks() is called and there is no logged user.");
    }

    @Test
    void testListTasksWithDateWhenTasksDoesNotExist()
            throws TaskNotFoundException, UserNotFoundException, WrongPasswordException {

        when(storageMock.listTasks("username", LocalDate.parse("2023-02-12")))
                .thenThrow(new TaskNotFoundException("No tasks found"));

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command listTasksCommand = CommandCreator.newCommand("list-tasks --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, listTasksCommand);

        assertEquals("Tasks cannot be listed. No tasks found", response,
                "Unexpected response returned when listTasks() is called and there are no tasks for this date");

        verify(storageMock).login("username", "password");
        verify(storageMock).listTasks("username", LocalDate.parse("2023-02-12"));
    }

    @Test
    void testListTasksWithDateWhenCollaborationNotFound()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException {

        when(storageMock.listTasks("username", "collaboration"))
                .thenThrow(new CollaborationNotFoundException("Collaboration not found"));

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command listTasksCommand = CommandCreator.newCommand("list-tasks --collaboration=collaboration");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, listTasksCommand);

        assertEquals("Tasks cannot be listed. Collaboration not found", response,
                "Unexpected response returned when listTasks() is called and collaboration does not exist");

        verify(storageMock).login("username", "password");
        verify(storageMock).listTasks("username", "collaboration");
    }

    @Test
    void testListTasksWhenTasksDoesNotExist() throws UserNotFoundException, WrongPasswordException {
        when(storageMock.listTasks("username")).thenReturn(new ArrayList<>());

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command listTasksCommand = CommandCreator.newCommand("list-tasks");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, listTasksCommand);

        assertEquals("No tasks found!", response,
                "Unexpected response returned when listTasks() is called and there are no tasks for the " +
                        "logged user");

        verify(storageMock).login("username", "password");
        verify(storageMock).listTasks("username");
    }

    @Test
    void testListCompletedTasksWhenTasksDoesNotExist() throws UserNotFoundException, WrongPasswordException {
        when(storageMock.listCompletedTasks("username")).thenReturn(new ArrayList<>());

        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command listTasksCommand = CommandCreator.newCommand("list-tasks --completed=true");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, listTasksCommand);

        assertEquals("No tasks found!", response,
                "Unexpected response returned when listCompletedTasks() is called and there are no tasks for " +
                        "the logged user");

        verify(storageMock).login("username", "password");
        verify(storageMock).listCompletedTasks("username");
    }

    @Test
    void testListTasksWhenMoreThanOnePropertyIsSet() throws UserNotFoundException, WrongPasswordException {
        Command loginCommand = CommandCreator.newCommand("login --username=username --password=password");
        Command listTasksCommand = CommandCreator.newCommand("list-tasks --completed=true --date=12.02.2023");

        executor.execute(0, loginCommand);
        String response = executor.execute(0, listTasksCommand);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "There are more than one set properties.", response,
                "Unexpected response returned when listTasks() is called with more than one property");

        verify(storageMock).login("username", "password");
    }

    @Test
    void testListTasksExistingTasks() throws UserNotFoundException, WrongPasswordException {
        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").setDescription("description").build();
        List<Task> results = List.of(task1, task2);
        String expected = RESULTS_SECTION_SEPARATOR +
                System.lineSeparator() +
                task1 +
                task2 +
                RESULTS_SECTION_SEPARATOR +
                System.lineSeparator();

        when(storageMock.listTasks("username")).thenReturn(results);

        Command listTasksCommand = CommandCreator.newCommand("list-tasks");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, listTasksCommand);

        assertEquals(expected, response, "Unexpected response returned for existing tasks");

        verify(storageMock).login("username", "password");
        verify(storageMock).listTasks("username");
    }

    @Test
    void testListCompletedTasksExistingTasks() throws UserNotFoundException, WrongPasswordException {
        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").setDescription("description").build();
        List<Task> results = List.of(task1, task2);
        String expected = RESULTS_SECTION_SEPARATOR +
                System.lineSeparator() +
                task1 +
                task2 +
                RESULTS_SECTION_SEPARATOR +
                System.lineSeparator();

        when(storageMock.listCompletedTasks("username")).thenReturn(results);

        Command listTasksCommand = CommandCreator.newCommand("list-tasks --completed=true");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, listTasksCommand);

        assertEquals(expected, response, "Unexpected response returned for existing completed tasks");

        verify(storageMock).listCompletedTasks("username");
        verify(storageMock).login("username", "password");
    }

    @Test
    void testListTasksWithDateExistingTasks() throws TaskNotFoundException, UserNotFoundException, WrongPasswordException {
        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").setDescription("description").build();
        List<Task> results = List.of(task1, task2);
        String expected = RESULTS_SECTION_SEPARATOR +
                System.lineSeparator() +
                task1 +
                task2 +
                RESULTS_SECTION_SEPARATOR +
                System.lineSeparator();

        when(storageMock.listTasks("username", LocalDate.parse("2023-02-12"))).thenReturn(results);

        Command listTasksCommand = CommandCreator.newCommand("list-tasks --date=12.02.2023");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, listTasksCommand);

        assertEquals(expected, response, "Unexpected response returned for existing tasks with specified date");

        verify(storageMock).listTasks("username", LocalDate.parse("2023-02-12"));
        verify(storageMock).login("username", "password");
    }

    @Test
    void testListTasksFromCollaborationExistingTasks() throws CollaborationNotFoundException, UserNotFoundException, WrongPasswordException {
        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").setDescription("description").build();
        List<Task> results = List.of(task1, task2);
        String expected = RESULTS_SECTION_SEPARATOR +
                System.lineSeparator() +
                task1 +
                task2 +
                RESULTS_SECTION_SEPARATOR +
                System.lineSeparator();

        when(storageMock.listTasks("username", "collaboration")).thenReturn(results);

        Command listTasksCommand = CommandCreator.newCommand("list-tasks --collaboration=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, listTasksCommand);

        assertEquals(expected, response, "Unexpected response returned for existing tasks from collaboration");

        verify(storageMock).listTasks("username", "collaboration");
        verify(storageMock).login("username", "password");
    }

    @Test
    void testListDashboardUserNotLogged() {
        Command cmd = CommandCreator.newCommand("list-dashboard");
        String response = executor.execute(0, cmd);

        assertEquals("No tasks found. There is no logged user.", response,
                "Unexpected response returned when listDashboard() is called and there is no logged user.");
    }

    @Test
    void testListDashboardTasksNotFound() throws TaskNotFoundException, UserNotFoundException, WrongPasswordException {
        when(storageMock.listDashboard("username")).thenThrow(new TaskNotFoundException("Tasks not found"));

        Command listCommand = CommandCreator.newCommand("list-dashboard");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, listCommand);

        assertEquals("No tasks found. Tasks not found", response,
                "Unexpected response returned when listDashboard() is called and no tasks are found.");

        verify(storageMock).listDashboard("username");
        verify(storageMock).login("username", "password");
    }

    @Test
    void testListDashboardExistingTasks() throws TaskNotFoundException, UserNotFoundException, WrongPasswordException {
        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").setDescription("description").build();
        List<Task> results = List.of(task1, task2);
        String expected = RESULTS_SECTION_SEPARATOR +
                System.lineSeparator() +
                task1 +
                task2 +
                RESULTS_SECTION_SEPARATOR +
                System.lineSeparator();

        when(storageMock.listDashboard("username")).thenReturn(results);

        Command listTasksCommand = CommandCreator.newCommand("list-dashboard");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, listTasksCommand);

        assertEquals(expected, response, "Unexpected response returned for existing tasks");

        verify(storageMock).listDashboard("username");
        verify(storageMock).login("username", "password");
    }

    @Test
    void testFinishTaskLessArguments() {
        Command cmd = CommandCreator.newCommand("finish-task");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "finish-task", 1), response,
                "Unexpected response returned when finishTask() is called with less arguments");
    }

    @Test
    void testFinishTaskMoreArguments() {
        Command cmd = CommandCreator.newCommand("finish-task --arg1=val1 --arg2=val2");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "finish-task", 1), response,
                "Unexpected response returned when finishTask() is called with more arguments");
    }

    @Test
    void testFinishTaskInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("finish-task --name->task");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when finishTask() is called with argument in invalid key-value " +
                        "format");
    }

    @Test
    void testFinishTaskNameIsMissing() {
        Command cmd = CommandCreator.newCommand("finish-task --arg=val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "\"name\" argument not found", response,
                "Unexpected response returned when finishTask() is called and \"name\" argument is missing");
    }

    @Test
    void testFinishTaskUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("finish-task --name=task");
        String response = executor.execute(0, cmd);

        assertEquals("Task cannot be finished. There is no logged user.", response,
                "Unexpected response returned when finishTask() is called with no logged user");
    }

    @Test
    void testFinishTaskTaskNotFound() throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {
        doThrow(new TaskNotFoundException("Task not found."))
                .when(storageMock).finishTask("username", "task");

        Command cmd = CommandCreator.newCommand("finish-task --name=task");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Task cannot be finished. Task not found.", response,
                "Unexpected response returned when finishTask() is called for non-existent task");

        verify(storageMock).login("username", "password");
        verify(storageMock).finishTask("username", "task");
    }

    @Test
    void testFinishTaskSuccessfullyFinished()
            throws UserNotFoundException, WrongPasswordException, TaskNotFoundException {

        Command cmd = CommandCreator.newCommand("finish-task --name=task");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Task \"task\" finished successfully!", response,
                "Unexpected response returned when finishTask() is successfully finished");

        verify(storageMock).login("username", "password");
        verify(storageMock).finishTask("username", "task");
    }

    @Test
    void testAddCollaborationLessArguments() {
        Command cmd = CommandCreator.newCommand("add-collaboration");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-collaboration", 1), response,
                "Unexpected response returned when addCollaboration() is called with less arguments");
    }

    @Test
    void testAddCollaborationMoreArguments() {
        Command cmd = CommandCreator.newCommand("add-collaboration --arg1=val1 --arg2=val2");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-collaboration", 1), response,
                "Unexpected response returned when addCollaboration() is called with more arguments");
    }

    @Test
    void testAddCollaborationInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("add-collaboration --name->collaboration");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when addCollaboration() is called with argument in invalid" +
                        " key-value format");
    }

    @Test
    void testAddCollaborationNameIsMissing() {
        Command cmd = CommandCreator.newCommand("add-collaboration --arg=val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration name parameter is missing.", response,
                "Unexpected response returned when addCollaboration() is called and \"name\" argument is" +
                        " missing");
    }

    @Test
    void testAddCollaborationUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("add-collaboration --name=collaboration");
        String response = executor.execute(0, cmd);

        assertEquals("Collaboration cannot be created. There is no logged user.", response,
                "Unexpected response returned when addCollaboration() is called with no logged user");
    }

    @Test
    void testAddCollaborationWhenCollaborationExists()
            throws CollaborationAlreadyExistsException, UserNotFoundException, WrongPasswordException {

        doThrow(new CollaborationAlreadyExistsException("Collaboration already exists."))
                .when(storageMock).addCollaboration("username", "collaboration");

        Command cmd = CommandCreator.newCommand("add-collaboration --name=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Collaboration cannot be created. Collaboration already exists.", response,
                "Unexpected response returned when addCollaboration() is called and collaboration exists");

        verify(storageMock).login("username", "password");
        verify(storageMock).addCollaboration("username", "collaboration");
    }

    @Test
    void testAddCollaborationSuccessfullyAdded()
            throws CollaborationAlreadyExistsException, UserNotFoundException, WrongPasswordException {

        Command cmd = CommandCreator.newCommand("add-collaboration --name=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Collaboration \"collaboration\" added successfully", response,
                "Unexpected response returned when addCollaboration() is called and the collaboration is " +
                        "successfully created.");

        verify(storageMock).login("username", "password");
        verify(storageMock).addCollaboration("username", "collaboration");
    }

    @Test
    void testDeleteCollaborationLessArguments() {
        Command cmd = CommandCreator.newCommand("delete-collaboration");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "delete-collaboration", 1), response,
                "Unexpected response returned when deleteCollaboration() is called with less arguments");
    }

    @Test
    void testDeleteCollaborationMoreArguments() {
        Command cmd = CommandCreator.newCommand("delete-collaboration --arg1=val1 --arg2=val2");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "delete-collaboration", 1), response,
                "Unexpected response returned when deleteCollaboration() is called with more arguments");
    }

    @Test
    void testDeleteCollaborationInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("delete-collaboration --collaboration->collaboration");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when deleteCollaboration() is called with argument in invalid" +
                        " key-value format");
    }

    @Test
    void testDeleteCollaborationCollaborationNameIsMissing() {
        Command cmd = CommandCreator.newCommand("delete-collaboration --arg=val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration name parameter is missing.", response,
                "Unexpected response returned when deleteCollaboration() is called and \"name\" argument is" +
                        " missing");
    }

    @Test
    void testDeleteCollaborationUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("delete-collaboration --collaboration=collaboration");
        String response = executor.execute(0, cmd);

        assertEquals("Collaboration cannot be deleted. There is no logged user.", response,
                "Unexpected response returned when deleteCollaboration() is called with no logged user");
    }

    @Test
    void testDeleteCollaborationWhenCollaborationDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException {

        doThrow(new CollaborationNotFoundException("Collaboration not found."))
                .when(storageMock).deleteCollaboration("username", "collaboration");

        Command cmd = CommandCreator.newCommand("delete-collaboration --collaboration=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Collaboration cannot be deleted. Collaboration not found.", response,
                "Unexpected response returned when deleteCollaboration() is called and collaboration not found");

        verify(storageMock).login("username", "password");
        verify(storageMock).deleteCollaboration("username", "collaboration");
    }

    @Test
    void testDeleteCollaborationSuccessfullyDeleted()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException {

        Command cmd = CommandCreator.newCommand("delete-collaboration --collaboration=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Collaboration \"collaboration\" deleted successfully", response,
                "Unexpected response returned when deleteCollaboration() is called and the collaboration is " +
                        "successfully deleted.");

        verify(storageMock).login("username", "password");
        verify(storageMock).deleteCollaboration("username", "collaboration");
    }

    @Test
    void testListCollaborationsUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("list-collaborations");
        String response = executor.execute(0, cmd);

        assertEquals("Cannot list collaborations. There is no logged user.", response,
                "Unexpected response returned when listCollaborations() is called and there is no logged user");
    }

    @Test
    void testListCollaborationsNoCollaborationsFound() throws UserNotFoundException, WrongPasswordException {
        when(storageMock.getCollaborations("username")).thenReturn(new ArrayList<>());

        Command cmd = CommandCreator.newCommand("list-collaborations");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("No collaborations found!", response,
                "Unexpected response returned when listCollaborations() is called and no collaborations found");

        verify(storageMock).login("username", "password");
        verify(storageMock).getCollaborations("username");
    }

    @Test
    void testListCollaborationsWhenCollaborationsAreFound() throws UserNotFoundException, WrongPasswordException {
        Collaboration coll1 = new Collaboration("coll1");
        Collaboration coll2 = new Collaboration("coll2");
        String expected = RESULTS_SECTION_SEPARATOR + System.lineSeparator() +
                "coll1" + System.lineSeparator() +
                "coll2" + System.lineSeparator() +
                RESULTS_SECTION_SEPARATOR + System.lineSeparator();

        when(storageMock.getCollaborations("username")).thenReturn(List.of(coll1, coll2));

        Command cmd = CommandCreator.newCommand("list-collaborations");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals(expected, response, "Unexpected response returned for existing collaborations");

        verify(storageMock).login("username", "password");
        verify(storageMock).getCollaborations("username");
    }

    @Test
    void testAddUserToCollaborationLessArguments() {
        Command cmd = CommandCreator.newCommand("add-user");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-user", 2), response,
                "Unexpected response returned when addUserToCollaboration() is called with less arguments");
    }

    @Test
    void testAddUserToCollaborationMoreArguments() {
        Command cmd = CommandCreator.newCommand("add-user --arg1=val1 --arg2=val2 --arg3=val3");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "add-user", 2), response,
                "Unexpected response returned when addUserToCollaboration() is called with more arguments");
    }

    @Test
    void testAddUserToCollaborationInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("add-user --user->username --collaboration: coll");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when addUserToCollaboration() is called with argument in" +
                        " invalid key-value format");
    }

    @Test
    void testAddUserToCollaborationCollaborationNameIsMissing() {
        Command cmd = CommandCreator.newCommand("add-user --user=user --arg=val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration parameter not found.", response,
                "Unexpected response returned when addUserToCollaboration() is called and \"collaboration\" " +
                        "argument is missing");
    }

    @Test
    void testAddUserToCollaborationUserIsMissing() {
        Command cmd = CommandCreator.newCommand("add-user --collaboration=collaboration --arg=val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Username parameter not found.", response,
                "Unexpected response returned when addUserToCollaboration() is called and \"user\" " +
                        "argument is missing");
    }

    @Test
    void testAddUserToCollaborationUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("add-user --collaboration=collaboration --user=user");
        String response = executor.execute(0, cmd);

        assertEquals("Cannot add user to collaboration. There is no logged user.", response,
                "Unexpected response returned when addUserToCollaboration() is called with no logged user");
    }

    @Test
    void testAddUserToCollaborationWhenCollaborationDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            UserAlreadyExistsException {

        doThrow(new CollaborationNotFoundException("Collaboration not found."))
                .when(storageMock)
                .addUserToCollaboration("username", "collaboration", "newUser");

        Command cmd = CommandCreator.newCommand("add-user --collaboration=collaboration --user=newUser");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot add user to collaboration. Collaboration not found.", response,
                "Unexpected response returned when addUserToCollaboration() is called and collaboration not" +
                        " found");

        verify(storageMock).login("username", "password");
        verify(storageMock).addUserToCollaboration("username", "collaboration",
                "newUser");
    }

    @Test
    void testAddUserToCollaborationWhenUserDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            UserAlreadyExistsException {

        doThrow(new UserNotFoundException("User not found."))
                .when(storageMock)
                .addUserToCollaboration("username", "collaboration", "newUser");

        Command cmd = CommandCreator.newCommand("add-user --collaboration=collaboration --user=newUser");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot add user to collaboration. User not found.", response,
                "Unexpected response returned when addUserToCollaboration() is called and user not found");

        verify(storageMock).login("username", "password");
        verify(storageMock).addUserToCollaboration("username", "collaboration",
                "newUser");
    }

    @Test
    void testAddUserToCollaborationWhenUserAlreadyAdded()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            UserAlreadyExistsException {

        doThrow(new UserAlreadyExistsException("User already added."))
                .when(storageMock)
                .addUserToCollaboration("username", "collaboration", "newUser");

        Command cmd = CommandCreator.newCommand("add-user --collaboration=collaboration --user=newUser");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot add user to collaboration. User already added.", response,
                "Unexpected response returned when addUserToCollaboration() is called and user is already " +
                        "added");

        verify(storageMock).login("username", "password");
        verify(storageMock).addUserToCollaboration("username", "collaboration",
                "newUser");
    }

    @Test
    void testAddUserToCollaborationUserSuccessfullyAdded()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            UserAlreadyExistsException {

        Command cmd = CommandCreator.newCommand("add-user --collaboration=collaboration --user=newUser");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("User \"newUser\" successfully added in collaboration \"collaboration\".", response,
                "Unexpected response returned when addUserToCollaboration() is called and the user is " +
                        "successfully added.");

        verify(storageMock).login("username", "password");
        verify(storageMock).addUserToCollaboration("username", "collaboration",
                "newUser");
    }

    @Test
    void testAssignTaskLessArguments() {
        Command cmd = CommandCreator.newCommand("assign-task");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "assign-task", "at least 3"), response,
                "Unexpected response returned when assignTask() is called with less arguments");
    }

    @Test
    void testAssignTaskInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("assign-task --user->user --collaboration: coll --task=task");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when assignTask() is called with argument in" +
                        " invalid key-value format");
    }

    @Test
    void testAssignTaskInvalidDateFormat() {
        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=coll --task=task" +
                " --date=12/02/2023");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Unknown date format for the date provided.", response,
                "Unexpected response returned when assignTask() is called and date is in invalid format");
    }

    @Test
    void testAssignTaskCollaborationNameIsMissing() {
        Command cmd = CommandCreator.newCommand("assign-task --user=user --arg=val --task=task");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration parameter not found.", response,
                "Unexpected response returned when assignTask() is called and \"collaboration\" " +
                        "argument is missing");
    }

    @Test
    void testAssignTaskUserIsMissing() {
        Command cmd = CommandCreator.newCommand("assign-task --arg=val --collaboration=coll --task=task");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "User parameter not found.", response,
                "Unexpected response returned when assignTask() is called and \"user\" " +
                        "argument is missing");
    }

    @Test
    void testAssignTaskTaskIsMissing() {
        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=coll --arg=val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Task parameter not found.", response,
                "Unexpected response returned when assignTask() is called and \"task\" " +
                        "argument is missing");
    }

    @Test
    void testAssignTaskUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=coll --task=task");
        String response = executor.execute(0, cmd);

        assertEquals("Cannot assign task. There is no logged user.", response,
                "Unexpected response returned when assignTask() is called with no logged user");
    }

    @Test
    void testAssignTaskWhenCollaborationDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            TaskNotFoundException, TaskAlreadyExistsException {

        doThrow(new CollaborationNotFoundException("Collaboration not found."))
                .when(storageMock)
                .assignTask("username", "collaboration", "user", "task");

        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=collaboration" +
                " --task=task");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot assign task. Collaboration not found.", response,
                "Unexpected response returned when assignTask() is called and collaboration is not" +
                        " found");

        verify(storageMock).login("username", "password");
        verify(storageMock).assignTask("username", "collaboration", "user",
                "task");
    }

    @Test
    void testAssignTaskWhenUserDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            TaskNotFoundException, TaskAlreadyExistsException {

        doThrow(new UserNotFoundException("User not found."))
                .when(storageMock)
                .assignTask("username", "collaboration", "user",
                        "task");

        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=collaboration " +
                "--task=task");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot assign task. User not found.", response,
                "Unexpected response returned when assignTask() is called and user not found");

        verify(storageMock).login("username", "password");
        verify(storageMock).assignTask("username", "collaboration", "user",
                "task");
    }

    @Test
    void testAssignTaskWhenUserTaskNotExist()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            TaskNotFoundException, TaskAlreadyExistsException {

        doThrow(new TaskNotFoundException("Task not found."))
                .when(storageMock)
                .assignTask("username", "collaboration", "user",
                        "task");

        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=collaboration " +
                "--task=task");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot assign task. Task not found.", response,
                "Unexpected response returned when assignTask() is called and task is not found");

        verify(storageMock).login("username", "password");
        verify(storageMock).assignTask("username", "collaboration", "user",
                "task");
    }

    @Test
    void testAssignTaskWhenUserTaskAlreadyAssigned()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            TaskNotFoundException, TaskAlreadyExistsException {

        doThrow(new TaskAlreadyExistsException("Task already assigned."))
                .when(storageMock)
                .assignTask("username", "collaboration", "user",
                        "task");

        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=collaboration " +
                "--task=task");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot assign task. Task already assigned.", response,
                "Unexpected response returned when assignTask() is called and task is already assigned");

        verify(storageMock).login("username", "password");
        verify(storageMock).assignTask("username", "collaboration", "user",
                "task");
    }

    @Test
    void testAssignTaskTaskSuccessfullyAssigned()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            TaskNotFoundException, TaskAlreadyExistsException {

        doNothing().when(storageMock).assignTask("username", "collaboration", "user",
                        "task");

        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=collaboration --task=task");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Task \"task\" successfully assigned with user \"user\".", response,
                "Unexpected response returned when assignTask() is called and the operation is successful");

        verify(storageMock).login("username", "password");
        verify(storageMock).assignTask("username", "collaboration", "user",
                "task");
    }

    @Test
    void testAssignTaskWithDateTaskSuccessfullyAssigned()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException,
            TaskNotFoundException, TaskAlreadyExistsException {

        doNothing().when(storageMock).assignTask("username", "collaboration", "user",
                "task", LocalDate.parse("2023-02-12"));

        Command cmd = CommandCreator.newCommand("assign-task --user=user --collaboration=collaboration " +
                "--task=task " +
                "--date=12.02.2023");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Task \"task\" successfully assigned with user \"user\".", response,
                "Unexpected response returned when assignTask() is called and the operation is successful");

        verify(storageMock).login("username", "password");
        verify(storageMock).assignTask("username", "collaboration", "user",
                "task", LocalDate.parse("2023-02-12"));
    }

    @Test
    void testListUsersInCollaborationLessArguments() {
        Command cmd = CommandCreator.newCommand("list-users");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "list-users", 1), response,
                "Unexpected response returned when listUsers() is called with less arguments");
    }

    @Test
    void testListUsersInCollaborationMoreArguments() {
        Command cmd = CommandCreator.newCommand("list-users --arg1=val1 --arg2=val2");
        String response = executor.execute(0, cmd);

        assertEquals(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, "list-users", 1), response,
                "Unexpected response returned when listUsers() is called with more arguments");
    }

    @Test
    void testListUsersInCollaborationInvalidKeyValueFormat() {
        Command cmd = CommandCreator.newCommand("list-users --collaboration->collaboration");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Command expected in \"key=value\" format", response,
                "Unexpected response returned when listUsers() is called with argument in invalid" +
                        " key-value format");
    }

    @Test
    void testListUsersInCollaborationCollaborationNameIsMissing() {
        Command cmd = CommandCreator.newCommand("list-users --arg=val");
        String response = executor.execute(0, cmd);

        assertEquals(INVALID_COMMAND_FORMAT_MESSAGE + "Collaboration name parameter is missing.", response,
                "Unexpected response returned when listUsers() is called and \"collaboration\" argument is" +
                        " missing");
    }

    @Test
    void testListUsersInCollaborationUserIsNotLogged() {
        Command cmd = CommandCreator.newCommand("list-users --collaboration=collaboration");
        String response = executor.execute(0, cmd);

        assertEquals("Cannot list users in this collaboration. There is no logged user.", response,
                "Unexpected response returned when listUsers() is called with no logged user");
    }

    @Test
    void testListUsersInCollaborationWhenCollaborationDoesNotExist()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException {

        when(storageMock.listUsersInCollaboration("username", "collaboration"))
                .thenThrow(new CollaborationNotFoundException("Collaboration not found."));

        Command cmd = CommandCreator.newCommand("list-users --collaboration=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("Cannot list users in this collaboration. Collaboration not found.", response,
                "Unexpected response returned when listUsers() is called and collaboration not found");

        verify(storageMock).login("username", "password");
        verify(storageMock).listUsersInCollaboration("username", "collaboration");
    }

    @Test
    void testListUsersInCollaborationUsersNotFound()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException {

        when(storageMock.listUsersInCollaboration("username", "collaboration"))
                .thenReturn(new ArrayList<>());

        Command cmd = CommandCreator.newCommand("list-users --collaboration=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals("No users found in this collaboration.", response,
                "Unexpected response returned when listUsers() is called and there are no any users found");

        verify(storageMock).login("username", "password");
        verify(storageMock).listUsersInCollaboration("username", "collaboration");
    }

    @Test
    void testListUsersInCollaborationSomeUsersFound()
            throws UserNotFoundException, WrongPasswordException, CollaborationNotFoundException {

        String user1 = "user1";
        String user2 = "user2";

        when(storageMock.listUsersInCollaboration("username", "collaboration"))
                .thenReturn(List.of(user1, user2));

        String expected = RESULTS_SECTION_SEPARATOR + System.lineSeparator() +
                user1 + System.lineSeparator() +
                user2 + System.lineSeparator() +
                RESULTS_SECTION_SEPARATOR + System.lineSeparator();

        Command cmd = CommandCreator.newCommand("list-users --collaboration=collaboration");

        executor.execute(0, LOGIN_COMMAND);
        String response = executor.execute(0, cmd);

        assertEquals(expected, response, "Unexpected response returned when listUsers() is called and there " +
                "are some users found");

        verify(storageMock).login("username", "password");
        verify(storageMock).listUsersInCollaboration("username", "collaboration");
    }

    @Test
    void testDisconnect() {
        Command cmd = CommandCreator.newCommand("disconnect");
        String response = executor.execute(0, cmd);

        assertEquals("Disconnected from server.", response, "Unexpected response returned when " +
                "disconnecting from server");
    }
}
