package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.content.Context;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;

import com.xcode.mobile.smilealarm.DataHelper;
import com.xcode.mobile.smilealarm.DateTimeHelper;
import com.xcode.mobile.smilealarm.R;

public class RisingPlanHandler {

    private static final int VALID_CODE = 1;
    private static final int INVALID_CODE_EXP = 2;
    private static final int INVALID_CODE_EXP_SOONER_AVG = 3;
    private static final int INVALID_CODE_EXP_AVG_TOO_FAR = 4;
    private static final int INVALID_CODE_EXP_AVG_TOO_NEAR = 5;
    private static final int INVALID_CODE_STARTDATE_BEFORE_THE_DAY_AFTER_TOMORROW = 6;
    private static final int[] ListOfDescreasingMinutes = new int[]{-1, 2, 5, 10, 15, 18, 20};
    private HashMap<Date, AlarmPoint> _risingPlan;
    private int _currentStage;
    private Date _theDateBefore;
    private Time _theWakingTimeBefore;
    private Time _expWakingTime;
    private int _checkCode;

    public static int Connect(Context ctx) {
        AlarmPointListHandler aplh_RisingPlan = new AlarmPointListHandler(true);
        AlarmPointListHandler aplh_AlarmPoint = new AlarmPointListHandler(false);
        return aplh_AlarmPoint.overwriteAlarmPointList(aplh_RisingPlan.getCurrentList(), ctx);
    }

    public int createRisingPlan(Time avgWakingTime, Time expWakingTime, Date startDate, Context ctx)
            throws IOException {
        _checkCode = checkParameters(avgWakingTime, expWakingTime, startDate);
        if (_checkCode != VALID_CODE)
            return _checkCode;

        _risingPlan = new HashMap<Date, AlarmPoint>();
        _currentStage = 1;
        _theDateBefore = new Date(DateTimeHelper.GetTheDateBefore(startDate));
        _theWakingTimeBefore = avgWakingTime;
        _expWakingTime = expWakingTime;

        AlarmPoint ap0 = new AlarmPoint(_theDateBefore);
        ap0.setColor();
        _risingPlan.put(ap0.getSQLDate(), ap0);

        while (DateTimeHelper.DurationTwoSQLTime(_theWakingTimeBefore,
                _expWakingTime) >= ListOfDescreasingMinutes[_currentStage]) {
            generateRisingPlanInCurrentStage();
            _currentStage++;
        }
        generateTheLastAlarmPoint();

        DataHelper.getInstance().saveAlarmPointListToData(true, _risingPlan);

        return ReturnCode.OK;
    }

    public String getNotificationFromErrorCode(Context ctx) {
        switch (_checkCode) {
            case INVALID_CODE_EXP:
                return ctx.getString(R.string.not_ExpTime);
            case INVALID_CODE_EXP_SOONER_AVG:
                return ctx.getString(R.string.not_AvgTime);
            case INVALID_CODE_EXP_AVG_TOO_FAR:
                return ctx.getString(R.string.not_AvgExpTooLong);
            case INVALID_CODE_EXP_AVG_TOO_NEAR:
                return ctx.getString(R.string.not_AvgExpTooShort);
            case INVALID_CODE_STARTDATE_BEFORE_THE_DAY_AFTER_TOMORROW:
                return ctx.getString(R.string.not_StrDate);
            default:
                return "invalid Code";
        }
    }

    private int checkParameters(Time avgTime, Time expTime, Date startDate) {

        if (!isAfterTomorrow(startDate))
            return INVALID_CODE_STARTDATE_BEFORE_THE_DAY_AFTER_TOMORROW;

        if (DateTimeHelper.CompareTo(avgTime, expTime) < 0)
            return INVALID_CODE_EXP_SOONER_AVG;

        Time BeginExpTime = new Time(DateTimeHelper.GetSQLTime(5, 0));
        Time EndExpTime = new Time(DateTimeHelper.GetSQLTime(8, 0));
        if (DateTimeHelper.CompareTo(expTime, BeginExpTime) < 0 || DateTimeHelper.CompareTo(expTime, EndExpTime) > 0) {
            return INVALID_CODE_EXP;
        }

        int maxMinutes = 875;
        int minMinutes = 20;
        if (DateTimeHelper.DurationTwoSQLTime(avgTime, expTime) > maxMinutes)
            return INVALID_CODE_EXP_AVG_TOO_FAR;
        if (DateTimeHelper.DurationTwoSQLTime(avgTime, expTime) < minMinutes)
            return INVALID_CODE_EXP_AVG_TOO_NEAR;

        return VALID_CODE;
    }

