package ru.tasktracker.service;

import ru.tasktracker.model.Task;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, Node> history = new HashMap<>();
    private Node first;
    private Node last;

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void add(Task task) {

        Node node = history.get(task.getId());
        if (node != null) {
            removeNode(node);

        }
        history.put(task.getId(), linkLast(task));
    }

    @Override
    public void remove(int id) {
        Node node = history.get(id);
        history.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    public void removeNode(Node node) {
        Node prev = node.prev;
        Node next = node.next;
        if (prev == null) {
            first = next;
        }

        if (next == null) {
            last = prev;
        }

        if (prev != null) {
            prev.next = next;
        }

        if (next != null) {
            next.prev = prev;
        }
    }

    private Node linkLast(Task item) {
        final Node l = last;
        final Node newNode = new Node(l, item, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        return newNode;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        if (first == null) {
            return tasks;
        }

        Node currentNode = new Node(first.prev, first.item, first.next);
        while (currentNode != null) {
            tasks.add(currentNode.item);
            currentNode = currentNode.next;
        }
        return tasks;
    }

    private static class Node {
        Node prev;
        Node next;
        Task item;

        Node(Node prev, Task item, Node next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }
    }
}