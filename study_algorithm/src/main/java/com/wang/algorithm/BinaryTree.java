package com.wang.algorithm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * 二叉树的遍历
 */
public class BinaryTree<T> {

    public static void main(String[] args) {
        /* 创建如下的树
         * 				A
         * 		B				C
         * 	D		E		F
         *
         */
        Node<String> root = new Node<>("A");
        BinaryTree<String> tree = new BinaryTree<>(root);
        Node<String> temp = tree.insertLChild(root, new Node<>("B"));
        tree.insertLChild(temp, new Node<>("D"));
        tree.insertRChild(temp, new Node<>("E"));
        temp = tree.insertRChild(root, new Node<>("C"));
        tree.insertLChild(temp, new Node<>("F"));

        System.out.println("********先写遍历的递归实现**************");
        BinaryTree.preOrderTraverse(root);
        System.out.println("\n********先序遍历的堆栈实现**************");
        BinaryTree.preOrderTraverseByStack(root);
        System.out.println("\n********morris先序遍历二叉树**************");
        morris(root, true, false, false);

        System.out.println("\n********中序遍历的递归实现**************");
        BinaryTree.inOrderTraverse(root);
        System.out.println("\n********中序遍历的堆栈实现**************");
        BinaryTree.inOrderTraverseByStack(root);
        System.out.println("\n********morris中序遍历二叉树**************");
        morris(root, false, true, false);

        System.out.println("\n********后序遍历的递归实现**************");
        BinaryTree.postOrderTraverse(root);
        System.out.println("\n********后序遍历的堆栈实现**************");
        BinaryTree.postOrderTraverseByStack2(root);
        System.out.println("\n********morris后序遍历二叉树**************");
        morris(root, false, false, true);

        System.out.println("\n********宽度优先遍历，并统计层中最大节点数**************");
        widthFirstTraverseWithStatLayerNum(root);

        System.out.println("\n********是否是平衡二叉树**************");
        System.out.println(isBalancedBinaryTree(root));

        System.out.println("\n********两个节点的最低公共祖先**************");
        System.out.println(lowestCommonAncestor(root, root, temp));
    }

    /**
     * 先序遍历的递归实现
     * 1.先遍历根节点
     * 2.再遍历左子树
     * 3.最后遍历右子树
     * 节点的左右子树也要按照这个步骤遍历
     */
    public static void preOrderTraverse(Node root) {
        if (null == root) {
            return;
        }

        //先访问根节点
        visitNode(root);

        //再访问左子树
        if (root.lchild != null) {
            preOrderTraverse(root.lchild);
        }

        //最后访问右子树
        if (root.rchild != null) {
            preOrderTraverse(root.rchild);
        }
    }


    /**
     * 先序遍历的堆栈实现实现
     */
    public static void preOrderTraverseByStack(Node root) {
        if (null == root) {
            return;
        }

        Stack<Node> stack = new Stack<>();
        stack.push(root);
        Node node;
        while (!stack.isEmpty()) {
            //先访问根
            node = stack.pop();
            visitNode(node);
            // 首先，右子树不为空，入栈
            if (null != node.rchild) {
                stack.push(node.rchild);
            }
            // 然后，左子树不为空，入栈
            if (null != node.lchild) {
                stack.push(node.lchild);
            }
        }
    }


    /**
     * 中序遍历的递归实现
     * 1.先遍历左子树
     * 2.再遍历根节点
     * 3.最后遍历右子树
     * 节点的左右子树也要按照这个步骤遍历
     */
    public static void inOrderTraverse(Node root) {
        if (null == root) {
            return;
        }

        //先访问左子树
        if (root.lchild != null) {
            inOrderTraverse(root.lchild);
        }

        // 再访问根节点
        visitNode(root);

        // 最后访问右子树
        if (root.rchild != null) {
            inOrderTraverse(root.rchild);
        }
    }

    /**
     * 中序遍历的堆栈实现实现
     */
    public static void inOrderTraverseByStack(Node root) {
        if (null == root) {
            return;
        }

        Stack<Node> stack = new Stack<>();
        Node node = root;
        while (!stack.isEmpty() || null != node) {
            // 左子树不为空，入栈
            if (node != null) {
                stack.push(node);
                node = node.lchild;
            } else {
                node = stack.pop();
                // 访问左子树
                visitNode(node);
                node = node.rchild;
            }
        }
    }


    /**
     * 后序遍历的迭代实现
     * 1.先遍历左子树
     * 2.再遍历右子树
     * 3.最后遍历根节点
     * 节点的左右子树也要按照这个步骤遍历
     */
    private static void postOrderTraverse(Node root) {
        if (null == root) {
            return;
        }

        // 先访问左子树
        if (root.lchild != null) {
            postOrderTraverse(root.lchild);
        }

        // 再访问右子树
        if (root.rchild != null) {
            postOrderTraverse(root.rchild);
        }

        // 最后访问根节点
        visitNode(root);
    }


