package ru.tasktracker.service;

import ru.tasktracker.service.TaskManager;
import ru.tasktracker.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;
import ru.tasktracker.util.Managers;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

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
        Subtask subtask = new Subtask(1, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);

        taskManager.addSubtask(subtask);

        assertEquals(1, taskManager.getSubtasks().size());
    }

    @Test
    void addTask_shouldOverwriteIdsSubtask() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask1 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(2, taskManager.getSubtasks().size());
        assertNotEquals(0, taskManager.getSubtasks().get(1).getId());
    }

    @Test
    void getSubtaskById_shouldFindSubtaskToManager() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);

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

    @Test
    void updateEpicStatus_shouldReturnInProgressStatusEpic() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.IN_PROGRESS, epic.getId());

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void updateEpicStatus_shouldReturnDoneStatusEpic() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.DONE, epic.getId());

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void addTask_shouldReturnDoneStatusEpic() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.DONE, epic.getId());

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void addTask_shouldNotChangeFielsTaskAfterAddToManager() {
        String nameTask = "навзание";
        String descriptionTask = "описание";
        Task task = new Task(0, nameTask, descriptionTask, TaskStatus.NEW);

        taskManager.addTask(task);
        Task taskFromManager = taskManager.getTaskById(task.getId());

        assertEquals(nameTask, taskFromManager.getName());
        assertEquals(descriptionTask, taskFromManager.getDescription());
    }

    @Test
    void deleteEpicById_shouldDeleteEpicAndSubtask() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask1 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

        taskManager.deleteEpicById(epic.getId());

        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
    }

    @Test
    void deleteSubtaskById_shouldDeleteSubtaskAndRemoveEpicLink() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask1 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1);

        taskManager.deleteSubtaskById(subtask1.getId());

        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getEpicById(epic.getId()).getSubtaskIds().isEmpty());
    }

    @Test
    void getHistory_shouldReturnUniqueTask() {
        Task task1 = new Task(0, "навзание1", "Описание1", TaskStatus.NEW);
        Task task2 = new Task(1, "навзание2", "Описание2", TaskStatus.NEW);
        Task task3 = new Task(3, "навзание3", "Описание3", TaskStatus.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());

        assertEquals(3, taskManager.getHistory().size());
        assertEquals(task3, taskManager.getHistory().get(0));
        assertEquals(task2, taskManager.getHistory().get(1));
        assertEquals(task1, taskManager.getHistory().get(2));
    }

    @Test
    void getHistory_shouldRemoveTaskFromHistory() {
        Task task1 = new Task(0, "навзание1", "Описание1", TaskStatus.NEW);
        Task task2 = new Task(1, "навзание2", "Описание2", TaskStatus.NEW);
        Task task3 = new Task(3, "навзание3", "Описание3", TaskStatus.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());

        taskManager.deleteTaskById(task3.getId());
        taskManager.deleteTaskById(task2.getId());
        taskManager.deleteTaskById(task1.getId());

        assertEquals(0, taskManager.getHistory().size());
    }

    @Test
    void getHistory_shouldRemoveSubtaskFromHistory() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask(0, "Подзача1_Эпик1", "Описание1_Эпик1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(1, "Подзача2_Эпик1", "Описание2_Эпик1", TaskStatus.NEW, epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.getEpicById(epic1.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());

        taskManager.deleteEpicById(epic1.getId());

        assertEquals(0, taskManager.getHistory().size());
    }


    void getHistory_shouldReturnUniqueHistory() {
        Task task1 = new Task(0, "навзание1", "Описание1", TaskStatus.NEW);
        taskManager.addTask(task1);
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask(0, "Подзача1_Эпик1", "Описание1_Эпик1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(1, "Подзача2_Эпик1", "Описание2_Эпик1", TaskStatus.NEW, epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getEpicById(epic1.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getEpicById(epic1.getId());

        assertEquals(4, taskManager.getHistory().size());
        assertEquals(subtask1, taskManager.getHistory().get(0));
        assertEquals(task1, taskManager.getHistory().get(1));
        assertEquals(subtask2, taskManager.getHistory().get(2));
        assertEquals(epic1, taskManager.getHistory().get(3));

    }
}