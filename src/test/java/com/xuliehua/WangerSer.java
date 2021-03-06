package com.xuliehua;

import java.io.Serializable;

/**
 * 1）一旦变量被transient修饰，变量将不再是对象持久化的一部分，该变量内容在序列化后无法获得访问。
 * 2）transient关键字只能修饰变量，而不能修饰方法和类。注意，本地变量是不能被transient关键字修饰的。变量如果是用户自定义类变量，则该类需要实现Serializable接口。
 * 3）被transient关键字修饰的变量不再能被序列化，一个静态变量不管是否被transient修饰，均不能被序列化。
 *
 * @author zhaoxuedui <zhaoxuedui@kuaishou.com>
 * Created on 2020-03-13
 * @Description
 */
public class WangerSer implements Serializable {
    private static final long serialVersionUID = 1315093194846591416L;

    private String name;
    public static String add;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "WangerSer{" + "name=" + name + ",age=" + age + ",add=" + add + "}";
    }
}
