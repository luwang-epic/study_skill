package com.wang;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelloWorld {


    public static void main(String[] args) {
        System.out.println("---------------------");

        HelloWorld helloWorld = new HelloWorld();
        System.out.println(helloWorld.hashCode());
        System.out.println(helloWorld);

        List<String> strs = new ArrayList<>();
        strs.add("a");
        strs.add("b");

        List<String> results = strs.stream().distinct().collect(Collectors.toList());
        System.out.println(results);
    }

}
