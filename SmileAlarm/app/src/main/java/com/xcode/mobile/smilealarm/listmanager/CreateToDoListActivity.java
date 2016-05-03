package com.xcode.mobile.smilealarm.listmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;

/**
 * Created by an.nguyen on 11/17/2015.
 */
public class CreateToDoListActivity extends AppCompatActivity {
    private ListView todoLV;
    private ToDoListAdapter todoAdpt;
    private ArrayList<Task> todoList;
    private ToDoListHandler todoHdlr;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_todo_list);

        ViewHelper.setupToolbar(this);

        todoHdlr = new ToDoListHandler();
        todoList = todoHdlr.getToDoList(true);
        todoLV = (ListView) findViewById(R.id.todolist);
        todoAdpt = new ToDoListAdapter(this, R.layout.todo_list, todoList);
        todoLV.setAdapter(todoAdpt);
        todoLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task item = (Task) parent.getItemAtPosition(position);
                todoList.remove(item);
                todoAdpt = new ToDoListAdapter(CreateToDoListActivity.this, R.layout.todo_list, todoList);
                todoLV.setAdapter(todoAdpt);
            }

        });

        todoLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Task item = (Task) parent.getItemAtPosition(position);
                Intent addTaskActivity = new Intent(CreateToDoListActivity.this, AddTaskActivity.class);
                addTaskActivity.putExtra("update", item);
                CreateToDoListActivity.this.startActivityForResult(addTaskActivity, 1);
                todoList.remove(item);
                return true;
            }
        });

        Button saveBtn = (Button) findViewById(R.id.saveTodoList);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                todoHdlr.saveToDoList(todoList);
                finish();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Task t = (Task) data.getSerializableExtra("result");
                todoList.add(t);
                todoAdpt = new ToDoListAdapter(this, R.layout.todo_list, todoList);
                todoLV.setAdapter(todoAdpt);
            }
        }
    }

    private void startAddTaskActivityForResult() {
        Intent addTaskActivity = new Intent(CreateToDoListActivity.this, AddTaskActivity.class);
        CreateToDoListActivity.this.startActivityForResult(addTaskActivity, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            startAddTaskActivityForResult();
        }
        return super.onOptionsItemSelected(item);
    }

}

