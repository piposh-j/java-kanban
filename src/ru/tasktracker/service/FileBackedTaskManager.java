package ru.tasktracker.service;

import ru.tasktracker.exception.ManagerSaveException;
import ru.tasktracker.model.*;
import ru.tasktracker.util.Managers;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File file;

    private FileBackedTaskManager() {
        super(Managers.getDefaultHistory());
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.append(getHeadFile());
            List<Epic> epics = getEpics();
            for (int i = 0; i < epics.size(); i++) {
                bw.append(toString(epics.get(i)));
                List<Integer> subtasks = epics.get(i).getSubtaskIds();
                for (int j = 0; j < subtasks.size(); j++) {
                    bw.append(toString(super.subtasks.get(subtasks.get(j))));
                }
            }
            List<Task> tasks = getTasks();

            for (int i = 0; i < tasks.size(); i++) {
                bw.append(toString(tasks.get(i)));
            }
            bw.newLine();
            bw.flush();
        } catch (IOException ioException) {
            throw new ManagerSaveException("Ошибка при сохранении данных");
        }

    }

    private String toString(Task task) {
        StringBuilder result = new StringBuilder();

        result.append(task.getId()).append(",");
        result.append(task.getName()).append(",");
        result.append(task.getStatus()).append(",");
        result.append(task.getDescription()).append(',');

        if (task.getStartTime() != null) {
            result.append(task.getStartTime()).append(',');
            result.append(task.getDuration().toMinutes()).append(',');
        } else {
            result.append("-").append(',');
            result.append("-").append(',');
        }

        if (task instanceof Epic) {
            if (task.getEndTime() != null) {
                result.append(task.getEndTime()).append(',');
            } else {
                result.append("-").append(',');
            }
            result.append(TypeTask.EPIC).append(",").append("-").append(",");
            List<Integer> list = ((Epic) task).getSubtaskIds();
            String joined = list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(";"));
            result.append(joined.isEmpty() ? "-" : joined);
        } else if (task instanceof Subtask) {
            result.append("-").append(',');
            result.append(TypeTask.SUBTASK).append(",");
            result.append(((Subtask) task).getEpicId()).append(",");
        } else {

            result.append("-").append(',');
            result.append(TypeTask.TASK).append(",");
            //пустая строка для epic_id
            result.append("-").append(",");
            //пустая строка для subtask_ids
            result.append("-").append(",");
        }
        result.append('\n');

        return result.toString();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager();
        fileBackedTaskManager.file = file;

        try {
            List<String> list = Files.readAllLines(file.toPath());

            for (int i = 1; i < list.size(); i++) {
                if (!fileBackedTaskManager.containsTask(list.get(i))) {
                    continue;
                }
                Task task = fromString(list.get(i));
                if (task instanceof Epic) {
                    fileBackedTaskManager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    fileBackedTaskManager.subtasks.put(task.getId(), (Subtask) task);
                } else {
                    fileBackedTaskManager.tasks.put(task.getId(), task);
                }
                if (fileBackedTaskManager.id < task.getId()) {
                    fileBackedTaskManager.id = task.getId() + 1;
                }
            }
        } catch (IOException ioException) {
            throw new ManagerSaveException("Ошибка при загрузке данных");
        }
        loadHistory(fileBackedTaskManager);
        return fileBackedTaskManager;
    }

    private static void loadHistory(FileBackedTaskManager fileBackedTaskManager) {
        Task task = null;
        try {
            List<String> lines = Files.readAllLines(fileBackedTaskManager.file.toPath());
            if (lines.isEmpty() || !fileBackedTaskManager.existsIdsInFile(lines)) {
                return;
            }

            for (String idStr : lines.getLast().split(",")) {
                Integer id = Integer.parseInt(idStr);
                if (fileBackedTaskManager.epics.containsKey(id)) {
                    task = fileBackedTaskManager.epics.get(id);
                }
                if (fileBackedTaskManager.subtasks.containsKey(id)) {
                    task = fileBackedTaskManager.subtasks.get(id);
                }
                if (fileBackedTaskManager.tasks.containsKey(id)) {
                    task = fileBackedTaskManager.tasks.get(id);
                }
                fileBackedTaskManager.historyManager.add(task);
            }
        } catch (IOException ioException) {
            throw new ManagerSaveException("Ошибка при загрузке истории");
        }
    }

    //Проверям есть ли в файле просмотренные таски
    private boolean existsIdsInFile(List<String> list) {
        return list.size() > 1 && list.get(list.size() - 2).isEmpty();
    }

    private boolean containsTask(String string) {
        return string.matches(".*,(SUBTASK|TASK|EPIC),.*");
    }

    private static Task fromString(String value) {
        String[] array = value.split(",");

        TypeTask typeTask = TypeTask.valueOf(array[7]);
        int id = Integer.parseInt(array[0]);
        String name = array[1];
        TaskStatus taskStatus = TaskStatus.valueOf(array[2]);
        String description = array[3];

        String tmpStartTime = array[4];
        String tmpDuration = array[5];
        String tmpEndTime = array[6];

        LocalDateTime startTime = null;
        Duration duration = null;
        LocalDateTime endTime = null;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        if (!"-".equals(tmpStartTime)) {
            startTime = LocalDateTime.parse(tmpStartTime, dateTimeFormatter);
            duration = Duration.ofMinutes(Long.parseLong(tmpDuration));
        }

        if (!"-".equals(tmpEndTime)) {
            endTime = LocalDateTime.parse(tmpEndTime, dateTimeFormatter);
        }


        switch (typeTask) {
            case EPIC -> {
                Epic epic = new Epic(id, name, description);
                epic.setStatus(taskStatus);
                String[] subtaskIds = array[9].split(";");
                if (!subtaskIds[0].endsWith("-")) {
                    List<Integer> list = new ArrayList<>();
                    for (String subtaskId : subtaskIds) {
                        list.add(Integer.parseInt(subtaskId));
                    }
                    epic.setSubtaskIds(list);
                }

                epic.setStartTime(startTime);
                epic.setDuration(duration);
                epic.setEndTime(endTime);
                return epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(array[8]);
                return new Subtask(
                        id,
                        name,
                        description,
                        taskStatus,
                        epicId,
                        startTime,
                        duration
                );
            }
            default -> {
                return new Task(id,
                        name,
                        description,
                        taskStatus,
                        startTime,
                        duration);
            }
        }
    }

    private void saveHistory() {
        try {
            List<String> list = Files.readAllLines(file.toPath());
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            if (existsIdsInFile(list)) {
                list.set(list.size() - 1, toString(historyManager.getHistory()));
            } else {
                list.add(list.size(), toString(historyManager.getHistory()));
            }

            for (String data : list) {
                bw.append(data);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException ioException) {
            throw new ManagerSaveException("Ошибка при сохранении истории");
        }
    }

    @Override
    public Task getTaskById(int id) {
        var task = super.getTaskById(id);
        saveHistory();
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        var subtask = super.getSubtaskById(id);
        saveHistory();
        return subtask;
    }

    private String toString(List<Task> tasks) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            stringBuilder.append(tasks.get(i).getId()).append(",");
        }

        return stringBuilder.toString();
    }

    private String getHeadFile() {
        return "id,name,status,description,startTime,duration,endTime,type,epic_id,subtask_ids\n";
    }

    @Override
    public Epic addEpic(Epic e) {
        var epic = super.addEpic(e);
        save();
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask s) {
        var subtask = super.addSubtask(s);
        save();
        return subtask;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public Task addTask(Task t) {
        var task = super.addTask(t);
        save();
        return task;
    }

    @Override
    public Task updateTask(Task t) {
        var task = super.updateTask(t);
        save();
        return task;
    }

    @Override
    public Task deleteTaskById(int id) {
        var task = super.deleteTaskById(id);
        save();
        saveHistory();
        return task;
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public Epic updateEpic(Epic e) {
        var epic = super.updateEpic(e);
        save();
        return epic;
    }

    @Override
    public Epic deleteEpicById(int id) {
        var epic = super.deleteEpicById(id);
        save();
        saveHistory();
        return epic;
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Subtask updateSubtask(Subtask s) {
        var subtask = super.updateSubtask(s);
        save();
        return subtask;

    }

    @Override
    public Subtask deleteSubtaskById(int id) {
        var subtask = super.deleteSubtaskById(id);
        save();
        saveHistory();
        return subtask;
    }

}
