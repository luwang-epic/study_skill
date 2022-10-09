package com.wang.representative;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 单调栈应用
 */
public class MonotoneStackAlgorithm {

    /*
    给定一个数组和滑动窗口的大小，找出所有滑动窗口里数值的最大值。
    例如，如果输入数组{2,3,4,2,6,2,5,1}及滑动窗口的大小3，那么一共存在6个滑动窗口，
    他们的最大值分别为{4,4,6,6,6,5}；针对数组{2,3,4,2,6,2,5,1}的滑动窗口有以下6个：
    {[2,3,4],2,6,2,5,1}， {2,[3,4,2],6,2,5,1}， {2,3,[4,2,6],2,5,1}， {2,3,4,[2,6,2],5,1}， {2,3,4,2,[6,2,5],1}， {2,3,4,2,6,[2,5,1]}。
     */
    @Test
    public void slidingWindowInArrayDemo() {
        int[] nums = new int[] {2, 3, 4, 2, 6, 2, 5, 1};
        int windowSize = 3;
        int[] results = slidingWindowInArray(nums, windowSize);
        System.out.println(Arrays.stream(results).boxed().collect(Collectors.toList()));
    }

    /*
    找出数组中的一个数，左边和右边都比这个数小，且离这个数最近的位置
    如果对数组中的每个数都求这个信息，能不能整体达到O(N)的诗句复杂度
     */
    @Test
    public void nearestMinNumberInArrayDemo() {
        int[] nums = new int[] {2, 3, 4, 6, 5, 1};
        int[][] results = nearestMinNumberInArray(nums);

        List<Integer> lefts = new ArrayList<>();
        List<Integer> rights = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            lefts.add(results[i][0]);
            rights.add(results[i][1]);
        }
        System.out.println(lefts);
        System.out.println(rights);
    }


    /*
    法一：简单的暴力法
    法二：双向队列
        用一个双向队列，队列第一个位置保存当前窗口的最大值，当窗口滑动一次，
        判断当前最大值是否过期（当前最大值的位置是不是在窗口之外），
        新增加的值从队尾开始比较，把所有比他小的值丢掉。这样时间复杂度为O(n)。
     */
    public int[] slidingWindowInArray(int[] nums, int windowSize) {
        if (null == nums || nums.length == 0) {
            return null;
        }

        // 双端队列，存放数组下标（因为下标可以知道值，而存值不知道下标了），
        // 保证下标对应的值严格递减的，保证头部是最大值
        LinkedList<Integer> maxDeque = new LinkedList<>();
        int[] results = new int[nums.length - windowSize + 1];
        int index = 0;
        // 窗口右边界
        for (int i = 0; i < nums.length; i++) {
            // 找到存放i的位置，将尾部小于的移除
            while (!maxDeque.isEmpty() && nums[maxDeque.peekLast()] <= nums[i]) {
                maxDeque.pollLast();
            }

            maxDeque.addLast(i);
            // i - windowSize 过期的小标
            if (maxDeque.peekFirst() == i - windowSize) {
                maxDeque.pollFirst();
            }
            // 窗口形成了
            if (i >= windowSize - 1) {
                results[index++] = nums[maxDeque.peekFirst()];
            }
        }

        return results;
    }

    /**
     * 使用单调栈求解
     *  如果有重复数，需要用一个链表表示栈中的一个元素，
     *  相同值时放到栈的同一个位置，也就是加入到那个位置链表的尾部
     * @param nums 数组，没有重复数字，
     * @return 左边最小的数组  和 右边最小的数组  数组中的-1表示没有最小值
     */
    public int[][] nearestMinNumberInArray(int[] nums) {
        int[][] results = new int[nums.length][2];
        // 单调栈，存放数组的下标（因为下标可以知道值，而存值不知道下标了）
        // 如果是求最小值，那么栈从栈底到顶需要单调递增，反之成立
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < nums.length; ++i) {
            while (!stack.isEmpty() && nums[stack.peek()] > nums[i]) {
                // 弹出栈中的元素，计算该元素的左右最小值
                Integer itemIndex = stack.pop();
                // 最右边的为第i个数
                results[itemIndex][1] = nums[i];
                // 如果空，左边没有最小值，否则，为栈顶的元素
                results[itemIndex][0] = stack.isEmpty() ? -1 : nums[stack.peek()];
            }

            // 如果比栈为空，或者比栈顶大，那么入栈，保持栈的递增特性
            stack.push(i);
        }

        // 处理栈中剩余元素
        while (!stack.isEmpty()) {
            Integer itemIndex = stack.pop();
            results[itemIndex][1] = -1;
            results[itemIndex][0] = stack.isEmpty() ? -1 : nums[stack.peek()];
        }
        return results;
    }
}
