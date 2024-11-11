package ru.yandex.practicum.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
        if (taskManager == null) {
            throw new IllegalArgumentException("TaskManager cannot be null");
        }
        this.taskManager = taskManager;
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.matches("/subtasks/?")) {
                    handleGetAllSubtasks(exchange);
                } else if (path.matches("/subtasks/\\d+")) {
                    handleGetSubtaskById(exchange);
                } else {
                    sendNotFound(exchange, "Некорректный путь: " + path);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreateSubtask(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (path.matches("/subtasks/\\d+")) {
                    handleDeleteSubtaskById(exchange);
                } else if (path.matches("/subtasks/?")) {
                    handleDeleteAllSubtasks(exchange);
                } else {
                    sendNotFound(exchange, "Некорректный путь: " + path);
                }
            } else {
                sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        sendResponse(exchange, subtasks);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        try {
            int subtaskId = extractIdFromPath(exchange.getRequestURI().getPath(), "/subtasks/");
            Subtask subtask = (Subtask) taskManager.getTaskById(subtaskId);
            if (subtask == null) {
                sendJsonErrorMessage(exchange, "Подзадача с id " + subtaskId + " не найдена", 404);
            } else {
                sendResponse(exchange, subtask);
            }
        } catch (NumberFormatException e) {
            sendJsonErrorMessage(exchange, "Неверный формат идентификатора подзадачи.", 400);
        }
    }

    private void handleCreateSubtask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (subtask.getTitle() == null || subtask.getTitle().isBlank() ||
                subtask.getDescription() == null || subtask.getDescription().isBlank()) {
            sendJsonErrorMessage(exchange, "Неверные данные подзадачи: название и описание " +
                            "не могут быть пустыми.",
                    400);
            return;
        }

        if (subtask.getEpicId() == 0 || taskManager.getEpicById(subtask.getEpicId()) == null) {
            sendJsonErrorMessage(exchange, "Неверный формат ID эпика для подзадачи", 400);
            return;
        }

        try {
            int newId = taskManager.addSubtask(subtask);
            sendJsonResponse(exchange, Map.of("id", newId), 201);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("пересекается по времени выполнения")) {
                sendJsonErrorMessage(exchange, e.getMessage(), 406); // Обработка пересечения
            } else {
                sendJsonErrorMessage(exchange, e.getMessage(), 400); // Другие ошибки
            }
        }
    }


    private void handleDeleteSubtaskById(HttpExchange exchange) throws IOException {
        try {
            int subtaskId = extractIdFromPath(exchange.getRequestURI().getPath(), "/subtasks/");
            Subtask subtask = (Subtask) taskManager.getTaskById(subtaskId);

            if (subtask == null) {
                sendJsonErrorMessage(exchange, "Подзадача с id " + subtaskId + " не найдена", 404);
                return;
            }

            taskManager.removeTaskById(subtaskId);
            sendJsonResponse(exchange, Map.of("success", true), 200);
        } catch (NumberFormatException e) {
            sendJsonErrorMessage(exchange, "Неверный формат идентификатора подзадачи.", 400);
        }
    }

    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        taskManager.removeAllSubtasks();
        sendJsonResponse(exchange, Map.of("success", true), 200);
    }
}
