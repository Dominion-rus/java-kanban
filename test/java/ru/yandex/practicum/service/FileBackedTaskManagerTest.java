package ru.yandex.practicum.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {

    private FileBackedTaskManager taskManager;
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("FileTaskManager", ".csv");
        HistoryManager historyManager = new InMemoryHistoryManager();
        taskManager = new FileBackedTaskManager(tempFile, historyManager);
    }

    @Test
    void testSaveAndLoadEmptyFile() {

        taskManager.save();

        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, historyManager);

        assertTrue(loadedTaskManager.getAllTasks().isEmpty(), "Должны быть нет задач.");
        assertTrue(loadedTaskManager.getAllEpics().isEmpty(), "Должны быть нет эпиков.");
        assertTrue(loadedTaskManager.getAllSubtasks().isEmpty(), "Должны быть нет подзадач.");
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 0));
        Task task2 = new Task("Task 2", "Description 2", Status.NEW, Duration.ofMinutes(15), LocalDateTime.of(2024, 11, 1, 10, 0));
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.save();

        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, historyManager);

        List<Task> loadedTasks = loadedTaskManager.getAllTasks();
        assertEquals(2, loadedTasks.size(), "Должны быть 2 задачи после загрузки.");
        assertEquals(task1.getTitle(), loadedTasks.get(0).getTitle(), "Первая задача должна " +
                "соответствовать сохраненной задаче 1.");
        assertEquals(task2.getTitle(), loadedTasks.get(1).getTitle(), "Вторая задача должна " +
                "соответствовать сохраненной задаче 2.");
    }



    @Test
    void testRemoveAllTasksAndCheckFileState() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 0));
        taskManager.addTask(task1);
        taskManager.removeAllTasks();

        taskManager.save();

        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, historyManager);

        assertTrue(loadedTaskManager.getAllTasks().isEmpty(), "Должны быть нет задач после удаления.");
    }

    @Test
    void testSaveAndLoadAllTaskTypes() {
        // Создание задач
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 0));
        Epic epic1 = new Epic("Epic 1", "Epic Description");
        taskManager.addTask(task1);  // Сначала добавляем задачу
        taskManager.addTask(epic1);   // Затем добавляем эпик

        // Теперь, когда эпик добавлен, мы можем получить его ID
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description", Status.NEW, epic1.getId(), Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask1); // Добавляем подзадачу, используя ID эпика

        // Сохранение в файл
        taskManager.save();


        // Загрузка из файла
        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, historyManager);

        // Проверка задач
        assertEquals(1, loadedTaskManager.getTasks().size(), "Должна быть одна задача.");
        assertEquals(1, loadedTaskManager.getAllEpics().size(), "Должен быть один эпик.");
        assertEquals(1, loadedTaskManager.getAllSubtasks().size(), "Должна быть одна подзадача.");

        // Проверка идентификаторов
        assertEquals(task1.getId(), loadedTaskManager.getTasks().get(1).getId(), "ID задачи" +
                " должен совпадать.");
        assertEquals(epic1.getId(), loadedTaskManager.getAllEpics().get(0).getId(), "ID эпика " +
                "должен совпадать.");
        assertEquals(epic1.getSubtaskIds(), loadedTaskManager.getAllEpics().get(0).getSubtaskIds(), "Subtasks" +
                " эпика должен совпадать.");
        assertEquals(subtask1.getId(), loadedTaskManager.getAllSubtasks().get(0).getId(), "ID подзадачи " +
                "должен совпадать.");
    }

    @Test
    public void testShouldPreserveMaxId() throws Exception {

        // Создание задач
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 0));
        Epic epic1 = new Epic("Epic 1", "Epic Description");
        taskManager.addTask(task1);  // Сначала добавляем задачу
        taskManager.addTask(epic1);   // Затем добавляем эпик

        // Теперь, когда эпик добавлен, мы можем получить его ID
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description", Status.NEW, epic1.getId(), Duration.ofMinutes(120), LocalDateTime.of(2024, 11, 1, 11, 0));
        taskManager.addTask(subtask1); // Добавляем подзадачу, используя ID эпика

        // Сохранение в файл
        taskManager.save();

        printFileContents(tempFile);

        // Загрузка из файла
        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, historyManager);

        Task newTask = new Task("New task", "New description", Status.NEW, Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 0));
        loadedTaskManager.addTask(newTask);
        assertEquals(newTask, loadedTaskManager.getTaskById(4));
    }




    private void printFileContents(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        for (String line : lines) {
            System.out.println(line);
        }
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
        taskManager.addTask(task); // Добавляем дублирующуюся задачу

        taskManager.save();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        assertEquals(1, loadedTaskManager.getTasks().size(),
                "Должна быть только одна задача после загрузки, несмотря на дублирование.");
    }



    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
