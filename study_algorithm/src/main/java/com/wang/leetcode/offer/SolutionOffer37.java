package com.wang.leetcode.offer;

/*

请实现两个函数，分别用来序列化和反序列化二叉树。

你需要设计一个算法来实现二叉树的序列化与反序列化。这里不限定你的序列 / 反序列化算法执行逻辑，你只需要保证一个二叉树可以被序列化为一个字符串并且将这个字符串反序列化为原始的树结构。

提示：输入输出格式与 LeetCode 目前使用的方式一致，详情请参阅 LeetCode 序列化二叉树的格式。你并非必须采取这种方式，你也可以采用其他的方法解决这个问题。



 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SolutionOffer37 {

    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        if (null == root) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        boolean left;
        boolean right;
        while (!queue.isEmpty()) {
            TreeNode poll = queue.poll();
            sb.append(poll.val);

            left = false;
            if (null != poll.left) {
                left = true;
                queue.offer(poll.left);
            }
            right = false;
            if (null != poll.right) {
                right = true;
                queue.offer(poll.right);
            }

            if (left && right) {
                sb.append("*");
            } else if (left) {
                sb.append("+");
            } else if (right) {
                sb.append("-");
            } else {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        if (null == data) {
            return null;
        }

        int thisLevelNum = 1;
        String thisLevelStr = data.substring(0, thisLevelNum * 2);
        data = data.substring(thisLevelNum * 2);;

        TreeNode root = new TreeNode(Integer.parseInt(thisLevelStr.charAt(0) + ""));
        List<TreeNode> thisLevelNodes = new ArrayList<>();
        thisLevelNodes.add(root);
        while (true) {
            int nextLevelNum = 0;
            for (int i = 1; i < thisLevelStr.length(); i = i + 2) {
                nextLevelNum += childNum(thisLevelStr.charAt(i));
            }
            if (nextLevelNum == 0) {
                break;
            }

            String nextLevelStr = data.substring(0, nextLevelNum * 2);
            data = data.substring(nextLevelNum * 2);
            List<TreeNode> nextLevelNodes = new ArrayList<>();
            for (int i = 0; i < nextLevelStr.length(); i = i + 2) {
                TreeNode treeNode = new TreeNode(Integer.parseInt(nextLevelStr.charAt(i) + ""));
                nextLevelNodes.add(treeNode);
            }

            int nextLevelIndex = 0;
            for (int i = 0; i < thisLevelStr.length(); i = i + 2) {
                TreeNode treeNode = thisLevelNodes.get(i/2);
                char ch = thisLevelStr.charAt(i + 1);
                if (ch == '*') {
                    treeNode.left = nextLevelNodes.get(nextLevelIndex++);
                    treeNode.right = nextLevelNodes.get(nextLevelIndex++);
                } else if (ch == '+') {
                    treeNode.left = nextLevelNodes.get(nextLevelIndex++);
                } else if (ch == '-') {
                    treeNode.right = nextLevelNodes.get(nextLevelIndex++);
                } else {

                }
            }

            thisLevelNum = nextLevelNum;
            thisLevelStr = nextLevelStr;
            thisLevelNodes = nextLevelNodes;
        }

        return root;
    }

    public int childNum(char ch) {
        if (ch == '*') {
            return 2;
        }
        if (ch == '+' || ch == '-') {
            return 1;
        }
        return 0;
    }


    public static void main(String[] args) {
        TreeNode treeNode1 = new TreeNode(1);
        TreeNode treeNode2 = new TreeNode(2);
        TreeNode treeNode3 = new TreeNode(3);
        TreeNode treeNode4 = new TreeNode(4);
        TreeNode treeNode5 = new TreeNode(5);
        treeNode1.left = treeNode2;
        treeNode1.right = treeNode3;
        treeNode3.left = treeNode4;
        treeNode3.right = treeNode5;

        SolutionOffer37 solution = new SolutionOffer37();
        String serialize = solution.serialize(treeNode1);
        System.out.println(serialize);

        TreeNode deserialize = solution.deserialize(serialize);
        System.out.println(solution.serialize(deserialize));
    }

}
