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
import java.util.Collection;

public interface Storage {

    void addUser(String username, String password) throws UserAlreadyExistsException;

    void checkPassword(String username, String password) throws UserNotFoundException, WrongPasswordException;

    void addTask(String currentUser, Task task) throws TaskNameAlreadyExistsException, UserNotFoundException;

    void updateTask(String currentUser, Task task) throws TaskNotFoundException, UserNotFoundException;

    Task deleteTask(String currentUser, String taskName) throws TaskNotFoundException, UserNotFoundException;

    Task deleteTask(String currentUser, String taskName, LocalDate date)
            throws TaskNotFoundException, UserNotFoundException;

    Task getTask(String currentUser, String taskName) throws TaskNotFoundException, UserNotFoundException;

    Task getTask(String currentUser, String taskName, LocalDate date)
            throws TaskNotFoundException, UserNotFoundException;

    Collection<Task> listTasks(String currentUser) throws UserNotFoundException;

    Collection<Task> listTasks(String currentUser, LocalDate date) throws TaskNotFoundException, UserNotFoundException;

    Collection<Task> listTasks(String currentUser, String collaborationName)
            throws CollaborationNotFoundException, UserNotFoundException;

    Collection<Task> listCompletedTasks(String currentUser) throws UserNotFoundException;

    Collection<Task> listDashboard(String currentUser) throws TaskNotFoundException, UserNotFoundException;

    void finishTask(String currentUser, String taskName) throws TaskNotFoundException, UserNotFoundException;

    void addCollaboration(String currentUser, String name)
            throws CollaborationAlreadyExistsException, UserNotFoundException;

    void deleteCollaboration(String currentUser, String name)
            throws CollaborationNotFoundException, UserNotFoundException;

    Collection<Collaboration> getCollaborations(String currentUser) throws UserNotFoundException;

    void addUserToCollaboration(String currentUser, String collaborationName, String username)
            throws CollaborationNotFoundException, UserNotFoundException, UserAlreadyExistsException;

    void assignTask(String currentUser, String collaborationName, String username, String taskName)
            throws CollaborationNotFoundException, UserNotFoundException, TaskNotFoundException,
            TaskAlreadyExistsException;

    void assignTask(String currentUser, String collaborationName, String username, String taskName, LocalDate date)
            throws CollaborationNotFoundException, UserNotFoundException, TaskNotFoundException,
            TaskAlreadyExistsException;

    Collection<String> listUsersInCollaboration(String currentUser, String collaborationName)
            throws CollaborationNotFoundException, UserNotFoundException;
}
