package com.wang.webpage;

import java.util.ArrayList;
import java.util.List;

/**

 我们需要打印数组中的所有 leader。Leader 元素大于它右侧的所有元素。
     arr[]={14, 12, 70, 15, 99, 65, 21, 90}
     Here 99 and 90 are leader elements（99和90是leader元素）

 * @Author: wanglu51
 * @Date: 2023/8/4 14:03
 */
public class FindArrayLeader {

    public static void main(String[] args) {
        int[] arrs = {14, 12, 70, 15, 99, 65, 21, 90};
        System.out.println(findLeaders(arrs));
    }

    public static List<Integer> findLeaders(int[] arrs) {
        List<Integer> leaders = new ArrayList<>();
        if (null == arrs || arrs.length == 0) {
            return leaders;
        }

        int max = arrs[arrs.length - 1];
        leaders.add(max);
        for (int i = arrs.length - 2; i >= 0; i--) {
            if (arrs[i] > max) {
                max = arrs[i];
                leaders.add(max);
            }
        }
        return leaders;
    }

}
