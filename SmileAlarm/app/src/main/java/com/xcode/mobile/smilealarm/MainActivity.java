package com.xcode.mobile.smilealarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView mTime;
    private TextView mDate;
    private ImageView mImageView;
    private ImageButton mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTime = (TextView) findViewById(R.id.etTime);
        mDate = (TextView) findViewById(R.id.etDate);
        mImageView = (ImageView) findViewById(R.id.image);
        mAddButton = (ImageButton) findViewById(R.id.btn_add);

        mAddButton.setImageResource(R.drawable.icn_morph_reverse);
        loadTimeDate();
    }

    private void loadTimeDate() {
        Calendar today = Calendar.getInstance();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        String minute = "";
        if (today.get(Calendar.MINUTE) > 9) {
            minute = "" + today.get(Calendar.MINUTE);
        }
        else {
            minute = "0" + today.get(Calendar.MINUTE);
        }
        String month = "";
        switch(today.get(Calendar.MONTH)) {
            case 1: month = "January"; break;
            case 2: month = "February"; break;
            case 3: month = "March"; break;
            case 4: month = "April"; break;
            case 5: month = "May"; break;
            case 6: month = "June"; break;
            case 7: month = "July"; break;
            case 8: month = "August"; break;
            case 9: month = "September"; break;
            case 10: month = "October"; break;
            case 11: month = "November"; break;
            case 12: month = "December"; break;
        }
        mTime.setText(hour + ":" + minute);
        mDate.setText( month + " " + today.get(Calendar.YEAR));
        if(hour > 5 && hour < 10) {
            mImageView.setImageResource(R.drawable.sun1);
        }
        else if (hour > 10 && hour < 14) {
            mImageView.setImageResource(R.drawable.sun2);
        }
        else if (hour > 14 && hour < 18) {
            mImageView.setImageResource(R.drawable.sun3);
        }
        else {
            mImageView.setImageResource(R.drawable.sun4);
        }
    }
}
