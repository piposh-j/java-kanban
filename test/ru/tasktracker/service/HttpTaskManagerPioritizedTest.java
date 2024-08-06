package ru.tasktracker.service;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tasktracker.HttpTaskServer;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerPioritizedTest {
    TaskManager manager = new InMemoryTaskManager((new InMemoryHistoryManager()));
    HttpTaskServer httpTaskServer = new HttpTaskServer(manager);
    Gson gson = httpTaskServer.getGson();

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        httpTaskServer.start();
    }

    @AfterEach
    public void tearDown() {
        httpTaskServer.stop();
    }

    @Test
    public void testGetPrioritized() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        Epic createdEpic = manager.addEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, createdEpic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        Subtask createdSubtask = manager.addSubtask(subtask);

        Task task = new Task(0, "Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));
        Task createdTask = manager.addTask(task);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + createdEpic.getId()))
                .GET()
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId()))
                .GET()
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + createdTask.getId()))
                .GET()
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        URI historyUrl = URI.create("http://localhost:8080/prioritized");
        request = HttpRequest
                .newBuilder()
                .uri(historyUrl)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Set<Task> historyFromManager = manager.getPrioritizedTasks();
        assertNotNull(historyFromManager, "Задачи не возвращаются");
        assertEquals(2, historyFromManager.size());
    }
}
