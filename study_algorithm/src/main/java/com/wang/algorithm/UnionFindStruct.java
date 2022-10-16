package com.wang.algorithm;

/*
并查集是一种树型的数据结构，用于处理一些不相交集合（disjoint sets）的合并及查询问题。
并查集通常包含两种操作:
    查找(Find)：查询两个元素是否在同一个集合中
    合并(Union)：把两个不相交的集合合并为一个集合

可以应用于最小生成树算法中
岛数量问题如果并发求解，也可以使用并发集合
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

/**
 * 并查集的实现
 */
public class UnionFindStruct<T> {

    // 用户数据和内部数据映射关系
    private HashMap<T, Element<T>> elementMap;
    // 某个元素和父元素的映射关系
    private HashMap<Element<T>, Element<T>> fatherMap;
    // 集合代表元素，也就是顶部元素 和 该集合的大小 映射
    private HashMap<Element<T>, Integer> sizeMap;

    /**
     * 构造函数，传入总的元素数，刚开始一个元素为一个集合，也就是每个元素在自己的集合中
     * @param collection 总元素集合
     */
    public UnionFindStruct(Collection<T> collection) {
        elementMap = new HashMap<>();
        fatherMap = new HashMap<>();
        sizeMap = new HashMap<>();
        for (T value : collection) {
            Element<T> element = new Element<>(value);
            elementMap.put(value, element);
            // 集合的代表元素的上一个元素为自己，也就是集合的顶部元素
            fatherMap.put(element, element);
            sizeMap.put(element, 1);
        }
    }


    /**
     * 查询操作，判断a和b是否在同一个集合
     * @param a 元素
     * @param b 另一个元素
     * @return 是否在同一个集合
     */
    public boolean isSameCollection(T a, T b) {
        if (elementMap.containsKey(a) && elementMap.containsKey(b)) {
            // 如果顶部元素是一个元素，说明a和b在同一个集合中
            return findHead(elementMap.get(a)) == findHead(elementMap.get(b));
        }
        return false;
    }

    /**
     * 合并操作，将两个集合放到一起
     * @param a 一个元素
     * @param b 另一个元素
     */
    public void union(T a, T b) {
        // 不在并查集中，忽略
        if (!elementMap.containsKey(a) || !elementMap.containsKey(b)) {
            return;
        }

        Element<T> aHead = findHead(elementMap.get(a));
        Element<T> bHead = findHead(elementMap.get(b));
        // 已经在同一个集合了，不需要操作
        if (aHead == bHead) {
            return;
        }

        // 找到元素更大的集合，将小集合放到大集合中
        Element<T> big = sizeMap.get(aHead) >= sizeMap.get(bHead) ? aHead : bHead;
        Element<T> small = big == aHead ? bHead : aHead;
        // 只需要将父设置为big节点就可以了
        fatherMap.put(small, big);
        // 更新大小
        sizeMap.put(big, sizeMap.get(aHead) + sizeMap.get(bHead));
        // small不是集合代表元素了，移除
        sizeMap.remove(small);
    }

    /**
     * 给定一个元素，往上一直找，把这个元素所在的集合代表元素找出来
     * @param element 给定的元素
     * @return 返回集合的代表元素，也就是顶部元素
     */
    private Element<T> findHead(Element<T> element) {
        // 存放沿途的元素
        Stack<Element<T>> pathStack = new Stack<>();
        // 集合元素如果等于自己，说明是顶部元素了（代表元素了）
        while (element != fatherMap.get(element)) {
            pathStack.push(element);
            element = fatherMap.get(element);
        }
        // 这里进行优化，将路径上的其他节点直接挂到顶部元素下面，这样下次查找时就不需要循环查找了
        while (!pathStack.isEmpty()) {
            fatherMap.put(pathStack.pop(), element);
        }
        return element;
    }

    /**
     * 并查集中的元素，对用户数据T的封装
     * @param <T> 用户数据类型
     */
    private static class Element<T> {
        public T t;
        public Element(T t) {
            this.t = t;
        }
    }
}
