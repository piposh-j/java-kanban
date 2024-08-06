package ru.tasktracker.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.tasktracker.enums.HttpMethod;
import ru.tasktracker.exception.ErrorMessage;
import ru.tasktracker.exception.NotFoundException;
import ru.tasktracker.exception.TaskTimeConflictException;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.BaseHttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager, Gson gson) {
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
                            Task task = taskManager.getTaskById(id);
                            writeResponse(exchange, task, 200);
                        } catch (NotFoundException exception) {
                            writeResponse(exchange, new ErrorMessage(exception.getMessage()), 404);
                        }
                    } else {
                        writeResponse(exchange, taskManager.getTasks(), 200);
                    }
                }
                case POST -> {
                    try {
                        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        Task task = gson.fromJson(requestBody, Task.class);
                        if (checkIdInUrl(path)) {
                            int id = getIdInUrl(path);
                            task.setId(id);
                            writeResponse(exchange, taskManager.updateTask(task), 200);
                        } else {
                            writeResponse(exchange, taskManager.addTask(task), 201);
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
                        Task task = taskManager.deleteTaskById(id);
                        writeResponse(exchange, task, 200);
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
