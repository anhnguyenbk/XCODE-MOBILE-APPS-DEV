package com.xcode.mobile.smilealarm.listmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import com.xcode.mobile.smilealarm.DateTimePickerFragment;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;

public class AddTaskActivity extends AppCompatActivity implements DateTimePickerFragment.OnTimeSetListener {
    private EditText taskET;
    private EditText timeET;
    private DateTimePickerFragment _timePicker;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        ViewHelper.setupToolbar(this);

        taskET = (EditText) findViewById(R.id.task);
        Task t = (Task) getIntent().getSerializableExtra("update");
        if (t != null) taskET.setText(t.getTask());

        _timePicker = new DateTimePickerFragment();

        timeET = (EditText) findViewById(R.id.time);
        timeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _timePicker.setView(timeET, true);
                _timePicker.show(getFragmentManager(), "timePicker");
            }
        });

        Button addBtn = (Button) findViewById(R.id.btnAddTask);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask(v);
            }
        });
    }

    public void addTask(View v) {
        String taskText = taskET.getText().toString();
        if (taskText.equalsIgnoreCase("")) {
            Toast.makeText(this, "enter the task description first!!", Toast.LENGTH_LONG);
        } else {
            Intent returnIntent = new Intent();
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            String nextDate = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
            c.add(Calendar.DAY_OF_MONTH, -1);
            Task keyValue = new Task(taskText, nextDate, timeET.getText().toString());
            returnIntent.putExtra("result", keyValue);
            setResult(AppCompatActivity.RESULT_OK, returnIntent);
            finish();
        }
    }

    public void onTimeSet(TextView timeView, TimePicker view, int hourOfDay, int minute) {
        String m = (minute < 10 ? ("0" + String.valueOf(minute)) : String.valueOf(minute));
        timeView.setText((hourOfDay > 12 ? String.valueOf(hourOfDay - 12) : String.valueOf(hourOfDay))
                + ":" + m + (hourOfDay > 11 ? " PM" : " AM"));
    }

}
