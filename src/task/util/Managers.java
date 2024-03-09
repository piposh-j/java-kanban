package task.util;

import task.manager.HistoryManager;
import task.manager.InMemoryHistoryManager;
import task.manager.TaskManager;
import task.manager.InMemoryTaskManager;

public class Managers {

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }
}
