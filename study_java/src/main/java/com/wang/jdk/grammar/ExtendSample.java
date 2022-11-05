package com.wang.jdk.grammar;

import org.junit.jupiter.api.Test;

/**
 * 继承相关的
 */
public class ExtendSample {

    @Test
    public void fieldValueDemo() {
        Father f = new Son();
        // 输出 0, 30, 20
        // 先初始化好父类的构造函数，父类中调用了print方法，被子类覆盖了，所以会调用子类的，
        // 但是子类这是x还没有初始化，只进行到了链接阶段，有初始值0
        // 调用完父类构造器后，父类的x值为20了
        // 然后调用子类的构造器（字节码中的init）方法，此时进行初始化，x值设置为30了，所以打印出30
        // 调用完子类的构造器后，子类的x值为40了
        // 然后执行下面的初始，但是由于字段不具备动态性，因此输出的是Father中的字段x的值20
        System.out.println(f.x); // 20
        System.out.println(((Son)f).x); // 40
    }

    class Father {
        int x = 10;

        public Father() {
            this.print();
            x = 20;
        }

        public void print() {
            System.out.println("Father.x = " + x);
        }
    }
    class Son extends Father {
        int x = 30;

        public Son() {
            this.print();
            x = 40;
        }

        public void print() {
            System.out.println("Son.x = " + x);
        }
    }
}
