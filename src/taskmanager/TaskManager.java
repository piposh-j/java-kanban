package taskmanager;

import taskmanager.enums.TaskStatus;
import utils.IdGenerator;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    /*
     * Tasks
     */
    public void printTasks() {
        for (Task task : tasks.values()) {
            System.out.println(task);
        }
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Task addTask(Task task) {
        task.setId(IdGenerator.getIdSequence());
        tasks.put(task.getId(), task);
        return task;
    }

    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public Task deleteTaskById(int id) {
        Task currentTask = tasks.get(id);
        tasks.remove(id);
        return currentTask;
    }


    /*
     * Epic
     */
    public void printEpics() {
        for (Epic epic : epics.values()) {
            System.out.println(epic);
        }
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    //Если удаляем эпик, значит отвязываем epicId у Subtask
    public void deleteEpics() {
        epics.clear();
        for (Subtask subtask : subtasks.values()) {
            subtask.setParentEpicId(-1);
        }
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Epic addEpic(Epic epic) {
        epic.setId(IdGenerator.getIdSequence());
        epics.put(epic.getId(), epic);
        return epic;
    }

    //При обновления эпика, сохраняем связь с subtasks
    public Epic updateEpic(Epic epic) {
        ArrayList<Subtask> temp;
        temp = epics.get(epic.getId()).getSubtasks();
        epic.setSubtasks(temp);
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        return epic;
    }

    //Если удаляем эпик, значит "отвязываем" epicId у Subtask
    public Epic deleteEpicById(int id) {
        Epic currentEpic = epics.get(id);
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getParentEpicId() == currentEpic.getId()) {
                subtasks.get(subtask.getId()).setParentEpicId(-1);
            }
        }
        epics.remove(id);
        return currentEpic;
    }

    /*
     * Subtask
     */

    public void printSubtask() {
        for (Subtask subtask : subtasks.values()) {
            System.out.println(subtask);
        }
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    // У каждого эпика очищаем свзять с сабтаском
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.setSubtasks(new ArrayList<>());
            updateEpicStatus(epic);
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Subtask addSubtask(Subtask subtask) {
        subtask.setId(IdGenerator.getIdSequence());
        subtasks.put(subtask.getId(), subtask);
        return subtask;
    }

    public Subtask updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        if (subtasks.containsKey(subtask.getId()) && subtask.getParentEpicId() != -1) {
            updateEpicLinkedSubtask(getEpicById(subtask.getParentEpicId()), subtask);
            updateEpicStatus(getEpicById(subtask.getParentEpicId()));
        }
        return subtask;
    }

    public Subtask deleteSubtaskById(int id) {
        Subtask currentSubtask = subtasks.get(id);
        Epic currentEpic = epics.get(currentSubtask.getParentEpicId());
        currentEpic.getSubtasks().remove(currentSubtask);
        subtasks.remove(id);
        updateEpicStatus(currentEpic);
        return currentSubtask;
    }

    public void addLinkSubtaskWithEpic(Subtask subtask, Epic epic) {

        // Если Эпик отсутствует в мапе, то добавляем
        if(!epics.containsKey(epic.getId())){
            addEpic(epic);
        }

        subtask.setParentEpicId(epic.getId());

        // Если сабтаска отсутствует в мапе, то добавляем
        if(!subtasks.containsKey(subtask.getId())){
            addSubtask(subtask);
        }

        if(!epic.getSubtasks().contains(subtask)) {
            epic.getSubtasks().add(subtask);
        }

        updateEpicStatus(epic);
    }

    public HashMap<Integer, Subtask> getSubtasksByEpic(Epic epic) {
        HashMap<Integer, Subtask> newSubtasks = new HashMap<>();
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getParentEpicId() == epic.getId()) {
                newSubtasks.put(subtask.getId(), subtask);
            }
        }
        return newSubtasks;
    }

    private void updateEpicStatus(Epic epic) {
        ArrayList<Subtask> epicSubtasks = epic.getSubtasks();

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean isAllTasksNew = true;
        boolean isAllTasksDone = true;
        for (Subtask subtask : epic.getSubtasks()) {
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

    private void updateEpicLinkedSubtask(Epic epic, Subtask newSubtask) {
        ArrayList<Subtask> epicSubtasks = epic.getSubtasks();

        for (int i = 0; i < epicSubtasks.size(); i++) {
            if (newSubtask.getId() == epicSubtasks.get(i).getId()) {
                epicSubtasks.set(i, newSubtask);
                break;
            }
        }
    }
}
