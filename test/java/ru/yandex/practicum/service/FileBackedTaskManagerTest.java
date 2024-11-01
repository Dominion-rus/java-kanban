package ru.yandex.practicum.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exceptions.ManagerLoadException;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends ru.yandex.practicum.service.TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
    }

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("FileTaskManager", ".csv");
        taskManager = createTaskManager();
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        taskManager.save();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());

        assertTrue(loadedTaskManager.getAllTasks().isEmpty(), "Должны быть нет задач.");
        assertTrue(loadedTaskManager.getAllEpics().isEmpty(), "Должны быть нет эпиков.");
        assertTrue(loadedTaskManager.getAllSubtasks().isEmpty(), "Должны быть нет подзадач.");
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(15),
                LocalDateTime.of(2024, 11, 1, 10, 0));
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.save();

        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        List<Task> loadedTasks = loadedTaskManager.getAllTasks();

        assertEquals(2, loadedTasks.size(), "Должны быть 2 задачи после загрузки.");
        assertEquals(task1.getTitle(), loadedTasks.get(0).getTitle(),
                "Первая задача должна соответствовать сохраненной задаче 1.");
        assertEquals(task2.getTitle(), loadedTasks.get(1).getTitle(),
                "Вторая задача должна соответствовать сохраненной задаче 2.");
    }

    @Test
    void testRemoveAllTasksAndCheckFileState() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        taskManager.addTask(task1);
        taskManager.removeAllTasks();
        taskManager.save();

        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        assertTrue(loadedTaskManager.getAllTasks().isEmpty(), "Должны быть нет задач после удаления.");
    }

    @Test
    void testSaveAndLoadAllTaskTypes() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        Epic epic1 = new Epic("Epic 1", "Epic Description");
        taskManager.addTask(task1);
        taskManager.addTask(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description", Status.NEW, epic1.getId(),
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask1);

        taskManager.save();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());

        assertEquals(1, loadedTaskManager.getTasks().size(), "Должна быть одна задача.");
        assertEquals(1, loadedTaskManager.getAllEpics().size(), "Должен быть один эпик.");
        assertEquals(1, loadedTaskManager.getAllSubtasks().size(), "Должна быть одна подзадача.");

        assertEquals(task1.getId(), loadedTaskManager.getTasks().get(1).getId(),
                "ID задачи должен совпадать.");
        assertEquals(epic1.getId(), loadedTaskManager.getAllEpics().get(0).getId(),
                "ID эпика должен совпадать.");
        assertEquals(subtask1.getId(), loadedTaskManager.getAllSubtasks().get(0).getId(),
                "ID подзадачи должен совпадать.");
    }

    @Test
    public void testShouldPreserveMaxId() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        Epic epic1 = new Epic("Epic 1", "Epic Description");
        taskManager.addTask(task1);
        taskManager.addTask(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description", Status.NEW, epic1.getId(),
                Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask1);

        taskManager.save();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());

        Task newTask = new Task("New task", "New description", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        int newTaskId = loadedTaskManager.addTask(newTask);

        assertEquals(newTaskId, 4, "Новый ID задачи должен быть 4 после загрузки.");
    }

    @Test
    void testEmptyHistoryPreserved() {
        taskManager.save();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        assertTrue(loadedTaskManager.getHistory().isEmpty(), "История должна быть пустой после загрузки.");
    }

    @Test
    void testPreventDuplicateTasksOnLoad() {
        Task task = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        taskManager.addTask(task);
        taskManager.addTask(task);

        taskManager.save();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());

        assertEquals(1, loadedTaskManager.getTasks().size(),
                "Должна быть только одна задача после загрузки, несмотря на дублирование.");
    }

    @Test
    void testLoadFromCorruptedFile() {
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("corrupted data line 1\n");
            writer.write("another corrupted line\n");
            writer.write("yet another corrupted line\n");
        } catch (IOException e) {
            fail("Не удалось записать в файл во время теста.");
        }

        assertThrows(ManagerLoadException.class, () -> {
            FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
            loadedTaskManager.loadFromFile();

        }, "Загрузка из поврежденного файла должна вызывать ManagerLoadException.");
    }

    @Test
    void testSaveTaskWithoutExceptions() {
        Task task = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30),
                LocalDateTime.of(2024, 11, 1, 9, 0));
        assertDoesNotThrow(() -> {
            taskManager.addTask(task);
            taskManager.save();
        }, "Сохранение задачи не должно вызывать исключений.");
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}