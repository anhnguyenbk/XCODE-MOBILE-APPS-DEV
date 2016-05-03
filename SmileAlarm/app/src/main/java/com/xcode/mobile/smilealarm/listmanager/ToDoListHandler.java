package com.xcode.mobile.smilealarm.listmanager;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.xcode.mobile.smilealarm.DataHelper;

/**
 * Created by an.nguyen on 11/17/2015.
 */
public class ToDoListHandler {
    public ArrayList<Task> getToDoList(boolean toView) {
        ArrayList<Task> todoList;
        Calendar c = Calendar.getInstance();
        String nextDate = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
        c.add(Calendar.DAY_OF_MONTH, 1);
        String currentDate = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);

        if (toView) {
            todoList = getToDoListFrom(currentDate);
        } else {
            todoList = getToDoListFrom(nextDate);
        }
        return todoList;
    }

    public void saveToDoList(ArrayList<Task> todoList) {
        DataHelper.getInstance().SaveToDoListToData(todoList);
    }

    private ArrayList<Task> getToDoListFrom(String date) {
        ArrayList<Task> todoListData = (ArrayList<Task>) DataHelper.getInstance().GetCurrentToDoListFromData();
        if (todoListData == null || todoListData.size() == 0 || todoListData.get(0).getDate().compareTo(date) != 0) {
            return new ArrayList<Task>();
        }

        todoListData = sortTodoList(todoListData);
        return todoListData;
    }

    @SuppressLint("SimpleDateFormat")
    private ArrayList<Task> sortTodoList(ArrayList<Task> unsortTodoList) {
        ArrayList<Task> todoList = new ArrayList<Task>();
        Task t0 = unsortTodoList.get(0);
        todoList.add(t0);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
        Date time0 = new Date();
        try {
            time0 = timeFormat.parse(t0.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date time1 = new Date();
        int size = unsortTodoList.size();
        for (int i = 1; i < size; i++) {
            Task t1 = unsortTodoList.get(i);
            try {
                time1 = timeFormat.parse(t1.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (time0.compareTo(time1) > 0) {
                todoList.remove(t0);
                todoList.add(t1);
                if (i == size - 1) {
                    todoList.add(t0);
                    break;
                }
            } else {
                todoList.add(t1);
            }
            t0 = t1;
            time0 = time1;
        }
        return todoList;
    }

}
