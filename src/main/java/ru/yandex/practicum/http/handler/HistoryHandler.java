package ru.yandex.practicum.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
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
            if ("GET".equalsIgnoreCase(method) && "/history".equalsIgnoreCase(path)) {
                handleGetHistory(exchange);
            } else {
                sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange, "Internal server error: " + e.getMessage());
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        sendResponse(exchange, taskManager.getHistory());
    }
}
