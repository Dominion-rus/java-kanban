import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.InMemoryHistoryManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    @Test
    void historyShouldContainTask() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task = new Task("Task", "Description", Status.NEW);

        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "История должна содержать одну задачу.");
        assertEquals(task, historyManager.getHistory().get(0),
                "Задача в истории должна быть такой же, как и добавленная задача.");
    }
}