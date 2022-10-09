package com.wang.algorithm;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * 图相关算法
 *  dijkstra
 *  floyd
 *  kruskal：最小生成树算法，从边角度出发，只适用于无向图
 *  prim：最小生成树算法，从顶点角度出发，只适用于无向图
 *
 */
public class GraphAlgorithm {

    /**
     * prim算法
     */
    @Test
    public void primDemo() {
        int nodeCounts = 6;
        int[][] weights = new int[nodeCounts][nodeCounts];
        for (int i = 0; i < nodeCounts; i++) {
            for (int j = 0; j < nodeCounts; j++) {
                weights[i][j] = Integer.MAX_VALUE;
            }
        }
        weights[0][1] = 10;
        weights[0][2] = 16;
        weights[0][3] = 14;
        weights[1][3] = 15;
        weights[1][4] = 24;
        weights[2][3] = 15;
        weights[2][5] = 16;
        weights[3][4] = 23;
        weights[3][5] = 8;
        weights[4][5] = 22;

        // 无向图，是对称的
        weights[1][0] = 10;
        weights[2][0] = 16;
        weights[3][0] = 14;
        weights[3][1] = 15;
        weights[4][1] = 24;
        weights[3][2] = 15;
        weights[5][2] = 16;
        weights[4][3] = 23;
        weights[5][3] = 8;
        weights[5][4] = 22;

        System.out.println(prim(weights));
    }

    /**
     * kruskal算法
     */
    @Test
    public void kruskalDemo() {
        int[] nodes = new int[] {1, 2, 3, 4, 5, 6};
        UndirectedEdge[] edges = new UndirectedEdge[10];
        edges[0] = new UndirectedEdge(10, 1, 2);
        edges[1] = new UndirectedEdge(16, 1, 3);
        edges[2] = new UndirectedEdge(14, 1, 4);
        edges[3] = new UndirectedEdge(15, 2, 4);
        edges[4] = new UndirectedEdge(24, 2, 5);
        edges[5] = new UndirectedEdge(15, 3, 4);
        edges[6] = new UndirectedEdge(16, 3, 6);
        edges[7] = new UndirectedEdge(23, 4, 5);
        edges[8] = new UndirectedEdge(8, 4, 6);
        edges[9] = new UndirectedEdge(22, 5, 6);

        UndirectedGraph graph = new UndirectedGraph(nodes, edges);
        List<UndirectedEdge> kruskalEdges = kruskal(graph);
        int sum = 0;
        for (UndirectedEdge kruskalEdge : kruskalEdges) {
            sum += kruskalEdge.weight;
            System.out.println(kruskalEdge);
        }
        System.out.println(sum);
    }

    /**
     * prim算法求最小生成树，只适用于无向图
     * @param weights 图的矩阵表示，保证图是连通图，graph[i][j]表示点i到点j的距离，最大值表示无路
     * @return 最小生成树的路径之和
     */
    public int prim(int[][] weights) {
        // 已经访问过的顶点，无需再访问
        Set<Integer> visitedNodes = new HashSet<>();
        // 随机选择一个顶点开始，比如0
        visitedNodes.add(0);
        // 记录整个最小生成树的权值和
        int sum = 0;

        // 开始选点
        while (true) {
            // 找出不在集合中的最短的边的点，只需要终点，起点在集合中拿
            int minEndNode = -1;
            int minWeight = Integer.MAX_VALUE;
            int currentNode = -1;
            for (int i : visitedNodes) {
                for (int j = 0; j < weights[i].length; j++) {
                    if (weights[i][j] < minWeight && !visitedNodes.contains(j)) {
                        minWeight = weights[i][j];
                        currentNode = i;
                        minEndNode = j;
                    }
                }
            }
            // 找不到这样的边，说明结束了
            if (minEndNode == -1) {
                break;
            }

            visitedNodes.add(minEndNode);
            System.out.println("from: " + currentNode + ", to: " + minEndNode + ", weight: " + minWeight);
            sum += minWeight;
        }

        return sum;
    }

