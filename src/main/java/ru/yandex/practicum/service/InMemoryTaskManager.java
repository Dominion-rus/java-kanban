package ru.yandex.practicum.service;

import ru.yandex.practicum.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    private int nextId = 1;

    private final Set<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
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

    protected void updateEpicFields(Epic epic) {
        Duration totalDuration = null;
        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;

        // Рассчитываем общие значения на основе подзадач
        for (Subtask subtask : getSubtasksForEpic(epic.getId())) {
            if (subtask.getDuration() != null) {
                totalDuration = (totalDuration == null) ? subtask.getDuration() : totalDuration
                        .plus(subtask.getDuration());
            }

            if (subtask.getStartTime() != null) {
                if (earliestStartTime == null || subtask.getStartTime().isBefore(earliestStartTime)) {
                    earliestStartTime = subtask.getStartTime();
                }
            }

            if (subtask.getEndTime() != null) {
                if (latestEndTime == null || subtask.getEndTime().isAfter(latestEndTime)) {
                    latestEndTime = subtask.getEndTime();
                }
            }
        }

        // Устанавливаем значения в эпик
        epic.setDuration(totalDuration);
        epic.setStartTime(earliestStartTime);
        epic.setEndTime(latestEndTime);
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
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task); // Удаляем задачу из TreeSet
            return;
        }

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
            return;
        }

        // Удаление эпика и всех его подзадач
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask removedSubtask = subtasks.remove(subtaskId);
                if (removedSubtask != null) {
                    prioritizedTasks.remove(removedSubtask); // Удаляем подзадачи из TreeSet
                }
            }
        }
    }

    // Метод для удаления всех обычных задач
    @Override
    public void removeAllTasks() {
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    // Метод для удаления всех эпиков и связанных с ними подзадач
    @Override
    public void removeAllEpics() {
        // Удаляем все подзадачи, связанные с эпиками
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().forEach(subtaskId -> {
                Task subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask); // Удаляем подзадачи из отсортированного списка
                }
            });
        });
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
        // Удаляем старую версию задачи из отсортированного списка, если это не эпик
        if (updatedTask.getType() != TaskType.EPIC) {
            Task oldTask = getTaskById(updatedTask.getId());
            if (oldTask != null) {
                prioritizedTasks.remove(oldTask);
            }
        }

        // Проверка на пересечение временных интервалов, если это не эпик
        if (updatedTask.getType() != TaskType.EPIC && hasOverlappingTasks(updatedTask)) {
            throw new IllegalArgumentException("Обновленная задача пересекается по времени выполнения " +
                    "с существующей задачей.");
        }

        // Обновляем задачу в зависимости от типа
        switch (updatedTask.getType()) {
            case SUBTASK:
                Subtask subtask = (Subtask) updatedTask;
                subtasks.put(updatedTask.getId(), subtask);
                Epic epic = epics.get(subtask.getEpicId());
                if (epic != null) {
                    updateEpicStatus(epic);
                    updateEpicFields(epic);
                }
                // Добавляем обновлённую подзадачу в отсортированный список
                prioritizedTasks.add(updatedTask);
                break;

            case EPIC:
                epics.put(updatedTask.getId(), (Epic) updatedTask);
                updateEpicStatus((Epic) updatedTask);
                updateEpicFields((Epic) updatedTask);
                // Эпики не добавляются в отсортированный список
                break;

            case TASK:
                tasks.put(updatedTask.getId(), updatedTask);
                // Добавляем обновлённую задачу в отсортированный список
                prioritizedTasks.add(updatedTask);
                break;

            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + updatedTask.getType());
        }
    }

    @Override
    public List<Subtask> getSubtasksForEpic(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(Epic::getSubtaskIds)
                .orElse(List.of()) // Возвращаем пустой список, если эпик не найден
                .stream()
                .map(subtasks::get)
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

    @Override
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
        return addTask(epic);
    }
    @Override
    public int addSubtask(Subtask subtask) {
        if (hasOverlappingTasks(subtask)) {
            throw new IllegalArgumentException("Новая задача пересекается по времени выполнения с существующей задачей.");
        }
        prioritizedTasks.add(subtask);
        return addTask(subtask);
    }
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void setNextId(int nextId) {
        this.nextId = nextId;
    }
}
