package org.practicum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vsmordvincev
 */
class TaskTracker {
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
            epics.get(subtask.getEpicId()).addSubtask(subtask.getId());
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
                epic.getSubtaskIds().remove((Integer) id);
                epic.updateStatus(getSubtasksForEpic(epic.getId()));
            } else {
                epics.remove(id);
            }
        }
    }

    public void removeAllTasks() {
        tasks.clear();
        subtasks.clear();
        epics.clear();
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

    public List<Task> getAllTasksList() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Метод для обновления статуса эпика
    private void epicUpdateStatus(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        epic.updateStatus(getSubtasksForEpic(epic.getId()));
    }

    private void epicUpdateStatus(Epic epic) {
        epic.updateStatus(getSubtasksForEpic(epic.getId()));
    }

    // Метод для печати информации об эпике и его подзадачах
    public void printEpicAndSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Эпик с ID " + epicId + " не найден.");
            return;
        }
        System.out.println(epic);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                System.out.println("  " + subtask);
            }
        }
    }

    public void printAllTasks() {
        System.out.println("Все задачи:");
        for (Task task : getAllTasksList()) {
            System.out.println(task);
        }
    }

    public void printAllEpics() {
        System.out.println("Все эпики:");
        for (Epic epic : getAllEpics()) {
            System.out.println(epic);
        }
    }

    // Метод для печати всех подзадач
    public void printAllSubtasks() {
        System.out.println("Все подзадачи:");
        for (Subtask subtask : getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}


