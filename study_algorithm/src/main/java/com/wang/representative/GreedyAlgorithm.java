package com.wang.representative;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * 贪心算法
 */
public class GreedyAlgorithm {

    /*
    安排会议：会议有开始时间和结束时间，有一个会议室，同一时间点只能安排一个会议，
        需要安排会议到这个会议室，确保安排的会议最多
     */
    @Test
    public void bestPlanForMeetingDemo() {
        Meeting[] meetings = new Meeting[5];
        meetings[0] = new Meeting(1, 3);
        meetings[1] = new Meeting(1, 2);
        meetings[2] = new Meeting(1, 4);
        meetings[3] = new Meeting(2, 4);
        meetings[4] = new Meeting(6, 7);
        System.out.println(bestPlanForMeeting(meetings, 0));
    }

    /*
    一群人想整分整块金条，怎么分最省铜板?
    给定数组[10,20,30]，代表一共三个人，整块金条长度为60，金条要分成10，20，30三个部分。
    如果先把长度60的金条分成10和50，花费60；再把长度50的金条分成20和30，花费50；一共花费110铜板
    但如果先把长度60的金条分成30和30，花费60；再把长度30金条分成10和20，花费30；一共花费90铜板
    输入一个数组，返回分割的最小代价
     */
    @Test
    public void splitGoldWithLessCostDemo() {
        int[] golds = new int[] {10, 20, 30};
        System.out.println(splitGoldWithLessCost(golds));
    }

    /*
    给你一批启动资金和最多可以做的项目，以及项目的花费和收益，输出可以获得的最大收益
        你没做完一个项目，麻烦获得收益，可以支持你做下一个项目
     */
    @Test
    public void maxProjectProfitDemo() {
        int k = 4;
        int m = 1;
        int[] costs = new int[] {1, 1, 2, 2, 3, 4};
        int[] profits = new int[] {1, 4, 3, 7, 2, 10};
        System.out.println(maxProjectProfit(k, m, costs, profits));
    }

    // 贪心算法，按照结束时间最早的安排会议，这样是最优解，返回最多安排的会议数
    public int bestPlanForMeeting(Meeting[] meetings, int timePoint) {
        Arrays.sort(meetings, (x, y) -> {
            return Integer.compare(x.end, y.end);
        });
        int result = 0;
        // 从左往右依此遍历所有的会议
        for (int i = 0; i < meetings.length; i++) {
            // 过滤掉时间冲突的会议
            if (timePoint <= meetings[i].start) {
                result++;
                timePoint = meetings[i].end;
            }
        }
        return result;
    }

    private static class Meeting {
        public int start;
        public int end;
        public Meeting(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }


    // 贪心算法，哈夫曼编码问题，哈夫曼树，从最小的两个开始合并切分
    public int splitGoldWithLessCost(int[] golds) {
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>();
        for (int i = 0; i < golds.length; i++) {
            priorityQueue.add(golds[i]);
        }

        int sum = 0;
        int cur = 0;
        while (priorityQueue.size() > 1) {
            cur = priorityQueue.poll() + priorityQueue.poll();
            sum += cur;
            priorityQueue.add(cur);
        }
        return sum;
    }

    public int maxProjectProfit(int k, int w, int[] costs, int[] profits) {
        PriorityQueue<Project> minCostHeap = new PriorityQueue<>((x, y) -> {
           return Integer.compare(x.cost, y.cost);
        });
        PriorityQueue<Project> maxProfitHeap = new PriorityQueue<>((x, y) -> {
            return Integer.compare(y.profit, x.profit);
        });

        // 将所有项目放到被锁定的池中，该池是花费的小顶堆
        for (int i = 0; i < profits.length; i++) {
            minCostHeap.add(new Project(costs[i], profits[i]));
        }

        int result = w;
        // 进行k轮
        for (int i = 0; i <k; i++) {
            // 能力所及的项目，全部解锁
            while (!minCostHeap.isEmpty() && minCostHeap.peek().cost <= result) {
                maxProfitHeap.add(minCostHeap.poll());
            }

            // 即使还有轮次没有完成，但是如果没有项目可以做了，返回所以的利润
            if (maxProfitHeap.isEmpty()) {
                return result;
            }
            // 做项目，并获得利润
            result = result + maxProfitHeap.poll().profit;
        }
        return result;
    }

    private static class Project {
        public int cost;
        public int profit;
        public Project(int cost, int profit) {
            this.cost = cost;
            this.profit = profit;
        }
    }
}
