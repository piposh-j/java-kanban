package ru.tasktracker.service;

import ru.tasktracker.model.TaskStatus;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private HistoryManager historyManager;
    private int id;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Task addTask(Task task) {
        task.setId(id++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
        return task;
    }

    @Override
    public Task deleteTaskById(int id) {
        historyManager.remove(id);
        return tasks.remove(id);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(id++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
        }
        return epic;
    }

    @Override
    public Epic deleteEpicById(int id) {
        historyManager.remove(id);
        Epic currentEpic = epics.get(id);
        List<Integer> subtaskIds = currentEpic.getSubtaskIds();
        for (Integer subtaskId : subtaskIds) {
            historyManager.remove(subtaskId);
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
        return currentEpic;
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(TaskStatus.NEW);
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        subtask.setId(id++);
        subtask.setEpicId(epic.getId());
        subtasks.put(subtask.getId(), subtask);
        epic.getSubtaskIds().add(subtask.getId());
        updateEpicStatus(epic);
        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(epics.get(subtask.getEpicId()));
        }
        return subtask;
    }

    @Override
    public Subtask deleteSubtaskById(int id) {
        Subtask currentSubtask = subtasks.get(id);
        if (currentSubtask != null) {
            Epic currentEpic = epics.get(currentSubtask.getEpicId());
            currentEpic.getSubtaskIds().remove(Integer.valueOf(currentSubtask.getId()));
            updateEpicStatus(currentEpic);
            historyManager.remove(id);
            subtasks.remove(id);
        }
        return currentSubtask;
    }

    @Override
    public List<Subtask> getSubtasksByEpic(Epic epic) {
        List<Subtask> newSubtasks = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            newSubtasks.add(subtasks.get(subtaskId));
        }
        return newSubtasks;
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean isAllTasksNew = true;
        boolean isAllTasksDone = true;
        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);

            if (isAllTasksNew && subtask.getStatus() != TaskStatus.NEW) {
                isAllTasksNew = false;
            }

            if (isAllTasksDone && subtask.getStatus() != TaskStatus.DONE) {
                isAllTasksDone = false;
            }
        }

        if (isAllTasksNew) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        if (isAllTasksDone) {
            epic.setStatus(TaskStatus.DONE);
            return;
        }

        epic.setStatus(TaskStatus.IN_PROGRESS);
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
