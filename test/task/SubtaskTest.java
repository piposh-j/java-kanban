package task;

import task.Epic;
import task.Subtask;
import task.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {
    @Test
    void equals_subtaskShouldNotBeEqual() {
        Epic epic1 = new Epic(0, "Название", "Описание");
        Subtask subtask1 = new Subtask(0, "Название", "Описание", TaskStatus.NEW, epic1);
        Subtask subtask2 = new Subtask(0, "Название", "Описание", TaskStatus.NEW, epic1);

        assertEquals(subtask1, subtask2);
    }
}
