package com.wang.leetcode.offer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

/**
 * 输入某二叉树的前序遍历和中序遍历的结果，请构建该二叉树并返回其根节点。
 * <p>
 * 假设输入的前序遍历和中序遍历的结果中都不含重复的数字。
 */

public class SolutionOffer07 {

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    public void preOrder(TreeNode root) {
        if (Objects.isNull(root)) {
            return;
        }

        // 先访问跟
        System.out.print(root.val + " ");
        // 访问左子树
        preOrder(root.left);
        // 访问右子树
        preOrder(root.right);
    }


    public void inOrder(TreeNode root) {
        if (Objects.isNull(root)) {
            return;
        }

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        TreeNode viewed = root;
        while (!stack.isEmpty()) {
            // 访问左子树
            while (Objects.nonNull(viewed.left)) {
                stack.push(viewed.left);
                viewed = viewed.left;
            }

            TreeNode pop = stack.pop();
            // 访问根
            System.out.print(pop.val + " ");
            // 访问右子树
            if (Objects.nonNull(pop.right)) {
                stack.push(pop.right);
                viewed = pop.right;
            }
        }
    }

    private Map<Integer, Integer> indexMap;

    public TreeNode myBuildTree(int[] preorder, int[] inorder, int preorder_left, int preorder_right, int inorder_left, int inorder_right) {
        if (preorder_left > preorder_right) {
            return null;
        }

        // 前序遍历中的第一个节点就是根节点
        int preorder_root = preorder_left;
        // 在中序遍历中定位根节点
        int inorder_root = indexMap.get(preorder[preorder_root]);

        // 先把根节点建立出来
        TreeNode root = new TreeNode(preorder[preorder_root]);
        // 得到左子树中的节点数目
        int size_left_subtree = inorder_root - inorder_left;
        // 递归地构造左子树，并连接到根节点
        // 先序遍历中「从 左边界+1 开始的 size_left_subtree」个元素就对应了中序遍历中「从 左边界 开始到 根节点定位-1」的元素
        root.left = myBuildTree(preorder, inorder, preorder_left + 1, preorder_left + size_left_subtree, inorder_left, inorder_root - 1);
        // 递归地构造右子树，并连接到根节点
        // 先序遍历中「从 左边界+1+左子树节点数目 开始到 右边界」的元素就对应了中序遍历中「从 根节点定位+1 到 右边界」的元素
        root.right = myBuildTree(preorder, inorder, preorder_left + size_left_subtree + 1, preorder_right, inorder_root + 1, inorder_right);
        return root;
    }


    public TreeNode buildTree(int[] preorder, int[] inorder) {
        int n = preorder.length;
        // 构造哈希映射，帮助我们快速定位根节点
        indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indexMap.put(inorder[i], i);
        }
        return myBuildTree(preorder, inorder, 0, n - 1, 0, n - 1);
    }


    public static void main(String[] args) {
        SolutionOffer07 solution = new SolutionOffer07();
        int[] preOrder = new int[]{3, 9, 21, 22, 20, 15, 7};
        int[] inOrder = new int[]{21, 9, 22, 3, 15, 20, 7};
        TreeNode root = solution.buildTree(preOrder, inOrder);

        System.out.println("\n先序遍历...");
        solution.preOrder(root);
        System.out.println("\n中序遍历...");
        solution.inOrder(root);
    }

}
