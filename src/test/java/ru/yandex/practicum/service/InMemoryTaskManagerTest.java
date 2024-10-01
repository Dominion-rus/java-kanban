package ru.yandex.practicum.service;

import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
    }

    @Test
    void testAddAndGetTask() {
        Task task = new Task("Test Task", "Description", Status.NEW);
        int taskId = taskManager.addTask(task);

        Task retrievedTask = taskManager.getTaskById(taskId);
        assertNotNull(retrievedTask, "Task должен быть notNull");
        assertEquals(task, retrievedTask, "Извлеченный task должен соответствовать добавленному task.");
    }

    @Test
    void testAddAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);

        Epic retrievedEpic = taskManager.getAllEpics().get(0);
        assertEquals(1, epicId, "EpicID должен быть 1");
        assertNotNull(retrievedEpic, "Epic должен быть notNull");
        assertEquals(epic, retrievedEpic, "Извлеченный epic должен соответствовать добавленному epic.");
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", Status.NEW, epicId);
        int subtaskId = taskManager.addTask(subtask);

        assertNotEquals(epicId, subtaskId, "Идентификатор подзадачи не должен совпадать с идентификатором Epic.");
    }

    @Test
    void testRemoveTask() {
        Task task = new Task("Task to Remove", "Description", Status.NEW);
        int taskId = taskManager.addTask(task);

        taskManager.removeTaskById(taskId);
        Task removedTask = taskManager.getTaskById(taskId);

        assertNull(removedTask, "Задача должна быть удалена.");
    }

    @Test
    void testRemoveAllTasks() {
        Task task1 = new Task("Task 1", "Description", Status.NEW);
        Task task2 = new Task("Task 2", "Description", Status.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.removeAllTasks();
        List<Task> allTasks = taskManager.getAllTasks();

        assertTrue(allTasks.isEmpty(), "Все Задачи должны быть удалены.");
    }

    @Test
    void testRemoveAllEpics() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", Status.NEW, epicId);
        taskManager.addTask(subtask);

        taskManager.removeAllEpics();
        List<Epic> allEpics = taskManager.getAllEpics();
        List<Subtask> allSubtasks = taskManager.getAllSubtasks();

        assertTrue(allEpics.isEmpty(), "Все Эпики должны быть удалены.");
        assertTrue(allSubtasks.isEmpty(), "Все подзадачи, связанные с удаленными Эпиками, должны быть удалены.");
    }

    @Test
    void testHistoryManager() {
        Task task = new Task("Task for History", "Description", Status.NEW);
        int taskId = taskManager.addTask(task);
        taskManager.getTaskById(taskId);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0), "История должна содержать правильную задачу");
    }

    @Test
    void testTaskIdUniqueness() {
        Task task1 = new Task("Task 1", "Description", Status.NEW);
        Task task2 = new Task("Task 2", "Description", Status.NEW);
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);

        assertNotEquals(task1Id, task2Id, "Задачи с разным содержанием должны иметь разные идентификаторы.");
    }

    @Test
    void testTaskImmutabilityOnAdd() {
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = taskManager.addTask(task);

        Task fetchedTask = taskManager.getTaskById(taskId);

        assertEquals("Task", fetchedTask.getTitle(), "Имя задачи не должно меняться.");
        assertEquals("Description", fetchedTask.getDescription(), "Описание задачи не должно меняться.");
        assertEquals(Status.NEW, fetchedTask.getStatus(), "Статус задачи не должен меняться.");
    }

    @Test
    @Description("TaskManagerTest")
    void historyManagerIntegration() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        TaskManager taskManager = new InMemoryTaskManager(historyManager);
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = taskManager.addTask(task);

        taskManager.getTaskById(taskId);

        assertEquals(1, historyManager.getHistory().size(), "История должна содержать одну задачу.");
        assertEquals(task, historyManager.getHistory().get(0), "Историческая задача должна " +
                "соответствовать найденной задаче.");
    }

    @Test
    @Description("TaskManagerTest")
    void taskEqualityById() {

        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);

        assertEquals(task1, taskManager.getTaskById(task1Id), "Задачи должны быть одинаковыми" +
                " по идентификатору.");
        assertEquals(task2, taskManager.getTaskById(task2Id), "Задачи должны быть одинаковыми" +
                " по идентификатору.");
    }

    @Test
    @Description("TaskManagerTest")
    void subtaskCannotBeItsOwnEpic() {

        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.addTask(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
        int subtaskId = taskManager.addTask(subtask);

        assertNotEquals(epicId, subtaskId, "Эпик не может быть самостоятельной подзадачей.");
    }


    @Test
    @Description("TaskManagerTest")
    void taskIdUniqueness() {

        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);

        assertNotEquals(task1Id, task2Id, "Задачи с разным содержанием должны иметь разные идентификаторы.");
    }

    @Test
    void testRemoveSubtaskShouldUpdateEpic() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", Status.NEW, epicId);
        taskManager.addTask(subtask);

        taskManager.removeTaskById(subtask.getId());

        Epic updatedEpic = taskManager.getAllEpics().get(0);
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "После удаления подзадачи у эпика не должно быть подзадач.");
    }

    @Test
    void testRemoveEpicShouldRemoveItsSubtasks() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId);
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);

        taskManager.removeTaskById(epicId);

        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Подзадачи должны быть удалены вместе с эпиком.");
    }

    @Test
    void testRemoveAllTasksAndEpicsShouldClearData() {
        Task task = new Task("Test Task", "Task Description", Status.NEW);
        taskManager.addTask(task);
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", Status.NEW, epicId);
        taskManager.addTask(subtask);

        taskManager.removeAllTasks();
        taskManager.removeAllEpics();

        assertTrue(taskManager.getAllTasks().isEmpty(), "Все задачи должны быть удалены.");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Все эпики должны быть удалены.");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены.");
    }

    @Test
    void testIdUniquenessAfterTaskRemoval() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        int task1Id = taskManager.addTask(task1);

        taskManager.removeTaskById(task1Id);

        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        int task2Id = taskManager.addTask(task2);

        assertNotEquals(task1Id, task2Id, "ID новой задачи не должен совпадать с ID удалённой задачи.");
    }

    @Test
    void testRemoveAllSubtasksShouldUpdateEpics() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = taskManager.addTask(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId);
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);

        taskManager.removeAllSubtasks();

        Epic updatedEpic = taskManager.getAllEpics().get(0);
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "Подзадачи должны быть удалены из эпика.");
        assertEquals(Status.NEW, updatedEpic.getStatus(), "Статус эпика должен быть обновлен на NEW.");
    }
}


