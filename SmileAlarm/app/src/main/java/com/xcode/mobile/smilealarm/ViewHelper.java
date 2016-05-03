package com.xcode.mobile.smilealarm;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

/**
 * Created by quochuy on 12/10/2015.
 */
public class ViewHelper {
    public static void setupToolbar(AppCompatActivity ACActivity) {
        Toolbar toolbar = (Toolbar) ACActivity.findViewById(R.id.toolbar);
        if (toolbar != null) ACActivity.setSupportActionBar(toolbar);
    }

    public static void disableButton(Button btn) {
        btn.setEnabled(false);
        btn.setTextColor(Color.LTGRAY);
    }

    public static void enableButton(Button btn) {
        btn.setEnabled(true);
        btn.setTextColor(Color.WHITE);
    }
}
