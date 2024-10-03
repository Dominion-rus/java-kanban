package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task);

    void remove(int id);

    List<Task> getHistory();

}
