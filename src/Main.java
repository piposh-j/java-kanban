import TypeTask.Epic;
import TypeTask.Subtask;
import TypeTask.Task;
import taskmanager.TaskManager;
import enums.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        Task task1 = new Task("Задача_1","Описание_1");
        Task task2 = new Task("Задача_2","Описание_2");

        taskManager.addTask(task1);
        taskManager.addTask(task2);


        Subtask subtask1 = new Subtask("Подзача1_Эпик1", "Описание1_Эпик1");
        Subtask subtask2 = new Subtask("Подзача2_Эпик1", "Описание2_Эпик1");
        Epic epic1 = new Epic("Эпик_1", "Описание_1");

        Epic epic2= new Epic("Эпик_2", "Описание_2");
        Subtask subtask3 = new Subtask("Подзача3_Эпик2", "Описание3_Эпик2");

        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        taskManager.addSubtask(subtask1, epic1);
        taskManager.addSubtask(subtask2, epic1);
        taskManager.addSubtask(subtask3, epic2);

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

        System.out.println("=========================");
        System.out.println("=====После изменения=====");
        System.out.println("=========================");

        Task newStatusTask = new Task(task1.getId(), "Новоое название задачи", "Новое описание задачи", TaskStatus.DONE);
        taskManager.updateTask(newStatusTask);
        taskManager.deleteTaskById(task2.getId());

        Subtask newSubtaskStatus1 = new Subtask(subtask1.getId(), "Новая подзадача_1",  "Новое описание подзадачи_1", TaskStatus.DONE, epic1);
        Subtask newSubtaskStatus2 = new Subtask(subtask3.getId(), "Новая подзадача_3",  "Новое описание подзадачи_3", TaskStatus.IN_PROGRESS, epic2);

        taskManager.updateSubtask(newSubtaskStatus1);
        taskManager.updateSubtask(newSubtaskStatus2);

        taskManager.deleteSubtaskById(subtask2.getId());

        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpics());
        System.out.println(taskManager.getSubtasks());

    }
}
