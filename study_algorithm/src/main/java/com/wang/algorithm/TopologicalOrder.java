package com.wang.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/*
对一个有向无环图(Directed Acyclic Graph简称DAG)G进行拓扑排序，是将G中所有顶点排成一个线性序列，
使得图中任意一对顶点u和v，若边<u,v>∈E(G)，则u在线性序列中出现在v之前。
通常，这样的线性序列称为满足拓扑次序(Topological Order)的序列，简称拓扑序列。
简单的说，由某个集合上的一个偏序得到该集合上的一个全序，这个操作称之为拓扑排序。

一个较大的工程往往被划分成许多子工程，我们把这些子工程称作活动(activity)。在整个工程中，
有些子工程(活动)必须在其它有关子工程完成之后才能开始，也就是说，一个子工程的开始是以它的所有前序子工程的结束为先决条件的，
但有些子工程没有先决条件，可以安排在任何时间开始。为了形象地反映出整个工程中各个子工程(活动)之间的先后关系，
可用一个有向图来表示，图中的顶点代表活动(子工程)，图中的有向边代表活动的先后关系，
即有向边的起点的活动是终点活动的前序活动，只有当起点活动完成之后，其终点活动才能进行。
通常，我们把这种顶点表示活动、边表示活动间先后关系的有向图称做顶点活动网(Activity On Vertex network)，简称AOV网。

AOV网是一个有向无环图，即不应该带有回路，因为若带有回路，会形成死循环。
在AOV网中，因为不存在回路，则所有活动可排列成一个线性序列，使得每个活动的所有前驱活动都排在该活动的前面，
我们把此序列叫做拓扑序列(Topological order)，由AOV网构造拓扑序列的过程叫做拓扑排序(Topological sort)。

从DGA图中找到一个没有前驱的顶点输出。(可以遍历，也可以用优先队列维护)
删除以这个点为起点的边。(它的指向的边删除，为了找到下个没有前驱的顶点)
重复上述，直到最后一个顶点被输出。如果还有顶点未被输出，则说明有环！

这样就完成一次拓扑排序，得到一个拓扑序列，但是这个序列并不唯一！
从过程中也看到有很多选择方案，具体得到结果看你算法的设计了。但只要满足即是拓扑排序序列。
 */
public class TopologicalOrder {
    private static class Node {
        int value;
        List<Integer> next;

        public Node(int value) {
            this.value = value;
            next = new ArrayList<>();
        }

        public void setNext(List<Integer> next) {
            this.next = next;
        }
    }


    public static void main(String[] args) {
        // 储存节点
        Node[] nodes = new Node[9];
        // 储存节点的入度
        int[] inDegrees = new int[9];
        // 临时空间，为了存储指向的集合
        List<List<Integer>> nexts = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            nodes[i] = new Node(i);
            nexts.add(new ArrayList<>());
        }
        // 初始化数据
        initData(nodes, nexts, inDegrees);

        // 主要流程
        Queue<Node> queue = new ArrayDeque<>();
        for (int i = 1; i < 9; i++) {
            if (inDegrees[i] == 0) {
                queue.add(nodes[i]);
            }
        }

        List<Integer> topologicalOrders = new ArrayList<>();
        while (!queue.isEmpty()) {
            // 按照拓扑顺序输出
            Node node = queue.poll();
            topologicalOrders.add(node.value);

            List<Integer> next = node.next;
            for (int i = 0; i < next.size(); i++) {
                Integer nextNodeIndex = next.get(i);
                // 入度减一
                inDegrees[nextNodeIndex]--;
                // 如果入度为0
                if (inDegrees[nextNodeIndex] == 0) {
                    queue.add(nodes[nextNodeIndex]);
                }
            }
        }
        System.out.println(topologicalOrders);
    }

    /**
     * 初始化数据，拓扑结构如下：
     *  1 -> 3 -> 5 -> 7 -> 8
     *  2 -> 4 -> 5
     *  2 -> 4 -> 6
     *  2 -> 6 -> 9
     * @param nodes 顶点集合，从1开始，不使用下标0
     * @param nexts 顶点的下一个顶点集合
     * @param inDegrees 顶点的入度
     */
    private static void initData(Node[] nodes, List<List<Integer>> nexts, int[] inDegrees) {
        nexts.get(1).add(3);
        nodes[1].setNext(nexts.get(1));
        inDegrees[3]++;

        nexts.get(2).add(4);
        nexts.get(2).add(6);
        nodes[2].setNext(nexts.get(2));
        inDegrees[4]++;
        inDegrees[6]++;

        nexts.get(3).add(5);
        nodes[3].setNext(nexts.get(3));
        inDegrees[5]++;

        nexts.get(4).add(5);
        nexts.get(4).add(6);
        nodes[4].setNext(nexts.get(4));
        inDegrees[5]++;
        inDegrees[6]++;

        nexts.get(5).add(7);
        nodes[5].setNext(nexts.get(5));
        inDegrees[7]++;

        nexts.get(6).add(8);
        nodes[6].setNext(nexts.get(6));
        inDegrees[8]++;

        nexts.get(7).add(8);
        nodes[7].setNext(nexts.get(7));
        inDegrees[8]++;
    }

}

