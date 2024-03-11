package ru.tasktracker.model;

import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {
    @Test
    void equals_subtaskShouldNotBeEqual() {
        Epic epic1 = new Epic(0, "Название", "Описание");
        Subtask subtask1 = new Subtask(0, "Название", "Описание", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "Название", "Описание", TaskStatus.NEW, epic1.getId());

        assertEquals(subtask1, subtask2);
    }
}
