package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import hirondelle.date4j.DateTime;
import com.xcode.mobile.smilealarm.DateTimeHelper;
import com.xcode.mobile.smilealarm.R;

public class CaldroidAlarmPointListAdapter extends CaldroidGridAdapter {

    private HashMap<Date, AlarmPoint> _alarmPointList;
    private HashMap<Date, AlarmPoint> _selectedAlarmPointList;
    private SimpleDateFormat sdfTime;
    private int _topPadding, _leftPadding, _bottomPadding, _rightPadding;

    @SuppressWarnings("unchecked")
    @SuppressLint("SimpleDateFormat")
    public CaldroidAlarmPointListAdapter(Context context, int month, int year, HashMap<String, Object> caldroidData,
                                         HashMap<String, Object> extraData) {
        super(context, month, year, caldroidData, extraData);

        _alarmPointList = (HashMap<Date, AlarmPoint>) this.extraData.get(StringKey.ALP_LIST);
        _selectedAlarmPointList = (HashMap<Date, AlarmPoint>) this.extraData.get(StringKey.SELECTED_LIST);

        sdfTime = new SimpleDateFormat("HH:mm");
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cellView = convertView;

        // For reuse
        if (convertView == null) {
            cellView = inflater.inflate(R.layout.caldroid_alarm_point_list_cell, null);
        }

        _topPadding = cellView.getPaddingTop();
        _leftPadding = cellView.getPaddingLeft();
        _bottomPadding = cellView.getPaddingBottom();
        _rightPadding = cellView.getPaddingRight();

        TextView dateTextView = (TextView) cellView.findViewById(R.id.tvDate);
        TextView event01TextView = (TextView) cellView.findViewById(R.id.tvEvent01);
        TextView event02TextView = (TextView) cellView.findViewById(R.id.tvEvent02);
        TextView repeatIdTextView = (TextView) cellView.findViewById(R.id.tvRepeatId);
        TextView repeatOfTextView = (TextView) cellView.findViewById(R.id.tvRepeatOf);

        dateTextView.setTextColor(Color.BLACK);

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);
        Resources resources = context.getResources();

        // Set color of the dates in previous / next month
        if (dateTime.getMonth() != month) {
            dateTextView.setTextColor(resources.getColor(com.caldroid.R.color.caldroid_darker_gray));
        }

        boolean shouldResetDiabledView = false;
        boolean shouldResetSelectedView = false;

        // Customize for disabled dates and date outside min/max dates
        if ((minDateTime != null && dateTime.lt(minDateTime)) || (maxDateTime != null && dateTime.gt(maxDateTime))
                || (disableDates != null && disableDates.indexOf(dateTime) != -1)) {

            dateTextView.setTextColor(CaldroidFragment.disabledTextColor);
            if (CaldroidFragment.disabledBackgroundDrawable == -1) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.disable_cell);
            } else {
                cellView.setBackgroundResource(CaldroidFragment.disabledBackgroundDrawable);
            }

            if (dateTime.equals(getToday())) {
                cellView.setBackgroundResource(com.caldroid.R.drawable.red_border_gray_bg);
            }

        } else {
            shouldResetDiabledView = true;
        }

        // Customize for selected dates
        // today is the selected date
        if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
            cellView.setBackgroundColor(resources.getColor(com.caldroid.R.color.caldroid_sky_blue));
        } else {
            shouldResetSelectedView = true;
        }

        if (shouldResetDiabledView && shouldResetSelectedView) {
            cellView.setBackgroundResource(com.caldroid.R.drawable.cell_bg);
        }

        dateTextView.setText("" + dateTime.getDay());
        event01TextView.setText("");
        event02TextView.setText("");
        repeatIdTextView.setText("");
        repeatOfTextView.setText("");

        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView.setPadding(_leftPadding, _topPadding, _rightPadding, _bottomPadding);

        java.sql.Date sqlDate = DateTimeHelper.ConvertToSqlDate(dateTime);
        if (!DateTimeHelper.IsPast(sqlDate)) {
            setMyCustomResources(dateTime, cellView, dateTextView, event01TextView, event02TextView, repeatOfTextView,
                    repeatIdTextView);
        }

        return cellView;
    }

    private void setMyCustomResources(DateTime dateTime, View cellView, TextView dateTextView, TextView event01TextView,
                                      TextView event02TextView, TextView repeatOfTextView, TextView repeatIdTextView) {
        this.setCustomResources(dateTime, cellView, dateTextView);

        java.sql.Date sqlDate = DateTimeHelper.ConvertToSqlDate(dateTime);

        // The way to show a cell
        if (_alarmPointList.size() > 0) {

            AlarmPoint ap = AlarmPointListHandler.GetAlarmPointFromList(_alarmPointList, sqlDate);

            if (ap != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(ap.getSQLDate());

                if (ap.isColor() && ap.getSQLDate().compareTo(sqlDate) == 0) {
                    cellView.setBackgroundColor(Color.YELLOW);
                }

                if (ap.isRepeat()) {

                    String repeatID = new String(new char[ap.getRepeatId()]).replace("\0", "*");

                    if (ap.getSQLDate().compareTo(sqlDate) < 0) {
                        repeatOfTextView.setText(repeatID);
                    }

                    if (ap.getSQLDate().compareTo(sqlDate) == 0) {
                        repeatIdTextView.setText(repeatID);
                    }
                }

                Calendar current = Calendar.getInstance();
                java.sql.Time tp1 = ap.getSQLTimePoint(1);
                java.sql.Time tp2 = ap.getSQLTimePoint(2);
                Calendar calTp1 = DateTimeHelper.GetCalendarFromSQLDateTime(ap.getSQLDate(), tp1);

                if (calTp1 != null && calTp1.compareTo(current) >= 0)
                    event01TextView.setText(sdfTime.format(tp1));
                if (tp2 != null)
                    // if calTp2 >= current => alarmPoint will be removed from RingOffReceiver
                    event02TextView.setText(sdfTime.format(tp2));
            }

        }

        if (dateTime.equals(getToday())) {
            cellView.setBackgroundResource(com.caldroid.R.drawable.red_border);
            cellView.setPadding(_leftPadding, _topPadding, _rightPadding, _bottomPadding);
        }

        // If you select one date of repeat set, the set is selected
        if (_selectedAlarmPointList.containsKey(sqlDate)) {
            cellView.setBackgroundColor(Color.CYAN);
        } else {
            AlarmPoint ap = AlarmPointListHandler.GetAlarmPointFromList(_selectedAlarmPointList, sqlDate);
            if (ap != null) {
                cellView.setBackgroundColor(Color.CYAN);
            }
        }
    }
}
