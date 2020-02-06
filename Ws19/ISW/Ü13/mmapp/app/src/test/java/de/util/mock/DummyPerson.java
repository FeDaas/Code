package de.util.mock;

public class DummyPerson {
    private String name;
    private int age;

    public DummyPerson(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String tellName() {
        return name;
    }

    public void changeNameTo(String name) {
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public void incrementAge() {
        ++this.age;
    }

    public void decrementAge() {
        --this.age;
    }
}