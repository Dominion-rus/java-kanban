package ru.yandex.practicum.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
        if (taskManager == null) {
            throw new IllegalArgumentException("TaskManager cannot be null");
        }
        this.taskManager = taskManager;
    }

    @Override
    public void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.matches("/epics/?")) {
                    handleGetAllEpics(exchange);
                } else if (path.matches("/epics/\\d+")) {
                    handleGetEpicById(exchange);
                } else if (path.matches("/epics/\\d+/subtasks")) {
                    handleGetEpicSubtasks(exchange); // Обработчик для /epics/{id}/subtasks
                } else {
                    sendNotFound(exchange, "Некорректный путь: " + path);
                }
            } else if ("POST".equalsIgnoreCase(method) && path.matches("/epics/?")) {
                handleCreateEpic(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (path.matches("/epics/\\d+")) {
                    handleDeleteEpicById(exchange);
                } else if (path.matches("/epics/?")) {
                    handleDeleteAllEpics(exchange);
                } else {
                    sendNotFound(exchange, "Некорректный путь: " + path);
                }
            } else {
                sendMethodNotAllowed(exchange); // Обработка неподдерживаемых методов
            }
        } catch (Exception e) {
            sendInternalServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        sendResponse(exchange, epics);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        try {
            int epicId = extractIdFromPath(exchange.getRequestURI().getPath(), "/epics/");
            Epic epic = taskManager.getEpicById(epicId);

            if (epic == null) {
                sendJsonErrorMessage(exchange, "Эпик с id " + epicId + " не найден.", 404);
            } else {
                sendResponse(exchange, epic);
            }
        } catch (NumberFormatException e) {
            sendJsonErrorMessage(exchange, "Неверный формат ID эпика.", 400);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        try {
            int epicId = extractIdFromPath(exchange.getRequestURI().getPath(), "/epics/");
            if (taskManager.getEpicById(epicId) == null) {
                sendJsonErrorMessage(exchange, "Эпик с id " + epicId + " не найден.", 404);
                return;
            }
            sendResponse(exchange, taskManager.getSubtasksForEpic(epicId));
        } catch (NumberFormatException e) {
            sendJsonErrorMessage(exchange, "Неверный формат ID эпика.", 400);
        }
    }

    private void handleCreateEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getTitle() == null || epic.getTitle().isBlank() ||
                epic.getDescription() == null || epic.getDescription().isBlank()) {
            sendJsonErrorMessage(exchange, "Некорректные данные эпика: название и описание не " +
                    "должны быть пустыми.", 400);
            return;
        }

        if (epic.getId() != 0) {
            // Проверка на существование элемента
            Epic existingEpic = taskManager.getEpicById(epic.getId());
            if (existingEpic == null) {
                sendJsonErrorMessage(exchange, "Эпик с id " + epic.getId() + " не найден.", 404);
                return;
            }
            // Если найден, обновляем
            taskManager.updateTask(epic);
            sendJsonMessage(exchange, "Эпик с id " + epic.getId() + " успешно обновлён.", 200);
        } else {
            // Если ID нет, создаём новый эпик
            int newId = taskManager.addEpic(epic);
            sendJsonResponse(exchange, Map.of("id", newId), 201);
        }
    }

    private void handleDeleteEpicById(HttpExchange exchange) throws IOException {
        try {
            int epicId = extractIdFromPath(exchange.getRequestURI().getPath(), "/epics/");
            if (taskManager.getEpicById(epicId) == null) {
                sendJsonErrorMessage(exchange, "Эпик с id " + epicId + " не найден.", 404);
                return;
            }

            taskManager.removeTaskById(epicId);
            sendJsonResponse(exchange, Map.of("success", true), 200);
        } catch (NumberFormatException e) {
            sendJsonErrorMessage(exchange, "Неверный формат ID эпика.", 400);
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        taskManager.removeAllEpics();
        sendJsonResponse(exchange, Map.of("success", true), 200);
    }
}
