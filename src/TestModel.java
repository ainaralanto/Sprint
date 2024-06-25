package model;

import mg.p16.Annotationcontroller.FieldParam;

public class TestModel {
    @FieldParam(name = "name")
    private String name;

    @FieldParam(name = "age")
    private int age;

    // Getters et setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}