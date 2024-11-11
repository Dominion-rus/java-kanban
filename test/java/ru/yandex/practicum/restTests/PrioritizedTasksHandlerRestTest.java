package yandex.practicum.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.utils.DurationAdapter;
import ru.yandex.practicum.utils.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

class PrioritizedTasksHandlerRestTest {
    private static HttpTaskServer server;
    private Gson gson;

    @BeforeEach
    void startServer() throws Exception {
        server = new HttpTaskServer();
        server.start();
        RestAssured.baseURI = "http://localhost:8080";
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();

    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void testGetPrioritizedTasks() {
        Task task1 = new Task("Task 1", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Description", Status.NEW, Duration.ofMinutes(15),
                LocalDateTime.now());

        given()
                .contentType("application/json")
                .body(gson.toJson(task1))
                .when()
                .post("/tasks")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body(gson.toJson(task2))
                .when()
                .post("/tasks")
                .then()
                .statusCode(201);

        get("/prioritized")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(2)) // Проверяем, что задачи есть
                .body("[0].title", equalTo("Task 2")) // Первая задача по приоритету
                .body("[1].title", equalTo("Task 1")); // Вторая задача по приоритету
    }
}

