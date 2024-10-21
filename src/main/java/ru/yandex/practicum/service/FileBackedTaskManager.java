package ru.yandex.practicum.service;

import ru.yandex.practicum.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
        loadFromFile();
    }

    void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            // Записываем заголовки
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            // Записываем все задачи
            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toString(Task task) {
        return String.format("%d,%s,%s,%s,%s,", task.getId(), TaskType.TASK, task.getTitle(), task.getStatus(),
                task.getDescription());

    }

    private String toString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s,", epic.getId(), TaskType.EPIC, epic.getTitle(), epic.getStatus(),
                epic.getDescription());

    }

    private String toString(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%d", subtask.getId(), TaskType.SUBTASK, subtask.getTitle(),
                subtask.getStatus(), subtask.getDescription(), subtask.getEpicId());

    }

        void loadFromFile() {
            if (!file.exists()) {
                return; // Если файл не существует, просто выходим
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                // Проверяем, есть ли строки для обработки
                if (lines.size() > 1) {
                    for (String line : lines.subList(1, lines.size())) {
                        Task task = fromString(line);
                        if (task instanceof Epic) {
                            addTask(task);
                        } else if (task instanceof Subtask) {
                            Subtask subtask = (Subtask) task;
                            Epic epic = getEpicById(subtask.getEpicId());
                            if (epic != null) {
                                addTask(subtask); // Добавляем подзадачу только если эпик существует
                            } else {
                                // Обработка ошибки: эпик не найден
                                System.err.println("Epic with ID " + subtask.getEpicId() + " not found for subtask "
                                        + subtask.getId());
                            }
                        } else if (!(task instanceof Epic)) {
                            addTask(task);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1].trim());
        String name = parts[2];
        Status status = Status.valueOf(parts[3].trim());
        String description = parts[4];
        int epicId = parts.length > 5 ? Integer.parseInt(parts[5]) : -1; // Если эпик, то получаем id, иначе -1

        // Создание задачи в зависимости от типа
        switch (type) {
            case TASK:
                return new Task(name, description, status);
            case EPIC:
                return new Epic(name, description);
            case SUBTASK:
                return new Subtask(name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}
