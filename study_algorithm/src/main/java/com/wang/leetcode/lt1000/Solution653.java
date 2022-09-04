package com.wang.leetcode.lt1000;

import java.util.Stack;

public class Solution653 {


    public static class TreeNode {
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


    public boolean findTarget(TreeNode root, int k) {
        if (null == root) {
            return false;
        }

        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode temp = stack.peek();
            while (temp != null) {
                stack.push(temp.left);
                temp = temp.left;
            }
            stack.pop();

            if(!stack.isEmpty()){
                //访问根节点
                temp = stack.pop();
                if (hasNode(root, temp, k - temp.val)) {
                    return true;
                }

                //访问右子树
                stack.push(temp.right);
            }
        }
        return false;
    }


    public boolean hasNode(TreeNode root, TreeNode visited, int k) {
        if (null == root) {
            return false;
        }

        TreeNode temp = root;
        while(null != temp) {
            if (k == temp.val) {
                if (visited != temp) {
                    System.out.println(visited.val + "  " + temp.val);
                    return true;
                } else {
                    return false;
                }
            }

            if (k > temp.val) {
                temp = temp.right;
            } else {
                temp = temp.left;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Solution653 solution = new Solution653();

        TreeNode root = new TreeNode(5);
        TreeNode left1 = new TreeNode(3);
        root.left = left1;
        TreeNode right1 = new TreeNode(6);
        root.right = right1;
        TreeNode left2 = new TreeNode(2);
        left1.left = left2;
        TreeNode left3 = new TreeNode(4);
        left1.right = left3;
        TreeNode right2 = new TreeNode(7);
        right1.right = right2;

        int k = 28;
        boolean target = solution.findTarget(root, k);
        System.out.println(target);
    }

}
