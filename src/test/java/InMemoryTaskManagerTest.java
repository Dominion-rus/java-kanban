import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.InMemoryHistoryManager;
import ru.yandex.practicum.service.InMemoryTaskManager;

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
}
