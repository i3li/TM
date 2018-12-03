package com.project.csc440.tm;

//import java.util.Date;

import java.util.Date;

public class Task {

    private String name;
    private Long dueDate; // It is the number of milliseconds since the epoch 'January 1, 1970, 00:00:00 GMT'
    private String details;
    private String owner;

    public Task() {}

    Task(String name, String details, Long dueDate, String owner) {
        this.name = name;
        this.details = details;
        this.dueDate = dueDate;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public String getDetails() {
        return details;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}