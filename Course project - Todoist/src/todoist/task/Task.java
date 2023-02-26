package todoist.task;

import todoist.exception.InvalidTimeIntervalException;

import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private final String name;
    private final LocalDate date;
    private final LocalDate dueDate;
    private final String description;
    private final String label;

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public static TaskBuilder builder(String name) {
        return new TaskBuilder(name);
    }

    private Task(TaskBuilder builder) {
        this.name = builder.name;
        this.date = builder.date;
        this.dueDate = builder.dueDate;
        this.description = builder.description;
        this.label = builder.label;
    }

    public static class TaskBuilder {
        private final String name;

        private LocalDate date;
        private LocalDate dueDate;
        private String description;
        private String label;

        private TaskBuilder(String name) {
            this.name = name;
        }

        public TaskBuilder setDate(LocalDate date) throws InvalidTimeIntervalException {
            if (dueDate != null && dueDate.isBefore(date)) {
                throw new InvalidTimeIntervalException("The date cannot be after the due date.");
            }

            this.date = date;
            return this;
        }

        public TaskBuilder setDueDate(LocalDate dueDate) throws InvalidTimeIntervalException {
            if (date != null && dueDate.isBefore(date)) {
                throw new InvalidTimeIntervalException("The due date cannot be before the date.");
            }

            this.dueDate = dueDate;
            return this;
        }

        public TaskBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }

    @Override
    public String toString() {
        return String.format("""
                # %s
                    date: %s
                    due-date: %s
                    description: %s
                """, name, date, dueDate, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Task other = (Task) o;
        return name.equals(other.name) && Objects.equals(date, other.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date);
    }
}