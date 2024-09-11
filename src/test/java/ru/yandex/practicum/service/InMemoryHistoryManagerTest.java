package ru.yandex.practicum.service;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {
    private static final int HISTORY_LIMIT = 10;
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }
    @Test
    void testHistoryShouldContainTask() {
        Task task = new Task("Task", "Description", Status.NEW);

        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "История должна содержать одну задачу.");
        assertEquals(task, historyManager.getHistory().get(0),
                "Задача в истории должна быть такой же, как и добавленная задача.");
    }

    @Test
    @Description("HistoryManagerTest")
    void testAdd() {
        Task task = new Task("Сходить в магазин", "Купить продукты", Status.NEW);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    void testAddTasksToHistoryWithLimit() {
        // Добавляем задачи больше, чем лимит истории
        for (int i = 1; i <= HISTORY_LIMIT + 5; i++) {
            Task task = new Task("Task " + i, "Description " + i, Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        // Проверяем, что размер истории не превышает лимит
        List<Task> history = historyManager.getHistory();
        assertEquals(HISTORY_LIMIT, history.size(), "История должна содержать не более " + HISTORY_LIMIT + " задач.");

        // Проверяем, что в истории находятся последние добавленные задачи
        for (int i = 0; i < HISTORY_LIMIT; i++) {
            assertEquals("Task " + (HISTORY_LIMIT + 5 - i), history.get(i).getTitle(),
                    "История должна содержать последние задачи.");
        }
    }

    @Test
    void testAddDuplicateTaskToHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setId(1);
        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не должна дублироваться в истории.");
        assertEquals(task1, history.get(0), "Задача должна находиться в истории.");
    }

    @Test
    void testMoveTaskToFrontOnDuplicateAdd() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 уникальные задачи.");
        assertEquals(task1, history.get(0), "Первая задача должна переместиться на первое место.");
        assertEquals(task3, history.get(1), "Вторая задача должна быть на втором месте.");
        assertEquals(task2, history.get(2), "Третья задача должна быть на третьем месте.");
    }
}