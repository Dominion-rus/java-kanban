package ru.yandex.practicum.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
public class EpicTest {
    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId());
        subtask.setId(2);

        assertNotEquals(subtask.getId(), epic.getId(), "Подзадача не может быть самостоятельной задачей.");
    }
}