    /**
     * 后序遍历的堆栈实现实现
     */
    public static void postOrderTraverseByStack(Node root) {
        if (null == root) {
            return;
        }

        Stack<Node> stack = new Stack<>();
        stack.push(root);

        Node node = stack.pop();
        Node prev = node;
        while (node != null || !stack.isEmpty()) {
            //先访问左子树
            while (node != null) {
                stack.push(node);
                node = node.lchild;
            }

            //再访问右子树
            if (!stack.isEmpty()) {
                node = stack.peek().rchild;

                if (node == null || node == prev) {
                    node = stack.pop();
                    //访问根节点
                    visitNode(node);
                    prev = node;
                    node = null;
                }
            }
        }
    }

    /**
     * 后序遍历的堆栈实现实现
     *    利用先序遍历的实现：根左右  -> 根右左
     *    那么，如果不打印，而是放到另一个栈中，那么变成了：左右根，就是后续遍历了
     */
    public static void postOrderTraverseByStack2(Node root) {
        if (null == root) {
            return;
        }

        Stack<Node> stack1 = new Stack<>();
        Stack<Node> stack2 = new Stack<>();
        stack1.push(root);
        Node node;
        while (!stack1.isEmpty()) {
            //先访问根
            node = stack1.pop();
            stack2.push(node);
            // 首先，左子树不为空，入栈
            if (null != node.lchild) {
                stack1.push(node.lchild);
            }
            // 然后，右子树不为空，入栈
            if (null != node.rchild) {
                stack1.push(node.rchild);
            }
        }
        // 最后输出
        while (!stack2.isEmpty()) {
            visitNode(stack2.pop());
        }
    }

    /**
     * morris遍历，该遍历方法可以不使用额外的空间，在O(N)复杂度完成遍历
     * 利用二叉树的叶子节点空指针完成遍历，可以改为先序遍历，中序遍历，和后序遍历
     * 步骤：
     *  1. 如果有左孩子，将左孩子的最后右指针指向当前节点，然后遍历左子树
     *  2. 如果没有左孩子或者左孩子的有指针指向了自己，将当前指针指向右孩子
     * 上面的步骤会将节点分为两种，只遍历一次的，和 来到两次的节点
     *
     * 改为先序遍历：只遍历一次的节点打印，遍历两次的节点，打印第一次的，第二次不管
     * 改为中序遍历：只遍历一次的节点打印，遍历两次的节点，第一次不管，打印第二次的
     * 改为后续遍历：只遍历一次的节点不管，遍历两次的节点,第一次不管，
     *      打印第二次的逆序打印左孩子的右边界，遍历完成后，单独打印整个树的右边界
     *      有个问题是如何不适用空间，逆序打印右边界，使用逆序链表的方式
     *
     * @param preOrder 是否先序遍历
     * @param inOrder 是否中序遍历
     * @param postOrder 是否后序遍历
     */
    public static void morris(Node root, boolean preOrder, boolean inOrder, boolean postOrder) {
        if (null == root) {
            return;
        }

        Node cur = root;
        Node mostRight = null;
        // 开始遍历
        while (null != cur) {
            // mostRight是cur的左孩子
            mostRight = cur.lchild;
            // 左孩子不为空，
            if (null != mostRight) {
                // 找到最右的指针
                while (null != mostRight.rchild && cur != mostRight.rchild) {
                    mostRight = mostRight.rchild;
                }
                // mostRight如果为空，那么是cur左子树上，最右的节点
                if (null == mostRight.rchild) {
                    // 先序遍历，第一次进入的时候访问节点
                    if (preOrder) {
                        visitNode(cur);
                    }

                    mostRight.rchild = cur;
                    // 继续遍历左子树
                    cur = cur.lchild;
                    continue;
                }
                // 否则mostRight == cur，说明之前已经改过了，这是第二次到达cur节点了
                else {
                    // 第二次达到，说明左子树到遍历完成了，跳出循环，开始遍历右子树
                    mostRight.rchild = null;

                    // 中序遍历，第二次进入的时候访问节点
                    if (inOrder) {
                        visitNode(cur);
                    }

                    // 后续遍历，第二次进入的时候逆序访问左孩子的所有右节点
                    if (postOrder) {
                        inversePrintRight(cur.lchild);
                    }
                }
            } else {
                if (preOrder || inOrder) {
                    visitNode(cur);
                }
            }

            // 遍历右子树
            cur = cur.rchild;
        }

        // 后续遍历中，树遍历完成，打印整个树的所有右孩子
        if (postOrder) {
            inversePrintRight(root);
        }
    }

    private static void inversePrintRight(Node node) {
        // 反转打印
        Node tail = reverseLink(node);
        Node cur = tail;
        while (null != cur) {
            visitNode(cur);
            cur = cur.rchild;
        }
        // 反转回去，保持不变
        reverseLink(tail);
    }

