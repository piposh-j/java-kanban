package ru.tasktracker.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.tasktracker.enums.HttpMethod;
import ru.tasktracker.exception.ErrorMessage;
import ru.tasktracker.exception.NotFoundException;
import ru.tasktracker.exception.TaskTimeConflictException;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.service.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager, Gson gson) {
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
                        try {
                            int id = getIdInUrl(path);
                            if (path.contains("/subtasks")) {
                                List<Subtask> subtaskList = taskManager.getSubtasksByEpic(id);
                                writeResponse(exchange, subtaskList, 200);
                                return;
                            }
                            Epic epic = taskManager.getEpicById(id);
                            writeResponse(exchange, epic, 200);
                        } catch (NotFoundException exception) {
                            writeResponse(exchange, new ErrorMessage(exception.getMessage()), 404);
                        }
                    } else {
                        writeResponse(exchange, taskManager.getEpics(), 200);
                    }
                }
                case POST -> {
                    try {
                        Epic epic = gson.fromJson(readRequest(exchange), Epic.class);
                        if (checkIdInUrl(path)) {
                            int id = getIdInUrl(path);
                            epic.setId(id);
                            writeResponse(exchange, taskManager.updateEpic(epic), 200);
                        } else {
                            writeResponse(exchange, taskManager.addEpic(epic), 201);
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
                        Epic epic = taskManager.deleteEpicById(id);
                        writeResponse(exchange, epic, 200);
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
