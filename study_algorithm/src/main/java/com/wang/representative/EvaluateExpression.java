package com.wang.representative;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * 求解算术表达式的值
 */
public class EvaluateExpression {

    public static void main(String[] args) {
        EvaluateExpression evaluateExpression = new EvaluateExpression();
        // 3
        String expression = "1 + 2 ";
        System.out.println(evaluateExpression.calcualte(expression));

        // 8
        expression = "4 + 2 * 3 - 10 / 5";
        System.out.println(evaluateExpression.calcualte(expression));

        // 26
        expression = "(1+2) * (4 + 5) - (9 / 7)";
        System.out.println(evaluateExpression.calcualte(expression));

        // -14
        expression = "(1 + (3 * (4 - 9)))";
        System.out.println(evaluateExpression.calcualte(expression));

        // 1
        expression = "(1 + (3 * (4 - 9))) + (3 * (2 + 3))";
        System.out.println(evaluateExpression.calcualte(expression));
    }

    // 运算符优先级关系表
    private Map<String, Map<String, String>> priorityMap = new HashMap<>();
    private Stack<String> optStack = new Stack<String>();
    // 运算符栈
    private Stack<Double> numStack = new Stack<Double>();
    // 操作数栈
    /**
     * 计算表达式
     * @param exp 四则运算表达式, 每个符号必须以空格分隔
     * @return
     */
    public double calcualte(String exp) {
        exp = exp + "&";
        StringTokenizer st = new StringTokenizer(exp);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            process(token);
        }
        return numStack.pop();
    }
    /**
     * 读入一个符号串。
     * 如果是数字，则压入numStack
     * 如果是运算符，则将optStack栈顶元素与该运算符进行优先级比较
     * 如果栈顶元素优先级低，则将运算符压入optStack栈,如果相同，则弹出左括号，如果高，则取出２个数字，取出一个运算符执行计算，然后将结果压入numStack栈中
     * @param token
     */
    private void process(String token) {
        while (!"#".equals(optStack.peek()) || !token.equals("#")) {
            // token is numeric
            if (isNumber(token)) {
                numStack.push(Double.parseDouble(token));
                break;
                // token is operator
            } else {
                String priority = priority(optStack.peek(), token);
                if ("<".equals(priority)) {
                    optStack.push(token);
                    break;
                } else if ("=".equals(priority)) {
                    optStack.pop();
                    break;
                } else {
                    double res = calculate(optStack.pop(), numStack.pop(), numStack.pop());
                    numStack.push(res);
                }
            }
        }
    }
    /**
     * 执行四则运算
     * @param opt
     * @param n1
     * @param n2
     * @return
     */
    private double calculate(String opt, double n1, double n2) {
        if ("+".equals(opt)) {
            return n2 + n1;
        } else if ("-".equals(opt)) {
            return n2 - n1;
        } else if ("*".equals(opt)) {
            return n2 * n1;
        } else if ("/".equals(opt)) {
            return n2 / n1;
        } else {
            throw new RuntimeException("unsupported operator:" + opt);
        }
    }
    /**
     * 检查一个String是否为数字
     * @param token
     * @return
     */
    private boolean isNumber(String token) {
        int length = token.length();
        for (int i = 0 ; i < length ; i++) {
            char ch = token.charAt(i);
            // 跳过小数点
            if (ch == '.') {
                continue;
            }
            if (!isNumber(ch)) {
                return false;
            }
        }
        return true;
    }
    /**
     * 检查一个字符是否为数字
     * @param ch
     * @return
     */
    private boolean isNumber(char ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        return false;
    }
    /**
     * 查表得到op1和op2的优先级
     * @param op1 运算符1
     * @param op2 运算符2
     * @return ">", "<" 或 "="
     */
    public String priority(String op1, String op2) {
        return priorityMap.get(op1).get(op2);
    }
    /**
     * 构造方法，初始化优先级表
     */
    public EvaluateExpression() {
        // 开始运算符
        optStack.push("#");
        // 初始化优先级
        // +
        Map<String, String> subMap = new HashMap<String, String>();
        subMap.put("+", ">");
        subMap.put("-", ">");
        subMap.put("*", "<");
        subMap.put("/", "<");
        subMap.put("(", "<");
        subMap.put(")", ">");
        subMap.put("#", "<");
        priorityMap.put("+", subMap);
        // -
        subMap = new HashMap<String, String>();
        subMap.put("+", ">");
        subMap.put("-", ">");
        subMap.put("*", "<");
        subMap.put("/", "<");
        subMap.put("(", "<");
        subMap.put(")", ">");
        subMap.put("#", "<");
        priorityMap.put("-", subMap);
        // *
        subMap = new HashMap<String, String>();
        subMap.put("+", ">");
        subMap.put("-", ">");
        subMap.put("*", ">");
        subMap.put("/", ">");
        subMap.put("(", "<");
        subMap.put(")", ">");
        subMap.put("#", "<");
        priorityMap.put("*", subMap);
        // /
        subMap = new HashMap<String, String>();
        subMap.put("+", ">");
        subMap.put("-", ">");
        subMap.put("*", ">");
        subMap.put("/", ">");
        subMap.put("(", "<");
        subMap.put(")", ">");
        subMap.put("#", "<");
        priorityMap.put("/", subMap);
        // (
        subMap = new HashMap<String, String>();
        subMap.put("+", "<");
        subMap.put("-", "<");
        subMap.put("*", "<");
        subMap.put("/", "<");
        subMap.put("(", "<");
        subMap.put(")", "=");
        subMap.put("#", "<");
        priorityMap.put("(", subMap);
        // )
        subMap = new HashMap<String, String>();
        subMap.put("+", ">");
        subMap.put("-", ">");
        subMap.put("*", ">");
        subMap.put("/", ">");
        subMap.put("(", "=");
        // 出现这种情况，说明表达式不合法
        //subMap.put(")", "<");
        subMap.put("#", "<");
        priorityMap.put(")", subMap);
        // #
        subMap = new HashMap<String, String>();
        subMap.put("+", "<");
        subMap.put("-", "<");
        subMap.put("*", "<");
        subMap.put("/", "<");
        subMap.put("(", "<");
        // 出现这种情况，说明表达式不合法
        //subMap.put(")", "<");
        subMap.put("#", "=");
        priorityMap.put("#", subMap);

        // &
        subMap = new HashMap<>();
        subMap.put("+", ">");
        subMap.put("-", ">");
        subMap.put("*", ">");
        subMap.put("/", ">");
        //subMap.put("(", "<");
        // 出现这种情况，说明表达式不合法
        //subMap.put(")", "<");
        subMap.put("#", "=");
        priorityMap.put("&", subMap);
    }
}
