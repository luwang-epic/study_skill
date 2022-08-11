package com.wang.leetcode.offer;

/*
给定一个 24 小时制（小时:分钟 "HH:MM"）的时间列表，找出列表中任意两个时间的最小时间差并以分钟数表示。

 

示例 1：

输入：timePoints = ["23:59","00:00"]
输出：1
示例 2：

输入：timePoints = ["00:00","23:59","00:00"]
输出：0

 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SolutionOffer2_35 {
    public int findMinDifference(List<String> timePoints) {
        if (null == timePoints || timePoints.size() < 2) {
            return 0;
        }

        Collections.sort(timePoints);
        int min = 24 * 60;
        for(int i = 1; i < timePoints.size(); i++) {
            int diff = diff(timePoints.get(i-1), timePoints.get(i));
            if (min > diff) {
                min = diff;
            }
        }

        int diff = 24 * 60 - diff(timePoints.get(0), timePoints.get(timePoints.size() - 1));
        return Math.min(min, diff);
    }

    int diff(String time1, String time2) {
        int hourDiff = Integer.parseInt(time2.split(":")[0]) - Integer.parseInt(time1.split(":")[0]);
        int minuteDiff = Integer.parseInt(time2.split(":")[1]) - Integer.parseInt(time1.split(":")[1]);
        return 60 * hourDiff + minuteDiff;
    }

    public static void main(String[] args) {
        SolutionOffer2_35 solution = new SolutionOffer2_35();
        List<String> timePoints = new ArrayList<>(Arrays.asList("23:59","00:00"));
        System.out.println(solution.findMinDifference(timePoints));

        timePoints = new ArrayList<>(Arrays.asList("00:00","23:59","00:00"));
        System.out.println(solution.findMinDifference(timePoints));
    }
}
