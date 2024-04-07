package ru.tasktracker.service;

import ru.tasktracker.model.Task;
import ru.tasktracker.util.Node;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

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

    public void removeNode(Node<Task> node) {
        Node<Task> prev = node.getPrev();
        Node<Task> next = node.getNext();
        if (prev == null) {
            first = next;
        }

        if (next == null) {
            last = prev;
        }

        if (prev != null) {
            prev.setNext(next);
        }

        if (next != null) {
            next.setPrev(prev);
        }
    }

    private Node<Task> linkLast(Task item) {

        final Node<Task> l = last;
        final Node<Task> newNode = new Node<>(l, item, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.setNext(newNode);
        }
        return newNode;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        if (first == null) {
            return tasks;
        }

        Node<Task> currentNode = new Node(first.getPrev(), first.getItem(), first.getNext());
        while (currentNode != null) {
            tasks.add(currentNode.getItem());
            currentNode = currentNode.getNext();
        }
        return tasks;
    }
}
