package ru.tasktracker.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tasktracker.http.HttpTaskServer;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.TaskStatus;
import ru.tasktracker.service.InMemoryHistoryManager;
import ru.tasktracker.service.InMemoryTaskManager;
import ru.tasktracker.service.TaskManager;

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

public class HttpTaskManagerSubtaskTest {
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
    public void testAddSubTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);
        Subtask subtask = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtaskJson = gson.toJson(subtask);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest
                .newBuilder()
                .uri(subTasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();

        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Subtask 1", subtasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask1Json = gson.toJson(subtask1);
        request = HttpRequest.newBuilder().uri(subTasksUrl).POST(HttpRequest.BodyPublishers.ofString(subtask1Json)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        Subtask subtask2 = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));
        String subtask2Json = gson.toJson(subtask2);
        request = HttpRequest
                .newBuilder()
                .uri(subTasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtask2Json))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(subTasksUrl).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(2, subtasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");

        Subtask subtask = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask1Json = gson.toJson(subtask);
        request = HttpRequest
                .newBuilder()
                .uri(subTasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask createdSubtask = gson.fromJson(response.body(), Subtask.class);

        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId())).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals(subtasksFromManager.getFirst(), createdSubtask);
    }

    @Test
    public void testGetSubtaskByIdNotFoundException() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");

        Subtask subtask = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask1Json = gson.toJson(subtask);
        request = HttpRequest
                .newBuilder()
                .uri(subTasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder().uri(
                URI.create("http://localhost:8080/subtasks/" + manager.getSubtasks().getLast().getId() + 1)
        ).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
    }


    @Test
    public void testAddSubtaskTimeConflictException() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask1Json = gson.toJson(subtask1);
        request = HttpRequest
                .newBuilder()
                .uri(subTasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        gson.fromJson(response.body(), Subtask.class);

        Subtask subtask2 = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask2Json = gson.toJson(subtask2);
        request = HttpRequest
                .newBuilder()
                .uri(subTasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtask2Json))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask1Json = gson.toJson(subtask1);
        request = HttpRequest.newBuilder().uri(subTasksUrl).POST(HttpRequest.BodyPublishers.ofString(subtask1Json)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask createdSubtask = gson.fromJson(response.body(), Subtask.class);

        Subtask subtask2 = new Subtask(0, "Subtask 2", "Testing Subtask 2", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));
        String subtask2Json = gson.toJson(subtask2);
        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(subtask2Json))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask1Json = gson.toJson(subtask1);
        request = HttpRequest.newBuilder().uri(subTasksUrl).POST(HttpRequest.BodyPublishers.ofString(subtask1Json)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask createdSubtask = gson.fromJson(response.body(), Subtask.class);

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId()))
                .DELETE()
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(0, subtasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testDeleteSubtaskNotFoundException() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        URI subTasksUrl = URI.create("http://localhost:8080/subtasks");

        Subtask subtask1 = new Subtask(0, "Subtask 1", "Testing Subtask 1", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtask1Json = gson.toJson(subtask1);
        request = HttpRequest
                .newBuilder()
                .uri(subTasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        gson.fromJson(response.body(), Subtask.class);

        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/3")).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
    }
}
