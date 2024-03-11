package ru.tasktracker.service;

import ru.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_SIZE_HISTORY = 10;
    private List<Task> history = new ArrayList<>(MAX_SIZE_HISTORY);

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void add(Task task) {
        if (history.size() == MAX_SIZE_HISTORY) {
            history.removeFirst();
        }
        history.add(task);
    }
}
