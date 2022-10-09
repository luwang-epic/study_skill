package com.wang.jvm.garbage;

import java.lang.ref.SoftReference;

public class SoftReferenceGcTest {

    private static class SoftObject {
        private String name;
        private byte[] bytes;

        public SoftObject(String name, int size) {
            this.name = name;
            this.bytes = new byte[1024 *1024 * size];
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println(name + "对象被回收 ---> " + Runtime.getRuntime().freeMemory() / (1024 * 1024));
        }
    }

    public static void main(String[] args) {
        // 如果空间不够，会抛OutOfMemoryError错误
        SoftReference<SoftObject> softReference1 = new SoftReference<>(new SoftObject("a", 1));
        SoftReference<SoftObject> softReference2 = new SoftReference<>(new SoftObject("b", 2));
        SoftReference<SoftObject> softReference3 = new SoftReference<>(new SoftObject("c", 3));
        SoftReference<SoftObject> softReference4 = new SoftReference<>(new SoftObject("d", 10));
        System.out.println(Runtime.getRuntime().totalMemory() / (1024 * 1024));
        System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024));

        // 这里存储不够了，不一定会 一起被回收的，有时候只回收a,b，有时候回收a,b,c，有时候a,b,c,d都回收
        byte[] bytes = new byte[1024 * 1024 * 2];
        System.out.println(Runtime.getRuntime().totalMemory() / (1024 * 1024));
        System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024));
    }
}
