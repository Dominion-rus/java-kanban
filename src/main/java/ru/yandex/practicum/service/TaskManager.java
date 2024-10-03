package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;

import java.util.List;
import java.util.Map;

/**
 * @author vsmordvincev
 */
public interface TaskManager {
    int addTask(Task task); // Добавление задачи

    Task getTaskById(int id); // Получение задачи по ID

    List<Task> getAllTasks(); // Получение всех задач

    List<Epic> getAllEpics(); // Получение всех эпиков

    List<Subtask> getAllSubtasks(); // Получение всех подзадач

    void removeTaskById(int id); // Удаление задачи по ID

    void removeAllTasks(); // Удаление всех задач

    void removeAllEpics(); // Удаление всех эпиков

    void removeAllSubtasks(); // Удаление всех подзадач

    void updateTask(Task updatedTask); // Обновление задачи

    List<Subtask> getSubtasksForEpic(int epicId); // Получение подзадач эпика

    Map<Epic, List<Subtask>> getEpicAndSubtasks(int epicId); // Получение эпика и его подзадач

    List<Task> getHistory(); // Получение истории


}


