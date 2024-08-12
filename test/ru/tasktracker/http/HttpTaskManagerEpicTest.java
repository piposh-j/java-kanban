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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicTest {
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
    public void testAddEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic 1", "Testing Epic 1");
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

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 1", epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
        assertEquals("Testing Epic 1", epicsFromManager.getFirst().getDescription(), "Некорректное описание эпика");
        assertFalse(epicsFromManager.getFirst().getSubtaskIds().isEmpty(), "Список сабтасков пуст");
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic1 = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson1 = gson.toJson(epic1);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson1))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        Epic epic2 = new Epic(0, "Epic2", "Testing Epic 2");
        String epicJson2 = gson.toJson(epic2);

        request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson2))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());


        request = HttpRequest.newBuilder().uri(epicUrl).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(2, epicsFromManager.size(), "Некорректное количество эпиков");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Epic epic = new Epic(0, "Epic 1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic = gson.fromJson(response.body(), Epic.class);

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(epicsFromManager.getFirst(), epic);
    }

    @Test
    public void testGetEpicByIdNotFoundException() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        Epic epic = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Epic> epicsFromManager = manager.getEpics();
        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicsFromManager.getLast().getId() + 1))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        Epic epic1 = new Epic(0, "Epic1", "Testing Epic 1");
        String epicJson1 = gson.toJson(epic1);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson1))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        epic1 = gson.fromJson(response.body(), Epic.class);


        Epic epic2 = new Epic(0, "Epic2", "Testing Epic 2");
        String epicJson2 = gson.toJson(epic2);
        URI epicUrlUpdate = URI.create("http://localhost:8080/epics/" + epic1.getId());
        request = HttpRequest
                .newBuilder()
                .uri(epicUrlUpdate)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson2))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
        assertEquals(epic2.getName(), epicsFromManager.getFirst().getName());
        assertEquals(epic2.getDescription(), epicsFromManager.getFirst().getDescription());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
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

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Epic> epicFromManager = manager.getEpics();
        assertNotNull(epicFromManager, "Задачи не возвращаются");
        assertEquals(0, epicFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testDeleteEpicNotFoundException() throws IOException, InterruptedException {

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

        request = HttpRequest
                .newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + manager.getEpics().getLast().getId() + 1))
                .DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
    }
}
