package ru.tasktracker.service;


import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    Path tempFile;
    @Override
    protected FileBackedTaskManager createTaskManager() throws IOException {
        tempFile = Files.createTempFile(null, null);
        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        return taskManager;
    }

    @Test
    void loadFromFile_initFromEmptyFile() {
        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void loadFromFileWithoutHistory_initTask() throws IOException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime startSubtaskTime = LocalDateTime.parse("2024-01-01T10:10:10", dateTimeFormatter);
        LocalDateTime startEpicTime = LocalDateTime.parse("1990-01-01T11:10:11", dateTimeFormatter);
        LocalDateTime endEpicTime = LocalDateTime.parse("3333-01-01T12:10:11", dateTimeFormatter);
        Duration duration = Duration.ofMinutes(60);

        String textData = """
                id,name,status,description,startTime,duration,endTime,type,epic_id,subtask_ids
                2,Эпик_1,NEW,Описание_1,1990-01-01T11:10:11,706350300,3333-01-01T12:10:11,EPIC,,4;5;
                4,Подзача1_Эпик1,NEW,Описание1_Эпик1,2024-01-01T10:10:10,60,-,SUBTASK,2,-
                5,Подзача2_Эпик1,NEW,Описание2_Эпик1,-,-,-,SUBTASK,2,-
                0,Новоое название задачи,DONE,Новое описание задачи,-,-,-,TASK,-,-,
                """;

        Path tempFile = Files.createTempFile(null, null);
        Files.writeString(tempFile, textData);
        taskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertEquals(taskManager.getEpics().getFirst().getSubtaskIds().size(), 2);
        assertEquals(taskManager.getEpics().getFirst().getName(), "Эпик_1");
        assertEquals(taskManager.getEpics().getFirst().getDescription(), "Описание_1");
        assertEquals(taskManager.getEpics().getFirst().getStatus(), TaskStatus.NEW);
        assertEquals(taskManager.getEpics().getFirst().getStartTime(), startEpicTime);
        assertEquals(taskManager.getEpics().getFirst().getEndTime(), endEpicTime);

        assertFalse(taskManager.getTasks().isEmpty());
        assertFalse(taskManager.getEpics().isEmpty());
        assertFalse(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());

        assertEquals(taskManager.getSubtasks().getFirst().getName(), "Подзача1_Эпик1");
        assertEquals(taskManager.getSubtasks().getFirst().getDescription(), "Описание1_Эпик1");
        assertEquals(taskManager.getSubtasks().getFirst().getStatus(), TaskStatus.NEW);
        assertEquals(taskManager.getSubtasks().getFirst().getStartTime(), startSubtaskTime);
        assertEquals(taskManager.getSubtasks().getFirst().getDuration(), duration);
        assertNotNull(taskManager.getSubtasks().getFirst().getEndTime());

        assertEquals(taskManager.getTasks().getFirst().getName(), "Новоое название задачи");
        assertEquals(taskManager.getTasks().getFirst().getDescription(), "Новое описание задачи");
        assertEquals(taskManager.getTasks().getFirst().getStatus(), TaskStatus.DONE);
        assertNull(taskManager.getTasks().getFirst().getStartTime());
        assertNull(taskManager.getTasks().getFirst().getEndTime());
        assertNull(taskManager.getTasks().getFirst().getDuration());
    }

    @Test
    void loadFromFileWithHistory_initTask() throws IOException {
        String textData = """
                id,name,status,description,startTime,duration,endTime,type,epic_id,subtask_ids
                2,Эпик_1,NEW,Описание_1,1990-01-01T11:10:11,706350300,3333-01-01T12:10:11,EPIC,,4;5;
                4,Подзача1_Эпик1,NEW,Описание1_Эпик1,2024-01-01T10:10:10,60,-,SUBTASK,2,
                5,Подзача2_Эпик1,NEW,Описание2_Эпик1,-,-,-,SUBTASK,2,-
                0,Новоое название задачи,DONE,Новое описание задачи,-,-,-,TASK,-,-,
                            
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

        Task task1 = new Task(0, "Задача_1", "Описание_1", TaskStatus.NEW);
        taskManager.addTask(task1);

        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Подзача1_Эпик1", "Описание1_Эпик1", TaskStatus.NEW, epic1.getId());
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(0, "Подзача2_Эпик1", "Описание2_Эпик1", TaskStatus.NEW, epic1.getId());
        taskManager.addSubtask(subtask2);

        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getTaskById(task1.getId());

        final int HEAD_AND_EMPTY_LINE_QTY_IN_FILE = 2;
        final int HISTORY_LINE = 1;

        assertEquals(taskManager.getEpics().size() +
                        taskManager.getTasks().size() +
                        taskManager.getSubtasks().size() +
                        HISTORY_LINE +
                        HEAD_AND_EMPTY_LINE_QTY_IN_FILE,
                Files.readAllLines(tempFile).size());
        assertFalse(taskManager.getHistory().isEmpty());
        assertEquals(taskManager.getHistory().size(), 2);
    }


    @Test
    void save_checkTaskSaveToFileWithoutHistorySuccess() throws IOException {

        Task task1 = new Task(0, "Задача_1", "Описание_1", TaskStatus.NEW);
        taskManager.addTask(task1);

        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Подзача1_Эпик1", "Описание1_Эпик1", TaskStatus.NEW, epic1.getId());
        taskManager.addSubtask(subtask1);

        assertTrue(taskManager.getHistory().isEmpty());
    }


}