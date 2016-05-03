package com.xcode.mobile.smilealarm.alarmpointmanager;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.xcode.mobile.smilealarm.DateTimeHelper;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesHandler;

@SuppressWarnings("serial")
public class AlarmPoint implements Serializable {

    private Date _date;
    private Time _time01;
    private Time _time02;
    private UUID _tuneId;
    private Boolean _fadeIn;
    private Boolean _protected;
    private Boolean _color;
    private List<Boolean> _repeat;
    private int _repeatID;

    public AlarmPoint(Date date) {

        // input date will be changed if the caller of this constructor change it
        // * so that, you need one new object
        java.util.Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        _date = new java.sql.Date(cal.getTimeInMillis());
        _time01 = null;
        _time02 = null;

        _fadeIn = true;
        _protected = false;
        _color = false;

        // _tuneId is default of Recommended Tunes
        set_tuneId(RecommendedTunesHandler.getInstance().get_defaultTuneId());

        _repeat = new ArrayList<Boolean>(Arrays.asList(new Boolean[7]));
        Collections.fill(_repeat, Boolean.FALSE);

        _repeatID = 0;
    }

    public Date getSQLDate() {
        if (_date != null)
            return new Date(_date.getTime());
        return null;
    }

    public Time getSQLTimePoint(int position) throws IllegalArgumentException {
        if (position == 1) {
            if (_time01 != null)
                return new Time(_time01.getTime());
            return null;
        }
        if (position == 2) {
            if (_time02 != null)
                return new Time(_time02.getTime());
            return null;
        }
        throw new IllegalArgumentException("getSQLTimePoint: POSITION IS ONLY 1 OR 2");
    }

    public UUID get_tuneId() {
        return UUID.fromString(_tuneId.toString());
    }

    public Boolean isFadeIn() {
        return _fadeIn;
    }

    public List<Boolean> getRepeatList() {
        List<Boolean> returnList = new ArrayList<Boolean>(Arrays.asList(new Boolean[7]));
        returnList.clear();
        for (Boolean r : _repeat) {
            // because Boolean is an object
            // so that, if you pass r into returnList, you edit on returnList,
            // _repeat will be edited, too
            returnList.add(r);
        }
        return returnList;
    }

    public Boolean isProtected() {
        return _protected;
    }

    public Boolean isColor() {
        return _color;
    }

    public Boolean isRepeat() {
        return _repeat.contains(Boolean.TRUE);
    }

    public int getRepeatId() {
        return _repeatID;
    }

    public int setTimePoint(Time newTime, int position) throws IllegalArgumentException {

        if (_protected)
            return ReturnCode.PROTECTED;

        java.util.Calendar cal = DateTimeHelper.GetCalendarFromSQLDateTime(_date, newTime);
        java.sql.Time sqlTime = new java.sql.Time(cal.getTimeInMillis());

        if (position == 1) {
            _time01 = sqlTime;
            return ReturnCode.OK;
        }
        if (position == 2) {
            _time02 = sqlTime;
            return ReturnCode.OK;
        }
        throw new IllegalArgumentException("setTimePoint: POSITION IS ONLY 1 OR 2");
    }

    public int set_tuneId(UUID tuneId) {
        if (_protected)
            return ReturnCode.PROTECTED;
        this._tuneId = UUID.fromString(tuneId.toString());
        return ReturnCode.OK;
    }

    public int setProperties(boolean isFadeIn, boolean isProtected, boolean isColor) {
        if (_protected)
            return ReturnCode.PROTECTED;

        _fadeIn = isFadeIn;
        _protected = isProtected;
        _color = isColor;

        return ReturnCode.OK;
    }

    public void setColor() {
        _color = true;
    }

    public int setRepeat(List<Boolean> newRepeatList) throws IllegalArgumentException {

        if (_protected)
            return ReturnCode.PROTECTED;

        if (newRepeatList == null || newRepeatList.size() != 7)
            throw new IllegalArgumentException(
                    "setRepeat: newRepeatList CANNOT BE NULL OR HAS SIZE WHICH IS DIFFERENT FROM 7");

        _repeat.clear();
        _repeat.addAll(newRepeatList);
        if (checkAndChangeDate())
            return ReturnCode.AP_CHANGEDATE;
        return ReturnCode.OK;
    }

    public int setRepeatId(int newId) throws IllegalArgumentException {
        if (isRepeat()) {
            _repeatID = newId;
            return ReturnCode.OK;
        }
        throw new IllegalArgumentException("setRepeatId: ONLY REPEAT ALARM POINTS ARE ABLE TO SET REPEAT ID");
    }

    private Boolean checkAndChangeDate() {
        int dayOfWeek = DateTimeHelper.GetDayOfWeek(_date);

        if (!_repeat.get(dayOfWeek - 1)) {
            for (int i = 0; i < 7; i++) {
                if (_repeat.get(i)) {
                    _date = new Date(DateTimeHelper.GetSQLDate(_date, i - (dayOfWeek - 1)));
                    return true;
                }
            }
        }
        return false;
    }

    public void removeProtect() {
        _protected = false;
    }

}
