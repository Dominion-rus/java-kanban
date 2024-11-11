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

import static org.junit.jupiter.api.Assertions.*;

class HistoryHandlerRestTest {
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
    public void tearDown() {
        server.stop();
    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> createTaskResponse = client.send(createTaskRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, createTaskResponse.statusCode(), "Задача должна успешно создаться.");

        HttpRequest getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .GET()
                .build();
        HttpResponse<String> getTaskResponse = client.send(getTaskRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getTaskResponse.statusCode(), "Должно вернуться 200 для существующей " +
                "задачи.");

        HttpRequest getHistoryRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> getHistoryResponse = client.send(getHistoryRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getHistoryResponse.statusCode(), "История должна возвращаться с кодом 200.");

        List<?> history = gson.fromJson(getHistoryResponse.body(), List.class);
        assertNotNull(history, "История не должна быть null.");
        assertFalse(history.isEmpty(), "История не должна быть пустой.");
    }

}
