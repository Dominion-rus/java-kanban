package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> taskMap = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    // Вспомогательный класс для двусвязного списка
    private static class Node<T> {
        T value;
        Node<T> next;
        Node<T> prev;

        Node(T value, Node<T> prev, Node<T> next) {
            this.value = value;
            this.prev = prev;
            this.next = next;
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
        removeNode(node);

    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>(taskMap.size());
        Node<Task> current = head;
        while (current != null) {
            history.add(current.value);
            current = current.next;
        }
        return history;
    }

    private void linkLast(Task task) {
        final Node<Task> newNode = new Node<>(task, tail, null);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
        // Сохраняем задачу в хэш-таблице для быстрого доступа
        taskMap.put(task.getId(), newNode);
    }

    // Удаляет узел из двусвязного списка
    private void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }

        Node<Task> prevNode = node.prev;
        Node<Task> nextNode = node.next;

        if (prevNode != null) {
            prevNode.next = nextNode;
        } else {
            head = nextNode;
        }
        if (nextNode != null) {
            nextNode.prev = prevNode;
        } else {
            tail = prevNode;
        }
    }
}

