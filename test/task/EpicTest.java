package task;

import org.junit.jupiter.api.Test;
import task.Epic;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {
    @Test
    void equals_epicShouldBeEqual() {
        Epic epic1 = new Epic(0, "Название", "Описание");
        Epic epic2 = new Epic(0, "Название", "Описание");

        assertEquals(epic1, epic2);
    }
}
