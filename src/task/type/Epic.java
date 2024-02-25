package task.type;

import enums.TaskStatus;

import java.util.ArrayList;

public class Epic extends Task{
    private ArrayList<Integer> IdsSubtasks = new ArrayList<>();
    public Epic(String name, String description) {
       super(name, description);
    }
    public ArrayList<Integer> getIdsSubtasks() {
        return IdsSubtasks;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "IdsSubtasks=" + IdsSubtasks +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
