package task.manager.history;

import task.type.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements IHistoryManager{
    final private byte MAX_SIZE_HISTORY = 10;
    private List<Task> history = new ArrayList<>(MAX_SIZE_HISTORY);

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void add(Task task) {
        if(history.size() == MAX_SIZE_HISTORY) {
            history.removeFirst();
            history.add(task);

        } else {
            history.add(task);
        }
    }
}
