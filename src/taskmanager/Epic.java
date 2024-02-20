package taskmanager;

import taskmanager.enums.TaskStatus;

import java.util.ArrayList;

public class Epic extends Task{
    private ArrayList<Subtask> subtasks = new ArrayList<>();
    public Epic(String name, String description) {
       super(name, description);
       this.status = TaskStatus.NEW;
    }

    public Epic(String name, String description, Subtask subtasks) {
        super(name, description);
        this.subtasks.add(subtasks);
    }

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasks=" + subtasks +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
