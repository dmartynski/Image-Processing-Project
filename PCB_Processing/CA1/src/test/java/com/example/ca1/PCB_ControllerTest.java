package com.example.ca1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PCB_ControllerTest {

    @Test
    void testFind() {
        //create disjoint set nodes for testing
        DisjointSetNode<Integer> node1 = new DisjointSetNode<>(1, 0, 0);
        DisjointSetNode<Integer> node2 = new DisjointSetNode<>(2, 0, 0);
        DisjointSetNode<Integer> node3 = new DisjointSetNode<>(3, 0, 0);
        node2.parent = node1;  // node2 points to node1
        node3.parent = node2;  // node3 points to node2

        PCB_Controller controller = new PCB_Controller();

        //find should return root of set
        assertEquals(node1, controller.find(node3), "Find should return the root of the set.");
        assertEquals(node1, controller.find(node2), "Find should return the root of the set.");
        assertEquals(node1, controller.find(node1), "Find should return the root of the set.");
    }

    @Test
    void testUnion() {
        DisjointSetNode<Integer> node1 = new DisjointSetNode<>(1, 0, 0);
        DisjointSetNode<Integer> node2 = new DisjointSetNode<>(2, 0, 0);
        DisjointSetNode<Integer> node3 = new DisjointSetNode<>(3, 0, 0);
        DisjointSetNode<Integer> node4 = new DisjointSetNode<>(4, 0, 0);
        node2.parent = node1; //node2 points to node1
        node4.parent = node3; //node4 points to node3

        PCB_Controller controller = new PCB_Controller();

        //do union
        controller.union(node1, node3);

        //nodes should have same root after union
        assertEquals(controller.find(node1), controller.find(node4), "Union should merge the two sets.");
    }
}