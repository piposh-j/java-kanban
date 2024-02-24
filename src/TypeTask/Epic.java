package TypeTask;

import enums.TaskStatus;

import java.util.ArrayList;

public class Epic extends Task{
    private ArrayList<Integer> IdsSubtasks = new ArrayList<>();
    public Epic(String name, String description) {
       super(name, description);
    }

    public Epic(String name, String description, Integer IdSubtask) {
        super(name, description);
        this.IdsSubtasks.add(IdSubtask);
    }

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
    }

    public ArrayList<Integer> getIdsSubtasks() {
        return IdsSubtasks;
    }

    public void setSubtasks(ArrayList<Integer> subtasks) {
        this.IdsSubtasks = subtasks;
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
