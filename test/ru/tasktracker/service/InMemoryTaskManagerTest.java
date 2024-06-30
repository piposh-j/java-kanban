package ru.tasktracker.service;

import ru.tasktracker.exception.TaskTimeConflictException;
import ru.tasktracker.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    void updateEpicStatus_shouldReturnStatusInProgressInEpic() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.IN_PROGRESS, epic.getId());

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void updateEpicStatus_shouldReturnStatusDoneInEpic() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.DONE, epic.getId());

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void addTask_shouldReturnStatusDoneInEpic() {
        Epic epic = new Epic(0, "навзание", "Описание");
        Subtask subtask = new Subtask(0, "название", "описание", TaskStatus.NEW, epic.getId());
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Subtask newSubtask = new Subtask(subtask.getId(), "Новая подзадача_1", "Новое описание подзадачи_1", TaskStatus.DONE, epic.getId());

        taskManager.updateSubtask(newSubtask);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void addTask_checkEqualsFieldsTaskAfterAddToManager() {
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
    void deleteSubtaskById_checkUnlinkEpicAfterDeleteSubtask() {
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
    void getHistory_checkRemoveTaskFromHistory() {
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

    @Test
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

    @Test
    void deleteEpicById_checkSortedSet() {
        Task task1 = new Task(0, "Задача_1", "Описание_1", TaskStatus.NEW);
        Task task2 = new Task(1, "Задача_2", "Описание_2", TaskStatus.NEW);
        Task task3 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2000, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        Epic epic2 = new Epic(1, "Эпик_2", "Описание_2");

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        Subtask subtask1 = new Subtask(0,
                "Подзача_1",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2001, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask(1,
                "Подзача_2",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2002, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));
        Subtask subtask3 = new Subtask(3,
                "Подзача_3",
                "Эпик_2",
                TaskStatus.NEW, epic2.getId(),
                LocalDateTime.of(2003, 1, 1, 11, 10, 11),
                Duration.ofMinutes(60));


        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        taskManager.deleteEpicById(epic1.getId());

        assertEquals(taskManager.getPrioritizedTasks().size(), 2);
        assertTrue(taskManager.getPrioritizedTasks().contains(task3));
        assertTrue(taskManager.getPrioritizedTasks().contains(subtask3));
    }

    @Test
    void deleteSubtaskById_checkSortedSet() {
        Task task1 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2000, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        taskManager.addTask(task1);

        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        Epic epic2 = new Epic(1, "Эпик_2", "Описание_2");

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        Subtask subtask1 = new Subtask(0,
                "Подзача_1",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2001, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask(3,
                "Подзача_3",
                "Эпик_2",
                TaskStatus.NEW, epic2.getId(),
                LocalDateTime.of(2003, 1, 1, 11, 10, 11),
                Duration.ofMinutes(60));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.deleteSubtaskById(subtask1.getId());

        assertEquals(taskManager.getPrioritizedTasks().size(), 2);
        assertTrue(taskManager.getPrioritizedTasks().contains(task1));
        assertTrue(taskManager.getPrioritizedTasks().contains(subtask2));
    }

    @Test
    void deleteSubtaskById_checkUpdateTimeAfterDeletedLastSubtask() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");

        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача_1",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2001, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        Subtask subtask2 = new Subtask(0,
                "Подзача_1",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2002, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.deleteSubtaskById(subtask2.getId());

        assertEquals(taskManager.getEpicById(epic1.getId()).getStartTime(),
                subtask1.getStartTime());

        assertEquals(taskManager.getEpicById(epic1.getId()).getEndTime(),
                subtask1.getEndTime());
    }

    @Test
    void deleteSubtaskById_checkUpdateTimeAfterDeletedFirstSubtask() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");

        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача_1",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2001, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        Subtask subtask2 = new Subtask(0,
                "Подзача_1",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2002, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        Subtask subtask3 = new Subtask(0,
                "Подзача_1",
                "Эпик_1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2004, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        taskManager.deleteSubtaskById(subtask1.getId());

        assertEquals(taskManager.getEpicById(epic1.getId()).getStartTime(),
                subtask2.getStartTime());

        assertEquals(taskManager.getEpicById(epic1.getId()).getEndTime(),
                subtask3.getEndTime());
    }

    @Test
    void addTask_shouldThrowTaskTimeConflictException() {
        Task task1 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2000, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        Task task2 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2000, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));

        taskManager.addTask(task1);

        assertThrows(TaskTimeConflictException.class, () -> taskManager.addTask(task2));
    }


    @Test
    void addSubtask_shouldThrowTaskTimeConflictException() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2024, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask(1,
                "Подзача2_Эпик1",
                "Описание2_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2024, 1, 1, 10, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addSubtask(subtask1);

        assertThrows(TaskTimeConflictException.class, () -> taskManager.addSubtask(subtask2));
    }

    @Test
    void deleteTasks_shouldDeletedAllTaskInSortedTree() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 11, 10, 10),
                Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask(1,
                "Подзача2_Эпик1",
                "Описание2_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 12, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Task task1 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2020, 1, 1, 13, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addTask(task1);

        taskManager.deleteTasks();

        assertEquals(taskManager.getPrioritizedTasks().size(), 2);
        assertFalse(taskManager.getPrioritizedTasks().contains(task1));
    }

    @Test
    void deleteSubtasks_shouldDeletedAllSubtaskInSortedTree() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 11, 10, 10),
                Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask(1,
                "Подзача2_Эпик1",
                "Описание2_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 12, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Task task1 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2020, 1, 1, 13, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addTask(task1);

        taskManager.deleteSubtasks();

        assertEquals(taskManager.getPrioritizedTasks().size(), 1);
        assertFalse(taskManager.getPrioritizedTasks().contains(subtask1));
        assertFalse(taskManager.getPrioritizedTasks().contains(subtask2));
        assertNull(epic1.getStartTime());
        assertNull(epic1.getEndTime());
    }

    @Test
    void deleteEpics_shouldDeletedAllSubtaskInSortedTree() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 11, 10, 10),
                Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask(1,
                "Подзача2_Эпик1",
                "Описание2_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 12, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Task task1 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2020, 1, 1, 13, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addTask(task1);

        taskManager.deleteEpics();

        assertEquals(taskManager.getPrioritizedTasks().size(), 1);
        assertFalse(taskManager.getPrioritizedTasks().contains(subtask1));
        assertFalse(taskManager.getPrioritizedTasks().contains(subtask2));
        assertTrue(taskManager.getPrioritizedTasks().contains(task1));
    }

    @Test
    void deleteTaskById_shouldDeletedTaskInSortedTree() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 11, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addSubtask(subtask1);

        Task task1 = new Task(0,
                "Задача_1",
                "Описание_1",
                TaskStatus.NEW,
                LocalDateTime.of(2020, 1, 1, 13, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addTask(task1);

        taskManager.deleteTaskById(task1.getId());

        assertEquals(taskManager.getPrioritizedTasks().size(), 1);
        assertTrue(taskManager.getPrioritizedTasks().contains(subtask1));
        assertFalse(taskManager.getPrioritizedTasks().contains(task1));
    }

    @Test
    void addSubtask_checkSubtaskPresenceWithStartTimeInSortedTree() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId());

        Subtask subtask2 = new Subtask(0,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 11, 10, 10),
                Duration.ofMinutes(60));
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(taskManager.getPrioritizedTasks().size(), 1);
        assertTrue(taskManager.getPrioritizedTasks().contains(subtask2));
    }


    @Test
    void updateSubtask_checkUpdateTimeInEpicAfterUpdateSubtask() {
        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(99,
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2021, 1, 1, 11, 10, 10),
                Duration.ofMinutes(60));

        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(subtask1.getId(),
                "Подзача1_Эпик1",
                "Описание1_Эпик1",
                TaskStatus.NEW,
                epic1.getId(),
                LocalDateTime.of(2020, 1, 1, 11, 10, 10),
                Duration.ofMinutes(60));

        taskManager.getHistory();
        taskManager.updateSubtask(subtask2);

        assertEquals(taskManager.getPrioritizedTasks().size(), 1);
        assertEquals(taskManager.getEpicById(epic1.getId()).getStartTime(), subtask2.getStartTime());
        assertEquals(taskManager.getEpicById(epic1.getId()).getEndTime(), subtask2.getEndTime());
    }
}