package com.wang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HelloWorld {


    public static void main(String[] args) throws IOException {
        System.out.println("---------------------");

        HelloWorld helloWorld = new HelloWorld();
        System.out.println(helloWorld.hashCode());
        System.out.println(helloWorld);

        List<Character>[] chars = new List[10];
        chars[0] = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        System.out.println(line);

        Scanner scanner = new Scanner(System.in);
        line = scanner.nextLine();
        System.out.println(line);
    }

}
