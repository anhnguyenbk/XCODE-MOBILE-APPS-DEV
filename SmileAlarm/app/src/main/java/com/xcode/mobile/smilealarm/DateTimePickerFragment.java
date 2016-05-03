package com.xcode.mobile.smilealarm;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

//For reusing
public class DateTimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private TextView _timeView;
    private boolean _isTimePick;
    private int _countCall;

    public DateTimePickerFragment() {
        super();
    }

    public DateTimePickerFragment setView(TextView view, boolean isTimePick) {
        this._timeView = view;
        this._isTimePick = isTimePick;
        this._countCall = 0;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        if (_isTimePick) {
            int hour, minute;
            try {
                int[] time1 = (int[]) this._timeView.getTag();
                hour = time1[0];
                minute = time1[1];

                if (hour == -1 && minute == -1) {
                    hour = c.get(Calendar.HOUR_OF_DAY);
                    minute = c.get(Calendar.MINUTE);
                }

            } catch (Exception e) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        } else {
            // Use the current date as the default date in the picker
            int year, month, day;
            try {
                int[] date = (int[]) this._timeView.getTag();
                year = date[0];
                month = date[1];
                day = date[2];

                if (year == -1 && month == -1 && day == -1) {
                    year = c.get(Calendar.YEAR);
                    month = c.get(Calendar.MONTH);
                    day = c.get(Calendar.DAY_OF_MONTH);
                }

            } catch (Exception e) {
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
    }

    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (this._countCall < 1) {
            ((OnDateSetListener) getActivity()).onDateSet(this._timeView, view, year, monthOfYear, dayOfMonth);
            this._countCall++;
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (this._countCall < 1) {
            ((OnTimeSetListener) getActivity()).onTimeSet(this._timeView, view, hourOfDay, minute);
            this._countCall++;
        }
    }

    public interface OnTimeSetListener {
        void onTimeSet(TextView timeView, TimePicker view, int hourOfDay, int minute);
    }

    public interface OnDateSetListener {
        void onDateSet(TextView dateView, DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

}
