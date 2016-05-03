package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.DataHelper;
import com.xcode.mobile.smilealarm.DateTimeHelper;
import com.xcode.mobile.smilealarm.RingOffBootReceiver;
import com.xcode.mobile.smilealarm.RingOffReceiver;

public class AlarmPointListHandler {

    private HashMap<Date, AlarmPoint> _currentAlarmPointList;
    private Boolean _isRisingPlan;

    public AlarmPointListHandler(Boolean isRisingPlan) {
        _isRisingPlan = isRisingPlan;
        getAlarmPointList();
    }

    public static int IntersectionOfRepeatListAndSinglePonint(List<Boolean> repeatList, AlarmPoint alarmPoint) {
        int dayOfWeek = DateTimeHelper.GetDayOfWeek(alarmPoint.getSQLDate());
        if (repeatList.get(dayOfWeek - 1)) {
            return dayOfWeek;
        }
        return -1;
    }

    public static List<Integer> IntersectionOfTwoRepeatLists(List<Boolean> repeatList01, List<Boolean> repeatList02) {
        List<Integer> intersections = new ArrayList<Integer>();
        for (int i = 0; i < 7; i++) {
            if (repeatList01.get(i) && repeatList02.get(i)) {
                intersections.add(i);
            }
        }
        return intersections;
    }

    public static AlarmPoint GetAlarmPointFromList(HashMap<Date, AlarmPoint> alarmPointList, Date selectedDate) {
        if (alarmPointList.containsKey(selectedDate))
            return alarmPointList.get(selectedDate);

        // Try to find the shadow copy of repeat Alarm Point
        int selectedDayOfWeek = DateTimeHelper.GetDayOfWeek(selectedDate);

        for (Map.Entry<Date, AlarmPoint> entry : alarmPointList.entrySet()) {
            AlarmPoint currentAlarmPoint = entry.getValue();

            if (selectedDate.compareTo(currentAlarmPoint.getSQLDate()) > 0) {
                if (currentAlarmPoint.isRepeat()) {
                    List<Boolean> repeat = currentAlarmPoint.getRepeatList();
                    if (repeat.get(selectedDayOfWeek - 1)) {
                        return currentAlarmPoint;
                    }
                }
            }
        }

        return null;
    }

    public static void ProcessAfterRingOff(Context context, Boolean isMorning) {
        HashMap<Date, AlarmPoint> currentAlarmPointList = DataHelper.getInstance().getAlarmPointListFromData(false);
        Date today = DateTimeHelper.GetToday();
        AlarmPoint currentAlarmPoint = GetAlarmPointFromList(currentAlarmPointList, today);

        if (currentAlarmPoint.isRepeat()) {
            // If remove all repeat alarm point from alarm manager
            // and change date, and re-register, then how you manager the
            // dayOfWeek of today?
            // So that, => change view
        } else {
            // We don't need to cancel alarmManager,
            // because it just executes only one time

            Calendar cal02 = DateTimeHelper.GetCalendarFromSQLDateTime(currentAlarmPoint.getSQLDate(),
                    currentAlarmPoint.getSQLTimePoint(2));

            Boolean removable = (isMorning && cal02 == null) || (!isMorning);
            if (removable) {
                DataHelper.getInstance().removeAlarmPointFromData(false, today);
            }

        }
    }

    public HashMap<Date, AlarmPoint> getCurrentList() {
        return _currentAlarmPointList;
    }

    public AlarmPoint getAlarmPoint(Date sqlDate) {
        return GetAlarmPointFromList(_currentAlarmPointList, sqlDate);
    }

    public void getAlarmPointList() {
        _currentAlarmPointList = DataHelper.getInstance().getAlarmPointListFromData(_isRisingPlan);
        if (_currentAlarmPointList == null) {
            _currentAlarmPointList = new HashMap<Date, AlarmPoint>();
        }
    }

