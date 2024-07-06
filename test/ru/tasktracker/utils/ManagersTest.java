package ru.tasktracker.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.tasktracker.service.HistoryManager;
import ru.tasktracker.service.InMemoryHistoryManager;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.service.InMemoryTaskManager;
import ru.tasktracker.util.Managers;

public class ManagersTest {

    @Test
    void getDefaultHistory_shouldReturnInstanceInMemoryHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();

        Assertions.assertInstanceOf(InMemoryHistoryManager.class, history);
    }

    @Test
    void getDefault_shouldReturnInstanceInMemoryTaskManager() {
        TaskManager history = Managers.getDefault();

        Assertions.assertInstanceOf(InMemoryTaskManager.class, history);
    }
}
