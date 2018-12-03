package com.project.csc440.tm;

//import java.util.Date;

import java.util.Date;

public class Task {

    private String name;
    private Long dueDate; // It is the timestamp since midnight 1/1/1970 in UTC
    private String details;

    public Task() {}

    public Task(String name, String details, Long dueDate) {
        this.name = name;
        this.details = details;
        this.dueDate = dueDate;
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

}