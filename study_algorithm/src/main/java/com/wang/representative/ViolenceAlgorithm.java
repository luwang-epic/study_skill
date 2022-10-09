package com.wang.representative;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 暴力求解
 *  又得可以改为动态规划，有的不行，一般看状态是否重复
 */
public class ViolenceAlgorithm {

    /**
     * 字符串排列组合
     */
    @Test
    public void permutationStringDemo() {
        String str = "abc";
        List<String> results = new ArrayList<>();
        permutationString(str.toCharArray(), 0, results);
        System.out.println(results);

        str = "aac";
        results.clear();
        permutationString(str.toCharArray(), 0, results);
        System.out.println(results);
    }

    /*
     题目：给定一个整型数组arr，代表数值不同的纸牌排成一条线。玩家A和玩家B依次拿走每张纸牌，规定玩家A先拿，
        玩家B后拿，但是每个玩家每次只能拿走最左或最右的纸牌，玩家A和玩家B都绝顶聪明。请返回最后获胜者的分数。
    【举例】
    arr=[1,2,100,4]。 开始时，玩家A只能拿走1或4。如果开始时玩家A拿走1，则排列变为[2,100,4]，
    接下来玩家B可以拿走2或4，然后继续轮到玩家A，如果开始时玩家A拿走4，则排列变为[1,2,100]，
    接下来玩家B可以拿走1或100，然后继续轮到玩家A，玩家A作为绝顶聪明的人不会先拿4，因为拿4之后，玩家B将拿走100。
    所以玩家A会先拿1，让排列变为[2,100,4]，接下来玩家B不管怎么选，100都会被玩家A拿走。玩家A会获胜，分数为101。所以返回101。
    arr=[1,100,2]。开始时，玩家A不管拿1还是2，玩家B作为绝顶聪明的人，都会把100拿走。
    玩家B会获胜，分数为100。所以返回100。
     */
    @Test
    public void cardProblemDemo() {
        int[] cards = new int[] {1, 2, 100, 4};
        System.out.println(cardProblem(cards));
        System.out.println(cardProblemWithDp(cards));
    }

    /*
    n皇后问题是将n个皇后放置在n*n的棋盘上，皇后彼此之间不能相互攻击(任意两个皇后不能位于同一行，同一列，同一斜线)。
    给定一个整数n，返回所有不同的n皇后问题的解决方案个数。
     */
    @Test
    public void nQueenDemo() {
        System.out.println(nQueen(3));
        System.out.println(nQueen( 4));
        System.out.println(nQueen(5));
    }

    /**
     * 打印字符串的所有排列
     * @param chars 在[i..]范围上，所有的字符，都可以放到i位置上，后续都快去尝试
     *              在[0..i-1]范围上，是之前做的选择
     * @param results 所有组合的结果
     */
    public void permutationString(char[] chars, int index, List<String> results) {
        if (index == chars.length) {
            results.add(String.valueOf(chars));
        }

        Set<Character> visitedSet = new HashSet<>();
        for (int i = index; i < chars.length; i++) {
            // 加入到已经访问的列表中，再有重复字符串的时候有用，
            // 如果字符串中都没有重复字符串，不需要这个集合的，还有一种办法就是先返回所有的，然后再去重
            if (visitedSet.contains(chars[i])) {
                continue;
            }
            visitedSet.add(chars[i]);

            char temp = chars[i];
            chars[i] = chars[index];
            chars[index] = temp;

            permutationString(chars, index + 1, results);

            // 交换回来
            temp = chars[i];
            chars[i] = chars[index];
            chars[index] = temp;
        }
    }


    /**
     * 暴力算法  纸牌问题求解
     * @param cards 纸牌
     * @return 最大的分数
     */
    public int cardProblem(int[] cards) {
        if (null == cards || cards.length == 0) {
            return 0;
        }

        // 返回先拿和后拿的最终分数，取最大的
        return Math.max(cardWithFirstHand(cards, 0, cards.length - 1),
                cardWithBackhand(cards, 0, cards.length - 1));
    }

