package ru.tasktracker.util;

public class Node<T> {
    private Node<T> prev;

    public Node<T> getPrev() {
        return prev;
    }

    public Node<T> getNext() {
        return next;
    }

    public T getItem() {
        return item;
    }

    private Node<T> next;

    private T item;

    public void setPrev(Node<T> prev) {
        this.prev = prev;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public Node(Node<T> prev, T item, Node<T> next) {
        this.prev = prev;
        this.item = item;
        this.next = next;
    }
}
