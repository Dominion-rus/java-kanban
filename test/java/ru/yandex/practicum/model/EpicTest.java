package ru.yandex.practicum.model;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.service.InMemoryHistoryManager;
import ru.yandex.practicum.service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
public class EpicTest {
    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());
        subtask.setId(2);

        assertNotEquals(subtask.getId(), epic.getId(), "Подзадача не может быть самостоятельной задачей.");
    }

    @Test
    void testEpicDurationAndStartTime() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 10, 0));
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epic.getId(),
                Duration.ofMinutes(45), LocalDateTime.of(2024, 11, 1, 11, 0));
        subtask2.setId(3);

        InMemoryTaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        manager.addEpic(epic);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        // Проверяем, что продолжительность эпика равна сумме продолжительностей подзадач
        assertEquals(Duration.ofMinutes(75), epic.getDuration(),
                "Продолжительность эпика должна быть равна сумме продолжительностей подзадач.");

        // Проверяем, что время начала эпика равно времени начала самой ранней подзадачи
        assertEquals(LocalDateTime.of(2024, 11, 1, 10, 0), epic.getStartTime(),
                "Время начала эпика должно быть равно времени начала самой ранней подзадачи.");

        // Проверяем, что время окончания эпика равно времени окончания самой поздней подзадачи
        assertEquals(LocalDateTime.of(2024, 11, 1, 11, 45), epic.getEndTime(),
                "Время окончания эпика должно быть равно времени окончания самой поздней подзадачи.");
    }
}
