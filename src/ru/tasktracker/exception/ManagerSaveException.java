package ru.tasktracker.exception;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String s) {
        super(s);
    }

    public ManagerSaveException() {
    }
}
