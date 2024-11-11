package yandex.practicum.http.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.service.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EpicsHandlerRestTest {
    private static HttpTaskServer server;
    private static HttpClient client;
    private static Gson gson;

    @BeforeAll
    static void setUp() throws IOException {
        server = new HttpTaskServer();
        server.start();
        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @BeforeEach
    void cleanUp() {
        Managers.getDefault().removeAllEpics();
    }

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Description");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Map<?, ?> responseBody = gson.fromJson(response.body(), Map.class);
        assertNotNull(responseBody.get("id"));
    }

    @Test
    void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 2", "Another description");
        String json = gson.toJson(epic);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Epic 2"));
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 3", "To be deleted");
        String json = gson.toJson(epic);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        Map<?, ?> responseBody = gson.fromJson(createResponse.body(), Map.class);
        int epicId = ((Double) responseBody.get("id")).intValue();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode());
        assertTrue(deleteResponse.body().contains("\"success\":true"));
    }

    @Test
    void testCreateEpicWithInvalidData() throws IOException, InterruptedException {
        Epic invalidEpic = new Epic("", "");
        String json = gson.toJson(invalidEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Некорректные данные эпика"));
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 4", "Description");
        String json = gson.toJson(epic);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        Map<?, ?> responseBody = gson.fromJson(createResponse.body(), Map.class);
        int epicId = ((Double) responseBody.get("id")).intValue();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Epic 4"));
    }

    @Test
    void testDeleteAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 6", "To be deleted 1");
        Epic epic2 = new Epic("Epic 7", "To be deleted 2");

        HttpRequest createRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1)))
                .header("Content-Type", "application/json")
                .build();
        HttpRequest createRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic2)))
                .header("Content-Type", "application/json")
                .build();
        client.send(createRequest1, HttpResponse.BodyHandlers.ofString());
        client.send(createRequest2, HttpResponse.BodyHandlers.ofString());

        HttpRequest deleteAllRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteAllRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, deleteResponse.statusCode());
        assertTrue(deleteResponse.body().contains("\"success\":true"));

        HttpRequest getAllRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getAllRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("[]"));
    }
}
