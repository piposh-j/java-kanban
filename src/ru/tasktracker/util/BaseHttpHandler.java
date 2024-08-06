package ru.tasktracker.util;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.exception.ErrorMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


public class BaseHttpHandler {
    protected final Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }

    protected void writeResponse(HttpExchange exchange, Object body, int responseCode) throws IOException {
        String response = gson.toJson(body);
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.getResponseHeaders().add("Content-type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(responseCode, response.getBytes().length);
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        exchange.close();
    }

    protected void writeResponse(HttpExchange exchange, ErrorMessage errorMessage, int responseCode) throws IOException {
        String response = gson.toJson(errorMessage);
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.getResponseHeaders().add("Content-type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(responseCode, response.getBytes().length);
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        exchange.close();
    }

    protected boolean checkIdInUrl(String path) {
        String[] pathArr = path.split("/");
        return pathArr.length >= 3;
    }

    protected int getIdInUrl(String path) {
        String[] pathArr = path.split("/");
        return Integer.parseInt(pathArr[2]);
    }
}
