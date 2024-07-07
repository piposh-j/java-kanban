package ru.tasktracker.exception;

public class TaskTimeConflictException extends RuntimeException {
    public TaskTimeConflictException(String s) {
        super(s);
    }

    public TaskTimeConflictException() {
    }
}
