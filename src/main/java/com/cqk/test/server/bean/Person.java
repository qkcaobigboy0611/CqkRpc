/**
 * @author qkcao
 * @date 2023/7/26 15:34
 */
package com.cqk.test.server.bean;

public class Person {
    private int age;
    private String name;
    public Person(){}
    public Person(int age, String name) {
        this.age = age;
        this.name = name;
    }
    /**
     * @return the age
     */
    public int getAge() {
        return age;
    }
    /**
     * @param age the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
