package com.staticstu.study;

/**
 * @author badBoy
 * @create 2019-11-11
 */
public class Father extends Super {

    public static int m = 33;

    static {
        System.out.println("执行了Father静态语句块 m = " + m);
    }

}