    private void generateRisingPlanInCurrentStage() {
        int daysInStage = 10;
        if (ListOfDescreasingMinutes[_currentStage] == 15)
            daysInStage = 15;

        for (int i = 0; i < daysInStage && DateTimeHelper.DurationTwoSQLTime(_theWakingTimeBefore,
                _expWakingTime) >= ListOfDescreasingMinutes[_currentStage]; i++) {
            // WakingTime
            Date currentDate = new Date(DateTimeHelper.GetTheNextDate(_theDateBefore));
            AlarmPoint ap = new AlarmPoint(currentDate);
            ap.setColor();
            Time currentWakingTime = DateTimeHelper.GetSQLTime(_theWakingTimeBefore,
                    -(ListOfDescreasingMinutes[_currentStage]));
            ap.setTimePoint(currentWakingTime, 1);

            // SleepingTime
            Time sleepingTimeBefore = getSleepingTime(currentWakingTime);
            AlarmPoint apBefore = _risingPlan.get(_theDateBefore);
            apBefore.setTimePoint(sleepingTimeBefore, 2);

            // put
            _risingPlan.put(apBefore.getSQLDate(), apBefore);
            _risingPlan.put(ap.getSQLDate(), ap);

            // reset before
            _theDateBefore = currentDate;
            _theWakingTimeBefore = currentWakingTime;
        }

    }

    private void generateTheLastAlarmPoint() {
        // WakingTime
        Date currentDate = new Date(DateTimeHelper.GetTheNextDate(_theDateBefore));
        AlarmPoint ap = new AlarmPoint(currentDate);
        ap.setColor();
        Time currentWakingTime = _expWakingTime;
        ap.setTimePoint(currentWakingTime, 1);

        // SleepingTime
        Time sleepingTimeBefore = getSleepingTime(currentWakingTime);
        AlarmPoint apBefore = _risingPlan.get(_theDateBefore);
        apBefore.setTimePoint(sleepingTimeBefore, 2);

        // put
        _risingPlan.put(apBefore.getSQLDate(), apBefore);
        _risingPlan.put(ap.getSQLDate(), ap);

        // reset before
        _theDateBefore = currentDate;
        _theWakingTimeBefore = currentWakingTime;
    }

    private Time getSleepingTime(Time wakingTime) {

        // wakingTime - 11 PM of thedaybefore > 8 hours => 11 PM
        // <=> if wakingTime >= 7 AM
        // or wakingTime >= 7AM => 11PM
        // 6AM <= wakingTime < 7AM => 10:30 PM
        // 5AM <= wakingTime < 6AM => 10 PM

        Time SevenAM = new Time(DateTimeHelper.GetSQLTime(7, 0));
        Time SixAM = new Time(DateTimeHelper.GetSQLTime(6, 0));
        Time FiveAM = new Time(DateTimeHelper.GetSQLTime(5, 0));
        Time EleventPM = new Time(DateTimeHelper.GetSQLTime(23, 0));
        Time Ten30PM = new Time(DateTimeHelper.GetSQLTime(22, 30));
        Time TenPM = new Time(DateTimeHelper.GetSQLTime(22, 0));

        if (DateTimeHelper.CompareTo(wakingTime, SevenAM) >= 0) {
            return EleventPM;
        }

        if (DateTimeHelper.CompareTo(wakingTime, SevenAM) < 0 && DateTimeHelper.CompareTo(wakingTime, SixAM) >= 0) {
            return Ten30PM;
        }

        if (DateTimeHelper.CompareTo(wakingTime, SixAM) < 0 && DateTimeHelper.CompareTo(wakingTime, FiveAM) >= 0) {
            return TenPM;
        }

        return null;
    }

    private Boolean isAfterTomorrow(Date startDate) {
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        Date tomorrow = new Date(DateTimeHelper.GetTheNextDate(today));

        return (DateTimeHelper.CompareTo(startDate, tomorrow) > 0);
    }
}
