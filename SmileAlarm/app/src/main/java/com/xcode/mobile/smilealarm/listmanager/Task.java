package com.xcode.mobile.smilealarm.listmanager;

import java.io.Serializable;

/**
 * Created by an.nguyen on 11/17/2015.
 */
@SuppressWarnings("serial")
public class Task implements Serializable {
    private int id;
    private String task;
    private String time;
    private String date;

    public Task(String task, String date, String time) {
        this.task = task;
        this.time = time;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
