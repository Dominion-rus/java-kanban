package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager{

    private final Map<Integer, Node<Task>> taskMap = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    // Вспомогательный класс для двусвязного списка
    private static class Node<T> {
        T value;
        Node<T> next;
        Node<T> prev;

        Node(T value) {
            this.value = value;
        }
    }

    @Override
    public void add(Task task) {
        // Удаляем задачу из списка, если она уже существует
        remove(task.getId());

        // Добавляем задачу в конец списка (считаем это последним просмотром)
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        // Получаем узел с задачей по id
        Node<Task> node = taskMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            history.add(current.value);
            current = current.next;
        }
        return history;
    }

    private void linkLast(Task task) {
        Node<Task> newNode = new Node<>(task);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        // Сохраняем задачу в хэш-таблице для быстрого доступа
        taskMap.put(task.getId(), newNode);
    }

    // Удаляет узел из двусвязного списка
    private void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }
}