    /**
     * 动态规划 纸牌问题求解
     * @param cards 纸牌
     * @return 最大的分数
     */
    public static int cardProblemWithDp(int[] cards) {
        if (cards == null || cards.length == 0) {
            return 0;
        }

        int[][] firstHandMap = new int[cards.length][cards.length];
        int[][] backHandMap = new int[cards.length][cards.length];
        for (int i = 0; i < cards.length; i++) {
            firstHandMap[i][i] = cards[i];
        }

        for (int startColumn = 1; startColumn < cards.length; startColumn++) {
            // 左边拿
            int left = 0;
            // 右边拿
            int right = startColumn;
            while (right < cards.length) {
                firstHandMap[left][right] = Math.max(cards[left] + backHandMap[left + 1][right], cards[right] + backHandMap[left][right - 1]);
                backHandMap[left][right] = Math.min(firstHandMap[left + 1][right], firstHandMap[left][right - 1]);
                left++;
                right++;
            }
        }
        return Math.max(firstHandMap[0][cards.length - 1], backHandMap[0][cards.length - 1]);
    }

    /**
     * A先手拿牌
     * @param cards 纸牌数组
     * @param start 开始下标
     * @param end 结束下标
     * @return 返回玩家分数
     */
    public int cardWithFirstHand(int[] cards, int start, int end) {
        // 最后一行了，只能拿这张牌
        if (start == end) {
            return cards[start];
        }

        // 否则两张牌都尝试，返回最大的分数;
        // 先手之后就变成后手了
        int left = cards[start] + cardWithBackhand(cards, start + 1, end);
        int right = cards[end] + cardWithBackhand(cards, start, end - 1);
        // 使用最大的一种情况
        return Math.max(left, right);
    }

    /**
     * A后手拿牌
     * @param cards 纸牌数组
     * @param start 开始下标
     * @param end 结束下标
     * @return 返回玩家分数
     */
    public int cardWithBackhand(int[] cards, int start, int end) {
        // 后手拿牌，B拿走后没有牌了，因此对A来说分数为0
        if (start == end) {
            return 0;
        }

        // B拿走一张牌，两张牌都尝试，那么A只能从剩余的牌中获取
        int left = cardWithFirstHand(cards, start + 1, end);
        int right = cardWithFirstHand(cards, start, end - 1);
        // AB都是绝顶聪明的玩家，因此B会让A拿到最小的分数
        return Math.min(left, right);
    }

    /**
     * n皇后问题求解
     * @param n 个数
     * @return 方案数
     */
    public int nQueen(int n) {
        if (n < 1) {
            return 0;
        }

        // 记录第i行的皇后，放在第几列
        int[] records = new int[n];
        for (int i = 0; i < records.length; i++) {
            records[i] = -1;
        }
        return nQueen(records, 0);
    }

    /**
     * 前index个皇后已经放好了，第index皇后如何放
     * @param records 皇后放的位置
     * @param index 前面index个放好了
     * @return 方案数
     */
    public int nQueen(int[] records, int index) {
        // 放到最后，返回可新方案
        if (index == records.length) {
            return 1;
        }

        int sum = 0;
        for (int i = 0; i < records.length; i++) {
            // 当前index行的皇后，放在i列，会不会和之前(0..index-1)的皇后 共行，共列或者共斜线
            // 如果是，无线，否则，有效  可以放置皇后
            if (isValidPosition(records, index, i)) {
                // 不需要报错状态的，因为都是向前推进的，前面的值改了，对后面的不影响，
                // 因为在isValidPosition中只需要判断前index即可
                //int temp = records[index];
                records[index] = i;
                sum += nQueen(records, index + 1);
                //records[index] = temp;
            }
        }
        return sum;
    }

    /**
     * 皇后是否可以放到pos位置
     * @param records 已经放置的位置
     * @param index 需要放置第index行皇后
     * @param pos 需要放到的位置
     * @return 是否可以放置在pos位置上
     */
    private boolean isValidPosition(int[] records, int index, int pos) {
        // record[0..index-1]位置需要看，后面的位置不需要看
        for (int i = 0; i < index; i++) {
            // 共列
            if (records[i] == pos) {
                return false;
            }
            // 由迭代保证不共行

            // 共斜线，x坐标和y坐标差一样
            if (Math.abs(records[i] - pos) == Math.abs(i - index)) {
                return false;
            }
        }
        return true;
    }
}
