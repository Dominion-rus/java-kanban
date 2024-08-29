package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vsmordvincev
 */
public class TaskTracker {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Subtask> subtasks;
    private final Map<Integer, Epic> epics;
    private int nextId;

    public TaskTracker() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.nextId = 1;
    }

    public int addTask(Task task) {
        task.setId(nextId++);
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            subtasks.put(task.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            epic.addSubtask(subtask.getId());
            updateEpicStatus(epic);
        } else if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
        } else {
            tasks.put(task.getId(), task);
        }
        return task.getId();
    }

    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        } else {
            return epics.get(id);
        }
    }

    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>(tasks.values());
        allTasks.addAll(subtasks.values());
        allTasks.addAll(epics.values());
        return allTasks;
    }

    public void removeTaskById(int id) {
        if (tasks.remove(id) == null) {
            Subtask subtask = subtasks.remove(id);
            if (subtask != null) {
                Epic epic = epics.get(subtask.getEpicId());
                epic.removeSubtask(subtask.getId());
                updateEpicStatus(epic); // Обновляем статус эпика после удаления подзадачи
            } else {
                Epic epic = epics.remove(id);
                if (epic != null) {
                    for (Integer subtaskId : epic.getSubtaskIds()) {
                        subtasks.remove(subtaskId);
                    }
                }
            }
        }
    }

    // Метод для удаления всех обычных задач
    public void removeAllTasks() {
        tasks.clear();
    }

    // Метод для удаления всех эпиков и связанных с ними подзадач
    public void removeAllEpics() {
        // Удаляем все подзадачи, связанные с эпиками
        for (Epic epic : epics.values()) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
        epics.clear();
    }

    // Метод для удаления всех подзадач и обновления статусов эпиков
    public void removeAllSubtasks() {
        // Для каждого эпика очищаем список подзадач и обновляем статус
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public void updateTask(Task updatedTask) {
        if (updatedTask instanceof Subtask) {
            Subtask subtask = (Subtask) updatedTask;
            subtasks.put(updatedTask.getId(), subtask);
            epicUpdateStatus(subtask);
        } else if (updatedTask instanceof Epic) {
            epics.put(updatedTask.getId(), (Epic) updatedTask);
            epicUpdateStatus((Epic) updatedTask);
        } else {
            tasks.put(updatedTask.getId(), updatedTask);
        }
    }

    public List<Subtask> getSubtasksForEpic(int epicId) {
        List<Subtask> subtasksForEpic = new ArrayList<>();
        for (Integer subtaskId : epics.get(epicId).getSubtaskIds()) {
            subtasksForEpic.add(subtasks.get(subtaskId));
        }
        return subtasksForEpic;
    }

    // Метод для обновления статуса эпика
    private void epicUpdateStatus(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        updateEpicStatus(epic);
    }

    private void epicUpdateStatus(Epic epic) {
        updateEpicStatus(epic);
    }

    // Возвращает список всех эпиков
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Возвращает список всех подзадач
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Возвращает эпик и его подзадачи в виде Map
    public Map<Epic, List<Subtask>> getEpicAndSubtasks(int epicId) {
        Map<Epic, List<Subtask>> epicAndSubtasks = new HashMap<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            List<Subtask> subtaskList = getSubtasksForEpic(epicId);
            epicAndSubtasks.put(epic, subtaskList);
        }
        return epicAndSubtasks;
    }

    private void updateEpicStatus(Epic epic) {
        boolean allDone = true;
        boolean allNew = true;

        List<Subtask> subtasks = getSubtasksForEpic(epic.getId());

        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}


