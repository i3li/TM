package com.project.csc440.tm;

public class Group {

    private String name;
    private String description;
    private String admin;

    public Group() {}

    public Group(String name, String description, String admin) {
        this.name = name;
        this.description = description;
        this.admin = admin;
    }

    public Group(String name, String admin) {
        this.name = name;
        this.description = "";
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

}