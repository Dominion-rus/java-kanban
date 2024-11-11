package yandex.practicum.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;

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

class SubtasksHandlerRestTest {
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
    void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> epicResponse = client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, epicResponse.statusCode());
        Map<?, ?> epicBody = gson.fromJson(epicResponse.body(), Map.class);
        int epicId = ((Double) epicBody.get("id")).intValue();

        Subtask subtask = new Subtask("Subtask 1", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest createSubtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> subtaskResponse = client.send(createSubtaskRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, subtaskResponse.statusCode());
        Map<?, ?> subtaskBody = gson.fromJson(subtaskResponse.body(), Map.class);
        int subtaskId = ((Double) subtaskBody.get("id")).intValue();

        assertTrue(subtaskId > 0);
    }

    @Test
    void testGetAllSubtasks() throws IOException, InterruptedException {
        testCreateSubtask();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<?> subtasks = gson.fromJson(response.body(), List.class);
        assertFalse(subtasks.isEmpty());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> epicResponse = client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode());

        Map<?, ?> epicBody = gson.fromJson(epicResponse.body(), Map.class);
        int epicId = ((Double) epicBody.get("id")).intValue();

        Subtask subtask = new Subtask("Subtask 1", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest createSubtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> subtaskResponse = client.send(createSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtaskResponse.statusCode());

        Map<?, ?> subtaskBody = gson.fromJson(subtaskResponse.body(), Map.class);
        int subtaskId = ((Double) subtaskBody.get("id")).intValue();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());

        Map<?, ?> deleteResponseBody = gson.fromJson(deleteResponse.body(), Map.class);
        assertTrue((Boolean) deleteResponseBody.get("success"));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }


    @Test
    void testCreateSubtaskWithInvalidData() throws IOException, InterruptedException {
        Subtask invalidSubtask = new Subtask("", "", null, 0, null, null);
        String subtaskJson = gson.toJson(invalidSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertEquals("Неверные данные подзадачи: название и описание не могут быть пустыми.",
                responseBody.get("message"));
    }

    @Test
    void testGetNonexistentSubtask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertEquals("Подзадача с id 999 не найдена", responseBody.get("message"));
    }

    @Test
    void testDeleteNonexistentSubtask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertEquals("Подзадача с id 999 не найдена", responseBody.get("message"));
    }

    @Test
    void testCreateSubtaskWithOverlappingTime() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> epicResponse = client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, epicResponse.statusCode());
        Map<?, ?> epicBody = gson.fromJson(epicResponse.body(), Map.class);
        int epicId = ((Double) epicBody.get("id")).intValue();

        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 8, 10, 0));
        String subtask1Json = gson.toJson(subtask1);

        HttpRequest createSubtask1Request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> subtask1Response = client.send(createSubtask1Request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(201, subtask1Response.statusCode());

        Subtask subtask2 = new Subtask("Subtask 2", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 8, 10, 15));
        String subtask2Json = gson.toJson(subtask2);

        HttpRequest createSubtask2Request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtask2Json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> subtask2Response = client.send(createSubtask2Request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, subtask2Response.statusCode());
        Map<?, ?> responseBody = gson.fromJson(subtask2Response.body(), Map.class);
        assertEquals("Новая задача пересекается по времени выполнения с существующей задачей.",
                responseBody.get("message"));
    }
}
