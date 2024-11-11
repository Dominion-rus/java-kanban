package ru.yandex.practicum.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
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
                if (path.matches("/tasks/?")) {
                    handleGetAllTasks(exchange);
                } else if (path.matches("/tasks/\\d+")) {
                    handleGetTaskById(exchange);
                } else {
                    sendNotFound(exchange, "Некорректный путь: " + path);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreateTask(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (path.matches("/tasks/\\d+")) {
                    handleDeleteTaskById(exchange);
                } else if (path.matches("/tasks/?")) {
                    handleDeleteAllTasks(exchange);
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

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        sendResponse(exchange, tasks);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        try {
            int taskId = extractIdFromPath(exchange.getRequestURI().getPath(), "/tasks/");
            Task task = taskManager.getTaskById(taskId);
            if (task == null) {
                sendJsonErrorMessage(exchange, "Задача с id " + taskId + " не найдена.", 404);
            } else {
                sendResponse(exchange, task);
            }
        } catch (NumberFormatException e) {
            sendJsonErrorMessage(exchange, "Неверный формат ID задачи.", 400);
        }
    }

    private void handleCreateTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        if (task.getTitle() == null || task.getTitle().isBlank() || task.getDescription() == null ||
                task.getDescription().isBlank()) {
            sendJsonErrorMessage(exchange, "Некорректные данные задачи: название и " +
                    "описание не могут быть пустыми.", 400);
            return;
        }

        try {
            if (task.getId() == 0) {
                int newId = taskManager.addTask(task);
                sendJsonResponse(exchange, Map.of("id", newId), 201); // Вернуть id созданной задачи
            } else {
                taskManager.updateTask(task);
                sendJsonMessage(exchange, "Задача с id " + task.getId() + " успешно обновлена.", 200);
            }
        } catch (IllegalArgumentException e) {
            sendJsonErrorMessage(exchange, e.getMessage(), 406);
        }
    }

    private void handleDeleteTaskById(HttpExchange exchange) throws IOException {
        try {
            int taskId = extractIdFromPath(exchange.getRequestURI().getPath(), "/tasks/");
            Task task = taskManager.getTaskById(taskId);

            if (task == null) {
                sendJsonErrorMessage(exchange, "Задача с id " + taskId + " не найдена.", 404);
                return;
            }

            taskManager.removeTaskById(taskId);
            sendJsonResponse(exchange, Map.of("success", true), 200);
        } catch (NumberFormatException e) {
            sendJsonErrorMessage(exchange, "Неверный формат ID задачи.", 400);
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.removeAllTasks();
        sendJsonResponse(exchange, Map.of("success", true), 200);
    }
}
