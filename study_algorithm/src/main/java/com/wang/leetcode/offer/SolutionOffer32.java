package com.wang.leetcode.offer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/*
从上到下打印出二叉树的每个节点，同一层的节点按照从左到右的顺序打印。

 

例如:
给定二叉树: [3,9,20,null,null,15,7],

    3
   / \
  9  20
    /  \
   15   7
返回：

[3,9,20,15,7]


 */
public class SolutionOffer32 {

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    public int[] levelOrder(TreeNode root) {
        if (null == root) {
            return new int[0];
        }

        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        List<Integer> result = new ArrayList<>();
        while(!queue.isEmpty()) {
            TreeNode poll = queue.poll();
            result.add(poll.val);
            if (null != poll.left) {
                queue.offer(poll.left);
            }
            if (null != poll.right) {
                queue.offer(poll.right);
            }
        }

        int[] values = new int[result.size()];
        for(int i = 0; i < values.length; i++) {
            values[i] = result.get(i);
        }
        return values;
    }
}
