package com.witcherbb.test;

public record Example(int age, String name, Farther farther) {
    @Override
    public String toString() {
        return "Example{" +
                "age=" + age +
                ", name='" + name + '\'' +
                ", farther=" + farther +
                '}';
    }
}
