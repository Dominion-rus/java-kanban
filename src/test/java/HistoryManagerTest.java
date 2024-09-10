import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class HistoryManagerTest {

    @Test
    void add() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Сходить в магазин", "Купить продукты", Status.NEW);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }
}
