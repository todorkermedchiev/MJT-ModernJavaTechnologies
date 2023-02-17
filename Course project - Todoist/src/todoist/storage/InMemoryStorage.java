package todoist.storage;

import todoist.collaboration.Collaboration;
import todoist.exception.CollaborationAlreadyExistsException;
import todoist.exception.CollaborationNotFoundException;
import todoist.exception.TaskAlreadyExistsException;
import todoist.exception.TaskNameAlreadyExistsException;
import todoist.exception.TaskNotFoundException;
import todoist.exception.UserAlreadyExistsException;
import todoist.exception.UserNotFoundException;
import todoist.exception.WrongPasswordException;
import todoist.task.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InMemoryStorage implements Storage {
    private static final String VALIDATION_EXCEPTION_MESSAGE = "Parameter \"%s\" cannot be %s.";
    private final Map<String, String> users;
    private final Map<String, Map<String, Task>> inbox;
    private final Map<String, Map<LocalDate, Map<String, Task>>> tasksByDate;
    private final Map<String, Set<Task>> completedTasks;
    private final Map<String, Map<String, Collaboration>> createdCollaborations;
    private final Map<String, Map<String, Collaboration>> assignedCollaborations;

    public InMemoryStorage() {
        users = new HashMap<>();
        inbox = new HashMap<>();
        tasksByDate = new HashMap<>();
        completedTasks = new HashMap<>();
        createdCollaborations = new HashMap<>();
        assignedCollaborations = new HashMap<>();
    }

    @Override
    public void addUser(String username, String password) throws UserAlreadyExistsException {
        validateString(username, "username");
        validateString(password, "password");

        if (users.containsKey(username)) {
            throw new UserAlreadyExistsException("User with username \"" + username + "\" already exists.");
        }

        users.put(username, password);

        inbox.putIfAbsent(username, new HashMap<>());
        tasksByDate.putIfAbsent(username, new HashMap<>());
        completedTasks.putIfAbsent(username, new HashSet<>());
        createdCollaborations.putIfAbsent(username, new HashMap<>());
        assignedCollaborations.putIfAbsent(username, new HashMap<>());
    }

    @Override
    public void login(String username, String password) throws UserNotFoundException, WrongPasswordException {
        validateString(username, "username");
        validateString(password, "password");

        if (!users.containsKey(username)) {
            throw new UserNotFoundException("User with username \"" + username + "\" does not exist.");
        }
        if (!users.get(username).equals(password)) {
            throw new WrongPasswordException("Wrong password");
        }
    }

    @Override
    public void addTask(String currentUser, Task task) throws TaskNameAlreadyExistsException, UserNotFoundException {
        validateObject(task, "task");
        checkIfUserExists(currentUser);

        if (task.getDate() == null) {

            if (inbox.get(currentUser).containsKey(task.getName())) {
                throw new TaskNameAlreadyExistsException("Task with name \"" + task.getName() +
                        "\" already exists in inbox folder");
            }

            inbox.get(currentUser).put(task.getName(), task);
        } else {
            tasksByDate.get(currentUser).putIfAbsent(task.getDate(), new HashMap<>());

            if (tasksByDate.get(currentUser).get(task.getDate()).containsKey(task.getName())) {
                throw new TaskNameAlreadyExistsException("Task with name \"" + task.getName() +
                        "\" and execution date " + task.getDate().toString() + " already exists.");
            }

            tasksByDate.get(currentUser).get(task.getDate()).put(task.getName(), task);
        }
    }

    @Override
    public void updateTask(String currentUser, Task newTask) throws TaskNotFoundException, UserNotFoundException {
        validateObject(newTask, "newTask");
        checkIfUserExists(currentUser);

        if (newTask.getDate() == null) {
            if (!inbox.containsKey(currentUser) || !inbox.get(currentUser).containsKey(newTask.getName())) {
                throw new TaskNotFoundException("Task with name \"" + newTask.getName() +
                        "\" does not exist in inbox folder");
            }

            inbox.get(currentUser).put(newTask.getName(), newTask);
        } else {
            if (!tasksByDate.containsKey(currentUser) ||
                !tasksByDate.get(currentUser).containsKey(newTask.getDate()) ||
                !tasksByDate.get(currentUser).get(newTask.getDate()).containsKey(newTask.getName())) {

                throw new TaskNotFoundException("Task with name \"" + newTask.getName() + "\" and execution date " +
                        newTask.getDate().toString() + " does not exist in inbox folder");
            }

            tasksByDate.get(currentUser).get(newTask.getDate()).put(newTask.getName(), newTask);
        }
    }

    @Override
    public Task deleteTask(String currentUser, String taskName) throws TaskNotFoundException, UserNotFoundException {
        validateString(taskName, "taskName");
        checkIfUserExists(currentUser);

        if (!inbox.get(currentUser).containsKey(taskName)) {
            throw new TaskNotFoundException("Task with name \"" + taskName + "\" does not exist.");
        }

        return inbox.get(currentUser).remove(taskName);
    }

    @Override
    public Task deleteTask(String currentUser, String taskName, LocalDate date)
            throws TaskNotFoundException, UserNotFoundException {

        validateString(taskName, "taskName");
        validateObject(date, "date");
        checkIfUserExists(currentUser);

        if (!tasksByDate.get(currentUser).containsKey(date) ||
            !tasksByDate.get(currentUser).get(date).containsKey(taskName)) {

            throw new TaskNotFoundException("Task with name \"" + taskName + "\" and execution date " +
                    date + "does not exist.");
        }

        return tasksByDate.get(currentUser).get(date).remove(taskName);
    }

    @Override
    public Task getTask(String currentUser, String taskName) throws TaskNotFoundException, UserNotFoundException {
        validateString(taskName, "taskName");
        checkIfUserExists(currentUser);

        if (!inbox.get(currentUser).containsKey(taskName)) {
            throw new TaskNotFoundException("Task with name \"" + taskName + "\" does not exist.");
        }

        return inbox.get(currentUser).get(taskName);
    }

    @Override
    public Task getTask(String currentUser, String taskName, LocalDate date)
            throws TaskNotFoundException, UserNotFoundException {

        validateString(taskName, "taskName");
        validateObject(date, "date");
        checkIfUserExists(currentUser);

        if (!tasksByDate.get(currentUser).containsKey(date) ||
            !tasksByDate.get(currentUser).get(date).containsKey(taskName)) {

            throw new TaskNotFoundException("Task with name \"" + taskName + "\" and execution date " +
                    date + "does not exist.");
        }

        return tasksByDate.get(currentUser).get(date).get(taskName);
    }

    @Override
    public Collection<Task> listTasks(String currentUser) throws UserNotFoundException {
        checkIfUserExists(currentUser);

        List<Task> toBeReturned = new ArrayList<>();

        if (tasksByDate.containsKey(currentUser)) {
            tasksByDate.get(currentUser).values().forEach(m -> toBeReturned.addAll(m.values()));
        }

        if (inbox.containsKey(currentUser)) {
            toBeReturned.addAll(inbox.get(currentUser).values());
        }

        return toBeReturned;
    }

    @Override
    public Collection<Task> listCompletedTasks(String currentUser) throws UserNotFoundException {
        checkIfUserExists(currentUser);

        return Collections.unmodifiableSet(completedTasks.get(currentUser));
    }

    @Override
    public Collection<Task> listTasks(String currentUser, LocalDate date)
            throws TaskNotFoundException, UserNotFoundException {

        validateObject(date, "date");
        checkIfUserExists(currentUser);

        if (!tasksByDate.get(currentUser).containsKey(date)) {
            throw new TaskNotFoundException("Tasks with execution date " + date + " not found for the logged user");
        }

        return Collections.unmodifiableCollection(tasksByDate.get(currentUser).get(date).values());
    }

    @Override
    public Collection<Task> listTasks(String currentUser, String collaborationName)
            throws CollaborationNotFoundException, UserNotFoundException {

        validateString(collaborationName, "collaborationName");
        checkIfUserExists(currentUser);

        if (createdCollaborations.get(currentUser).containsKey(collaborationName)) {
            return createdCollaborations.get(currentUser).get(collaborationName).getTasks();
        }

        if (assignedCollaborations.get(currentUser).containsKey(collaborationName)) {
            return assignedCollaborations.get(currentUser).get(collaborationName).getTasks();
        }

        throw new CollaborationNotFoundException("Collaboration with name \"" + collaborationName +
                "\" not found for the logged user.");
    }

    @Override
    public Collection<Task> listDashboard(String currentUser) throws TaskNotFoundException, UserNotFoundException {
        checkIfUserExists(currentUser);
        return listTasks(currentUser, LocalDate.now());
    }

    @Override
    public void finishTask(String currentUser, String taskName) throws TaskNotFoundException, UserNotFoundException {
        validateString(taskName, "taskName");
        checkIfUserExists(currentUser);

        completedTasks.get(currentUser).add(deleteTask(currentUser, taskName));
    }

    @Override
    public void addCollaboration(String currentUser, String name)
            throws CollaborationAlreadyExistsException, UserNotFoundException {

        validateString(name, "collaborationName");
        checkIfUserExists(currentUser);

        if (createdCollaborations.get(currentUser).containsKey(name)) {
            throw new CollaborationAlreadyExistsException("Collaboration \"" + name +
                    "\" already exists for the logged user.");
        }

        createdCollaborations.get(currentUser).put(name, new Collaboration(name));
    }

    @Override
    public void deleteCollaboration(String currentUser, String collaborationName)
            throws CollaborationNotFoundException, UserNotFoundException {

        validateString(collaborationName, "collaborationName");
        checkIfUserExists(currentUser);

        if (!createdCollaborations.get(currentUser).containsKey(collaborationName)) {
            throw new CollaborationNotFoundException("Collaboration \"" + collaborationName +
                    "\" not found for the logged user.");
        }

        for (String user : createdCollaborations.get(currentUser).get(collaborationName).getUsers()) {
            assignedCollaborations.get(user).remove(collaborationName);
        }

        for (Task currentTask : createdCollaborations.get(currentUser).get(collaborationName).getTasks()) {
            if (currentTask.getDate() == null) {
                inbox.get(currentUser).remove(currentTask.getName());
            } else {
                tasksByDate.get(currentUser).get(currentTask.getDate()).remove(currentTask.getName());
            }
        }

        createdCollaborations.get(currentUser).remove(collaborationName);
    }

    @Override
    public Collection<Collaboration> getCollaborations(String currentUser) throws UserNotFoundException {
        checkIfUserExists(currentUser);

        List<Collaboration> toBeReturned = new ArrayList<>();

        if (!createdCollaborations.get(currentUser).isEmpty()) {
            toBeReturned.addAll(createdCollaborations.get(currentUser).values());
        }

        if (!assignedCollaborations.get(currentUser).isEmpty()) {
            toBeReturned.addAll(assignedCollaborations.get(currentUser).values());
        }

        return toBeReturned;
    }

    @Override
    public void addUserToCollaboration(String currentUser, String collaborationName, String username)
            throws CollaborationNotFoundException, UserNotFoundException, UserAlreadyExistsException {

        validateString(collaborationName, "collaborationName");
        validateString(username, "username");

        checkIfUserExists(currentUser);

        if (!createdCollaborations.get(currentUser).containsKey(collaborationName)) {
            throw new CollaborationNotFoundException("Collaboration \"" + collaborationName +
                    "\" was not found for the logged user.");
        }
        if (!users.containsKey(username)) {
            throw new UserNotFoundException("User \"" + username + "\" not found.");
        }

        Collaboration collaboration = createdCollaborations.get(currentUser).get(collaborationName);

        collaboration.addUser(username);
        assignedCollaborations.get(username).put(collaborationName, collaboration);
    }

    @Override
    public void assignTask(String currentUser, String collaborationName, String username, String taskName)
            throws CollaborationNotFoundException, UserNotFoundException, TaskNotFoundException,
                    TaskAlreadyExistsException {

        validateString(collaborationName, "collaborationName");
        validateString(username, "username");
        validateString(taskName, "taskName");

        checkIfUserExists(currentUser);

        if (!createdCollaborations.get(currentUser).containsKey(collaborationName)) {
            throw new CollaborationNotFoundException("Collaboration \"" + collaborationName +
                    "\" was not found for the logged user.");
        }
        if (!users.containsKey(username)) {
            throw new UserNotFoundException("User \"" + username + "\" not found.");
        }
        if (!inbox.get(currentUser).containsKey(taskName)) {
            throw new TaskNotFoundException("Task \"" + taskName + "\" not found in inbox folder.");
        }

        createdCollaborations.get(currentUser)
                .get(collaborationName)
                .assignTask(username, inbox.get(currentUser).get(taskName));
    }

    @Override
    public void assignTask(String currentUser, String collaborationName, String username, String task, LocalDate date)
            throws CollaborationNotFoundException, UserNotFoundException, TaskNotFoundException,
                   TaskAlreadyExistsException {

        checkIfUserExists(currentUser);

        validateString(collaborationName, "collaborationName");
        validateString(username, "username");
        validateString(task, "task");

        if (!createdCollaborations.get(currentUser).containsKey(collaborationName)) {
            throw new CollaborationNotFoundException("Collaboration \"" + collaborationName +
                    "\" was not found for the logged user.");
        }
        if (!users.containsKey(username)) {
            throw new UserNotFoundException("User \"" + username + "\" not found.");
        }
        if (!tasksByDate.get(currentUser).containsKey(date) ||
            !tasksByDate.get(currentUser).get(date).containsKey(task)) {
            throw new TaskNotFoundException(String.format("Task \"%s\" not found for %s.", task, date));
        }

        createdCollaborations.get(currentUser)
                .get(collaborationName)
                .assignTask(username, tasksByDate.get(currentUser).get(date).get(task));
    }

    @Override
    public Collection<String> listUsersInCollaboration(String currentUser, String collaborationName)
            throws CollaborationNotFoundException, UserNotFoundException {

        validateString(collaborationName, "collaborationName");
        checkIfUserExists(currentUser);

        if (createdCollaborations.get(currentUser).containsKey(collaborationName)) {
            Collection<String> users = createdCollaborations.get(currentUser).get(collaborationName).getUsers();
            return Collections.unmodifiableCollection(users);
        }

        if (assignedCollaborations.get(currentUser).containsKey(collaborationName)) {
            Collection<String> users = assignedCollaborations.get(currentUser).get(collaborationName).getUsers();
            return Collections.unmodifiableCollection(users);
        }

        throw new CollaborationNotFoundException("Collaboration with name \"" + collaborationName +
                "\" not found for the logged user.");
    }

    private void validateObject(Object object, String variableName) {
        if (object == null) {
            throw new IllegalArgumentException(String.format(VALIDATION_EXCEPTION_MESSAGE, variableName, "null"));
        }
    }

    private void validateString(String string, String variableName) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException(String.format(VALIDATION_EXCEPTION_MESSAGE, variableName,
                    "null, empty or blank"));
        }
    }

    private void checkIfUserExists(String username) throws UserNotFoundException {
        if (!users.containsKey(username)) {
            throw new UserNotFoundException("User \"" + username + "\" does not exists.");
        }
    }
}
