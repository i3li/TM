package com.project.csc440.tm;

//import java.util.Date;

public class Task {

    private String name;
    private String dueDate;
    private String details;

    public Task() {}

    public String getName() {
        return name;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDetails() {
        return details;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}