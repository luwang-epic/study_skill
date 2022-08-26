package com.wang.algorithm;

import java.util.Stack;

public class BinaryTree<T> {

    final private Node<T> root;

    public static class Node<T> {
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

    public Node<T> getRoot(){
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


    private static void visitNode(Node<?> node){
        System.out.print(node + "  ");
    }

    /**
     *  先序遍历的迭代实现
     *  1.先遍历根节点
     *  2.再遍历左子树
     *  3.最后遍历右子树
     *
     *	节点的左右子树也要按照这个步骤遍历
     */
    public static void preOrderTraverse(Node<?> root) {
        //先访问根节点
        visitNode(root);

        //再访问左子树
        if(root.lchild != null) {
            preOrderTraverse(root.lchild);
        }

        //最后访问右子树
        if(root.rchild != null) {
            preOrderTraverse(root.rchild);
        }
    }


    /**
     *  先序遍历的堆栈实现实现
     */
    public static void preOrderTraverseByStack(Node<?> root) {
        Stack<Node<?>> stack = new Stack<Node<?>>();
        stack.push(root);

        while(!stack.isEmpty()){
            Node<?> node = stack.peek();
            while(node != null){
                //先访问根节点
                visitNode(node);

                //再访问左子树
                node = node.lchild;
                stack.push(node);
            }

            //空指针出栈
            stack.pop();

            //在访问右子树
            if(!stack.isEmpty()){
                node = stack.pop();
                stack.push(node.rchild);
            }
        }
    }



    /**
     *  中序遍历的迭代实现
     *  1.先遍历左子树
     *  2.再遍历根节点
     *  3.最后遍历右子树
     *
     *	节点的左右子树也要按照这个步骤遍历
     */
    public static void inOrderTraverse(Node<?> root) {
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
     *  中序遍历的堆栈实现实现
     */
    public static void inOrderTraverseByStack(Node<?> root) {
        Stack<Node<?>> stack = new Stack<Node<?>>();
        stack.push(root);

        while(!stack.isEmpty()){
            //先访问左子树
            Node<?> node = stack.peek();
            while(node != null){
                node = node.lchild;
                stack.push(node);
            }
            //空指针退栈
            stack.pop();

            if(!stack.isEmpty()){
                //访问根节点
                node = stack.pop();
                visitNode(node);

                //访问右子树
                stack.push(node.rchild);
            }
        }

    }



    /**
     *  后序遍历的迭代实现
     *  1.先遍历左子树
     *  2.再遍历右子树
     *  3.最后遍历根节点
     *
     *	节点的左右子树也要按照这个步骤遍历
     */
    private static void postOrderTraverse(Node<?> root) {
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
     *  后序遍历的堆栈实现实现
     */
    public static void postOrderTraverseByStack(Node<?> root) {
        Stack<Node<?>> stack = new Stack<Node<?>>();
        stack.push(root);

        Node<?> node = stack.pop();
        Node<?> prev = node;
        while(node != null || !stack.isEmpty()){
            //先访问左子树
            while(node != null){
                stack.push(node);
                node = node.lchild;
            }

            //再访问右子树
            if(!stack.isEmpty()){
                node = stack.peek().rchild;

                if(node == null || node == prev){
                    node = stack.pop();
                    //访问根节点
                    visitNode(node);
                    prev = node;
                    node = null;
                }
            }
        }

    }



    public static void main(String[] args) {
        Node<String> root = new Node<String>("A");
        BinaryTree<String> tree = new BinaryTree<String>(root);

        /* 创建如下的树
         * 				A
         * 		B				C
         * 	D		E		F
         *
         */

        Node<String> temp = tree.insertLChild(root, new Node<String>("B"));
        tree.insertLChild(temp, new Node<String>("D"));
        tree.insertRChild(temp, new Node<String>("E"));
        temp = tree.insertRChild(root, new Node<String>("C"));
        tree.insertLChild(temp, new Node<String>("F"));

        System.out.println("********先序遍历的迭代实现**************");
        BinaryTree.preOrderTraverse(root);
        System.out.println("\n********中序遍历的迭代实现**************");
        BinaryTree.inOrderTraverse(root);
        System.out.println("\n********后序遍历的迭代实现**************");
        BinaryTree.postOrderTraverse(root);

        System.out.println("\n********先序遍历的堆栈实现**************");
        BinaryTree.preOrderTraverseByStack(root);
        System.out.println("\n********中序遍历的堆栈实现**************");
        BinaryTree.inOrderTraverseByStack(root);
        System.out.println("\n********后序遍历的堆栈实现**************");
        BinaryTree.postOrderTraverseByStack(root);

    }
}
