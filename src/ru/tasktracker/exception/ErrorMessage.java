package ru.tasktracker.exception;

public class ErrorMessage {
    public String getErrorMessage() {
        return errorMessage;
    }

    private final String errorMessage;

    public ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
