package ru.tasktracker.service;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tasktracker.HttpTaskServer;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerTasksTest {
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
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task(0, "Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasks();

        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllTask() throws IOException, InterruptedException {
        Task task1 = new Task(0, "Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task(1, "Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));

        String taskJson1 = gson.toJson(task1);
        String taskJson2 = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task1 = new Task(0, "Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson = gson.toJson(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task task = gson.fromJson(response.body(), Task.class);

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(tasksFromManager.getFirst(), task);
    }

    @Test
    public void testGetTaskByIdNotFoundException() throws IOException, InterruptedException {
        Task task1 = new Task(0, "Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson = gson.toJson(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(
                URI.create("http://localhost:8080/tasks/" + manager.getTasks().getLast().getId() + 1)
        ).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }


    @Test
    public void testAddTaskTimeConflictException() throws IOException, InterruptedException {

        Task task1 = new Task(0, "Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task(0, "Test 3", "Testing task 3",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson1 = gson.toJson(task1);
        String taskJson2 = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {

        Task task1 = new Task(0, "Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task(0, "Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));

        String taskJson1 = gson.toJson(task1);
        String taskJson2 = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task task = gson.fromJson(response.body(), Task.class);

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {

        Task task1 = new Task(0, "Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson1 = gson.toJson(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task task = gson.fromJson(response.body(), Task.class);

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testDeleteTaskNotFoundException() throws IOException, InterruptedException {

        Task task1 = new Task(0, "Test 1", "Testing task 1",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson1 = gson.toJson(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        gson.fromJson(response.body(), Task.class);

        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/2")).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }
}
