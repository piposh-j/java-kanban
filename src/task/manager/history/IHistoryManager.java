package task.manager.history;

import task.type.Task;

import java.util.ArrayList;
import java.util.List;

public interface IHistoryManager {
    void add(Task task);
    List<Task> getHistory();
}
