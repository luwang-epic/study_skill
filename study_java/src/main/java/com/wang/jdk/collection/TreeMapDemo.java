package com.wang.jdk.collection;

/*
有序数组（链表不能随机访问下标，所以没法用于二分查找）
    二分查找效率高，但是插入和删除需要移动大量数据，因此引入了二叉排序树

跳表
    二分查找依赖数组的随机访问，所以只能用数组来实现。如果数据存储在链表中，就真的没法用二分查找了吗？
    而实际上，我们只需要对链表稍加改造，就可以实现类似“二分”的查找算法，这种改造之后的数据结构叫作跳表（Skip List）

    其中，插入、删除、查找以及迭代输出有序序列这几个操作，红黑树也可以完成，时间复杂度和跳表是一样的。
    但是，按照区间查找数据这个操作，红黑树的效率没有跳表高。跳表可以在 O(logn)
    时间复杂度定位区间的起点，然后在原始链表中顺序向后查询就可以了，这样非常高效。
    此外，相比于红黑树，跳表还具有代码更容易实现、可读性好、不容易出错、更加灵活等优点

二叉排序树（二叉搜索树，二叉查找树） Binary Search Tree
    是指一棵空树或者具有下列性质的二叉树。 若任意节点的左子树不空，则左子树上所有节点的值均小于它的根节点的值；
    若任意节点的右子树不空，则右子树上所有节点的值均大于或等于它的根节点的值；任意节点的左、右子树也分别为二叉查找树

    类似二分查找，效率高，但是二叉查找树有个非常严重的问题，如果数据的插入是从大到小插入的，或者是从小到大插入的话，
    会导致二叉查找树退化成单链表的形式，查找效率低下，为了解决该问题，引入平衡树，能够使得树趋向平衡，
    这种自平衡的树叫做平衡树。平衡树（Balance Tree，BT）指的是：任意节点的子树的高度差都小于等于 1。
    常见的符合平衡树的有 AVL 树（二叉平衡搜索树），B 树（多路平衡搜索树，2-3 树，2-3-4 树中的一种），红黑树等。

平衡二叉搜索树（AVL数）
    指任意节点的两个子树的高度差不超过 1 的平衡树。

    类似二分查找，效率高，平均情况优良，
    但是插入和删除过程，为了保证平衡特性，需要频繁进行平衡操作，因此引入红黑树

2-3树
    2-3 树是指每个具有子节点的节点（内部节点）要么有两个子节点和一个数据元素，
    要么有三个子节点和两个数据元素的自平衡的树，它的所有叶子节点都具有相同的高度。
    简单点讲，2-3 树的非叶子节点都具有两个分叉或者三个分叉，有两个分叉的节点包含一个数据元素，
    有三个分叉的节点包含两个数据元素所以，称作 2 叉3 叉树更容易理解

2-3-4树
    同2-3树一样，2-3-4 树是指每个具有子节点的节点（内部节点）要么是2节点，要么是3节点，要么是4节点的自平衡的树，
    不同节点的子节点树和存储的数据元素数量不同，如下，它的所有叶子节点都具有相同的高度

B 树
    表示的是一类树：它允许一个节点可以有多于两个子节点，同时，也是自平衡的，叶子节点的高度都是相同的。
    所以，为了更好地区分一颗 B 树到底属于哪一类树，我们给它一个新的属性：阶数（Order）：
    阶数是一个节点最多能有多少箭头指向其他节点。具有阶为 3 的 B 树，表示一个节点最多有三个子节点，
    也就是 2-3 树的定义。具有阶为 4 的 B 树，表示一个节点最多有四个子节点，也就是 2-3-4 树的定义。

红黑树
    满足下面5个条件的平衡二叉搜索树，为红黑树
        1. 每个节点要么是黑色，要么是红色
        2. 根节点是黑色
        3. 每个叶子节点（NIL）是黑色，注意：这里叶子节点，是指为空（NIL 或NULL）的叶子节点（在Java中，叶子结点是为null的结点）
        4. 如果一个节点是红色的，则它的子节点必须是黑色的；也就是说在一条路径上不能出现相邻的两个红色结点。
        5. 从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。

    类似二分查找，效率高，平均情况优良，插入和删除过程，不需要频繁的平衡操作


红黑树的算法讲解参考：https://www.bilibili.com/video/BV1BB4y1X7u3
TreeMap红黑数的具体实现参考：https://blog.csdn.net/chenssy/article/details/26668941

 */

// TreeMap的底层是通过红黑树算法来实现的
public class TreeMapDemo {


    public static void main(String[] args) {

    }

}