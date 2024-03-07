package task.manager.task;

import task.type.Epic;
import task.type.Subtask;
import task.type.Task;
import java.util.ArrayList;
import java.util.List;

public interface ITaskManager {
    /*
     * Tasks
     */
    ArrayList<Task> getTasks();

    void deleteTasks();

    Task getTaskById(int id);

    Task addTask(Task task);

    Task updateTask(Task task);

    Task deleteTaskById(int id);

    ArrayList<Epic> getEpics();

    void deleteEpics();

    Epic getEpicById(int id);

    Epic addEpic(Epic epic);

    Epic updateEpic(Epic epic);

    Epic deleteEpicById(int id);

    ArrayList<Subtask> getSubtasks();

    void deleteSubtasks();

    Subtask getSubtaskById(int id);

    Subtask addSubtask(Subtask subtask, Epic epic);

    Subtask updateSubtask(Subtask subtask);

    Subtask deleteSubtaskById(int id);

    ArrayList<Subtask> getSubtasksByEpic(Epic epic);

     List<Task> getHistory();
}
