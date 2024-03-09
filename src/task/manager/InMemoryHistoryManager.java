package task.manager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    final private static int MAX_SIZE_HISTORY = 10;
    private List<Task> history = new ArrayList<>(MAX_SIZE_HISTORY);

    @Override
    public ArrayList<Task> getHistory() {
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
