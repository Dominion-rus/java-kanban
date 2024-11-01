package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    private int nextId = 1;

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
        if (task1.getStartTime() == null && task2.getStartTime() == null) {
            return task1.getId() - task2.getId(); // Сравнение по ID, если нет startTime
        } else if (task1.getStartTime() == null) {
            return 1; // Если task1 не имеет startTime, он идет после task2
        } else if (task2.getStartTime() == null) {
            return -1; // Если task2 не имеет startTime, он идет после task1
        } else {
            return task1.getStartTime().compareTo(task2.getStartTime());
        }
    });

    private boolean isOverlapping(Task newTask, Task existingTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null ||
                existingTask.getStartTime() == null || existingTask.getEndTime() == null) {
            return false; // Если одна из задач не имеет времени выполнения, пересечения нет
        }

        // Проверяем пересечение отрезков
        return !(newTask.getEndTime().isBefore(existingTask.getStartTime()) ||
                newTask.getStartTime().isAfter(existingTask.getEndTime()));
    }

    // Метод для проверки пересечения новой задачи со всеми существующими задачами
    private boolean hasOverlappingTasks(Task newTask) {
        return prioritizedTasks.stream()
                .filter(existingTask -> !existingTask.equals(newTask)) // Исключаем саму себя (в случае обновления)
                .anyMatch(existingTask -> isOverlapping(newTask, existingTask));
    }


    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }


    @Override
    public int addTask(Task task) {
        if (hasOverlappingTasks(task)) {
            throw new IllegalArgumentException("Новая задача пересекается по времени выполнения с существующей задачей.");
        }
        task.setId(nextId++);
        addTaskWithPredefinedId(task);
        prioritizedTasks.add(task);
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
                    updateEpicFields(epic);
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

    public void updateEpicFields(Epic epic) {
        List<Subtask> subtasks = getSubtasksForEpic(epic.getId());

        if (subtasks.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        // Рассчитываем общую продолжительность
        Duration totalDuration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        // Определяем самое раннее время начала
        LocalDateTime startTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Определяем самое позднее время окончания
        LocalDateTime endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setDuration(totalDuration);
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
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
        // Удаление обычной задачи
        if (tasks.remove(id) != null) {
            Task task = tasks.get(id);
            if (task != null) {
                prioritizedTasks.remove(task); // Удаляем задачу из TreeSet
            }
        } else {
            // Удаление подзадачи
            Subtask subtask = subtasks.remove(id);
            if (subtask != null) {
                prioritizedTasks.remove(subtask); // Удаляем подзадачу из TreeSet
                Epic epic = epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.removeSubtask(subtask.getId());
                    updateEpicStatus(epic);
                    updateEpicFields(epic); // Обновление полей эпика после удаления подзадачи
                }
            } else {
                // Удаление эпика и всех его подзадач
                Epic epic = epics.remove(id);
                if (epic != null) {
                    prioritizedTasks.remove(epic); // Удаляем эпик из TreeSet
                    for (Integer subtaskId : epic.getSubtaskIds()) {
                        Subtask removedSubtask = subtasks.remove(subtaskId);
                        if (removedSubtask != null) {
                            prioritizedTasks.remove(removedSubtask); // Удаляем подзадачи из TreeSet
                        }
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
        epics.values().forEach(epic -> epic.getSubtaskIds().forEach(subtasks::remove));
        epics.clear();
    }

    // Метод для удаления всех подзадач и обновления статусов эпиков
    @Override
    public void removeAllSubtasks() {
        // Для каждого эпика очищаем список подзадач и обновляем статус
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        });
        subtasks.clear();
    }




    @Override
    public void updateTask(Task updatedTask) {
        if (updatedTask instanceof Subtask) {
            Subtask subtask = (Subtask) updatedTask;
            subtasks.put(updatedTask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                updateEpicFields(epic);
            }
        } else if (updatedTask instanceof Epic) {
            epics.put(updatedTask.getId(), (Epic) updatedTask);
            updateEpicStatus((Epic) updatedTask);
            updateEpicFields((Epic) updatedTask);
        } else {
            tasks.put(updatedTask.getId(), updatedTask);
        }
    }

    @Override
    public List<Subtask> getSubtasksForEpic(int epicId) {
        return epics.getOrDefault(epicId, new Epic("", "")).getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
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


    public Epic getEpicById(int epicId) {
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

    public int addEpic(Epic epic) {
        if (hasOverlappingTasks(epic)) {
            throw new IllegalArgumentException("Новая задача пересекается по времени выполнения с существующей задачей.");
        }
        prioritizedTasks.add(epic);
        return addTask(epic);
    }

    public int addSubtask(Subtask subtask) {
        if (hasOverlappingTasks(subtask)) {
            throw new IllegalArgumentException("Новая задача пересекается по времени выполнения с существующей задачей.");
        }
        prioritizedTasks.add(subtask);
        return addTask(subtask);
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void setNextId(int nextId) {
        this.nextId = nextId;
    }
}
