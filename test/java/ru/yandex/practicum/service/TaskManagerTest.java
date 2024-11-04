package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();
    }

    @Test
    void testAddTask() {
        Task task = new Task("Task 1", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        int taskId = taskManager.addTask(task);

        Task retrievedTask = taskManager.getTaskById(taskId);
        assertNotNull(retrievedTask, "Задача должна быть добавлена.");
        assertEquals(task, retrievedTask, "Добавленная задача должна совпадать с оригинальной.");
    }

    @Test
    void testAddEpicWithSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = taskManager.addTask(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description", Status.NEW, epicId, Duration.ofMinutes(45),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask Description", Status.NEW, epicId, Duration.ofMinutes(60),
                LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);

        Map<Epic, List<Subtask>> epicWithSubtasks = taskManager.getEpicAndSubtasks(epicId);
        assertNotNull(epicWithSubtasks, "Эпик должен существовать.");
        assertEquals(2, epicWithSubtasks.get(epic).size(), "Эпик должен содержать 2 подзадачи.");
    }

    @Test
    void testEpicStatusCalculation() {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = taskManager.addTask(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, epicId, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 0));

        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);

        Epic loadedEpic = (Epic) taskManager.getTaskById(epicId);
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS.");
    }

    @Test
    void testTaskOverlapping() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        int task1Id = taskManager.addTask(task1);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 15));

        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2),
                "Ожидалось исключение при добавлении пересекающейся задачи.");
    }

    @Test
    void testGetHistory() {
        Task task = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        int taskId = taskManager.addTask(task);
        taskManager.getTaskById(taskId); // Добавляем задачу в историю

        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "Задача в истории должна совпадать с добавленной.");
    }
}