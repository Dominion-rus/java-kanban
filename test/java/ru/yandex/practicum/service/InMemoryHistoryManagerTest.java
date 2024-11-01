package ru.yandex.practicum.service;

import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testHistoryShouldContainTask() {
        Task task = new Task("Task", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task.setId(1);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "История должна содержать одну задачу.");
        assertEquals(task, historyManager.getHistory().get(0),
                "Задача в истории должна быть такой же, как и добавленная задача.");
    }

    @Test
    @Description("HistoryManagerTest")
    void testAdd() {
        Task task = new Task("Сходить в магазин", "Купить продукты", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task.setId(1);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "Добавленная задача должна находиться в истории.");
    }

    @Test
    void testAddDuplicateTaskToHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не должна дублироваться в истории.");
        assertEquals(task1, history.get(0), "Задача должна находиться в истории.");
    }

    @Test
    void testMoveTaskToFrontOnDuplicateAdd() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 11, 0));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 уникальные задачи.");
        assertEquals(task2, history.get(0), "Задача task2 должна быть на первом месте.");
        assertEquals(task3, history.get(1), "Задача task3 должна быть на втором месте.");
        assertEquals(task1, history.get(2), "Задача task1 должна переместиться на последнее место.");
    }

    @Test
    void testRemoveTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу после удаления.");
        assertEquals(task2, history.get(0), "Задача task2 должна остаться в истории.");
    }

    @Test
    void testRemoveMiddleTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 11, 0));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи после удаления.");
        assertEquals(task1, history.get(0), "Задача task1 должна быть на первом месте.");
        assertEquals(task3, history.get(1), "Задача task3 должна быть на втором месте.");
    }

    @Test
    void testRemoveLastTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 11, 0));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи после удаления.");
        assertEquals(task1, history.get(0), "Задача task1 должна быть на первом месте.");
        assertEquals(task2, history.get(1), "Задача task2 должна быть на втором месте.");
    }

    @Test
    void testRemoveFirstTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 11, 0));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 задачи после удаления.");
        assertEquals(task2, history.get(0), "Задача task2 должна быть на первом месте.");
        assertEquals(task3, history.get(1), "Задача task3 должна быть на втором месте.");
    }

    @Test
    void testRemoveNonExistentTask() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        historyManager.add(task1);

        // Попытка удалить задачу, которой нет в истории
        historyManager.remove(99);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна измениться при удалении несуществующей задачи.");
        assertEquals(task1, history.get(0), "Единственная задача в истории должна остаться неизменной.");
    }

    @Test
    void testAddTaskWithNull() {
        // Попытка добавить null в историю
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна оставаться пустой при попытке добавления null.");
    }

    @Test
    void testAddTasksWithSameId() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(45),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        task2.setId(1); // Задача с тем же ID

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только одну задачу с данным ID.");
        assertEquals(task2, history.get(0), "Последняя добавленная задача с одинаковым ID должна быть в истории.");
    }

}