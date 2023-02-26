package todoist.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import todoist.collaboration.Collaboration;
import todoist.exception.CollaborationAlreadyExistsException;
import todoist.exception.CollaborationNotFoundException;
import todoist.exception.InvalidTimeIntervalException;
import todoist.exception.TaskAlreadyExistsException;
import todoist.exception.TaskNameAlreadyExistsException;
import todoist.exception.TaskNotFoundException;
import todoist.exception.UserAlreadyExistsException;
import todoist.exception.UserNotFoundException;
import todoist.exception.WrongPasswordException;
import todoist.task.Task;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryStorageTest {
    private Storage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
    }

    @Test
    void testAddUserNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> storage.addUser(null, "password"),
                "Expected IllegalArgumentException to be thrown when username is null");
    }

    @Test
    void testAddUserBlankUsername() {
        assertThrows(IllegalArgumentException.class, () -> storage.addUser("  ", "password"),
                "Expected IllegalArgumentException to be thrown when username is blank");
    }

    @Test
    void testAddUserEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> storage.addUser("", "password"),
                "Expected IllegalArgumentException to be thrown when username is empty");
    }

    @Test
    void testAddUserNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> storage.addUser("username", null),
                "Expected IllegalArgumentException to be thrown when password is null");
    }

    @Test
    void testAddUserBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> storage.addUser("username", "  "),
                "Expected IllegalArgumentException to be thrown when password is blank");
    }

    @Test
    void testAddUserEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> storage.addUser("username", ""),
                "Expected IllegalArgumentException to be thrown when password is empty");
    }

    @Test
    void testAddUserWhenUserExists() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(UserAlreadyExistsException.class, () -> storage.addUser("username", "password"),
                "Expected UserAlreadyExistsException to be thrown when the user exists");
    }

    @Test
    void testAddUserWhenUserDoesNotExist() {
        assertDoesNotThrow(() -> storage.addUser("username", "password"),
                "Unexpected exception thrown when user is successfully added");
    }

    @Test
    void testLoginNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> storage.checkPassword(null, "password"),
                "Expected IllegalArgumentException to be thrown when username is null");
    }

    @Test
    void testLoginBlankUsername() {
        assertThrows(IllegalArgumentException.class, () -> storage.checkPassword("  ", "password"),
                "Expected IllegalArgumentException to be thrown when username is blank");
    }

    @Test
    void testLoginEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> storage.checkPassword("", "password"),
                "Expected IllegalArgumentException to be thrown when username is empty");
    }

    @Test
    void testLoginNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> storage.checkPassword("username", null),
                "Expected IllegalArgumentException to be thrown when password is null");
    }

    @Test
    void testLoginBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> storage.checkPassword("username", "  "),
                "Expected IllegalArgumentException to be thrown when password is blank");
    }

    @Test
    void testLoginEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> storage.checkPassword("username", ""),
                "Expected IllegalArgumentException to be thrown when password is empty");
    }

    @Test
    void testLoginWhenUserDoesNotExist() {
        assertThrows(UserNotFoundException.class, () -> storage.checkPassword("username", "password"),
                "Expected UserNotFoundException to be thrown when the user does not exist");
    }

    @Test
    void testLoginWhenUserExistsWrongPassword() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(WrongPasswordException.class, () -> storage.checkPassword("username", "other_password"),
                "Expected WrongPasswordException to be thrown when the password does not match");
    }

    @Test
    void testLoginWhenUserExistsCorrectPassword() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertDoesNotThrow(() -> storage.checkPassword("username", "password"),
                "Unexpected exception thrown when user is successfully logged");
    }

    @Test
    void testAddTaskNullTask() {
        assertThrows(IllegalArgumentException.class, () -> storage.addTask("user", null),
                "Expected IllegalArgumentException to be thrown when task is null");
    }

    @Test
    void testAddTaskMissingUser() {
        Task task = Task.builder("task").build();
        assertThrows(UserNotFoundException.class, () -> storage.addTask("username", task),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testAddTaskExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException {

        Task task = Task.builder("task").build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        assertThrows(TaskNameAlreadyExistsException.class, () -> storage.addTask("username", task),
                "Expected TaskNameAlreadyExistsException to be thrown when this task is already added");
    }

    @Test
    void testAddTaskWithDateExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            InvalidTimeIntervalException {

        Task task = Task.builder("task").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        assertThrows(TaskNameAlreadyExistsException.class, () -> storage.addTask("username", task),
                "Expected TaskNameAlreadyExistsException to be thrown when this task is already added");
    }

    @Test
    void testAddTaskSuccessfullyAdded() throws UserAlreadyExistsException, InvalidTimeIntervalException {
        Task task = Task.builder("task").setDueDate(LocalDate.now()).build();
        storage.addUser("username", "password");

        assertDoesNotThrow(() -> storage.addTask("username", task),
                "Unexpected exception thrown when task is successfully added");
    }

    @Test
    void testAddTaskWithDateSuccessfullyAdded() throws UserAlreadyExistsException, InvalidTimeIntervalException {

        Task task = Task.builder("task").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");

        assertDoesNotThrow(() -> storage.addTask("username", task),
                "Unexpected exception thrown when task is successfully added");
    }

    @Test
    void testUpdateTaskNullTask() {
        assertThrows(IllegalArgumentException.class, () -> storage.updateTask("user", null),
                "Expected IllegalArgumentException to be thrown when task is null");
    }

    @Test
    void testUpdateTaskMissingUser() {
        Task task = Task.builder("task").build();
        assertThrows(UserNotFoundException.class, () -> storage.updateTask("username", task),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testUpdateTaskWhenTaskDoesNotExist() throws UserAlreadyExistsException {
        Task task = Task.builder("task").build();
        storage.addUser("username", "password");

        assertThrows(TaskNotFoundException.class, () -> storage.updateTask("username", task),
                "Expected TaskNotFoundException to be thrown when this task is already added");
    }

    @Test
    void testUpdateTaskWithDateTaskDoesNotExist() throws UserAlreadyExistsException, InvalidTimeIntervalException {
        Task task = Task.builder("task").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");

        assertThrows(TaskNotFoundException.class, () -> storage.updateTask("username", task),
                "Expected TaskNotFoundException to be thrown when this task is already added");
    }

    @Test
    void testAddTaskSuccessfullyUpdated()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException {

        Task task = Task.builder("task").build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        assertDoesNotThrow(() -> storage.updateTask("username", task),
                "Unexpected exception thrown when task is successfully updated");
    }

    @Test
    void testAddTaskWithDateSuccessfullyUpdated()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            InvalidTimeIntervalException {

        Task task = Task.builder("task").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        assertDoesNotThrow(() -> storage.updateTask("username", task),
                "Unexpected exception thrown when task is successfully added");
    }

    @Test
    void testDeleteTaskNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.deleteTask("user", null),
                "Expected IllegalArgumentException to be thrown when task name is null");
    }

    @Test
    void testDeleteTaskBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.deleteTask("user", "  "),
                "Expected IllegalArgumentException to be thrown when task name is blank");
    }

    @Test
    void testDeleteTaskEmptyTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.deleteTask("user", ""),
                "Expected IllegalArgumentException to be thrown when task name is empty");
    }

    @Test
    void testDeleteTaskMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.deleteTask("username", "task"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testDeleteTaskMissingTask() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(TaskNotFoundException.class, () -> storage.deleteTask("username", "task"),
                "Expected TaskNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testDeleteTaskExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException {

        Task task = Task.builder("task").build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        final Task[] actual = new Task[1];
        assertDoesNotThrow(() -> actual[0] = storage.deleteTask("username", "task"),
                "Unexpected exception thrown when deleting task is successfully");
        assertEquals(task, actual[0], "Another task deleted");
    }

    @Test
    void testDeleteTaskWithDateNullTaskName() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.deleteTask("user", null, LocalDate.now()),
                "Expected IllegalArgumentException to be thrown when task name is null");
    }

    @Test
    void testDeleteTaskWithDateBlankTaskName() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.deleteTask("user", "  ", LocalDate.now()),
                "Expected IllegalArgumentException to be thrown when task name is blank");
    }

    @Test
    void testDeleteTaskWithDateEmptyTaskName() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.deleteTask("user", "", LocalDate.now()),
                "Expected IllegalArgumentException to be thrown when task name is empty");
    }

    @Test
    void testDeleteTaskWithDateNullDate() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.deleteTask("user", "task", null),
                "Expected IllegalArgumentException to be thrown when date is null");
    }

    @Test
    void testDeleteTaskWithDateMissingUser() {
        assertThrows(UserNotFoundException.class,
                () -> storage.deleteTask("username", "task", LocalDate.now()),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testDeleteTaskWithDateMissingTask() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(TaskNotFoundException.class,
                () -> storage.deleteTask("username", "task", LocalDate.now()),
                "Expected TaskNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testDeleteTaskWithDateExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            InvalidTimeIntervalException {

        Task task = Task.builder("task").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        final Task[] actual = new Task[1];
        assertDoesNotThrow(() -> actual[0] = storage.deleteTask("username", "task", LocalDate.now()),
                "Unexpected exception thrown when deleting task is successfully");
        assertEquals(task, actual[0], "Another task deleted");
    }

    @Test
    void testGetTaskNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.getTask("username", null),
                "Expected IllegalArgumentException to be thrown when task name is null");
    }

    @Test
    void testGetTaskBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.getTask("username", "  "),
                "Expected IllegalArgumentException to be thrown when task name is blank");
    }

    @Test
    void testGetTaskEmptyTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.getTask("username", ""),
                "Expected IllegalArgumentException to be thrown when task name is empty");
    }

    @Test
    void testGetTaskMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.getTask("username", "task"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testGetTaskMissingTask() throws UserAlreadyExistsException {
        storage.addUser("username", "password");
        assertThrows(TaskNotFoundException.class, () -> storage.getTask("username", "task"),
                "Expected TaskNotFoundException to be thrown when task does not exist");
    }

    @Test
    void testGetTaskExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            TaskNotFoundException {

        Task task = Task.builder("task").build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        Task actual = storage.getTask("username", "task");

        assertEquals(task, actual, "Unexpected task returned");
    }

    @Test
    void testGetTaskWithDateNullTaskName() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.getTask("username", null, LocalDate.now()),
                "Expected IllegalArgumentException to be thrown when task name is null");
    }

    @Test
    void testGetTaskWithDateBlankTaskName() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.getTask("username", "  ", LocalDate.now()),
                "Expected IllegalArgumentException to be thrown when task name is blank");
    }

    @Test
    void testGetTaskWithDateEmptyTaskName() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.getTask("username", "", LocalDate.now()),
                "Expected IllegalArgumentException to be thrown when task name is empty");
    }

    @Test
    void testGetTaskWithDateNullDate() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.getTask("username", "task", null),
                "Expected IllegalArgumentException to be thrown when date is null");
    }

    @Test
    void testGetTaskWithDateMissingUser() {
        assertThrows(UserNotFoundException.class,
                () -> storage.getTask("username", "task", LocalDate.now()),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testGetTaskWithDateMissingTask() throws UserAlreadyExistsException {
        storage.addUser("username", "password");
        assertThrows(TaskNotFoundException.class,
                () -> storage.getTask("username", "task", LocalDate.now()),
                "Expected TaskNotFoundException to be thrown when task does not exist");
    }

    @Test
    void testGetTaskWithDateExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            TaskNotFoundException, InvalidTimeIntervalException {

        Task task = Task.builder("task").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        Task actual = storage.getTask("username", "task", LocalDate.now());

        assertEquals(task, actual, "Unexpected task returned");
    }

    @Test
    void testListTasksMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.listTasks("username"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testListTasksNoExistingTasks() throws UserAlreadyExistsException, UserNotFoundException {
        storage.addUser("username", "password");
        Collection<Task> actual = storage.listTasks("username");

        assertTrue(actual.isEmpty(), "Expected empty collection to be returned when there are no tasks");
    }

    @Test
    void testListTasksTasksWithoutDateExist()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException {

        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").build();
        storage.addUser("username", "password");
        storage.addTask("username", task1);
        storage.addTask("username", task2);

        Collection<Task> actual = storage.listTasks("username");

        assertEquals(2, actual.size(), "Unexpected number of tasks returned");
        assertTrue(actual.contains(task1), "Expected task not returned");
        assertTrue(actual.contains(task2), "Expected task not returned");
    }

    @Test
    void testListTasksTasksWithDateExist()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            InvalidTimeIntervalException {

        Task task1 = Task.builder("task1").setDate(LocalDate.now()).build();
        Task task2 = Task.builder("task2").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");
        storage.addTask("username", task1);
        storage.addTask("username", task2);

        Collection<Task> actual = storage.listTasks("username");

        assertEquals(2, actual.size(), "Unexpected number of tasks returned");
        assertTrue(actual.contains(task1), "Expected task not returned");
        assertTrue(actual.contains(task2), "Expected task not returned");
    }

    @Test
    void testListCompletedTasksMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.listCompletedTasks("username"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testListCompletedTaskNoExistingTasks() throws UserAlreadyExistsException, UserNotFoundException {
        storage.addUser("username", "password");
        Collection<Task> actual = storage.listCompletedTasks("username");

        assertTrue(actual.isEmpty(), "Expected empty collection to be returned when there are no tasks");
    }

    @Test
    void testListCompletedTasksExistingTasks()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            TaskNotFoundException {

        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").build();
        storage.addUser("username", "password");
        storage.addTask("username", task1);
        storage.addTask("username", task2);

        storage.finishTask("username", "task1");
        storage.finishTask("username", "task2");

        Collection<Task> actual = storage.listCompletedTasks("username");

        assertEquals(2, actual.size(), "Unexpected number of tasks returned");
        assertTrue(actual.contains(task1), "Expected task not returned");
        assertTrue(actual.contains(task2), "Expected task not returned");
    }

    @Test
    void testListTasksWithDateNullDate() {
        assertThrows(IllegalArgumentException.class, () -> storage.listTasks("username", (LocalDate) null),
                "Expected IllegalArgumentException to be thrown when date is null");
    }

    @Test
    void testListTasksWithDateMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.listTasks("username", LocalDate.now()),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testListTasksWithDateNoExistingTasks() throws UserAlreadyExistsException {
        storage.addUser("username", "password");
        assertThrows(TaskNotFoundException.class, () -> storage.listTasks("username", LocalDate.now()),
                "Expected TaskNotFoundException to be thrown when there are no any tasks for this date");
    }

    @Test
    void testListTasksWithDateExistingTasks()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            TaskNotFoundException, InvalidTimeIntervalException {

        Task task1 = Task.builder("task1").setDate(LocalDate.now()).build();
        Task task2 = Task.builder("task2").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");
        storage.addTask("username", task1);
        storage.addTask("username", task2);

        Collection<Task> actual = storage.listTasks("username", LocalDate.now());

        assertEquals(2, actual.size(), "Unexpected number of tasks returned");
        assertTrue(actual.contains(task1), "Expected task not returned");
        assertTrue(actual.contains(task2), "Expected task not returned");
    }

    @Test
    void testListTasksFromCollaborationNullCollaborationName() {
        assertThrows(IllegalArgumentException.class, () -> storage.listTasks("username", (String) null),
                "Expected IllegalArgumentException to be thrown when collaboration name is null");
    }

    @Test
    void testListTasksFromCollaborationBlankCollaborationName() {
        assertThrows(IllegalArgumentException.class, () -> storage.listTasks("username", "  "),
                "Expected IllegalArgumentException to be thrown when collaboration name is blank");
    }

    @Test
    void testListTasksFromCollaborationEmptyCollaborationName() {
        assertThrows(IllegalArgumentException.class, () -> storage.listTasks("username", ""),
                "Expected IllegalArgumentException to be thrown when collaboration name is empty");
    }

    @Test
    void testListTasksFromCollaborationMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.listTasks("username", "coll"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testListTasksFromCollaborationMissingCollaboration() throws UserAlreadyExistsException {
        storage.addUser("username", "password");
        assertThrows(CollaborationNotFoundException.class,
                () -> storage.listTasks("username", "collaboration"),
                "Expected CollaborationNotFoundException when collaboration is missing");
    }

    @Test
    void testListTasksFromCollaborationNoExistingTasks()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationNotFoundException,
            CollaborationAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addCollaboration("username", "collaboration");
        Collection<Task> actual = storage.listTasks("username", "collaboration");

        assertTrue(actual.isEmpty(), "Expected empty collection to be returned when there are no tasks");
    }

    @Test
    void testListTasksFromCreatedCollaboration()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            CollaborationAlreadyExistsException, CollaborationNotFoundException, TaskNotFoundException, TaskAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addUser("user", "password");

        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").build();

        storage.addTask("username", task1);
        storage.addTask("username", task2);

        storage.addCollaboration("username", "collaboration");
        storage.addUserToCollaboration("username", "collaboration", "user");

        storage.assignTask("username", "collaboration", "user", "task1");
        storage.assignTask("username", "collaboration", "user", "task2");

        Collection<Task> actual = storage.listTasks("username", "collaboration");

        assertEquals(2, actual.size(), "Unexpected number of tasks returned");
        assertTrue(actual.contains(task1), "Expected task not returned");
        assertTrue(actual.contains(task2), "Expected task not returned");
    }

    @Test
    void testListTasksFromAssignedCollaboration()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            CollaborationAlreadyExistsException, CollaborationNotFoundException, TaskNotFoundException, TaskAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addUser("user", "password");

        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").build();

        storage.addTask("username", task1);
        storage.addTask("username", task2);

        storage.addCollaboration("username", "collaboration");
        storage.addUserToCollaboration("username", "collaboration", "user");

        storage.assignTask("username", "collaboration", "user", "task1");
        storage.assignTask("username", "collaboration", "user", "task2");

        Collection<Task> actual = storage.listTasks("user", "collaboration");

        assertEquals(2, actual.size(), "Unexpected number of tasks returned");
        assertTrue(actual.contains(task1), "Expected task not returned");
        assertTrue(actual.contains(task2), "Expected task not returned");
    }

    @Test
    void testListDashboardMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.listDashboard("username"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testListDashboardNoExistingTasks() throws UserAlreadyExistsException {
        storage.addUser("username", "password");
        assertThrows(TaskNotFoundException.class, () -> storage.listDashboard("username"),
                "Expected TaskNotFoundException to be thrown when there are no any tasks for this date");
    }

    @Test
    void testListDashboardExistingTasks()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException,
            TaskNotFoundException, InvalidTimeIntervalException {

        Task task1 = Task.builder("task1").setDate(LocalDate.now()).build();
        Task task2 = Task.builder("task2").setDate(LocalDate.now()).build();
        storage.addUser("username", "password");
        storage.addTask("username", task1);
        storage.addTask("username", task2);

        Collection<Task> actual = storage.listDashboard("username");

        assertEquals(2, actual.size(), "Unexpected number of tasks returned");
        assertTrue(actual.contains(task1), "Expected task not returned");
        assertTrue(actual.contains(task2), "Expected task not returned");
    }

    @Test
    void testFinishTaskNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.finishTask("username", null),
                "Expected IllegalArgumentException to be thrown when task name is null");
    }

    @Test
    void testFinishTaskBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.finishTask("username", "  "),
                "Expected IllegalArgumentException to be thrown when task name is blank");
    }

    @Test
    void testFinishTaskEmptyTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.finishTask("username", ""),
                "Expected IllegalArgumentException to be thrown when task name is empty");
    }

    @Test
    void testFinishTaskMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.finishTask("username", "task"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testFinishTaskMissingTask() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(TaskNotFoundException.class, () -> storage.finishTask("username", "task"),
                "Expected TaskNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testFinishTaskExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, TaskNameAlreadyExistsException {

        Task task = Task.builder("task").build();
        storage.addUser("username", "password");
        storage.addTask("username", task);

        assertDoesNotThrow(() ->storage.finishTask("username", "task"),
                "Unexpected exception thrown when finishing task is successfully");

        Collection<Task> completed = storage.listCompletedTasks("username");
        assertTrue(completed.contains(task), "Completed task not returned");
    }

    @Test
    void testAddCollaborationNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.addCollaboration("username", null),
                "Expected IllegalArgumentException to be thrown when task name is null");
    }

    @Test
    void testAddCollaborationBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.addCollaboration("username", "  "),
                "Expected IllegalArgumentException to be thrown when task name is blank");
    }

    @Test
    void testAddCollaborationEmptyTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.addCollaboration("username", ""),
                "Expected IllegalArgumentException to be thrown when task name is empty");
    }

    @Test
    void testAddCollaborationMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.addCollaboration("username", "coll"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testAddCollaborationExistingCollaboration()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addCollaboration("username", "collaboration");

        assertThrows(CollaborationAlreadyExistsException.class,
                () -> storage.addCollaboration("username", "collaboration"),
                "Expected CollaborationAlreadyExistsException to be thrown when collaboration exists");
    }

    @Test
    void testAddCollaborationNewCollaboration() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertDoesNotThrow(() -> storage.addCollaboration("username", "collaboration"),
                "Expected CollaborationAlreadyExistsException to be thrown when collaboration exists");
    }

    @Test
    void testDeleteCollaborationNullTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.deleteCollaboration("user", null),
                "Expected IllegalArgumentException to be thrown when task name is null");
    }

    @Test
    void testDeleteCollaborationBlankTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.deleteCollaboration("username", " "),
                "Expected IllegalArgumentException to be thrown when task name is blank");
    }

    @Test
    void testDeleteCollaborationEmptyTaskName() {
        assertThrows(IllegalArgumentException.class, () -> storage.deleteCollaboration("username", ""),
                "Expected IllegalArgumentException to be thrown when task name is empty");
    }

    @Test
    void testDeleteCollaborationMissingUser() {
        assertThrows(UserNotFoundException.class, () -> storage.deleteCollaboration("username", "coll"),
                "Expected UserNotFoundException to be thrown when user does not exist");
    }

    @Test
    void testDeleteCollaborationMissingCollaboration() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(CollaborationNotFoundException.class,
                () -> storage.deleteCollaboration("username", "collaboration"),
                "Expected CollaborationNotFoundException when collaboration does not exist");
    }

    @Test
    void testDeleteCollaborationExistingCollaboration()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException,
            CollaborationNotFoundException, TaskNotFoundException, TaskAlreadyExistsException,
            TaskNameAlreadyExistsException, InvalidTimeIntervalException {

        storage.addUser("username", "password");
        storage.addUser("newUser", "password");
        storage.addCollaboration("username", "collaboration");
        storage.addUserToCollaboration("username", "collaboration", "newUser");

        Task task1 = Task.builder("task1").build();
        Task task2 = Task.builder("task2").setDate(LocalDate.now()).build();

        storage.addTask("username", task1);
        storage.addTask("username", task2);

        storage.assignTask("username", "collaboration", "newUser", "task1");
        storage.assignTask("username", "collaboration", "newUser", "task2",
                LocalDate.now());

        assertDoesNotThrow(() -> storage.deleteCollaboration("username", "collaboration"),
                "Unexpected exception thrown when deleting existing collaboration");
    }
