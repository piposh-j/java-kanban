package ru.tasktracker.service;

import ru.tasktracker.model.TaskStatus;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;
import ru.tasktracker.exception.TaskTimeConflictException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected Set<Task> sortedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    protected HistoryManager historyManager;
    protected int id;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteTasks() {
        tasks.values().forEach(task -> sortedTasks.remove(task));
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Task addTask(Task task) {
        if (task != null && !hasOverlap(task)) {
            task.setId(id++);
            tasks.put(task.getId(), task);
            addTaskToSortedTasks(task);
        }
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
        Task task = tasks.remove(id);
        historyManager.remove(id);
        sortedTasks.remove(task);
        return task;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteEpics() {
        subtasks.values().forEach(subtask -> sortedTasks.remove(subtask));
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
        if (epic != null) {
            epic.setId(id++);
            epics.put(epic.getId(), epic);
        }
        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        return epic;
    }

    @Override
    public Epic deleteEpicById(int id) {
        historyManager.remove(id);
        Epic currentEpic = epics.get(id);
        currentEpic.getSubtaskIds().forEach(subtaskId -> {
            historyManager.remove(subtaskId);
            Subtask subtask = subtasks.remove(subtaskId);
            sortedTasks.remove(subtask);
        });
        epics.remove(id);
        return currentEpic;
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteSubtasks() {
        subtasks.values().forEach(subtask -> sortedTasks.remove(subtask));
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            epic.setStatus(TaskStatus.NEW);
            updateEpicTime(epic);
        });
    }

    @Override
    public Subtask getSubtaskById(int id) {
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask != null && !hasOverlap(subtask)) {
            Epic epic = epics.get(subtask.getEpicId());
            subtask.setId(id++);
            subtask.setEpicId(epic.getId());
            subtasks.put(subtask.getId(), subtask);
            addTaskToSortedTasks(subtask);
            epic.getSubtaskIds().add(subtask.getId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        return subtask;
    }

    @Override
    public Subtask deleteSubtaskById(int id) {
        Subtask currentSubtask = subtasks.get(id);
        if (currentSubtask != null) {
            Epic currentEpic = epics.get(currentSubtask.getEpicId());
            currentEpic.getSubtaskIds().remove(Integer.valueOf(currentSubtask.getId()));
            historyManager.remove(id);
            Subtask subtask = subtasks.remove(id);
            sortedTasks.remove(subtask);
            updateEpicStatus(currentEpic);
            updateEpicTime(currentEpic);
        }
        return currentSubtask;
    }

    @Override
    public List<Subtask> getSubtasksByEpic(Epic epic) {
        List<Subtask> newSubtasks = new ArrayList<>();
        epic.getSubtaskIds().forEach(subtaskId -> newSubtasks.add(subtasks.get(subtaskId)));
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

    private void updateEpicTime(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();

        LocalDateTime startTime = findStartTimeInSubtasks(subtaskIds);
        LocalDateTime endTime = findEndTimeInSubtasks(subtaskIds);

        epic.setStartTime(startTime);

        if (endTime != null) {
            epic.setEndTime(endTime);
            epic.setDuration(Duration.between(startTime, endTime));
        } else {
            epic.setEndTime(null);
        }
    }

    private LocalDateTime findStartTimeInSubtasks(List<Integer> subtaskIds) {
        return subtaskIds
                .stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getStartTime() != null)
                .min(Comparator.comparing(Task::getStartTime))
                .map(Task::getStartTime)
                .orElse(null);
    }

    private LocalDateTime findEndTimeInSubtasks(List<Integer> subtaskIds) {
        return subtaskIds
                .stream()
                .map(subtasks::get)
                .filter(subtask -> subtask.getStartTime() != null)
                .max(Comparator.comparing(Task::getEndTime))
                .map(Task::getEndTime)
                .orElse(null);
    }

    public Set<Task> getPrioritizedTasks() {
        TreeSet<Task> treeSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        treeSet.addAll(sortedTasks);
        return treeSet;
    }

    private void addTaskToSortedTasks(Task task) {
        if (task.getStartTime() == null || hasOverlap(task)) {
            return;
        }
        sortedTasks.add(task);
    }

    private boolean hasOverlap(Task newTask) {
        if (sortedTasks.stream().anyMatch(task -> checkIntersection(task, newTask))) {
            throw new TaskTimeConflictException("Конфликт времени между задачами");
        }
        return false;
    }


    private boolean checkIntersection(Task first, Task second) {
        LocalDateTime startFirst = first.getStartTime();
        LocalDateTime endFirst = first.getEndTime();

        LocalDateTime startSecond = second.getStartTime();
        LocalDateTime endSecond = second.getEndTime();
        return (startFirst.isBefore(endSecond) && endFirst.isAfter(startSecond));
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
