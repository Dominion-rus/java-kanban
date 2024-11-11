package yandex.practicum.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.utils.DurationAdapter;
import ru.yandex.practicum.utils.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

class EpicsHandlerRestTest {
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
    void testCreateEpic() {
        Epic epic = new Epic("Epic 1", "Description");
        given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201)
                .body("id", notNullValue());
    }

    @Test
    void testGetAllEpics() {
        Epic epic = new Epic("Epic 2", "Another description");
        given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201);

        get("/epics")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testDeleteEpic() {
        Epic epic = new Epic("Epic 3", "To be deleted");
        int epicId = given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        delete("/epics/" + epicId)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void testCreateEpicWithInvalidData() {
        Epic invalidEpic = new Epic("", "");
        given()
                .contentType("application/json")
                .body(gson.toJson(invalidEpic))
                .when()
                .post("/epics")
                .then()
                .statusCode(400)
                .body("message", equalTo("Некорректные данные эпика: " +
                        "название и описание не должны быть пустыми."));
    }

    @Test
    void testUpdateNonexistentEpic() {
        Epic nonExistentEpic = new Epic("Nonexistent", "Description");
        nonExistentEpic.setId(999);

        given()
                .contentType("application/json")
                .body(gson.toJson(nonExistentEpic))
                .when()
                .post("/epics")
                .then()
                .statusCode(404) // Ошибка 404 для несуществующего эпика
                .body("status", equalTo("error"))
                .body("message", equalTo("Эпик с id 999 не найден."));
    }

    @Test
    void testGetEpicById() {
        Epic epic = new Epic("Epic 4", "Description");
        int epicId = given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        get("/epics/" + epicId)
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("title", equalTo("Epic 4"))
                .body("description", equalTo("Description"));
    }

    @Test
    void testGetEpicSubtasks() {
        Epic epic = new Epic("Epic 5", "With subtasks");
        int epicId = given()
                .contentType("application/json")
                .body(gson.toJson(epic))
                .when()
                .post("/epics")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        get("/epics/" + epicId + "/subtasks")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("size()", equalTo(0)); // Подзадач пока нет
    }

    @Test
    void testDeleteAllEpics() {
        Epic epic1 = new Epic("Epic 6", "To be deleted 1");
        Epic epic2 = new Epic("Epic 7", "To be deleted 2");

        given()
                .contentType("application/json")
                .body(gson.toJson(epic1))
                .when()
                .post("/epics")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body(gson.toJson(epic2))
                .when()
                .post("/epics")
                .then()
                .statusCode(201);

        delete("/epics")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        get("/epics")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }
}
