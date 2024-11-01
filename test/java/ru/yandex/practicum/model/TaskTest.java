package ru.yandex.practicum.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 11, 1, 9, 0));
        Task task2 = new Task("Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(15), LocalDateTime.of(2024, 11, 1, 10, 0));
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым идентификатором должны быть одинаковыми.");
    }


}