    @SuppressWarnings("unchecked")
    public int saveAlarmPoint(AlarmPoint newAlarmPoint, Context context) {

        // if isRisingPlan, don't remove or save to alarm manager
        // if alarmpoint is existed,
        // + + protected? => return -1
        // + + remove it
        // if alarmpoint is repeat,
        // + + consider that to other repeat
        // + + + + edit other repeat, remove (if any), remove,
        // re-register ...
        // + + + + if protected? => edit new
        // + + set repeatID to that (if any), register new repeat to system
        // + + consider that to other not repeat
        // + + + + if protected? => edit new
        // + + + + if not, remove old

        Date date = newAlarmPoint.getSQLDate();
        // if we want to undo, we can undo within a clone of current List
        HashMap<Date, AlarmPoint> currentListClone = (HashMap<Date, AlarmPoint>) _currentAlarmPointList.clone();

        if (currentListClone.containsKey(date)) {
            AlarmPoint oldAlarmPoint = currentListClone.get(date);

            if (oldAlarmPoint.isProtected() && newAlarmPoint.isProtected()) {
                return ReturnCode.PROTECTED;
            }

            if (!_isRisingPlan)
                removeAlarmPointFromAlarmManager(oldAlarmPoint, context);
            currentListClone.remove(date); // don't need to care when iterating
        }

        // If you click on one shadow of repeat alarm point,
        // * it will return that alarm point
        // => don't need to care (!newAlarmPoint.isRepeat()),
        // * * because, it will be a new alarm point

        if (newAlarmPoint.isRepeat()) {

            List<Boolean> newRepeat = newAlarmPoint.getRepeatList();
            Boolean isNewChanged = false;

            for (Iterator<Map.Entry<Date, AlarmPoint>> it = currentListClone.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Date, AlarmPoint> entry = it.next();
                AlarmPoint entry_alarmPoint = entry.getValue();

                if (entry_alarmPoint.isRepeat()) {
                    Boolean isEntryChanged = false;
                    List<Boolean> entry_repeat = entry_alarmPoint.getRepeatList();
                    List<Integer> intersections = IntersectionOfTwoRepeatLists(newRepeat, entry_repeat);

                    if (intersections.size() > 0) {
                        if (entry_alarmPoint.isProtected()) {
                            isNewChanged = true;
                            for (Integer i : intersections) {
                                newRepeat.set(i, Boolean.FALSE);
                            }
                        } else {
                            isEntryChanged = true;
                            for (Integer i : intersections) {
                                entry_repeat.set(i, Boolean.FALSE);
                            }
                        }

                        if (isEntryChanged) {
                            if (!_isRisingPlan)
                                removeAlarmPointFromAlarmManager(entry_alarmPoint, context);

                            if (!entry_repeat.contains(Boolean.TRUE)) {
                                // each AlarmPoint will repeat itself,
                                // so that, remove all repeat List => remove
                                // that alarm point
                                DataHelper.getInstance().removeAlarmPointFromData(_isRisingPlan,
                                        entry_alarmPoint.getSQLDate());
                                it.remove();
                            } else {

                                Date oldDate = new Date(DateTimeHelper.GetSQLDate(entry_alarmPoint.getSQLDate(), 0));

                                if (entry_alarmPoint.setRepeat(entry_repeat) == ReturnCode.AP_CHANGEDATE) {
                                    DataHelper.getInstance().removeAlarmPointFromData(_isRisingPlan, oldDate);
                                    it.remove();
                                }

                                DataHelper.getInstance().saveAlarmPointToData(_isRisingPlan, entry_alarmPoint);
                                currentListClone.put(entry_alarmPoint.getSQLDate(), entry_alarmPoint);
                            }
                        }
                    }

                } else {
                    int i = IntersectionOfRepeatListAndSinglePonint(newRepeat, entry_alarmPoint);

                    if (i != -1) {
                        if (entry_alarmPoint.isProtected()) {
                            isNewChanged = true;
                            newRepeat.set(i, Boolean.FALSE);
                        } else {
                            if (!_isRisingPlan)
                                removeAlarmPointFromAlarmManager(entry_alarmPoint, context);
                            DataHelper.getInstance().removeAlarmPointFromData(_isRisingPlan,
                                    entry_alarmPoint.getSQLDate());
                            it.remove();
                        }
                    }
                }
            }

            if (isNewChanged) {
                if (!newRepeat.contains(Boolean.TRUE)) {
                    newAlarmPoint.setRepeatId(0);
                    // still store new alarm point
                    // that make you keep all your database safe
                } else {
                    newAlarmPoint.setRepeatId(getAvailableRepeatId(currentListClone));
                }
                newAlarmPoint.setRepeat(newRepeat);
            } else {
                newAlarmPoint.setRepeatId(getAvailableRepeatId(currentListClone));
            }
        }

        registerAlarmPointToAlarmManager(newAlarmPoint, context);
        currentListClone.put(newAlarmPoint.getSQLDate(), newAlarmPoint);

        _currentAlarmPointList.clear();
        _currentAlarmPointList.putAll(currentListClone);

        return DataHelper.getInstance().saveAlarmPointToData(_isRisingPlan, newAlarmPoint);
    }

