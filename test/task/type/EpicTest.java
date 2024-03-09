package task.type;

import org.junit.jupiter.api.Test;
import task.Epic;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {
    @Test
    void equals_epicShouldBeEqual() {
        Epic epic1 = new Epic("Название", "Описание");
        Epic epic2 = new Epic("Название", "Описание");

        assertEquals(epic1, epic2);
    }
}
