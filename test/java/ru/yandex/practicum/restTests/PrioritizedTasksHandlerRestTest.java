package yandex.practicum.http.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PrioritizedTasksHandlerRestTest {
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
    public void testGetPrioritizedTasks() throws Exception {
        Task task1 = new Task("Task 1", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Description", Status.NEW, Duration.ofMinutes(15),
                LocalDateTime.now());

        HttpRequest requestTask1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .header("Content-Type", "application/json")
                .build();

        HttpRequest requestTask2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .header("Content-Type", "application/json")
                .build();

        client.send(requestTask1, HttpResponse.BodyHandlers.ofString());
        client.send(requestTask2, HttpResponse.BodyHandlers.ofString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type taskListType = new TypeToken<List<Task>>() {}.getType();
        List<Task> prioritizedTasks = gson.fromJson(response.body(), taskListType);

        assertNotNull(prioritizedTasks, "Приоритетные задачи не возвращаются.");
        assertEquals(2, prioritizedTasks.size(), "Некорректное количество задач.");
        assertEquals("Task 2", prioritizedTasks.get(0).getTitle(), "Первая задача " +
                "не соответствует приоритету.");
        assertEquals("Task 1", prioritizedTasks.get(1).getTitle(), "Вторая задача " +
                "не соответствует приоритету.");
    }

}
