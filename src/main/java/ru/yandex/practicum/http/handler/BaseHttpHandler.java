package ru.yandex.practicum.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.utils.DurationAdapter;
import ru.yandex.practicum.utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseHttpHandler implements HttpHandler {
    protected static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleRequest(exchange);
        } catch (IllegalArgumentException e) {
            sendNotAcceptable(exchange, e.getMessage());
        } catch (Exception e) {
            sendInternalServerError(exchange, "Ошибка обработки запроса: " + e.getMessage());
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected void sendResponse(HttpExchange exchange, Object response) throws IOException {

        String jsonResponse = gson.toJson(response);
        sendResponse(exchange, jsonResponse, 200);
    }

    protected void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendJsonErrorMessage(HttpExchange exchange, String message, int statusCode) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);

        String jsonResponse = gson.toJson(errorResponse);

        sendResponse(exchange, jsonResponse, statusCode);
    }

    protected void sendJsonMessage(HttpExchange exchange, String message, int statusCode) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "success");
        errorResponse.put("message", message);

        String jsonResponse = gson.toJson(errorResponse);

        sendResponse(exchange, jsonResponse, statusCode);
    }

    protected void sendJsonResponse(HttpExchange exchange, Object response, int statusCode) throws IOException {
        String jsonResponse = gson.toJson(response); // Конвертируем объект в JSON
        sendResponse(exchange, jsonResponse, statusCode);
    }

    protected void sendCreated(HttpExchange exchange, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(201, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, gson.toJson(message), 404);
    }

    protected void sendNotAcceptable(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, gson.toJson(message), 406);
    }

    protected void sendInternalServerError(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, gson.toJson(message), 500);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Метод не поддерживается", 405);
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected int extractIdFromPath(String path, String prefix) {
        try {
            String idPart = path.replace(prefix, "").split("/")[0];
            return Integer.parseInt(idPart);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Некорректный формат пути: " + path, e);
        }
    }
}