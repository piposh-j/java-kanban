package ru.tasktracker.util;

import ru.tasktracker.service.*;

import java.io.File;

public class Managers {

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault() {
        //return new InMemoryTaskManager(getDefaultHistory());
        return FileBackedTaskManager.loadFromFile(new File("./src/resources/data.csv"));
    }
}
