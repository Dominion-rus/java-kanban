package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicStatusCalculationTest {
    private InMemoryTaskManager manager;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    public void testAllSubtasksNew() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertEquals(Status.NEW, manager.getEpicById(epicId).getStatus(), "Статус эпика должен быть NEW.");
    }

    @Test
    public void testAllSubtasksDone() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.DONE, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, epicId,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertEquals(Status.DONE, manager.getEpicById(epicId).getStatus(), "Статус эпика должен быть DONE.");
    }

    @Test
    public void testMixedSubtasksNewAndDone() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, epicId,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epicId).getStatus(),
                "Статус эпика должен быть IN_PROGRESS.");
    }

    @Test
    public void testAllSubtasksInProgress() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.IN_PROGRESS,
                epicId, Duration.ofMinutes(30), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.IN_PROGRESS,
                epicId, Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epicId).getStatus(),
                "Статус эпика должен быть IN_PROGRESS.");
    }
}