    @SuppressWarnings("unchecked")
    public int removeAlarmPoint(Date date, Context context) {

        // if isRisingPlan, don't remove or save to alarm manager

        // if alarm point is existed,
        // + + protected? => return -1
        // + + remove it

        HashMap<Date, AlarmPoint> currentListClone = (HashMap<Date, AlarmPoint>) _currentAlarmPointList.clone();

        if (currentListClone.containsKey(date)) {

            AlarmPoint oldAlarmPoint = currentListClone.get(date);
            if (oldAlarmPoint.isProtected()) {
                return ReturnCode.PROTECTED;
            }

            if (!_isRisingPlan)
                removeAlarmPointFromAlarmManager(oldAlarmPoint, context);
            currentListClone.remove(date);

            _currentAlarmPointList.clear();
            _currentAlarmPointList.putAll(currentListClone);

            return DataHelper.getInstance().removeAlarmPointFromData(_isRisingPlan, date);
        }
        return ReturnCode.NOT_EXISTS;
    }

    public int overwriteAlarmPointList(HashMap<Date, AlarmPoint> newAlarmPointList, Context context) {

        // View Rising Plan Activity
        // => click on Connect
        // => overwrite everything, but protected

        // if new in current, check current if protected? => remove new
        // if new repeat,
        // => if shadow of new over current protected => remove dayofweek
        // => if shadow of new over shadow current protected => remove dayofweek

        // Overwrite all

        // To safely remove from a collection while iterating over it,
        // an Iterator should be used

        for (Iterator<Map.Entry<Date, AlarmPoint>> it = newAlarmPointList.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Date, AlarmPoint> entry = it.next();
            Date entry_date = entry.getKey();
            AlarmPoint entry_alarmpoint = entry.getValue();

            if (_currentAlarmPointList.containsKey(entry_date)) {
                if (_currentAlarmPointList.get(entry_date).isProtected()) {
                    it.remove();
                }
            } else {
                if (entry_alarmpoint.isRepeat()) {
                    List<Boolean> entry_repeat = entry_alarmpoint.getRepeatList();
                    Boolean isEntryChanged = false;

                    for (Map.Entry<Date, AlarmPoint> current_entry : _currentAlarmPointList.entrySet()) {
                        AlarmPoint cur_entry_alarmPoint = current_entry.getValue();

                        if (cur_entry_alarmPoint.isProtected()) {
                            if (cur_entry_alarmPoint.isRepeat()) {
                                List<Boolean> cur_entry_repeat = cur_entry_alarmPoint.getRepeatList();
                                List<Integer> intersections = IntersectionOfTwoRepeatLists(entry_repeat,
                                        cur_entry_repeat);
                                if (intersections.size() > 0) {
                                    isEntryChanged = true;
                                    for (Integer i : intersections) {
                                        entry_repeat.set(i, Boolean.FALSE);
                                    }
                                }

                            } else {
                                int i = IntersectionOfRepeatListAndSinglePonint(entry_repeat, cur_entry_alarmPoint);
                                if (i != -1) {
                                    isEntryChanged = true;
                                    entry_repeat.set(i, Boolean.FALSE);
                                }
                            }
                        }
                    }

                    if (isEntryChanged) {
                        if (!entry_repeat.contains(Boolean.TRUE)) {
                            entry_alarmpoint.setRepeatId(0);
                        } else {
                            entry_alarmpoint.setRepeatId(getAvailableRepeatId(newAlarmPointList));
                        }

                        if (entry_alarmpoint.setRepeat(entry_repeat) == ReturnCode.AP_CHANGEDATE) {
                            it.remove();
                        }
                        newAlarmPointList.put(entry_alarmpoint.getSQLDate(), entry_alarmpoint);

                    } else {
                        entry_alarmpoint.setRepeatId(getAvailableRepeatId(newAlarmPointList));
                    }

                }
            }
        }

        for (Map.Entry<Date, AlarmPoint> entry : _currentAlarmPointList.entrySet()) {
            AlarmPoint ap = entry.getValue();
            removeAlarmPointFromAlarmManager(ap, context);
            ap.setProperties(ap.isFadeIn(), ap.isProtected(), false);
        }

        //
        // CurrentList contains current alarm points
        // NewList contains new alarm points without protected alarm points of
        // current list
        // CurrentList.putAll(newList) => overwrite the same date alarm points
        // unless it's protected
        _currentAlarmPointList.putAll(newAlarmPointList);

        for (Map.Entry<Date, AlarmPoint> entry : _currentAlarmPointList.entrySet()) {
            registerAlarmPointToAlarmManager(entry.getValue(), context);
        }

        return DataHelper.getInstance().saveAlarmPointListToData(false, _currentAlarmPointList);

    }

