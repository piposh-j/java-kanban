package task.manager;

import task.type.Epic;
import task.type.Subtask;
import task.type.Task;
import enums.TaskStatus;
import utils.IdGenerator;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private IdGenerator idGenerator = new IdGenerator();

    /*
     * Tasks
     */
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Task addTask(Task task) {
        task.setId(idGenerator.getIdSequence());
        tasks.put(task.getId(), task);
        return task;
    }

    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
        return task;
    }

    public Task deleteTaskById(int id) {
        return tasks.remove(id);
    }

    /*
     * Epic
     */
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    //Если удаляем эпик, значит отвязываем epicId у Subtask
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Epic addEpic(Epic epic) {
        epic.setId(idGenerator.getIdSequence());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Epic updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
        }
        return epic;
    }

    //Если удаляем эпик, значит удаляем и Subtask
    public Epic deleteEpicById(int id) {
        Epic currentEpic = epics.get(id);
        ArrayList<Integer> idsSubtask = currentEpic.getIdsSubtasks();
        for (Integer idSubtask : idsSubtask) {
            subtasks.remove(idSubtask);
        }
        epics.remove(id);
        return currentEpic;
    }

    /*
     * Subtask
     */
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // У каждого эпика очищаем свзять с сабтаском
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getIdsSubtasks().clear();
            updateEpicStatus(epic);
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Subtask addSubtask(Subtask subtask, Epic epic) {
        subtask.setId(idGenerator.getIdSequence());
        subtask.setParentEpicId(epic.getId());
        subtasks.put(subtask.getId(), subtask);
        epic.getIdsSubtasks().add(subtask.getId());
        updateEpicStatus(epic);
        return subtask;
    }

    public Subtask updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(getEpicById(subtask.getParentEpicId()));
        }
        return subtask;
    }

    public Subtask deleteSubtaskById(int id) {
        Subtask currentSubtask = subtasks.get(id);
        if (currentSubtask != null) {
            Epic currentEpic = epics.get(currentSubtask.getParentEpicId());
            currentEpic.getIdsSubtasks().remove(Integer.valueOf(currentSubtask.getId()));
            updateEpicStatus(currentEpic);
            subtasks.remove(id);
        }
        return currentSubtask;
    }

    public ArrayList<Subtask> getSubtasksByEpic(Epic epic) {
        ArrayList<Subtask> newSubtasks = new ArrayList<>();
        for (Integer idSubtask : epic.getIdsSubtasks()) {
            newSubtasks.add(subtasks.get(idSubtask));
        }
        return newSubtasks;
    }

    private void updateEpicStatus(Epic epic) {
        ArrayList<Integer> idsSubtasks = epic.getIdsSubtasks();

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
}
