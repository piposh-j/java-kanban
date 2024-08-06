package ru.tasktracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TaskStatus;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    @BeforeEach
    public void beforeEach() throws IOException {
        taskManager = createTaskManager();
    }

    protected abstract T createTaskManager() throws IOException;

    @Test
    void addTask_shouldAddTaskToManager() {
        Task task = new Task(0, "навзание", "Описание", TaskStatus.NEW);

        taskManager.addTask(task);

        assertEquals(1, taskManager.getTasks().size());
    }

    @Test
    void addTask_shouldOverwriteIdsTask() {
        Task task1 = new Task(0, "навзание", "Описание", TaskStatus.NEW);
        Task task2 = new Task(0, "навзание", "Описание", TaskStatus.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertEquals(2, taskManager.getTasks().size());
        assertNotEquals(0, taskManager.getTasks().get(1).getId());
    }

    @Test
    void getTaskById_shouldFindTaskToManager() {
        Task task = new Task(0, "навзание", "Описание", TaskStatus.NEW);

        taskManager.addTask(task);

        assertEquals(task, taskManager.getTaskById(task.getId()));
    }

    @Test
    void addSubtask_shouldAddSubtaskToManager() {
        Epic epic = new Epic(0, "навзание", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask(1, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        assertEquals(1, taskManager.getSubtasks().size());
    }

    @Test
    void addTask_shouldOverwriteIdsSubtask() {
        Epic epic = new Epic(0, "навзание", "Описание");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(2, taskManager.getSubtasks().size());
        assertNotEquals(0, taskManager.getSubtasks().get(1).getId());
    }

    @Test
    void getSubtaskById_shouldFindSubtaskToManager() {
        Epic epic = new Epic(0, "навзание", "Описание");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());

        taskManager.addSubtask(subtask);

        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void addEpic_shouldAddEpicToManager() {
        Epic epic = new Epic(0, "навзание", "Описание");

        taskManager.addEpic(epic);

        assertEquals(1, taskManager.getEpics().size());
    }

    @Test
    void getSubtaskById_shouldFindEpicToManager() {
        Epic epic = new Epic(0, "навзание", "Описание");

        taskManager.addEpic(epic);

        assertEquals(epic, taskManager.getEpicById(epic.getId()));
    }

    @Test
    void addTask_shouldOverwriteIdsEpic() {
        Epic epic1 = new Epic(0, "навзание", "Описание");
        Epic epic2 = new Epic(0, "навзание", "Описание");

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        assertEquals(2, taskManager.getEpics().size());
        assertNotEquals(0, taskManager.getEpics().get(1).getId());
    }

}
