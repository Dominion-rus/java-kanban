package ru.yandex.practicum;

import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.TaskTracker;

import java.util.List;
import java.util.Map;

/**
 * @author vsmordvincev
 */
public class Main {
    public static void main(String[] args) {
        TaskTracker tracker = new TaskTracker();

        // Создание обычной задачи
        Task task1 = new Task("Сходить в магазин", "Купить продукты", Status.NEW);
        tracker.addTask(task1);
        Task task2 = new Task("Выйти из магазина", "Съесть продукты", Status.NEW);
        tracker.addTask(task2);

        // Создание эпика
        Epic epic = new Epic("Переезд", "Организация переезда");
        int epicId = tracker.addTask(epic);

        // Создание подзадач
        Subtask subtask1 = new Subtask("Упаковка вещей", "Упаковать все вещи", Status.NEW, epicId);
        tracker.addTask(subtask1);

        Subtask subtask2 = new Subtask("Аренда грузовика", "Арендовать грузовик для перевозки", Status.NEW, epicId);
        tracker.addTask(subtask2);

        // Печать всех задач после их добавления
        System.out.println("\nВсе задачи после их добавления:");
        List<Task> allTasks = tracker.getAllTasks();
        for (Task task : allTasks) {
            System.out.println(task);
        }

        // Печать всех эпиков после их добавления
        System.out.println("\nВсе эпики после их добавления:");
        List<Epic> allEpics = tracker.getAllEpics();
        for (Epic e : allEpics) {
            System.out.println(e);
        }

        // Печать всех подзадач после их добавления
        System.out.println("\nВсе подзадачи после их добавления:");
        List<Subtask> allSubtasks = tracker.getAllSubtasks();
        for (Subtask s : allSubtasks) {
            System.out.println(s);
        }

        // Обновление статуса подзадачи
        subtask1.setStatus(Status.IN_PROGRESS);
        tracker.updateTask(subtask1);

        // Печать состояния эпика и подзадач после обновления статуса первой подзадачи
        System.out.println("\nПосле обновления статуса первой подзадачи:");
        Map<Epic, List<Subtask>> epicAndSubtasks = tracker.getEpicAndSubtasks(epicId);
        for (Map.Entry<Epic, List<Subtask>> entry : epicAndSubtasks.entrySet()) {
            System.out.println(entry.getKey());
            for (Subtask subtask : entry.getValue()) {
                System.out.println("  " + subtask);
            }
        }

        // Завершение всех подзадач
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        tracker.updateTask(subtask1);
        tracker.updateTask(subtask2);

        // Печать состояния эпика после завершения всех подзадач
        System.out.println("\nПосле завершения всех подзадач:");
        epicAndSubtasks = tracker.getEpicAndSubtasks(epicId);
        for (Map.Entry<Epic, List<Subtask>> entry : epicAndSubtasks.entrySet()) {
            System.out.println(entry.getKey());
            for (Subtask subtask : entry.getValue()) {
                System.out.println("  " + subtask);
            }
        }

        // Использование метода getTaskById
        System.out.println("\nПолучение задачи по ID:");
        Task retrievedTask = tracker.getTaskById(task1.getId());
        System.out.println(retrievedTask);

        // Печать всех задач с использованием метода getAllTasks
        System.out.println("\nВсе задачи с использованием getAllTasks:");
        allTasks = tracker.getAllTasks();
        for (Task task : allTasks) {
            System.out.println(task);
        }

        // Удаление первой задачи
        tracker.removeTaskById(task1.getId());

        // Печать всех задач после удаления первой задачи
        System.out.println("\nВсе задачи после удаления первой задачи:");
        allTasks = tracker.getAllTasks();
        for (Task task : allTasks) {
            System.out.println(task);
        }

        // Удаление всех эпиков и подзадач
        tracker.removeAllEpics();
        tracker.removeAllSubtasks();
        tracker.removeAllTasks();

        // Печать всех задач после удаления всех задач
        System.out.println("\nВсе задачи после удаления всех задач:");
        allTasks = tracker.getAllTasks();
        for (Task task : allTasks) {
            System.out.println(task);
        }
    }
}
