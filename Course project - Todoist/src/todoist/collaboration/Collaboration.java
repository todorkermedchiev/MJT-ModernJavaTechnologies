package todoist.collaboration;

import todoist.exception.TaskAlreadyExistsException;
import todoist.exception.UserAlreadyExistsException;
import todoist.exception.UserNotFoundException;
import todoist.task.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Collaboration {
    private final String name;
    private final Map<String, Set<Task>> tasks;
    private final Set<String> users;

    public Collaboration(String name) {
        validateString(name, "Collaboration name cannot be null, empty or blank");

        this.name = name;
        this.tasks = new HashMap<>();
        this.users = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Collection<Task> getTasks() {
        Collection<Task> toBeReturned = new ArrayList<>();

        tasks.values().forEach(toBeReturned::addAll);

        return toBeReturned;
    }

    public Collection<String> getUsers() {
        return Collections.unmodifiableCollection(users);
    }

    public void addUser(String username) throws UserAlreadyExistsException {
        validateString(username, "Username cannot be null, empty or blank");

        if (users.contains(username)) {
            throw new UserAlreadyExistsException("User " + username + " already added in this collaboration");
        }

        users.add(username);
    }

    public void assignTask(String username, Task task)
            throws UserNotFoundException, TaskAlreadyExistsException {

        validateString(username, "Username cannot be null, empty ot blank");
        validateObject(task, "Task cannot be null");

        if (!users.contains(username)) {
            throw new UserNotFoundException("User \"" + username + "\" does not exist in this collaboration");
        }

        tasks.putIfAbsent(username, new HashSet<>());

        if (tasks.get(username).contains(task)) {
            throw new TaskAlreadyExistsException(String.format("Task \"%S\" already is assigned with user \"%s\".",
                    task.getName(), username));
        }
        tasks.get(username).add(task);
    }

    private void validateString(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateObject(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Collaboration other = (Collaboration) o;

        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
