package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    private int nextId = 1;


    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }


    @Override
    public int addTask(Task task) {
        task.setId(nextId++);
        addTaskWithPredefinedId(task);
        return task.getId();
    }


    protected void addTaskWithPredefinedId(Task task) {
        switch (task.getType()) {
            case SUBTASK:
                Subtask subtask = (Subtask) task;
                subtasks.put(task.getId(), subtask);
                Epic epic = epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtask(subtask.getId());
                    updateEpicStatus(epic);
                }
                break;
            case EPIC:
                epics.put(task.getId(), (Epic) task);
                break;
            case TASK:
                tasks.put(task.getId(), task);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + task.getType());
        }

    }



    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);  // Добавляем обычную задачу в историю
            return task;
        } else {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                historyManager.add(subtask);  // Добавляем подзадачу в историю
                return subtask;
            } else {
                Epic epic = epics.get(id);
                if (epic != null) {
                    historyManager.add(epic);  // Добавляем эпик в историю
                    return epic;
                }
            }
        }
        return null;  // Возвращаем null, если задача не найдена
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>(tasks.values());
        allTasks.addAll(subtasks.values());
        allTasks.addAll(epics.values());
        return allTasks;
    }

    @Override
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
    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    // Метод для удаления всех эпиков и связанных с ними подзадач
    @Override
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
    @Override
    public void removeAllSubtasks() {
        // Для каждого эпика очищаем список подзадач и обновляем статус
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    @Override
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

    @Override
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
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Возвращает список всех подзадач
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Возвращает эпик и его подзадачи в виде Map
    @Override
    public Map<Epic, List<Subtask>> getEpicAndSubtasks(int epicId) {
        Map<Epic, List<Subtask>> epicAndSubtasks = new HashMap<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            List<Subtask> subtaskList = getSubtasksForEpic(epicId);
            epicAndSubtasks.put(epic, subtaskList);
        }
        return epicAndSubtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }



    protected void updateEpicStatus(Epic epic) {
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


    protected Epic getEpicById(int epicId) {
        return epics.get(epicId);
    }

    protected Map<Integer, Task> getTasks() {
        return new HashMap<>(tasks);
    }

    protected Map<Integer, Epic> getEpics() {
        return new HashMap<>(epics);
    }

    protected Map<Integer, Subtask> getSubtasks() {
        return new HashMap<>(subtasks);
    }

    protected int addEpic(Epic epic) {
        return addTask(epic);
    }

    protected int addSubtask(Subtask subtask) {
        return addTask(subtask);
    }


    protected void setNextId(int nextId) {
        this.nextId = nextId;
    }
}
