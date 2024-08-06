package ru.tasktracker.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.tasktracker.enums.HttpMethod;
import ru.tasktracker.exception.ErrorMessage;
import ru.tasktracker.exception.NotFoundException;
import ru.tasktracker.exception.TaskTimeConflictException;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.BaseHttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String httpMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            switch (HttpMethod.valueOf(httpMethod)) {
                case GET -> {
                    if (checkIdInUrl(path)) {
                        int id = getIdInUrl(path);
                        try {
                            Subtask task = taskManager.getSubtaskById(id);
                            writeResponse(exchange, task, 200);
                        } catch (NotFoundException exception) {
                            writeResponse(exchange, new ErrorMessage(exception.getMessage()), 404);
                        }
                    } else {
                        writeResponse(exchange, taskManager.getSubtasks(), 200);
                    }
                }
                case POST -> {
                    try {
                        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        Subtask subtask = gson.fromJson(requestBody, Subtask.class);
                        if (checkIdInUrl(path)) {
                            int id = getIdInUrl(path);
                            subtask.setId(id);
                            writeResponse(exchange, taskManager.updateSubtask(subtask), 200);
                        } else {
                            writeResponse(exchange, taskManager.addSubtask(subtask), 201);
                        }
                    } catch (NotFoundException exception) {
                        writeResponse(exchange, new ErrorMessage(exception.getMessage()), 404);
                    } catch (TaskTimeConflictException exception) {
                        writeResponse(exchange, new ErrorMessage(exception.getMessage()), 406);
                    }
                }
                case DELETE -> {
                    int id = getIdInUrl(path);
                    try {
                        Subtask subtask = taskManager.deleteSubtaskById(id);
                        writeResponse(exchange, subtask, 200);
                    } catch (NotFoundException exception) {
                        writeResponse(exchange, new ErrorMessage(exception.getMessage()), 404);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            writeResponse(exchange, new ErrorMessage("Внутренняя ошибка сервера"), 500);
        }
    }
}
