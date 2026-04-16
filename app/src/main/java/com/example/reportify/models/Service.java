package com.example.reportify.models;

public class Service {

    private String name;
    private int icon;

    public Service(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() { return name; }
    public int getIcon() { return icon; }
}
