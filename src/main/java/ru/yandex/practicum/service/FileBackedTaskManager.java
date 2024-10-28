package ru.yandex.practicum.service;

import ru.yandex.practicum.exceptions.ManagerLoadException;
import ru.yandex.practicum.exceptions.ManagerSaveException;
import ru.yandex.practicum.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    private static final String CSV_HEADER = "id,type,name,status,description,epic";

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
        loadFromFile();
    }

    void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            // Записываем заголовки
            writer.write(CSV_HEADER);
            writer.newLine();

            // Записываем все задачи
            for (Task task : getAllEpics()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            for (Task task : getTasks().values()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
            for (Task task : getAllSubtasks()) {
                writer.write(taskToString(task));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл: " + file.getName(), e);

        }
    }

    @Override
    public int addTask(Task task) {
        int taskId = super.addTask(task);
        save();
        return taskId;
    }

    @Override
    public void updateTask(Task updatedTask) {
        super.updateTask(updatedTask);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public int addEpic(Epic epic) {
        int epicId = super.addEpic(epic);
        save();
        return epicId;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int subtaskId = super.addSubtask(subtask);
        save();
        return subtaskId;
    }

    private String taskToString(Task task) {
        final TaskType type = task.getType();
        StringBuilder result = new StringBuilder();

        // Записываем общие поля для всех типов задач
        result.append(task.getId()).append(",");
        result.append(type).append(",");
        result.append(task.getTitle()).append(",");
        result.append(task.getStatus()).append(",");
        result.append(task.getDescription());

        // Добавляем Epic ID, если задача — подзадача
        if (type == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            result.append(",").append(subtask.getEpicId());
        }

        return result.toString();
    }

    void loadFromFile() {
        if (!file.exists()) {
            return; // Если файл не существует, просто выходим
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath());

            // Проверяем, есть ли строки для обработки
            int maxId = 0;
            if (lines.size() > 1) {
                for (String line : lines.subList(1, lines.size())) {
                    Task task = fromString(line);
                    System.out.println("Загруженная задача: " + task);

                    maxId = Math.max(task.getId(), maxId);
                    switch (task.getType()) {
                        case TASK:
                            super.addTaskWithPredefinedId(task);
                            break;
                        case EPIC:
                            super.addTaskWithPredefinedId(task);
                            break;
                        case SUBTASK:
                            super.addTaskWithPredefinedId(task);
                            break;
                        default:
                            throw new ManagerLoadException("Нe известный тип задачи: " + task.getType());
                    }
                }
            }
            setNextId(maxId + 1);
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка чтения данных из файла: " + file.getName(), e);
        }
    }


    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1].trim());
        String name = parts[2];
        Status status = Status.valueOf(parts[3].trim());
        String description = parts[4];
        int epicId = -1;

        // Создание задачи в зависимости от типа
        switch (type) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                // Для подзадачи получаем epicId, если он присутствует
                if (parts.length > 5) {
                    epicId = Integer.parseInt(parts[5]);
                }
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

}
