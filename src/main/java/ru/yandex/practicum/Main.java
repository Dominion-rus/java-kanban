package ru.yandex.practicum;

import ru.yandex.practicum.model.Epic;
import ru.yandex.practicum.model.Status;
import ru.yandex.practicum.model.Subtask;
import ru.yandex.practicum.model.Task;
import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.InMemoryTaskManager;
import ru.yandex.practicum.service.Managers;
import ru.yandex.practicum.service.TaskManager;

import static ru.yandex.practicum.utils.PrintManager.printAllTasks;
import static ru.yandex.practicum.utils.PrintManager.printEpicWithSubtasks;

/**
 * @author vsmordvincev
 */
public class Main {
    public static void main(String[] args) {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager tracker = new InMemoryTaskManager(historyManager);

        // Создание обычных задач
        Task task1 = new Task("Сходить в магазин", "Купить продукты", Status.NEW);
        tracker.addTask(task1);

        Task task2 = new Task("Выйти из магазина", "Съесть продукты", Status.NEW);
        tracker.addTask(task2);

        // Создание эпика и подзадач
        Epic epic = new Epic("Переезд", "Организация переезда");
        int epicId = tracker.addTask(epic);

        Subtask subtask1 = new Subtask("Упаковка вещей", "Упаковать все вещи", Status.NEW, epicId);
        tracker.addTask(subtask1);

        Subtask subtask2 = new Subtask("Аренда грузовика", "Арендовать грузовик для перевозки", Status.NEW, epicId);
        tracker.addTask(subtask2);

        // Печать всех задач после их добавления
        System.out.println("\nВсе задачи после их добавления:");
        printAllTasks(tracker);

        System.out.println("\nСостояние эпика и его подзадач:");
        printEpicWithSubtasks(tracker, epicId);

        // Обновление статуса подзадачи
        subtask1.setStatus(Status.IN_PROGRESS);
        tracker.updateTask(subtask1);

        // Печать состояния эпика и подзадач после обновления статуса подзадачи
        System.out.println("\nПосле обновления статуса первой подзадачи:");
        printAllTasks(tracker);

        // Завершение всех подзадач
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        tracker.updateTask(subtask1);
        tracker.updateTask(subtask2);

        // Печать состояния эпика после завершения всех подзадач
        System.out.println("\nПосле завершения всех подзадач:");
        printAllTasks(tracker);

        // Использование метода getTaskById для создания истории просмотров
        System.out.println("\nПолучение задач для истории просмотров:");
        tracker.getTaskById(task1.getId());
        tracker.getTaskById(epicId);
        tracker.getTaskById(subtask1.getId());
        tracker.getTaskById(task2.getId());

        // Печать истории просмотров
        System.out.println("\nИстория просмотров задач:");
        printAllTasks(tracker);

        // Удаление первой задачи
        tracker.removeTaskById(task1.getId());

        // Печать всех задач после удаления первой задачи
        System.out.println("\nВсе задачи после удаления первой задачи:");
        printAllTasks(tracker);

        // Удаление всех эпиков и подзадач
        tracker.removeAllEpics();
        tracker.removeAllSubtasks();
        tracker.removeAllTasks();

        // Печать всех задач после удаления всех задач
        System.out.println("\nВсе задачи после удаления всех задач:");
        printAllTasks(tracker);
    }

}