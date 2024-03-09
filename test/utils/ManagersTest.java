package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.manager.HistoryManager;
import task.manager.InMemoryHistoryManager;
import task.manager.TaskManager;
import task.manager.InMemoryTaskManager;
import task.util.Managers;

public class ManagersTest {

    @Test
    void getDefaultHistory_shouldReturnInstanceInMemoryHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();

        Assertions.assertTrue(history instanceof InMemoryHistoryManager);
    }

    @Test
    void getDefault_shouldReturnInstanceInMemoryTaskManager() {
        TaskManager history = Managers.getDefault();

        Assertions.assertTrue(history instanceof InMemoryTaskManager);
    }
}
