package utils;

import task.manager.history.IHistoryManager;
import task.manager.history.InMemoryHistoryManager;
import task.manager.task.ITaskManager;
import task.manager.task.InMemoryTaskManager;

public class Managers {

    public static IHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static ITaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }
}
