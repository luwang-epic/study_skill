package com.wang.algorithm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * �������ı���
 */
public class BinaryTree<T> {

    public static void main(String[] args) {
        /* �������µ���
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

        System.out.println("********��д�����ĵݹ�ʵ��**************");
        BinaryTree.preOrderTraverse(root);
        System.out.println("\n********��������Ķ�ջʵ��**************");
        BinaryTree.preOrderTraverseByStack(root);
        System.out.println("\n********morris�������������**************");
        morris(root, true, false, false);

        System.out.println("\n********��������ĵݹ�ʵ��**************");
        BinaryTree.inOrderTraverse(root);
        System.out.println("\n********��������Ķ�ջʵ��**************");
        BinaryTree.inOrderTraverseByStack(root);
        System.out.println("\n********morris�������������**************");
        morris(root, false, true, false);

        System.out.println("\n********��������ĵݹ�ʵ��**************");
        BinaryTree.postOrderTraverse(root);
        System.out.println("\n********��������Ķ�ջʵ��**************");
        BinaryTree.postOrderTraverseByStack2(root);
        System.out.println("\n********morris�������������**************");
        morris(root, false, false, true);

        System.out.println("\n********������ȱ�������ͳ�Ʋ������ڵ���**************");
        widthFirstTraverseWithStatLayerNum(root);

        System.out.println("\n********�Ƿ���ƽ�������**************");
        System.out.println(isBalancedBinaryTree(root));

        System.out.println("\n********�����ڵ����͹�������**************");
        System.out.println(lowestCommonAncestor(root, root, temp));
    }

    /**
     * ��������ĵݹ�ʵ��
     * 1.�ȱ������ڵ�
     * 2.�ٱ���������
     * 3.������������
     * �ڵ����������ҲҪ��������������
     */
    public static void preOrderTraverse(Node root) {
        if (null == root) {
            return;
        }

        //�ȷ��ʸ��ڵ�
        visitNode(root);

        //�ٷ���������
        if (root.lchild != null) {
            preOrderTraverse(root.lchild);
        }

        //������������
        if (root.rchild != null) {
            preOrderTraverse(root.rchild);
        }
    }


    /**
     * ��������Ķ�ջʵ��ʵ��
     */
    public static void preOrderTraverseByStack(Node root) {
        if (null == root) {
            return;
        }

        Stack<Node> stack = new Stack<>();
        stack.push(root);
        Node node;
        while (!stack.isEmpty()) {
            //�ȷ��ʸ�
            node = stack.pop();
            visitNode(node);
            // ���ȣ���������Ϊ�գ���ջ
            if (null != node.rchild) {
                stack.push(node.rchild);
            }
            // Ȼ����������Ϊ�գ���ջ
            if (null != node.lchild) {
                stack.push(node.lchild);
            }
        }
    }


    /**
     * ��������ĵݹ�ʵ��
     * 1.�ȱ���������
     * 2.�ٱ������ڵ�
     * 3.������������
     * �ڵ����������ҲҪ��������������
     */
    public static void inOrderTraverse(Node root) {
        if (null == root) {
            return;
        }

        //�ȷ���������
        if (root.lchild != null) {
            inOrderTraverse(root.lchild);
        }

        // �ٷ��ʸ��ڵ�
        visitNode(root);

        // ������������
        if (root.rchild != null) {
            inOrderTraverse(root.rchild);
        }
    }

    /**
     * ��������Ķ�ջʵ��ʵ��
     */
    public static void inOrderTraverseByStack(Node root) {
        if (null == root) {
            return;
        }

        Stack<Node> stack = new Stack<>();
        Node node = root;
        while (!stack.isEmpty() || null != node) {
            // ��������Ϊ�գ���ջ
            if (node != null) {
                stack.push(node);
                node = node.lchild;
            } else {
                node = stack.pop();
                // ����������
                visitNode(node);
                node = node.rchild;
            }
        }
    }


    /**
     * ��������ĵ���ʵ��
     * 1.�ȱ���������
     * 2.�ٱ���������
     * 3.���������ڵ�
     * �ڵ����������ҲҪ��������������
     */
    private static void postOrderTraverse(Node root) {
        if (null == root) {
            return;
        }

        // �ȷ���������
        if (root.lchild != null) {
            postOrderTraverse(root.lchild);
        }

        // �ٷ���������
        if (root.rchild != null) {
            postOrderTraverse(root.rchild);
        }

        // �����ʸ��ڵ�
        visitNode(root);
    }


    /**
     * ��������Ķ�ջʵ��ʵ��
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
            //�ȷ���������
            while (node != null) {
                stack.push(node);
                node = node.lchild;
            }

            //�ٷ���������
            if (!stack.isEmpty()) {
                node = stack.peek().rchild;

                if (node == null || node == prev) {
                    node = stack.pop();
                    //���ʸ��ڵ�
                    visitNode(node);
                    prev = node;
                    node = null;
                }
            }
        }
    }

    /**
     * ��������Ķ�ջʵ��ʵ��
     *    �������������ʵ�֣�������  -> ������
     *    ��ô���������ӡ�����Ƿŵ���һ��ջ�У���ô����ˣ����Ҹ������Ǻ���������
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
            //�ȷ��ʸ�
            node = stack1.pop();
            stack2.push(node);
            // ���ȣ���������Ϊ�գ���ջ
            if (null != node.lchild) {
                stack1.push(node.lchild);
            }
            // Ȼ����������Ϊ�գ���ջ
            if (null != node.rchild) {
                stack1.push(node.rchild);
            }
        }
        // ������
        while (!stack2.isEmpty()) {
            visitNode(stack2.pop());
        }
    }

    /**
     * morris�������ñ����������Բ�ʹ�ö���Ŀռ䣬��O(N)���Ӷ���ɱ���
     * ���ö�������Ҷ�ӽڵ��ָ����ɱ��������Ը�Ϊ�������������������ͺ������
     * ���裺
     *  1. ��������ӣ������ӵ������ָ��ָ��ǰ�ڵ㣬Ȼ�����������
     *  2. ���û�����ӻ������ӵ���ָ��ָ�����Լ�������ǰָ��ָ���Һ���
     * ����Ĳ���Ὣ�ڵ��Ϊ���֣�ֻ����һ�εģ��� �������εĽڵ�
     *
     * ��Ϊ���������ֻ����һ�εĽڵ��ӡ���������εĽڵ㣬��ӡ��һ�εģ��ڶ��β���
     * ��Ϊ���������ֻ����һ�εĽڵ��ӡ���������εĽڵ㣬��һ�β��ܣ���ӡ�ڶ��ε�
     * ��Ϊ����������ֻ����һ�εĽڵ㲻�ܣ��������εĽڵ�,��һ�β��ܣ�
     *      ��ӡ�ڶ��ε������ӡ���ӵ��ұ߽磬������ɺ󣬵�����ӡ���������ұ߽�
     *      �и���������β����ÿռ䣬�����ӡ�ұ߽磬ʹ����������ķ�ʽ
     *
     * @param preOrder �Ƿ��������
     * @param inOrder �Ƿ��������
     * @param postOrder �Ƿ�������
     */
    public static void morris(Node root, boolean preOrder, boolean inOrder, boolean postOrder) {
        if (null == root) {
            return;
        }

        Node cur = root;
        Node mostRight = null;
        // ��ʼ����
        while (null != cur) {
            // mostRight��cur������
            mostRight = cur.lchild;
            // ���Ӳ�Ϊ�գ�
            if (null != mostRight) {
                // �ҵ����ҵ�ָ��
                while (null != mostRight.rchild && cur != mostRight.rchild) {
                    mostRight = mostRight.rchild;
                }
                // mostRight���Ϊ�գ���ô��cur�������ϣ����ҵĽڵ�
                if (null == mostRight.rchild) {
                    // �����������һ�ν����ʱ����ʽڵ�
                    if (preOrder) {
                        visitNode(cur);
                    }

                    mostRight.rchild = cur;
                    // ��������������
                    cur = cur.lchild;
                    continue;
                }
                // ����mostRight == cur��˵��֮ǰ�Ѿ��Ĺ��ˣ����ǵڶ��ε���cur�ڵ���
                else {
                    // �ڶ��δﵽ��˵������������������ˣ�����ѭ������ʼ����������
                    mostRight.rchild = null;

                    // ����������ڶ��ν����ʱ����ʽڵ�
                    if (inOrder) {
                        visitNode(cur);
                    }

                    // �����������ڶ��ν����ʱ������������ӵ������ҽڵ�
                    if (postOrder) {
                        inversePrintRight(cur.lchild);
                    }
                }
            } else {
                if (preOrder || inOrder) {
                    visitNode(cur);
                }
            }

            // ����������
            cur = cur.rchild;
        }

        // ���������У���������ɣ���ӡ�������������Һ���
        if (postOrder) {
            inversePrintRight(root);
        }
    }

    private static void inversePrintRight(Node node) {
        // ��ת��ӡ
        Node tail = reverseLink(node);
        Node cur = tail;
        while (null != cur) {
            visitNode(cur);
            cur = cur.rchild;
        }
        // ��ת��ȥ�����ֲ���
        reverseLink(tail);
    }

    /**
     * ��תnode������next��rchild��ʾ��
     * @param node ͷ���
     * @return �������ͷ���
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
     * ������ȱ�������ͳ��ÿ��Ľڵ���
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
        // ��ǰ��Ľڵ���
        int curLayerCount = 0;
        // ÿ������ڵ���
        int max = 1;

        Node cur;
        int curNodeLayer;
        while (!queue.isEmpty()) {
            cur = queue.poll();
            // ���ʵ�ǰ�ڵ�
            visitNode(cur);

            curNodeLayer = nodeLayerMap.get(cur);
            // ��ȣ�˵������һ�㣬�����������һ���ӡ
            if (curNodeLayer == curLayer) {
                curLayerCount++;
            } else {
                max = Math.max(max, curLayerCount);
                curLayer++;
                curLayerCount = 1;
            }

            // ������ӣ����Ӳ����Ǹ�����+1
            if (null != cur.lchild) {
                nodeLayerMap.put(cur.lchild, curNodeLayer + 1);
                queue.add(cur.lchild);
            }
            // �Һ�����ӣ����Ӳ����Ǹ�����+1
            if (null != cur.rchild) {
                nodeLayerMap.put(cur.rchild, curNodeLayer + 1);
                queue.add(cur.rchild);
            }
        }
        // �Ա����һ��
        max = Math.max(max, curLayerCount);

        System.out.println("���ڵ�ĸ�����" + max);
    }

    /**
     * �Ƿ���ƽ�������
     */
    public static boolean isBalancedBinaryTree(Node head) {
        return isBalanced(head).isBalanced;
    }

    private static class BalancedInfo {
        public boolean isBalanced;
        // ���ĸ߶�
        private int height;
        public BalancedInfo(boolean isBalanced, int height) {
            this.isBalanced = isBalanced;
            this.height = height;
        }
    }
    private static BalancedInfo isBalanced(Node head) {
        // ����ʱƽ����������Ҹ߶�Ϊ0
        if (null == head) {
            return new BalancedInfo(true, 0);
        }

        // �������������Ƿ���ƽ�������
        BalancedInfo left = isBalanced(head.lchild);
        BalancedInfo right = isBalanced(head.rchild);

        // �߶�Ϊ���������и߶ȴ�ļ�1
        int height = Math.max(left.height, right.height) + 1;
        // ������������ƽ����������Ҹ߶�ֻ��С�ڵ���1
        boolean isBalanced = left.isBalanced && right.isBalanced
                && Math.abs(left.height - right.height) <= 1;
        return new BalancedInfo(isBalanced, height);
    }

    /**
     * ���ض������������ڵ����͵Ĺ�������
     *  ��Ϊ�����ڵ��ڶ������У����Կ϶��й������ȣ����head�ǹ����ģ����Կ϶�����͵�һ����������
     *      1. ��һ�������һ���ڵ�����һ���ڵ������
     *      2. �ڶ�������������ڵ㶼���ǻ���Ĺ�������
     * @param head ��������
     * @param node1 �������е�һ���ڵ�
     * @param node2 �������е���һ���ڵ�
     * @return ��͹�������
     */
    public static Node lowestCommonAncestor(Node head, Node node1, Node node2) {
        // ��������ڵ��е�һ���Ǹ��ڵ㣬��ô���ڵ�Ϊ���Ƚڵ�
        if (null == head || head == node1 || head == node2) {
            return head;
        }

        // ����������Ҫ��͵Ĺ����ڵ�
        Node left = lowestCommonAncestor(head.lchild, node1, node2);
        Node right = lowestCommonAncestor(head.rchild, node1, node2);

        // ������������������null����ô�����Լ���������������Ĺ�������
        if (null != left && null != right) {
            return head;
        }

        // ��������������һ����null����һ����ȷ�������ز�ȷ����
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