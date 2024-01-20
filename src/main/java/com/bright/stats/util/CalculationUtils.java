package com.bright.stats.util;


import lombok.extern.slf4j.Slf4j;

/**
 * <p> Project: stats - CalculationUtils </p>
 *
 * 计算器类
 * @author Tz
 * @date 2024/01/20 23:45
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class CalculationUtils {

    /**
     * 栈值
     */
    private int num;
    /**
     * 存储栈值
     */
    private double[] sk1;
    /**
     * 存储符号
     */
    private char[] sk2;
    /**
     * 存储栈顶值，默认为-1
     */
    private int top = -1;
    /**
     * 存储栈顶值，默认为-1
     */
    private int top2 = -1;

    public CalculationUtils(int num) {
        this.num = num;
        sk1 = new double[num];
        sk2 = new char[num];
    }

    public static boolean isNumber(Character number) {
        return (number <= 57 && number >= 48) || number == '.';
    }

    /**
     * 计算表达式的值
     * @param expression
     * @return
     */
    public static Double calculation(String expression) {

        //  \\s+匹配任意空白字符
        String express = expression.replaceAll("\\s+", "");

        log.info("导入excel计算的表达式为：{}", expression);


        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (!(isNumber(c)
                    || "+".equals(c + "")
                    || "-".equals(c + "")
                    || "*".equals(c + "")
                    || "/".equals(c + "")
                    || "=".equals(c + "")
                    || "(".equals(c + "")
                    || ")".equals(c + "")
                    || ".".equals(c + "")
            )) {
                //结束程序
                return (double) 0;
            }
        }

        //创建两个栈，一个存放数据，一个存放符号
        CalculationUtils calculationUtils = new CalculationUtils(expression.length());

        //定义遍历辅助值
        //遍历字符串，相当图中指针
        int tem = 0;
        double tem1 = 0;
        double tem2 = 0;

        //将负数转换为正数
        boolean i = false;
        //区分(-1+2) 和 (1-2)
        boolean j = false;
        //返回值，暂存数据
        double res = 0;
        //存放扫描所得的数字
        char ch;
        //多位数使用
        String total = "";

        while (true) {
            //将字符串第一位转化为字符
            ch = express.substring(tem, tem + 1).charAt(0);
            //特判，以免第一位就是符号，如 -1+2
            if (ch == '-') {
                i = true;
                j = true;
            }

            //判断是否为符号和()
            if (calculationUtils.isOperation(ch)) {
                //栈顶为(,且下一个符号为-
                if (calculationUtils.isCharEmpty() || (calculationUtils.peek() == '(' && j)) {
                    //特判符号
                    if (i) {
                        calculationUtils.push(0);
                    }
                    calculationUtils.pushChar(ch);
                } else {
                    if (ch == ')') {
                        calculationUtils.clear();
                    } else if (ch == '(') {
                        calculationUtils.pushChar(ch);
                    } else if (calculationUtils.pri(ch) > calculationUtils.pri(calculationUtils.peek())) {
                        //优先级大于，直接入栈
                        calculationUtils.pushChar(ch);
                    } else {
                        //优先级小于，运算
                        tem1 = calculationUtils.pop();
                        tem2 = calculationUtils.pop();
                        //计算函数
                        res = calculationUtils.cla(tem1, tem2, calculationUtils.popChar());
                        calculationUtils.push(res);
                        calculationUtils.pushChar(ch);
                    }
                }
            } else {
                //为数据

                //多位数，小数拼接
                total += ch;
                //如果到最后一位
                if (tem >= express.length() - 1) {
                    calculationUtils.push(Double.parseDouble(total));
                } else {
                    if (calculationUtils.isOperation(express.substring(tem + 1, tem + 2).charAt(0))) {
                        calculationUtils.push(Double.parseDouble(total));
                        j = false;
                        i = false;
                        total = "";
                    }
                }

            }

            //向后遍历
            tem++;
            if (tem >= express.length()) {
                break;
            }
        }
        //当遍历完成后，对栈中剩存的数据和符号操作
        calculationUtils.clear();
        return calculationUtils.pop();
    }


    /**
     * 清除数据(到结束或者到'('处)
     */
    public void clear() {
        while (true) {
            if (isCharEmpty()) {
                break;
            }

            if (peek() == '(') {
                popChar();
                break;
            }

            double k = 0;
            k = cla(pop(), pop(), popChar());
            push(k);
        }
    }

    /**
     * 判断是否为空
     * @return
     */
    public boolean isEmpty() {
        return top == -1;
    }

    /**
     * 判断符号是否为空
     * @return
     */
    public boolean isCharEmpty() {
        return top2 == -1;
    }

    /**
     * 判断是否为满
     * @return
     */
    public boolean isFull() {
        return top == num - 1;
    }

    /**
     * 入栈
     * @param i
     */
    public void push(double i) {
        if (isFull()) {
            log.error("栈满~~");
            return;
        }

        top++;
        sk1[top] = i;
    }

    /**
     * 入栈(符号)
     * @param i
     */
    public void pushChar(char i) {
        if (isFull()) {
            log.error("栈满~~");
            return;
        }

        top2++;
        sk2[top2] = i;
    }

    /**
     * 查看栈顶符号
     * @return
     */
    public char peek() {
        return sk2[top2];
    }

    /**
     * 出栈
     * @return
     */
    public double pop() {
        if (isEmpty()) {
            return 0;
        }

        top--;
        return sk1[top + 1];
    }

    /**
     * 弹出符号
     * @return
     */
    public char popChar() {
        top2--;
        return sk2[top2 + 1];
    }


    /**
     * 返回优先级
     * @param a
     * @return
     */
    public int pri(int a) {
        if (a == '+' || a == '-') {
            return 0;
        }
        if (a == '*' || a == '/') {
            return 1;
        }
        if (a == '(') {
            return -1;
        }

        return -1;
    }

    /**
     * 判断是否为运算符
     * @param i
     * @return
     */
    public boolean isOperation(int i) {
        return i == '+' || i == '-' || i == '*' || i == '/' || i == '(' || i == ')';
    }

    /**
     * 运算方式
     * @param i
     * @param j
     * @param k
     * @return
     */
    public double cla(double i, double j, int k) {
        double res = 0;
        switch (k) {
            case '+':
                res = i + j;
                break;

            case '-':
                res = j - i;
                break;

            case '*':
                res = i * j;
                break;

            case '/':
                res = j / i;
                break;

            default:
                break;
        }

        return res;
    }

}