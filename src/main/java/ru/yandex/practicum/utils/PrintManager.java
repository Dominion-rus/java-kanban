package ru.yandex.practicum.utils;

import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.TaskManager;

import java.util.List;
import java.util.Map;

public class PrintManager {
    public static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            if (task instanceof Epic) continue; // Исключаем эпики, чтобы не дублировать их вывод
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
            for (Subtask subtask : manager.getSubtasksForEpic(epic.getId())) {
                System.out.println("--> " + subtask);
            }
        }

        System.out.println("\nИстория просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
}

    public static void printEpicWithSubtasks(TaskManager manager, int epicId) {
        Map<Epic, List<Subtask>> epicAndSubtasks = manager.getEpicAndSubtasks(epicId);
        for (Map.Entry<Epic, List<Subtask>> entry : epicAndSubtasks.entrySet()) {
            System.out.println(entry.getKey());
            for (Subtask subtask : entry.getValue()) {
                System.out.println("  " + subtask);
            }
        }
    }
}
