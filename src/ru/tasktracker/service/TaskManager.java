package ru.tasktracker.service;

import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;

import java.util.List;
import java.util.Set;

public interface TaskManager {

    List<Task> getTasks();

    void deleteTasks();

    Task getTaskById(int id);

    Task addTask(Task task);

    Task updateTask(Task task);

    Task deleteTaskById(int id);

    List<Epic> getEpics();

    void deleteEpics();

    Epic getEpicById(int id);

    Epic addEpic(Epic epic);

    Epic updateEpic(Epic epic);

    Epic deleteEpicById(int id);

    List<Subtask> getSubtasks();

    void deleteSubtasks();

    Subtask getSubtaskById(int id);

    Subtask addSubtask(Subtask subtask);

    Subtask updateSubtask(Subtask subtask);

    Subtask deleteSubtaskById(int id);

    List<Subtask> getSubtasksByEpic(Epic epic);

    List<Task> getHistory();

    Set<Task> getPrioritizedTasks();
}
