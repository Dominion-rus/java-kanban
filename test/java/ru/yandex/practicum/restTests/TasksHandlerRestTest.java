package ru.yandex.practicum.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import io.restassured.response.Response;
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

import static io.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TasksHandlerRestTest {
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
    void testCreateTask() {
        Task task = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = given()
                .contentType("application/json")
                .body(gson.toJson(task))
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        assertThat(taskId, greaterThan(0));
    }

    @Test
    void testGetAllTasks() {
        testCreateTask();
        Response response = get("/tasks");
        response.then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testGetTaskById() {
        Task task = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = given()
                .contentType("application/json")
                .body(gson.toJson(task))
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        get("/tasks/" + taskId)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("title", equalTo("Task 1"))
                .body("description", equalTo("Description"));
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Task to Delete", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = given()
                .contentType("application/json")
                .body(gson.toJson(task))
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        delete("/tasks/" + taskId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void testDeleteAllTasks() {
        testCreateTask();
        delete("/tasks")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        Response response = get("/tasks");
        response.then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testCreateTaskWithInvalidData() {
        Task invalidTask = new Task("", "", null, null, null);
        given()
                .contentType("application/json")
                .body(gson.toJson(invalidTask))
                .when()
                .post("/tasks")
                .then()
                .statusCode(400)
                .body("message", equalTo("Некорректные данные задачи:" +
                        " название и описание не могут быть пустыми."));
    }

    @Test
    void testUpdateTask() {
        Task task = new Task("Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = given()
                .contentType("application/json")
                .body(gson.toJson(task))
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        task.setId(taskId);
        task.setStatus(Status.DONE);

        given()
                .contentType("application/json")
                .body(gson.toJson(task))
                .when()
                .post("/tasks")
                .then()
                .statusCode(200)
                .body("message", equalTo("Задача с id " + taskId + " успешно обновлена."));
    }

    @Test
    void testGetNonexistentTask() {
        get("/tasks/999")
                .then()
                .statusCode(404)
                .body("message", equalTo("Задача с id 999 не найдена."));
    }

    @Test
    void testDeleteNonexistentTask() {
        delete("/tasks/999")
                .then()
                .statusCode(404)
                .body("message", equalTo("Задача с id 999 не найдена."));
    }
}
