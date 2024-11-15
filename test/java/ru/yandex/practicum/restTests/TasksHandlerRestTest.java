package yandex.practicum.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TasksHandlerRestTest {
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws Exception {
        server = new HttpTaskServer();
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertTrue(responseBody.containsKey("id"));
        assertNotNull(responseBody.get("id"));
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        testCreateTask();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<?> tasks = gson.fromJson(response.body(), List.class);
        assertFalse(tasks.isEmpty());
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, createResponse.statusCode());
        Map<?, ?> responseBody = gson.fromJson(createResponse.body(), Map.class);
        int taskId = ((Double) responseBody.get("id")).intValue();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());
        Task fetchedTask = gson.fromJson(getResponse.body(), Task.class);
        assertEquals("Task 1", fetchedTask.getTitle());
        assertEquals("Description", fetchedTask.getDescription());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Task to Delete", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, createResponse.statusCode());
        Map<?, ?> responseBody = gson.fromJson(createResponse.body(), Map.class);
        int taskId = ((Double) responseBody.get("id")).intValue();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode());
        Map<?, ?> deleteResponseBody = gson.fromJson(deleteResponse.body(), Map.class);
        assertEquals(true, deleteResponseBody.get("success"));
    }

    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        testCreateTask();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode());
        Map<?, ?> responseBody = gson.fromJson(deleteResponse.body(), Map.class);
        assertEquals(true, responseBody.get("success"));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        List<?> tasks = gson.fromJson(getResponse.body(), List.class);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testCreateTaskWithInvalidData() throws IOException, InterruptedException {
        Task invalidTask = new Task("", "", null, null, null);
        String taskJson = gson.toJson(invalidTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertEquals("Некорректные данные задачи: название и описание не могут быть пустыми.",
                responseBody.get("message"));
    }

    @Test
    void testGetNonexistentTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertEquals("Задача с id 999 не найдена.", responseBody.get("message"));
    }

    @Test
    void testDeleteNonexistentTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertEquals("Задача с id 999 не найдена.", responseBody.get("message"));
    }
}