    private int registerAlarmPointToAlarmManager(AlarmPoint newAlarmPoint, Context context)
            throws IllegalArgumentException {

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Date alarmPointDate = newAlarmPoint.getSQLDate();
        int dayOfWeek = DateTimeHelper.GetDayOfWeek(alarmPointDate);
        UUID tuneId = newAlarmPoint.get_tuneId();
        Boolean isFadeIn = newAlarmPoint.isFadeIn();
        Calendar cal01 = DateTimeHelper.GetCalendarFromSQLDateTime(alarmPointDate, newAlarmPoint.getSQLTimePoint(1));
        Calendar cal02 = DateTimeHelper.GetCalendarFromSQLDateTime(alarmPointDate, newAlarmPoint.getSQLTimePoint(2));

        if (cal01 == null && cal02 == null)
            throw new IllegalArgumentException("registerAlarmPointToAlarmManager: TIME POINTs HAVEN'T BEEN SET, YET");

        if (newAlarmPoint.isRepeat()) {
            List<Boolean> repeatList = newAlarmPoint.getRepeatList();
            for (int i = dayOfWeek - 1; i < 7; i++) {
                if (repeatList.get(i)) {
                    if (cal01 != null) {
                        Intent i01 = createRepeatIntent(context, RingOffReceiver.class, alarmPointDate, i + 1,
                                ActivityConstant.ACTION_START_GAME, tuneId, isFadeIn);
                        PendingIntent pi01 = PendingIntent.getBroadcast(context,
                                ActivityConstant.REQUESTCODE_PENDINGINTENT, i01, 0);
                        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal01.getTimeInMillis(),
                                DateTimeHelper.ONE_WEEK_MILLIS, pi01);
                        cal01.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    if (cal02 != null) {
                        Intent i02 = createRepeatIntent(context, RingOffReceiver.class, alarmPointDate, i + 1,
                                ActivityConstant.ACTION_START_VLT, tuneId, isFadeIn);
                        PendingIntent pi02 = PendingIntent.getBroadcast(context,
                                ActivityConstant.REQUESTCODE_PENDINGINTENT, i02, 0);
                        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal02.getTimeInMillis(),
                                DateTimeHelper.ONE_WEEK_MILLIS, pi02);
                        cal02.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
            }

        } else {
            if (cal01 != null) {
                Intent i01 = createSingleIntent(context, RingOffReceiver.class, alarmPointDate,
                        ActivityConstant.ACTION_START_GAME, tuneId, isFadeIn);
                PendingIntent pi01 = PendingIntent.getBroadcast(context, ActivityConstant.REQUESTCODE_PENDINGINTENT,
                        i01, 0);
                alarmMgr.set(AlarmManager.RTC_WAKEUP, cal01.getTimeInMillis(), pi01);
            }
            if (cal02 != null) {
                Intent i02 = createSingleIntent(context, RingOffReceiver.class, alarmPointDate,
                        ActivityConstant.ACTION_START_VLT, tuneId, isFadeIn);
                PendingIntent pi02 = PendingIntent.getBroadcast(context, ActivityConstant.REQUESTCODE_PENDINGINTENT,
                        i02, 0);
                alarmMgr.set(AlarmManager.RTC_WAKEUP, cal02.getTimeInMillis(), pi02);
            }
        }

        ComponentName receiver = new ComponentName(context, RingOffBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        return ReturnCode.OK;
    }

    private int removeAlarmPointFromAlarmManager(AlarmPoint oldAlarmPoint, Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Date alarmPointDate = oldAlarmPoint.getSQLDate();
        int dayOfWeek = DateTimeHelper.GetDayOfWeek(alarmPointDate);
        UUID tuneId = oldAlarmPoint.get_tuneId();
        Boolean isFadeIn = oldAlarmPoint.isFadeIn();
        Calendar cal01 = DateTimeHelper.GetCalendarFromSQLDateTime(alarmPointDate, oldAlarmPoint.getSQLTimePoint(1));
        Calendar cal02 = DateTimeHelper.GetCalendarFromSQLDateTime(alarmPointDate, oldAlarmPoint.getSQLTimePoint(2));

        if (cal01 == null && cal02 == null)
            throw new IllegalArgumentException("registerAlarmPointToAlarmManager: TIME POINTs HAVEN'T BEEN SET, YET");

        if (oldAlarmPoint.isRepeat()) {
            List<Boolean> repeatList = oldAlarmPoint.getRepeatList();
            for (int i = dayOfWeek - 1; i < 7; i++) {
                if (repeatList.get(i)) {
                    if (cal01 != null) {
                        Intent i01 = createRepeatIntent(context, RingOffReceiver.class, alarmPointDate, i + 1,
                                ActivityConstant.ACTION_START_GAME, tuneId, isFadeIn);
                        PendingIntent pi01 = PendingIntent.getBroadcast(context,
                                ActivityConstant.REQUESTCODE_PENDINGINTENT, i01, 0);
                        alarmMgr.cancel(pi01);
                        cal01.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    if (cal02 != null) {
                        Intent i02 = createRepeatIntent(context, RingOffReceiver.class, alarmPointDate, i + 1,
                                ActivityConstant.ACTION_START_VLT, tuneId, isFadeIn);
                        PendingIntent pi02 = PendingIntent.getBroadcast(context,
                                ActivityConstant.REQUESTCODE_PENDINGINTENT, i02, 0);
                        alarmMgr.cancel(pi02);
                        cal02.add(Calendar.DAY_OF_MONTH, 1);
                    }
                }
            }
        } else {
            if (cal01 != null) {
                Intent i01 = createSingleIntent(context, RingOffReceiver.class, alarmPointDate,
                        ActivityConstant.ACTION_START_GAME, tuneId, isFadeIn);
                PendingIntent pi01 = PendingIntent.getBroadcast(context, ActivityConstant.REQUESTCODE_PENDINGINTENT,
                        i01, 0);

                alarmMgr.cancel(pi01);
            }
            if (cal02 != null) {
                Intent i02 = createSingleIntent(context, RingOffReceiver.class, alarmPointDate,
                        ActivityConstant.ACTION_START_VLT, tuneId, isFadeIn);
                PendingIntent pi02 = PendingIntent.getBroadcast(context, ActivityConstant.REQUESTCODE_PENDINGINTENT,
                        i02, 0);
                alarmMgr.cancel(pi02);
            }

        }
        if (_currentAlarmPointList == null || _currentAlarmPointList.size() == 0) {
            ComponentName receiver = new ComponentName(context, RingOffBootReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        return ReturnCode.OK;
    }

    private int getAvailableRepeatId(HashMap<Date, AlarmPoint> list) {

        int availableRepeatId = 0;
        List<Boolean> currentRepeadId = new ArrayList<Boolean>(Arrays.asList(new Boolean[8]));
        Collections.fill(currentRepeadId, Boolean.FALSE);

        for (Map.Entry<Date, AlarmPoint> entry : list.entrySet()) {
            currentRepeadId.set(entry.getValue().getRepeatId(), Boolean.TRUE);
        }

        for (int i = 1; i <= 7; i++) {
            if (!currentRepeadId.get(i)) {
                availableRepeatId = i;
                break;
            }
        }
        return availableRepeatId;
    }

    private Intent createSingleIntent(Context ctx, Class<?> cls, Date alarmPointDate, String ACTION, UUID tuneId,
                                      Boolean isFadeIn) {

        // In two date or two repeat set
        // * two intent has two different AcctivityConstant.VALUE_NAME_AP_DATE
        // => INTENT_TYPE

        // In one repeat alarm point
        // * 2-7 intents have 2-7 different
        // ActivityConstant.VALUE_NAME_AP_DAYOFWEEK => INTENT_TYPE

        // In a date of single alarm point or In a dayOfWeek of repeat alarm
        // point
        // * two intent has two different ActivityConstant.VALUE_NAME_AP_PERIOD
        // => different INTENT_ACTION

        // Intent also contains tuneId, isFadeIn value... => INTENT_EXTRAS

        Intent i = new Intent(ctx, cls);
        i.setAction(ACTION);
        String TYPE = String.valueOf(alarmPointDate.getTime());
        i.setType(TYPE);
        i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ID, tuneId);
        i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, isFadeIn);
        return i;
    }

    private Intent createRepeatIntent(Context ctx, Class<?> cls, Date alarmPointDate, int dayOfWeek, String ACTION,
                                      UUID tuneId, Boolean isFadeIn) {
        Intent i = new Intent(ctx, cls);
        i.setAction(ACTION);
        String TYPE = String.valueOf(alarmPointDate.getTime()) + "|" + dayOfWeek;
        i.setType(TYPE);
        i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ID, tuneId);
        i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, isFadeIn);
        return i;
    }

    public void RegisterAllAlarmPoint(Context context) {

        HashMap<Date, AlarmPoint> currentAlarmPointList = DataHelper.getInstance().getAlarmPointListFromData(false);
        Date today = DateTimeHelper.GetToday();
        Time currentTime = new Time(DateTimeHelper.GetCurrentTime());

        // Test for Today
        AlarmPoint currentAlarmPoint = GetAlarmPointFromList(currentAlarmPointList, today);

        if (currentAlarmPoint != null) {
            if (!currentAlarmPoint.isRepeat()) {
                Time time01 = currentAlarmPoint.getSQLTimePoint(1);
                Boolean isTime01NotValue = time01 == null || DateTimeHelper.CompareTo(currentTime, time01) > 0;

                Time time02 = currentAlarmPoint.getSQLTimePoint(2);
                Boolean isTime02NotValue = time02 == null || DateTimeHelper.CompareTo(currentTime, time02) > 0;

                Boolean removable = (isTime01NotValue && isTime02NotValue);
                if (removable) {
                    DataHelper.getInstance().removeAlarmPointFromData(false, today);
                }
            }
        }
        // Remove past and Register now and future
        for (Iterator<Map.Entry<Date, AlarmPoint>> it = currentAlarmPointList.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Date, AlarmPoint> entry = it.next();
            if (DateTimeHelper.IsPast(entry.getKey())) {
                DataHelper.getInstance().removeAlarmPointFromData(false, entry.getKey());
                it.remove();
            } else {
                registerAlarmPointToAlarmManager(entry.getValue(), context);
            }
        }
    }

}
