package ru.yandex.practicum.http;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.http.handler.*;
import ru.yandex.practicum.service.Managers;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedTasksHandler(taskManager));
    }

    public void start() {
        System.out.println("HTTP сервер запущен на порту: " + PORT);
        server.start();
    }

    public void stop() {
        System.out.println("HTTP сервер остановлен.");
        server.stop(0);
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Ошибка запуска HTTP-сервера: " + e.getMessage());
        }
    }
}

