package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import task.manager.history.IHistoryManager;
import task.manager.history.InMemoryHistoryManager;
import task.manager.task.ITaskManager;
import task.manager.task.InMemoryTaskManager;

public class ManagersTest {

    @Test
    void getDefaultHistory_shouldReturnInstanceInMemoryHistoryManager() {
        IHistoryManager history = Managers.getDefaultHistory();

        Assertions.assertTrue(history instanceof InMemoryHistoryManager);
    }

    @Test
    void getDefault_shouldReturnInstanceInMemoryTaskManager() {
        ITaskManager history = Managers.getDefault();

        Assertions.assertTrue(history instanceof InMemoryTaskManager);
    }
}
