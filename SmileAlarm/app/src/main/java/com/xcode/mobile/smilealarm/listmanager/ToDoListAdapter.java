package com.xcode.mobile.smilealarm.listmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.xcode.mobile.smilealarm.R;

/**
 * Created by an.nguyen on 11/17/2015.
 */
public class ToDoListAdapter extends ArrayAdapter<Task> {

    private Context c;
    private ArrayList<Task> tasks;

    public ToDoListAdapter(Context context, int textViewResourceId, ArrayList<Task> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        this.tasks = new ArrayList<Task>();
        this.tasks.addAll(objects);
    }

    @Override
    public Task getItem(int i) {
        return tasks.get(i);
    }

    @SuppressLint({"DefaultLocale", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(c);
            convertView = vi.inflate(R.layout.todo_list, null);

            holder = new ViewHolder();
            holder.task = (TextView) convertView.findViewById(R.id.task);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Task task = tasks.get(position);
        String s = task.getTask();
        if(task.getTime() == null) {
            holder.task.setText(s.toUpperCase().charAt(0) + s.substring(1));
        }
        else {
            holder.task.setText(task.getTime() + ". " + s.toUpperCase().charAt(0) + s.substring(1));
        }
        return convertView;
    }

    private class ViewHolder {
        TextView task;
    }

}
