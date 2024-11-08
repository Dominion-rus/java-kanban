package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends ru.yandex.practicum.service.TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void testAddAndGetTask() {
        Task task = new Task("Test Task", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        int taskId = taskManager.addTask(task);

        Task retrievedTask = taskManager.getTaskById(taskId);
        assertNotNull(retrievedTask, "Задача должна быть добавлена.");
        assertEquals(task, retrievedTask, "Извлеченная задача должна соответствовать добавленной.");
    }

    @Test
    void testAddAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);

        Epic retrievedEpic = taskManager.getAllEpics().get(0);
        assertNotNull(retrievedEpic, "Эпик должен быть добавлен.");
        assertEquals(epic, retrievedEpic, "Извлеченный эпик должен соответствовать добавленному.");
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        int subtaskId = taskManager.addTask(subtask);

        assertNotEquals(epicId, subtaskId, "Подзадача не должна иметь тот же идентификатор, что и эпик.");
    }

    @Test
    void testRemoveTask() {
        Task task = new Task("Task to Remove", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        int taskId = taskManager.addTask(task);

        taskManager.removeTaskById(taskId);
        Task removedTask = taskManager.getTaskById(taskId);

        assertNull(removedTask, "Задача должна быть удалена.");
    }

    @Test
    void testRemoveAllTasks() {
        Task task1 = new Task("Task 1", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        Task task2 = new Task("Task 2", "Description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.removeAllTasks();
        List<Task> allTasks = taskManager.getAllTasks();

        assertTrue(allTasks.isEmpty(), "Все задачи должны быть удалены.");
    }

    @Test
    void testRemoveAllEpics() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask);

        taskManager.removeAllEpics();
        List<Epic> allEpics = taskManager.getAllEpics();
        List<Subtask> allSubtasks = taskManager.getAllSubtasks();

        assertTrue(allEpics.isEmpty(), "Все эпики должны быть удалены.");
        assertTrue(allSubtasks.isEmpty(), "Все подзадачи должны быть удалены вместе с эпиками.");
    }

    @Test
    void testRemoveSubtaskShouldUpdateEpic() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask);

        taskManager.removeTaskById(subtask.getId());
        Epic updatedEpic = taskManager.getAllEpics().get(0);

        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "Эпик должен обновиться после удаления подзадачи.");
    }

    @Test
    void testRemoveEpicShouldRemoveItsSubtasks() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 3, 12, 0));
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);

        taskManager.removeTaskById(epicId);
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены вместе с эпиком.");
    }

    @Test
    void testRemoveAllTasksAndEpicsShouldClearData() {
        Task task = new Task("Test Task", "Task Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 0));
        taskManager.addTask(task);
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask);

        taskManager.removeAllTasks();
        taskManager.removeAllEpics();

        assertTrue(taskManager.getAllTasks().isEmpty(), "Все задачи должны быть удалены.");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Все эпики должны быть удалены.");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены.");
    }

    @Test
    void testIdUniquenessAfterTaskRemoval() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        int task1Id = taskManager.addTask(task1);

        taskManager.removeTaskById(task1Id);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 12, 0));
        int task2Id = taskManager.addTask(task2);

        assertNotEquals(task1Id, task2Id, "ID новой задачи не должен совпадать с ID удаленной задачи.");
    }

    @Test
    void testRemoveAllSubtasksShouldUpdateEpics() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId,
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 3, 12, 0));
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);

        taskManager.removeAllSubtasks();
        Epic updatedEpic = taskManager.getAllEpics().get(0);

        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "Подзадачи должны быть удалены из эпика.");
        assertEquals(Status.NEW, updatedEpic.getStatus(), "Статус эпика должен быть обновлен на NEW.");
    }

    @Test
    public void testGetPrioritizedTasks() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 10, 0));
        taskManager.addTask(task1);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(45), LocalDateTime.of(2024, 11, 1, 9, 0));
        taskManager.addTask(task2);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 3", Status.NEW, 3,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addSubtask(subtask1);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(task2, prioritizedTasks.get(0), "Первая задача должна быть task2.");
        assertEquals(task1, prioritizedTasks.get(1), "Вторая задача должна быть task1.");
        assertEquals(subtask1, prioritizedTasks.get(2), "Третья задача должна быть subtask1.");
    }

    @Test
    public void testAddTaskWithOverlappingTimes() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(60),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        taskManager.addTask(task1);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 10, 30));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2));
        assertEquals("Новая задача пересекается по времени выполнения с существующей задачей.", exception.getMessage());

        Task task3 = new Task("Task 3", "Description 3", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 11, 30));
        int task3Id = taskManager.addTask(task3);

        assertNotNull(taskManager.getTaskById(task3Id));
    }

    @Test
    public void testAddSubtaskWithOverlappingTimes() {
        Epic epic = new Epic("Epic 1", "Description");
        int epicId = taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 11, 1, 9, 0));
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 30));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> taskManager.addSubtask(subtask2));
        assertEquals("Новая задача пересекается по времени выполнения с существующей задачей.", exception.getMessage());

        Subtask subtask3 = new Subtask("Subtask 3", "Description 3", Status.NEW, epicId,
                Duration.ofMinutes(45), LocalDateTime.of(2024, 11, 1, 11, 0));
        int subtask3Id = taskManager.addSubtask(subtask3);

        assertNotNull(taskManager.getTaskById(subtask3Id));
    }
}


