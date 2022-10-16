package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    // 区间集合 [8,9]  [1,3] [2,6]

    public static void main(String[] args) {
        List<List<Integer>> lists = new ArrayList<>(Arrays.asList(Arrays.asList(1, 3), Arrays.asList(2, 6), Arrays.asList(8, 9)));
        System.out.println(merge(lists));
    }


    public static List<List<Integer>> merge(List<List<Integer>> lists) {

        // 1 2 8
        // 1 -  [1,3]  2  [2,6]    8 [8,9]

        // 1 --3 -- 2---6  ---8

        if (null == lists || lists.size() < 2) {
            return lists;
        }

        List<Integer> firstNums = new ArrayList<>();
        Map<Integer, List<Integer>> firstNumEndNumMap = new HashMap<>();

        // 根据第一个元素排序
        lists.sort(new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> o1, List<Integer> o2) {
                return Integer.compare(o1.get(0), o2.get(0));
            }
        });
        for(List<Integer> nums : lists) {
            firstNums.add(nums.get(0));
            firstNumEndNumMap.put(nums.get(0), nums);
        }

        // 查找元素区间
        int startIndex = 0;
        int firstNum = firstNums.get(startIndex);
        int endNum = firstNumEndNumMap.get(firstNum).get(1);
        List<List<Integer>> results = new ArrayList<>();
        while(startIndex < firstNums.size()) {
            if (startIndex == firstNums.size() - 1) {
                results.add(Arrays.asList(firstNumEndNumMap.get(firstNums.get(startIndex)).get(0), endNum));
                break;
            }

            firstNum = firstNums.get(startIndex + 1);
            if (endNum < firstNum) {
                results.add(Arrays.asList(firstNumEndNumMap.get(firstNums.get(startIndex)).get(0), endNum));
            }
            startIndex++;
            endNum = firstNumEndNumMap.get(firstNum).get(1);
        }

        return results;
    }


}
