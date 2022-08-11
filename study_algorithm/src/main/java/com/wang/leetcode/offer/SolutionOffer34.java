package com.wang.leetcode.offer;

/*
给你二叉树的根节点 root 和一个整数目标和 targetSum ，找出所有 从根节点到叶子节点 路径总和等于给定目标和的路径。

叶子节点 是指没有子节点的节点。

 */

import java.util.ArrayList;
import java.util.List;

public class SolutionOffer34 {
    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {
        }

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    public List<List<Integer>> pathSum(TreeNode root, int target) {
        List<List<Integer>> paths = new ArrayList<>();
        if (null == root) {
            return paths;
        }

        pathOne(root, target, new ArrayList<>(), paths);
        return paths;
    }

    void pathOne(TreeNode root, int target, List<Integer> onePath, List<List<Integer>> paths) {
        // 是否是子节点
        if (null == root.left && null == root.right) {
            int sum = 0;
            for(int point : onePath) {
                sum = sum + point;
            }

            if (sum + root.val == target) {
                onePath.add(root.val);
                paths.add(onePath);
            }
            return;
        }

        if (null != root.left) {
            List<Integer> tempPath = new ArrayList<>(onePath);
            tempPath.add(root.val);
            pathOne(root.left, target, tempPath, paths);
        }

        if (null != root.right) {
            List<Integer> tempPath = new ArrayList<>(onePath);
            tempPath.add(root.val);
            pathOne(root.right, target, tempPath, paths);
        }
    }

}
