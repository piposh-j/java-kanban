package task.manager;

import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

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

    public InMemoryTaskManager(HistoryManager defaultHistory) {
        this.historyManager = defaultHistory;
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
        Epic currentEpic = epics.get(id);
        List<Integer> idsSubtask = currentEpic.getSubtaskIds();
        for (Integer idSubtask : idsSubtask) {
            subtasks.remove(idSubtask);
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
            updateEpicStatus(epic);
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
            subtasks.remove(id);
        }
        return currentSubtask;
    }

    @Override
    public List<Subtask> getSubtasksByEpic(Epic epic) {
        List<Subtask> newSubtasks = new ArrayList<>();
        for (Integer idSubtask : epic.getSubtaskIds()) {
            newSubtasks.add(subtasks.get(idSubtask));
        }
        return newSubtasks;
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> idsSubtasks = epic.getSubtaskIds();

        if (idsSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean isAllTasksNew = true;
        boolean isAllTasksDone = true;
        for (Integer idSubtask : idsSubtasks) {
            Subtask subtask = subtasks.get(idSubtask);

            if (isAllTasksNew && subtask.getStatus() != TaskStatus.NEW) {
                isAllTasksNew = false;
            }

            if (isAllTasksDone && subtask.getStatus() != TaskStatus.DONE) {
                isAllTasksDone = false;
            }
        }

        //Если все сабтаски NEW, эпик так же переводим NEW
        if (isAllTasksNew) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        //Если все сабтаски в DONE, эпик так же переводим в DONE
        if (isAllTasksDone) {
            epic.setStatus(TaskStatus.DONE);
            return;
        }

        // В остальных случиях эпик IN_PROGRESS
        epic.setStatus(TaskStatus.IN_PROGRESS);
    }

    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
