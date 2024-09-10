import org.junit.jupiter.api.Test;
import ru.yandex.practicum.service.*;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {
    @Test
    void getDefaultTaskManagerShouldReturnInitializedInstance() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "TaskManager  должен быть инициализирован.");
        assertInstanceOf(InMemoryTaskManager.class, taskManager, "TaskManager должен быть экземпляром " +
                "InMemoryTaskManager.");
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedInstance() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "HistoryManager  должен быть инициализирован.");
        assertInstanceOf(InMemoryHistoryManager.class, historyManager, "HistoryManager должен быть экземпляром" +
                " InMemoryHistoryManager.");
    }
}
