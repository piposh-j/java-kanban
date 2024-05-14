import ru.tasktracker.service.TaskManager;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Subtask;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TaskStatus;
import ru.tasktracker.util.Managers;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task(0, "Задача_1", "Описание_1", TaskStatus.NEW);
        Task task2 = new Task(1, "Задача_2", "Описание_2", TaskStatus.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Epic epic1 = new Epic(0, "Эпик_1", "Описание_1");
        Epic epic2 = new Epic(1, "Эпик_2", "Описание_2");

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        Subtask subtask1 = new Subtask(0, "Подзача1_Эпик1", "Описание1_Эпик1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(1, "Подзача2_Эпик1", "Описание2_Эпик1", TaskStatus.NEW, epic1.getId());
        Subtask subtask3 = new Subtask(3, "Подзача3_Эпик2", "Описание3_Эпик2", TaskStatus.NEW, epic2.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println("=========================");
        System.out.println("=====После изменения=====");
        System.out.println("=========================");

        Task newStatusTask = new Task(task1.getId(), "Новоое название задачи", "Новое описание задачи", TaskStatus.DONE);
        taskManager.updateTask(newStatusTask);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        taskManager.getTaskById(task1.getId());
        taskManager.getSubtaskById(subtask1.getId());
        System.out.println("getHistory");
        System.out.println(taskManager.getHistory());

    }
}