    /*
    克鲁斯卡尔算法查找最小生成树的方法是：将连通网中所有的边按照权值大小做升序排序，从权值最小的边开始选择，
    只要此边不和已选择的边一起构成环路，就可以选择它组成最小生成树。对于 N 个顶点的连通网，
    挑选出 N-1 条符合条件的边，这些边组成的生成树就是最小生成树。

    实现克鲁斯卡尔算法的难点在于“如何判断一个新边是否会和已选择的边构成环路”，
    这里教大家一种判断的方法：初始状态下，为连通网中的各个顶点配置不同的标记。
    对于一个新边，如果它两端顶点的标记不同，就不会构成环路，可以组成最小生成树。
    一旦新边被选择，需要将它的两个顶点改为相同的标记；
    反之，如果新边两端顶点的标记相同，就表示会构成环路。
     */
    /**
     * kruskal：最小生成树算法，只适用于无向图
     * @param graph 图对象，包括边和顶点信息
     * @return 返回需要哪些边
     */
    public List<UndirectedEdge> kruskal(UndirectedGraph graph) {
        // 按照权重存放表，最小的在堆顶
        PriorityQueue<UndirectedEdge> priorityQueue = new PriorityQueue<>((x, y) -> {
            return x.weight - y.weight;
        });
        // 初始选择最小的边
        UndirectedEdge minEdge = graph.undirectedEdges[0];
        for (UndirectedEdge edge : graph.undirectedEdges) {
            if (edge.weight < minEdge.weight) {
                minEdge = edge;
            }
        }
        priorityQueue.add(minEdge);

        // 存放选中的边集合，为了保证顺序，将其用list返回
        List<UndirectedEdge> result = new ArrayList<>();
        // 标记的顶点
        Set<Integer> markNodes = new HashSet<>();
        boolean isInit = true;

        // 开始选边
        while (!priorityQueue.isEmpty()) {
            // 从最小的边开始选择
            UndirectedEdge edge = priorityQueue.poll();
            // 如果该边的两个顶点有一个不在集合中，那么选这个边
            if (isInit // 第一次进入为初始状态，直接加入到result集合中
                // from不在，to在
                || (!markNodes.contains(edge.from) && markNodes.contains(edge.to))
                // to在，from不在
                || (markNodes.contains(edge.from) && !markNodes.contains(edge.to))) {
                isInit = false;
                // 选择这个边
                result.add(edge);

                // 最小边的两个顶点加入集合
                markNodes.add(edge.from);
                markNodes.add(edge.to);
                // 加入有序队列中，重复没有关系，因为重复的不会再选择了
                priorityQueue.addAll(graph.nodeEdgesMap.get(edge.from));
                priorityQueue.addAll(graph.nodeEdgesMap.get(edge.to));
            }
        }

        return result;
    }

    private static class UndirectedGraph {
        // 顶点集合
        public int[] undirectedNodes;
        // 边集合
        public UndirectedEdge[] undirectedEdges;

        // 边到顶点的集合
        public Map<Integer, Set<UndirectedEdge>> nodeEdgesMap = new HashMap<>();

        public UndirectedGraph(int[] undirectedNodes, UndirectedEdge[] undirectedEdges) {
            this.undirectedNodes = undirectedNodes;
            this.undirectedEdges = undirectedEdges;

            for (int i = 0; i < undirectedNodes.length; i++) {
                Set<UndirectedEdge> edges = new HashSet<>();
                for (int j = 0; j < undirectedEdges.length; j++) {
                    // 只要有一个等于i，那么顶点i和j就是相连的
                    if (undirectedEdges[j].from == i + 1 || undirectedEdges[j].to == i + 1) {
                        edges.add(undirectedEdges[j]);
                    }
                }
                nodeEdgesMap.put(i + 1, edges);
            }
        }

    }
    @AllArgsConstructor
    @ToString
    private static class UndirectedEdge {
        // 边权重
        public int weight;
        // 起点
        public int from;
        // 终点
        public int to;
    }
}
