package TypeTask;

import enums.TaskStatus;

public class Subtask extends Task {
    private int parentEpicId = 0;

    public Subtask(String name, String description) {
        super(name, description);
    }

    public Subtask(String name, String description, int parentEpicId) {
        super(name, description);
        this.parentEpicId = parentEpicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int parentEpicId) {
        super(id, name, description, status);
        this.parentEpicId = parentEpicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, Epic epic) {
        super(id, name, description, status);
        this.parentEpicId = epic.getId();
    }

    public void setParentEpicId(int parentEpicId) {
        this.parentEpicId = parentEpicId;
    }

    public int getParentEpicId() {
        return parentEpicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "parentEpicId=" + parentEpicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
