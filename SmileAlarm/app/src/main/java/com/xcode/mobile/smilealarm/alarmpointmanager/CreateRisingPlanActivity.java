package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.DateTimeHelper;
import com.xcode.mobile.smilealarm.DateTimePickerFragment;
import com.xcode.mobile.smilealarm.NotificationHelper;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;

public class CreateRisingPlanActivity extends AppCompatActivity implements View.OnClickListener,
        DateTimePickerFragment.OnTimeSetListener, DateTimePickerFragment.OnDateSetListener {

    private View _startDate;
    private View _avgTime;
    private View _expTime;

    private DateTimePickerFragment _dateTimePicker;
    private RisingPlanHandler _risingPlanHandler;

    private Time _avgTimeValue;
    private Time _expTimeValue;
    private Date _startDateValue;
    private Calendar _cal;

    private Boolean _isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_rising_plan);

        ViewHelper.setupToolbar(this);

        _startDate = findViewById(R.id.tv_CRP_Start_Date);
        _avgTime = findViewById(R.id.tv_CRP_AVG_Time);
        _expTime = findViewById(R.id.tv_CRP_EXP_Time);
        View _btnPlanIt = findViewById(R.id.btn_CRP_Plan_It);

        _startDate.setOnClickListener(this);
        _avgTime.setOnClickListener(this);
        _expTime.setOnClickListener(this);
        _btnPlanIt.setOnClickListener(this);

        _dateTimePicker = new DateTimePickerFragment();
        _risingPlanHandler = new RisingPlanHandler();

        _isConnected = false;

        prepareData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            _isConnected = true;
        }
    }

    @Override
    public void onBackPressed() {
        if (_isConnected) {
            setResult(RESULT_OK);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void prepareData() {
        _cal = Calendar.getInstance();

        _cal.set(Calendar.HOUR_OF_DAY, 8);
        _cal.set(Calendar.MINUTE, 0);

        int year = _cal.get(Calendar.YEAR);
        int month = _cal.get(Calendar.MONTH);
        int day = _cal.get(Calendar.DAY_OF_MONTH);
        int hour = _cal.get(Calendar.HOUR_OF_DAY);
        int minute = _cal.get(Calendar.MINUTE);

        _startDateValue = new Date(_cal.getTimeInMillis());
        _avgTimeValue = new Time(_cal.getTimeInMillis());

        ((TextView) _startDate)
                .setText(new DateFormatSymbols(Locale.getDefault()).getMonths()[month] + " " + day + ", " + year);

        DateTimeHelper.SetTime(this, (TextView) _avgTime, hour, minute);

        _cal.set(Calendar.HOUR_OF_DAY, 5);
        _cal.set(Calendar.MINUTE, 30);
        hour = _cal.get(Calendar.HOUR_OF_DAY);
        minute = _cal.get(Calendar.MINUTE);

        _expTimeValue = new Time(_cal.getTimeInMillis());
        DateTimeHelper.SetTime(this, (TextView) _expTime, hour, minute);
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_CRP_Start_Date:
                _dateTimePicker.setView((TextView) _startDate, false);
                _dateTimePicker.show(getFragmentManager(), "datePicker");
                break;
            case R.id.tv_CRP_AVG_Time:
                _dateTimePicker.setView((TextView) _avgTime, true);
                _dateTimePicker.show(getFragmentManager(), "timePicker");
                break;

            case R.id.tv_CRP_EXP_Time:
                _dateTimePicker.setView((TextView) _expTime, true);
                _dateTimePicker.show(getFragmentManager(), "timePicker");
                break;

            case R.id.btn_CRP_Plan_It:
                try {
                    btnPlanIt_onClick(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }

    }

    private void btnPlanIt_onClick(View v) throws IOException {
        int checkCode = _risingPlanHandler.createRisingPlan(_avgTimeValue, _expTimeValue, _startDateValue, this);
        if (checkCode == ReturnCode.OK) {
            Intent intent = new Intent(this, ViewRisingPlanActivity.class);
            startActivityForResult(intent, ActivityConstant.REQUESTCODE_CREATE_R_VR);
        } else {
            NotificationHelper.ShowError(this, _risingPlanHandler.getNotificationFromErrorCode(this));
        }
    }

    public void onDateSet(TextView dateView, DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (dateView != null) {
            dateView.setText(new DateFormatSymbols(Locale.getDefault()).getMonths()[monthOfYear] + " " + dayOfMonth
                    + ", " + year);
            dateView.setTag(new int[]{year, monthOfYear, dayOfMonth});
            _cal.set(Calendar.YEAR, year);
            _cal.set(Calendar.MONTH, monthOfYear);
            _cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            _startDateValue = new Date(_cal.getTimeInMillis());
        }

    }

    public void onTimeSet(TextView timeView, TimePicker view, int hourOfDay, int minute) {
        if (timeView != null) {
            timeView.setTag(new int[]{hourOfDay, minute});
            DateTimeHelper.SetTime(this, timeView, hourOfDay, minute);
            _cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            _cal.set(Calendar.MINUTE, minute);

            if (timeView.getId() == R.id.tv_CRP_AVG_Time) {
                _avgTimeValue = new Time(_cal.getTimeInMillis());
            } else {
                _expTimeValue = new Time(_cal.getTimeInMillis());
            }

        }

    }
}
