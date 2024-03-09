package task.manager.task;

import task.manager.TaskManager;
import task.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.util.Managers;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    void addTask_shouldAddTaskToManager() {
        Task task = new Task("навзание", "Описание");

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
        Task task = new Task("навзание", "Описание");

        taskManager.addTask(task);

        assertEquals(task, taskManager.getTaskById(task.getId()));
    }

    @Test
    void addSubtask_shouldAddSubtaskToManager() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask = new Subtask("название", "описание");
        taskManager.addEpic(epic);

        taskManager.addSubtask(subtask, epic);

        assertEquals(1, taskManager.getSubtasks().size());
    }

    @Test
    void addTask_shouldOverwriteIdsSubtask() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask1 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic);
        Subtask subtask2 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic);
        taskManager.addEpic(epic);

        taskManager.addSubtask(subtask1, epic);
        taskManager.addSubtask(subtask2, epic);

        assertEquals(2, taskManager.getSubtasks().size());
        assertNotEquals(0, taskManager.getSubtasks().get(1).getId());
    }

    @Test
    void getSubtaskById_shouldFindSubtaskToManager() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask = new Subtask("название", "описание");
        taskManager.addEpic(epic);

        taskManager.addSubtask(subtask, epic);

        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void addEpic_shouldAddEpicToManager() {
        Epic epic = new Epic("навзание", "Описание");

        taskManager.addEpic(epic);

        assertEquals(1, taskManager.getEpics().size());
    }

    @Test
    void getSubtaskById_shouldFindEpicToManager() {
        Epic epic = new Epic("навзание", "Описание");

        taskManager.addEpic(epic);

        assertEquals(epic, taskManager.getEpicById(epic.getId()));
    }

    @Test
    void addTask_shouldOverwriteIdsEpic() {
        Epic epic1 = new Epic("навзание", "Описание");
        Epic epic2 = new Epic("навзание", "Описание");

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        assertEquals(2, taskManager.getEpics().size());
        assertNotEquals(0, taskManager.getEpics().get(1).getId());
    }

    @Test
    void updateEpicStatus_shouldReturnInProgressStatusEpic() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask = new Subtask("название", "описание");
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask, epic);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.IN_PROGRESS, epic);

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void updateEpicStatus_shouldReturnDoneStatusEpic() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask = new Subtask("название", "описание");
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask, epic);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.DONE, epic);

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void addTask_shouldReturnDoneStatusEpic() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask = new Subtask("название", "описание");
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask, epic);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.DONE, epic);

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void addTask_shouldNotChangeFielsTaskAfterAddToManager() {
        String nameTask = "навзание";
        String descriptionTask = "описание";
        Task task = new Task(nameTask, descriptionTask);

        taskManager.addTask(task);
        Task taskFromManager = taskManager.getTaskById(task.getId());

        assertEquals(nameTask, taskFromManager.getName());
        assertEquals(descriptionTask, taskFromManager.getDescription());
    }

    @Test
    void deleteEpicById_shouldDeleteEpicAndSubtask() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask1 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1, epic);

        taskManager.deleteEpicById(epic.getId());

        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
    }

    @Test
    void deleteSubtaskById_shouldDeleteSubtaskAndRemoveEpicLink() {
        Epic epic = new Epic("навзание", "Описание");
        Subtask subtask1 = new Subtask(0, "название", "описание", TaskStatus.NEW, epic);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask1, epic);

        taskManager.deleteSubtaskById(subtask1.getId());

        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getEpicById(epic.getId()).getIdsSubtasks().isEmpty());
    }

    @Test
    void getHistory_shouldReturnHistory() {
        Task task1 = new Task("навзание1", "Описание1");
        Task task2 = new Task("навзание2", "Описание2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        assertEquals(2, taskManager.getHistory().size());
        assertEquals(task1, taskManager.getHistory().get(0));
        assertEquals(task2, taskManager.getHistory().get(1));
    }
}