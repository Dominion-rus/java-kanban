package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{

    private final LinkedList<Task> history = new LinkedList<>();
    private static final int HISTORY_LIMIT = 10;


    @Override
    public void add(Task task) {
        history.remove(task);
        history.addFirst(task);
        if (history.size() > HISTORY_LIMIT) {
            history.removeLast();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
