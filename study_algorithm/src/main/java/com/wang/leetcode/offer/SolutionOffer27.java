package com.wang.leetcode.offer;

/**
 * 请完成一个函数，输入一个二叉树，该函数输出它的镜像。
 */

public class SolutionOffer27 {


    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    public TreeNode mirrorTree(TreeNode root) {
        if (null == root) {
            return root;
        }

        TreeNode left = mirrorTree(root.left);
        TreeNode right = mirrorTree(root.right);
        root.left = right;
        root.right = left;
        return root;
    }


}
