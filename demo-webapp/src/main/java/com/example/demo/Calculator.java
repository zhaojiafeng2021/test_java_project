package com.example.demo;

/** 计算器业务：只在页面点击"计算"时被调用 */
public class Calculator {

    public double calculate(double a, double b, String op) {
        switch (op) {
            case "add":
                return a + b;
            case "sub":
                return a - b;
            case "mul":
                return a * b;
            case "div":
                if (b == 0) {
                    throw new IllegalArgumentException("除数不能为 0");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("不支持的运算: " + op);
        }
    }
}
