package task.type;

import enums.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {
    @Test
    void equals_tasksShouldBeEqual() {
        Task test1 = new Task(0, "Название", "Описание", TaskStatus.NEW);
        Task test2 = new Task(0, "Название", "Описание", TaskStatus.NEW);

        assertEquals(test1, test2);
    }
}
