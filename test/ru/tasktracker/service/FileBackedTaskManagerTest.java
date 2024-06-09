package ru.tasktracker.service;


import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TaskStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private TaskManager taskManager;

    @Test
    void loadFromFile_initFromEmptyFile() throws IOException {
        Path tempFile = Files.createTempFile(null, null);

        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void loadFromFileWithoutHistory_initTask() throws IOException {
        String textData = """
                id,name,status,description,type,epic_id,subtask_ids
                2,Эпик_1,NEW,Описание_1,EPIC,,4;5;
                4,Подзача1_Эпик1,NEW,Описание1_Эпик1,SUBTASK,2,
                5,Подзача2_Эпик1,NEW,Описание2_Эпик1,SUBTASK,2,
                0,Новоое название задачи,DONE,Новое описание задачи,TASK,
                """;

        Path tempFile = Files.createTempFile(null, null);
        Files.writeString(tempFile, textData);
        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertFalse(taskManager.getTasks().isEmpty());
        assertFalse(taskManager.getEpics().isEmpty());
        assertFalse(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void loadFromFileWithHistory_initTask() throws IOException {
        String textData = """
                id,name,status,description,type,epic_id,subtask_ids
                2,Эпик_1,NEW,Описание_1,EPIC,,4;5;
                4,Подзача1_Эпик1,NEW,Описание1_Эпик1,SUBTASK,2,
                5,Подзача2_Эпик1,NEW,Описание2_Эпик1,SUBTASK,2,
                0,Новоое название задачи,DONE,Новое описание задачи,TASK,
                            
                2,4,0,
                """;

        Path tempFile = Files.createTempFile(null, null);
        Files.writeString(tempFile, textData);
        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertFalse(taskManager.getTasks().isEmpty());
        assertFalse(taskManager.getEpics().isEmpty());
        assertFalse(taskManager.getSubtasks().isEmpty());
        assertFalse(taskManager.getHistory().isEmpty());
        assertEquals(taskManager.getHistory().get(0), taskManager.getEpics().getFirst());
        assertEquals(taskManager.getHistory().get(1), taskManager.getSubtasks().getFirst());
        assertEquals(taskManager.getHistory().get(2), taskManager.getTasks().getFirst());
    }


    @Test
    void save_checkTaskSaveToFileSuccess() throws IOException {
        Path tempFile = Files.createTempFile(null, null);
        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        Task task1 = new Task(0, "Задача_1", "Описание_1", TaskStatus.NEW);
        taskManager.addTask(task1);

        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Подзача1_Эпик1", "Описание1_Эпик1", TaskStatus.NEW, epic1.getId());
        taskManager.addSubtask(subtask1);

        taskManager.getTaskById(task1.getId());

        final int HEAD_AND_EMPTY_LINE_QTY_IN_FILE = 2;
        assertEquals(taskManager.getEpics().size() +
                        taskManager.getTasks().size() +
                        taskManager.getSubtasks().size() +
                        taskManager.getHistory().size() +
                        HEAD_AND_EMPTY_LINE_QTY_IN_FILE,
                Files.readAllLines(tempFile).size());
        assertFalse(taskManager.getHistory().isEmpty());
    }


    @Test
    void save_checkTaskSaveToFileWithoutHistorySuccess() throws IOException {
        Path tempFile = Files.createTempFile(null, null);
        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        Task task1 = new Task(0, "Задача_1", "Описание_1", TaskStatus.NEW);
        taskManager.addTask(task1);

        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Подзача1_Эпик1", "Описание1_Эпик1", TaskStatus.NEW, epic1.getId());
        taskManager.addSubtask(subtask1);

        assertTrue(taskManager.getHistory().isEmpty());
    }
}