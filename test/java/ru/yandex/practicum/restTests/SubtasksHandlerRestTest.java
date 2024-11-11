package yandex.practicum.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.utils.DurationAdapter;
import ru.yandex.practicum.utils.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubtasksHandlerRestTest {
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
    void testCreateSubtask() {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Subtask subtask = new Subtask("Subtask 1", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        int subtaskId = given()
                .contentType("application/json")
                .body(gson.toJson(subtask))
                .when()
                .post("/subtasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        assertTrue(subtaskId > 0);
    }

    @Test
    void testGetAllSubtasks() {
        testCreateSubtask();
        Response response = get("/subtasks");
        response.then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Subtask subtask = new Subtask("Subtask 1", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        int subtaskId = given()
                .contentType("application/json")
                .body(gson.toJson(subtask))
                .when()
                .post("/subtasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        delete("/subtasks/" + subtaskId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }


    @Test
    void testCreateSubtaskWithInvalidData() {
        Subtask invalidSubtask = new Subtask("", "", null, 0, null, null);
        given()
                .contentType("application/json")
                .body(gson.toJson(invalidSubtask))
                .when()
                .post("/subtasks")
                .then()
                .statusCode(400)
                .body("message", equalTo("Неверные данные подзадачи: название и описание " +
                        "не могут быть пустыми."));
    }

    @Test
    void testGetNonexistentSubtask() {
        get("/subtasks/999")
                .then()
                .statusCode(404)
                .body("message", equalTo("Подзадача с id 999 не найдена"));
    }

    @Test
    void testDeleteNonexistentSubtask() {
        delete("/subtasks/999")
                .then()
                .statusCode(404)
                .body("message", equalTo("Подзадача с id 999 не найдена"));
    }

    @Test
    void testCreateSubtaskWithOverlappingTime() {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 8, 10, 0));
        given()
                .contentType("application/json")
                .body(gson.toJson(subtask1))
                .when()
                .post("/subtasks")
                .then()
                .statusCode(201);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 8, 10, 15));
        given()
                .contentType("application/json")
                .body(gson.toJson(subtask2))
                .when()
                .post("/subtasks")
                .then()
                .statusCode(406)
                .body("message", equalTo("Новая задача пересекается по времени выполнения с " +
                        "существующей задачей."));
    }

}

