package com.example.ca1;

public class DisjointSetNode<T> {
    T data;
    DisjointSetNode<T> parent;
    int size;
    // x and y coordinates needed for rectangle function
    private int x;
    private int y;

    public DisjointSetNode(T data, int x, int y) {
        this.data = data;
        this.x = x;
        this.y = y;
        this.size = 1;
        this.parent = null;
    }

    // Getter for x-coordinate
    public int getX() {
        return x;
    }

    // Getter for y-coordinate
    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }
}