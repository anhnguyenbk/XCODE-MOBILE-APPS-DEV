package com.xcode.mobile.smilealarm;

import android.app.AlarmManager;
import android.content.Context;
import android.text.format.DateFormat;
import android.widget.TextView;

import java.util.Calendar;

public class DateTimeHelper {

    public static final long ONE_WEEK_MILLIS = AlarmManager.INTERVAL_DAY * 7;

    // -- utilDate set month 0-11
    // -- calendar set month 0-11
    // -- sqlDate set month 0-11
    // -- DateTime set month 1-12
    // -- DayOfWeek Sun-Sat ~ 1-7

    public static java.sql.Date ConvertToSqlDate(hirondelle.date4j.DateTime dateTime) {
        // convert DateTime to sql.Date
        java.util.Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateTime.getYear());
        cal.set(Calendar.MONTH, dateTime.getMonth() - 1);
        cal.set(Calendar.DAY_OF_MONTH, dateTime.getDay());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        java.sql.Date sqlDate = new java.sql.Date(cal.getTime().getTime());

        return sqlDate;
    }

    public static int GetDayOfWeek(java.sql.Date sqlDate) {
        java.util.Calendar cal = Calendar.getInstance();
        cal.setTime(sqlDate);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));

        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static Calendar GetCalendarFromSQLDateTime(java.sql.Date date, java.sql.Time time) {
        if (date == null || time == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar calTime = Calendar.getInstance();
        calTime.setTime(time);

        cal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static int CompareTo(java.sql.Time time01, java.sql.Time time02) {

        Calendar calTime01 = Calendar.getInstance();
        calTime01.setTime(time01);
        int hourOfDay01 = calTime01.get(Calendar.HOUR_OF_DAY);
        int minutes01 = calTime01.get(Calendar.MINUTE);

        Calendar calTime02 = Calendar.getInstance();
        calTime02.setTime(time02);
        int hourOfDay02 = calTime02.get(Calendar.HOUR_OF_DAY);
        int minutes02 = calTime02.get(Calendar.MINUTE);

        if (hourOfDay01 < hourOfDay02) {
            return -1;
        } else if (hourOfDay01 > hourOfDay02) {
            return 1;
        } else if (minutes01 < minutes02) {
            return -1;
        } else if (minutes01 > minutes02) {
            return 1;
        }

        return 0;
    }

    public static int CompareTo(java.sql.Date date01, java.sql.Date date02) {

        Calendar calDate01 = Calendar.getInstance();
        calDate01.setTime(date01);
        calDate01.set(Calendar.HOUR_OF_DAY, 0);
        calDate01.set(Calendar.MINUTE, 0);
        calDate01.set(Calendar.SECOND, 0);
        calDate01.set(Calendar.MILLISECOND, 0);


        Calendar calDate02 = Calendar.getInstance();
        calDate02.setTime(date02);
        calDate02.set(Calendar.HOUR_OF_DAY, 0);
        calDate02.set(Calendar.MINUTE, 0);
        calDate02.set(Calendar.SECOND, 0);
        calDate02.set(Calendar.MILLISECOND, 0);

        return calDate01.compareTo(calDate02);
    }

    public static long GetSQLTime(int hours, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static int DurationTwoSQLTime(java.sql.Time futureTime, java.sql.Time pastTime) {
        Calendar calFutureTime = Calendar.getInstance();
        calFutureTime.setTime(futureTime);
        int hourOfDayFutureTime = calFutureTime.get(Calendar.HOUR_OF_DAY);
        int minutesFutureTime = calFutureTime.get(Calendar.MINUTE);

        Calendar calPastTime = Calendar.getInstance();
        calPastTime.setTime(pastTime);
        int hourOfDayPastTime = calPastTime.get(Calendar.HOUR_OF_DAY);
        int minutesPastTime = calPastTime.get(Calendar.MINUTE);

        return (hourOfDayFutureTime - hourOfDayPastTime) * 60 + (minutesFutureTime - minutesPastTime);
    }

    public static long GetTheDateBefore(java.sql.Date date) {
        return GetSQLDate(date, -1);
    }

    public static long GetTheNextDate(java.sql.Date date) {
        return GetSQLDate(date, 1);
    }

    public static long GetSQLDate(java.sql.Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, days);

        return cal.getTimeInMillis();
    }

    public static java.sql.Time GetSQLTime(java.sql.Time time01, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time01);
        cal.add(Calendar.MINUTE, minutes);

        java.sql.Time newTime = new java.sql.Time(cal.getTimeInMillis());

        return newTime;
    }

    public static void SetTime(Context ctx, TextView view, int hour, int minute) {
        String _12hFormat = "";
        String _hour;
        String _minute;
        if (hour < 0 || minute < 0) {
            _hour = "--";
            _minute = "--";
        } else {
            if (!DateFormat.is24HourFormat(ctx)) {
                if (hour >= 12) {
                    hour -= 12;
                    _12hFormat = " PM";
                } else {
                    _12hFormat = " AM";
                }
            }

            _hour = String.valueOf(hour);
            _minute = String.valueOf(minute);

            if (hour < 10) {
                _hour = "0" + _hour;
            }
            if (minute < 10) {
                _minute = "0" + _minute;
            }
        }

        view.setText(_hour + ":" + _minute + _12hFormat);
    }

    public static boolean IsNowAndFuture(java.sql.Date sqlDate) {
        Calendar calDate01 = Calendar.getInstance();
        calDate01.setTime(sqlDate);
        calDate01.set(Calendar.HOUR_OF_DAY, 0);
        calDate01.set(Calendar.MINUTE, 0);
        calDate01.set(Calendar.SECOND, 0);
        calDate01.set(Calendar.MILLISECOND, 0);

        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);

        return calDate01.compareTo(calToday) >= 0;
    }

    public static boolean IsPast(java.sql.Date sqlDate) {
        Calendar calDate01 = Calendar.getInstance();
        calDate01.setTime(sqlDate);
        calDate01.set(Calendar.HOUR_OF_DAY, 0);
        calDate01.set(Calendar.MINUTE, 0);
        calDate01.set(Calendar.SECOND, 0);
        calDate01.set(Calendar.MILLISECOND, 0);

        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);

        return calDate01.compareTo(calToday) < 0;
    }

    public static java.sql.Date GetToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    public static long GetCurrentTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

}