//    todo...

    @Test
    void testGetCollaborationsExistingCollaboration()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException,
            CollaborationNotFoundException {

        storage.addUser("username", "password");
        storage.addUser("newUser", "password");
        storage.addCollaboration("username", "collaboration1");
        storage.addCollaboration("newUser", "collaboration2");
        storage.addUserToCollaboration("username", "collaboration1", "newUser");

        Collection<Collaboration> actual = storage.getCollaborations("newUser");

        Collaboration coll1 = new Collaboration("collaboration1");
        Collaboration coll2 = new Collaboration("collaboration2");

        assertEquals(2, actual.size(), "Unexpected number of collaborations returned");
        assertTrue(actual.contains(coll1), "Expected collaboration not returned");
        assertTrue(actual.contains(coll2), "Expected collaboration not returned");
    }

    // todo testAddUserToCollaborationNullStrings()...

    @Test
    void testAddUserToCollaborationMissingUser() {
        assertThrows(UserNotFoundException.class,
                () -> storage.addUserToCollaboration("username", "collaboration", "newUser"),
                "Expected UserNotFoundException to be thrown when user is not found");
    }

    @Test
    void testAddUserToCollaborationMissingCollaboration() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(CollaborationNotFoundException.class,
                () -> storage.addUserToCollaboration("username", "collaboration", "newUser"),
                "Expected CollaborationNotFoundException to be thrown when collaboration is not found");
    }

    @Test
    void testAddUserToCollaborationMissingNewUser()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addCollaboration("username", "collaboration");

        assertThrows(UserNotFoundException.class,
                () -> storage.addUserToCollaboration("username", "collaboration", "newUser"),
                "Expected UserNotFoundException to be thrown when new user is not found");
    }

    // todo test for validations - assignTask()

    @Test
    void testAssignTaskMissingCollaboration() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(CollaborationNotFoundException.class,
                () -> storage.assignTask("username", "collaboration",
                        "newUser", "task"),
                "Expected CollaborationNotFoundException to be thrown when collaboration is not found");
    }

    @Test
    void testAssignTaskMissingNewUser()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addCollaboration("username", "collaboration");

        assertThrows(UserNotFoundException.class,
                () -> storage.assignTask("username", "collaboration", "newUser",
                        "task"),
                "Expected UserNotFoundException to be thrown when new user is not found");
    }

    @Test
    void testAssignTaskMissingTask()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addUser("newUser", "password");
        storage.addCollaboration("username", "collaboration");

        assertThrows(TaskNotFoundException.class,
                () -> storage.assignTask("username", "collaboration", "newUser",
                        "task"),
                "Expected TaskNotFoundException to be thrown when new user is not found");
    }

    @Test
    void testAssignTaskExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException,
            TaskNameAlreadyExistsException, CollaborationNotFoundException {

        storage.addUser("username", "password");
        storage.addUser("newUser", "password");
        storage.addCollaboration("username", "collaboration");
        storage.addUserToCollaboration("username", "collaboration", "newUser");

        Task task = Task.builder("task").build();
        storage.addTask("username", task);

        assertDoesNotThrow(() -> storage.assignTask("username", "collaboration",
                "newUser", "task"),
                "Unexpected exception thrown when task is successfully assigned");
    }

    // todo test for validations - assignTask()

    @Test
    void testAssignTaskWithDateMissingCollaboration() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(CollaborationNotFoundException.class,
                () -> storage.assignTask("username", "collaboration",
                        "newUser", "task", LocalDate.now()),
                "Expected CollaborationNotFoundException to be thrown when collaboration is not found");
    }

    @Test
    void testAssignTaskWithDateMissingNewUser()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addCollaboration("username", "collaboration");

        assertThrows(UserNotFoundException.class,
                () -> storage.assignTask("username", "collaboration", "newUser",
                        "task", LocalDate.now()),
                "Expected UserNotFoundException to be thrown when new user is not found");
    }

    @Test
    void testAssignTaskWithDateMissingTask()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException {

        storage.addUser("username", "password");
        storage.addUser("newUser", "password");
        storage.addCollaboration("username", "collaboration");

        assertThrows(TaskNotFoundException.class,
                () -> storage.assignTask("username", "collaboration", "newUser",
                        "task", LocalDate.now()),
                "Expected TaskNotFoundException to be thrown when new user is not found");
    }

    @Test
    void testAssignTaskWithDateExistingTask()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException,
            TaskNameAlreadyExistsException, CollaborationNotFoundException, InvalidTimeIntervalException {

        storage.addUser("username", "password");
        storage.addUser("newUser", "password");
        storage.addCollaboration("username", "collaboration");
        storage.addUserToCollaboration("username", "collaboration", "newUser");

        Task task = Task.builder("task").setDate(LocalDate.now()).build();
        storage.addTask("username", task);

        assertDoesNotThrow(() -> storage.assignTask("username", "collaboration",
                        "newUser", "task", LocalDate.now()),
                "Unexpected exception thrown when task is successfully assigned");
    }

    @Test
    void testListUsersInCollaborationMissingCollaboration() throws UserAlreadyExistsException {
        storage.addUser("username", "password");

        assertThrows(CollaborationNotFoundException.class,
                () -> storage.listUsersInCollaboration("username", "collaboration"),
                "Expected CollaborationNotFoundException when collaboration does not exist");
    }

    @Test
    void testListUsersInCollaborationCreatedCollaboration()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException,
            CollaborationNotFoundException {

        storage.addUser("username", "password");
        storage.addUser("user1", "password");
        storage.addUser("user2", "password");
        storage.addCollaboration("username", "collaboration");
        storage.addUserToCollaboration("username", "collaboration", "user1");
        storage.addUserToCollaboration("username", "collaboration", "user2");

        Collection<String> response = storage.listUsersInCollaboration("username",
                "collaboration");

        assertEquals(2, response.size(), "Unexpected number of users returned");
        assertTrue(response.contains("user1"), "Expected user not returned");
        assertTrue(response.contains("user2"), "Expected user not returned");
    }

    @Test
    void testListUsersInCollaborationAssignedCollaboration()
            throws UserAlreadyExistsException, UserNotFoundException, CollaborationAlreadyExistsException,
            CollaborationNotFoundException {

        storage.addUser("username", "password");
        storage.addUser("user1", "password");
        storage.addUser("user2", "password");
        storage.addCollaboration("username", "collaboration");
        storage.addUserToCollaboration("username", "collaboration", "user1");
        storage.addUserToCollaboration("username", "collaboration", "user2");

        Collection<String> response = storage.listUsersInCollaboration("user1",
                "collaboration");

        assertEquals(2, response.size(), "Unexpected number of users returned");
        assertTrue(response.contains("user1"), "Expected user not returned");
        assertTrue(response.contains("user2"), "Expected user not returned");
    }
}