    /**
     * 反转node的链表（next右rchild表示）
     * @param node 头结点
     * @return 新链表的头结点
     */
    public static Node reverseLink(Node node) {
        Node pre = null;
        Node next = null;
        while (null != node) {
            next = node.rchild;
            node.rchild = pre;
            pre = node;
            node = next;
        }
        return pre;
    }

    /**
     * 宽度优先遍历，并统计每层的节点数
     */
    public static void widthFirstTraverseWithStatLayerNum(Node root) {
        if (null == root) {
            return;
        }

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        HashMap<Node, Integer> nodeLayerMap = new HashMap<>();
        nodeLayerMap.put(root, 1);
        int curLayer = 1;
        // 当前层的节点数
        int curLayerCount = 0;
        // 每层的最大节点数
        int max = 1;

        Node cur;
        int curNodeLayer;
        while (!queue.isEmpty()) {
            cur = queue.poll();
            // 访问当前节点
            visitNode(cur);

            curNodeLayer = nodeLayerMap.get(cur);
            // 相等，说明还在一层，否则进入了下一层打印
            if (curNodeLayer == curLayer) {
                curLayerCount++;
            } else {
                max = Math.max(max, curLayerCount);
                curLayer++;
                curLayerCount = 1;
            }

            // 左孩子入队，孩子层数是父层数+1
            if (null != cur.lchild) {
                nodeLayerMap.put(cur.lchild, curNodeLayer + 1);
                queue.add(cur.lchild);
            }
            // 右孩子入队，孩子层数是父层数+1
            if (null != cur.rchild) {
                nodeLayerMap.put(cur.rchild, curNodeLayer + 1);
                queue.add(cur.rchild);
            }
        }
        // 对比最后一层
        max = Math.max(max, curLayerCount);

        System.out.println("最大节点的个数：" + max);
    }

    /**
     * 是否是平衡二叉树
     */
    public static boolean isBalancedBinaryTree(Node head) {
        return isBalanced(head).isBalanced;
    }

    private static class BalancedInfo {
        public boolean isBalanced;
        // 数的高度
        private int height;
        public BalancedInfo(boolean isBalanced, int height) {
            this.isBalanced = isBalanced;
            this.height = height;
        }
    }
    private static BalancedInfo isBalanced(Node head) {
        // 空数时平衡二叉树，且高度为0
        if (null == head) {
            return new BalancedInfo(true, 0);
        }

        // 计算左右子树是否是平衡二叉树
        BalancedInfo left = isBalanced(head.lchild);
        BalancedInfo right = isBalanced(head.rchild);

        // 高度为左右子树中高度大的加1
        int height = Math.max(left.height, right.height) + 1;
        // 左右子树都是平衡二叉树，且高度只差小于等于1
        boolean isBalanced = left.isBalanced && right.isBalanced
                && Math.abs(left.height - right.height) <= 1;
        return new BalancedInfo(isBalanced, height);
    }

    /**
     * 返回二叉树中两个节点的最低的公共祖先
     *  因为两个节点在二叉树中，所以肯定有公共祖先，因此head是公共的，所以肯定有最低的一个公共祖先
     *      1. 第一中情况，一个节点是另一个节点的祖先
     *      2. 第二种情况，两个节点都不是互相的公共祖先
     * @param head 二叉树根
     * @param node1 二叉树中的一个节点
     * @param node2 二叉树中的另一个节点
     * @return 最低公共祖先
     */
    public static Node lowestCommonAncestor(Node head, Node node1, Node node2) {
        // 如果两个节点中的一个是根节点，那么根节点为祖先节点
        if (null == head || head == node1 || head == node2) {
            return head;
        }

        // 向左右子树要最低的公共节点
        Node left = lowestCommonAncestor(head.lchild, node1, node2);
        Node right = lowestCommonAncestor(head.rchild, node1, node2);

        // 左右两个子树都不是null，那么返回自己，这个就是两个的公共祖先
        if (null != left && null != right) {
            return head;
        }

        // 左右两颗数，有一个是null，另一个不确定，返回不确定的
        return null != left ? left : right;
    }

    private static void visitNode(Node<?> node) {
        System.out.print(node + "  ");
    }


    final private Node<T> root;

    public Node<T> getRoot() {
        return this.root;
    }

    public BinaryTree(Node<T> root) {
        this.root = root;
    }

    public Node<T> insertRChild(Node<T> parent, Node<T> node) {
        parent.rchild = node;
        return node;
    }

    public Node<T> insertLChild(Node<T> parent, Node<T> node) {
        parent.lchild = node;
        return node;
    }

    private static class Node<T> {
        protected Node<T> lchild;
        protected T data;
        // protected Node parent;
        protected Node<T> rchild;

        public Node(T t) {
            lchild = null;
            rchild = null;
            this.data = t;
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }
}