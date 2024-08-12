package ru.tasktracker.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.tasktracker.http.handler.*;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private HttpServer httpServer;

    protected Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(Duration.class, new DurationSerializer())
            .registerTypeAdapter(Duration.class, new DurationDeserializer())
            .create();

    public Gson getGson() {
        return gson;
    }

    public HttpTaskServer(TaskManager taskManager) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TaskHandler(taskManager, gson));
            httpServer.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
            httpServer.createContext("/epics", new EpicHandler(taskManager, gson));
            httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
            httpServer.createContext("/prioritized", new PrioritizedTasksHandler(taskManager, gson));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getDefault());
        httpTaskServer.start();
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
