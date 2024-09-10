import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.InMemoryHistoryManager;
import ru.yandex.practicum.service.InMemoryTaskManager;
import ru.yandex.practicum.service.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
public class TaskManagerTest {
    @Test
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
    void taskEqualityById() {
        TaskManager taskManager = createTaskManager();

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
    void subtaskCannotBeItsOwnEpic() {
        TaskManager taskManager = createTaskManager();

        Epic epic = new Epic("Epic", "Description");
        int epicId = taskManager.addTask(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
        int subtaskId = taskManager.addTask(subtask);

        assertNotEquals(epicId, subtaskId, "Эпик не может быть самостоятельной подзадачей.");
    }


    @Test
    void taskIdUniqueness() {
        TaskManager taskManager = createTaskManager();

        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);

        assertNotEquals(task1Id, task2Id, "Задачи с разным содержанием должны иметь разные идентификаторы.");
    }


    private TaskManager createTaskManager() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        return new InMemoryTaskManager(historyManager);
    }
}

