package com.wang.leetcode.offer;

/*

输入一个整数数组，判断该数组是不是某二叉搜索树的后序遍历结果。如果是则返回 true，否则返回 false。假设输入的数组的任意两个数字都互不相同。

 

参考以下这颗二叉搜索树：

     5
    / \
   2   6
  / \
 1   3
示例 1：

输入: [1,6,3,2,5]
输出: false
示例 2：

输入: [1,3,2,6,5]
输出: true

 */
public class SolutionOffer33 {
    public boolean verifyPostorder(int[] postorder) {
        if (null == postorder || postorder.length == 0) {
            return true;
        }

        return verifyPostorder(postorder, 0, postorder.length - 1);
    }

    boolean verifyPostorder(int[] postorder, int start, int end) {
        if (start >= end) {
            return true;
        }

        int root = postorder[end];
        // 分为左右子树，左子树比end小，右子树比其大
        int mid = start;
        for (int i = end - 1; i >= start; i--) {
            if (postorder[i] < root) {
                mid = i;
                break;
            }
        }

        // 左子树有比root大的，说明不能构建这种搜索二叉树
        for (int i = start; i < mid; i ++) {
            if (postorder[i] > root) {
                return false;
            }
        }
        return verifyPostorder(postorder, start, mid) && verifyPostorder(postorder, mid + 1, end - 1);
    }

    public static void main(String[] args) {
        SolutionOffer33 solution = new SolutionOffer33();
        int[] postorder = {1,3,2,6,5};
        System.out.println(solution.verifyPostorder(postorder));

        postorder = new int[] {1,6,3,2,5};
        System.out.println(solution.verifyPostorder(postorder));
    }
}
