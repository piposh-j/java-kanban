package ru.tasktracker.model;
public enum TypeTask {
    TASK("TASK"),
    SUBTASK("SUBTASK"),
    EPIC("EPIC");

    private String value;

    TypeTask(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}