package com.wang.algorithm;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 堆通常是一个可以被看做一棵树的数组对象。堆总是满足下列性质：
 *   堆中某个结点的值总是不大于或不小于其父结点的值；
 *   堆总是一棵完全二叉树。
 * 将根结点最大的堆叫做最大堆或大根堆，根结点最小的堆叫做最小堆或小根堆。
 */
public class HeapStruct {
    int[] nums;
    int currentSize;

    public HeapStruct() {
        nums = new int[100];
        currentSize = 0;
    }

    public HeapStruct(int[] nums) {
        this.nums = nums;
        this.currentSize = nums.length;
        heapify(nums.length);
    }

    public void insert(int val) {
        nums[currentSize] = val;
        percolateUp(currentSize);
        currentSize++;
    }

    public int delMax() {
        int tempMax = nums[0];
        nums[0] = nums[--currentSize];
        percolateDown(currentSize, 0);
        nums[currentSize] = 0;
        return tempMax;
    }

    public void heapify(int n) {
        for (int i=nums.length / 2; i >= 0; i--) {
            percolateDown(n, i);
        }
    }

    /**
     * 下滤
     * @param n 大小
     * @param i 下标
     */
    public int percolateDown(int n, int i) {
        int j;
        while (i != (j = properParent(n, i))) {
            swap(i, j);
            i = j;
        }
        return i;
    }

    /**
     * 上滤
     * @param i 下标
     */
    public int percolateUp(int i) {
        while(0 < i) {
            int parent = (i-1)/2;
            if(nums[parent] > nums[i]) {
                break;
            }
            swap(parent, i);
            i = parent;
        }
        return i;
    }


    /**
     * 左右节点谁更适合做父节点，返回下标
     * @param n 大小
     * @param i 下标
     */
    public int properParent(int n, int i) {
        if (hasRightChild(n, i)) {
            int biggerIndex = nums[2*i+1] > nums[i] ? 2*i+1 : i;
            return nums[2*i+2] > nums[biggerIndex] ? 2*i+2 : biggerIndex;
        }

        if (hasLeftChild(n, i)) {
            return nums[2*i+1] > nums[i] ? 2*i+1 : i;
        }

        return i;
    }

    public void swap(int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    /**
     * 是否有右节点
     * @param n 大小
     * @param i 下标
     */
    public boolean hasLeftChild(int n, int i) {
        if (i * 2 + 1 < n) {
            return true;
        }
        return false;
    }

    /**
     * 是否有右节点
     * @param n 大小
     * @param i 下标
     */
    public boolean hasRightChild(int n, int i) {
        if (i * 2 + 2 < n) {
            return true;
        }
        return false;
    }

    public int[] heapSort() {
        int[] sortNums = new int[currentSize];
        int index = 0;
        while(0 != currentSize) {
            sortNums[index++] = delMax();
        }
        return sortNums;
    }

    public void display() {
        for(int i = 0; i< currentSize; i++) {
            System.out.print(nums[i] + " ");
        }
        System.out.println();
    }


    public static void main(String[] args) {
        HeapStruct heapStruct = new HeapStruct();
        heapStruct.insert(16);
        heapStruct.insert(59);
        heapStruct.insert(5);
        heapStruct.insert(28);
        heapStruct.insert(44);
        heapStruct.insert(31);
        heapStruct.insert(32);
        heapStruct.insert(14);
        System.out.println("建堆后的数组值：");
        heapStruct.display();
        heapStruct.delMax();
        System.out.println("删除最大值后的数组值：");
        heapStruct.display();

        heapStruct.insert(59);
        int[] result = heapStruct.heapSort();
        System.out.println("排序后的值：");
        System.out.println(Arrays.stream(result).boxed().collect(Collectors.toList()));
    }

}
