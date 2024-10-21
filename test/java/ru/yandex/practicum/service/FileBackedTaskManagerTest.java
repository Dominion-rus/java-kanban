package ru.yandex.practicum.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;

import java.io.File;
import java.io.IOException;
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
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
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
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        taskManager.addTask(task1);
        taskManager.removeAllTasks();

        taskManager.save();

        HistoryManager historyManager = new InMemoryHistoryManager();
        FileBackedTaskManager loadedTaskManager = new FileBackedTaskManager(tempFile, historyManager);

        assertTrue(loadedTaskManager.getAllTasks().isEmpty(), "Должны быть нет задач после удаления.");
    }





    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
