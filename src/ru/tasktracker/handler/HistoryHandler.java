package ru.tasktracker.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.tasktracker.enums.HttpMethod;
import ru.tasktracker.exception.ErrorMessage;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.BaseHttpHandler;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String httpMethod = exchange.getRequestMethod();
        try {
            switch (HttpMethod.valueOf(httpMethod)) {
                case GET -> {
                    writeResponse(exchange, taskManager.getHistory(), 200);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            writeResponse(exchange, new ErrorMessage("Внутренняя ошибка сервера"), 500);
        }
    }
}
