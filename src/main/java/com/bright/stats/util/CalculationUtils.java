package com.bright.stats.util;

import java.util.Stack;

public class CalculationUtils {

    private static Stack<Character> chars;
    private static Stack<Double> result;

    public static Double calculation(String expression) {
        expression = expression == null ? "" : expression + "=";
        //校验 （）匹配
        Stack<Character> brackets = new Stack<>();
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
                return (double) -1;
            }
            if ("(".equals(c + "")) {
                brackets.push(c);
            }
            if (")".equals(c + "")) {
                if (brackets.empty() || !"(".equals(brackets.pop() + "")) {
                    //结束程序
                    return (double) -1;
                }
            }
        }
        if (!brackets.isEmpty()) {
            //结束程序
            return (double) -1;
        }
        chars = new Stack<>();
        result = new Stack<>();
        //缓存多位操作数
        StringBuilder stringBuffer = new StringBuilder();
        // 计算
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (isNumber(c)) {
                stringBuffer.append(c);
            } else {
                //加入数字
                if (!"".equals(stringBuffer.toString())) {
                    result.push(Double.parseDouble(stringBuffer.toString()));
                    stringBuffer = new StringBuilder();
                }
                // 栈顶有符号 且c的优先级小于等于栈顶的优先级 一直计算
                while (!chars.isEmpty() && priority(c)) {
                    Double after = result.pop();
                    Double before = result.pop();
                    char operation = chars.pop();
                    switch (operation) {
                        case '+':
                            result.push(before + after);
                            break;
                        case '-':
                            result.push(before - after);
                            break;
                        case '*':
                            result.push(before * after);
                            break;
                        case '/':
                            result.push(before / after);
                            break;
                        default:
                    }
                }
                if (!"=".equals(c + "")) {
                    chars.push(c);
                    if (")".equals(c + "")) {
                        chars.pop();
                        chars.pop();
                    }
                }
            }
        }
        //返回计算结果
        return result.pop();
    }

    private static boolean priority(char c) {
        if ("(".equals(chars.peek() + "")) {
            return false;
        }
        //c 需要入符号栈
        char peek = chars.peek();
        switch (c) {
            case '.':
            case '+':
            case '=':
            case '-':
            case ')':
                //不入栈 去计算
                return true;
            case '*':
            case '/': {
                if (peek == '+' || peek == '-') {
                    return false;
                }
                return true;
            }
            case '(':
                return false;
            default:
        }

        return false;
    }

    public static boolean isNumber(Character number) {
        return (number <= 57 && number >= 48) || number == '.';
    }
}