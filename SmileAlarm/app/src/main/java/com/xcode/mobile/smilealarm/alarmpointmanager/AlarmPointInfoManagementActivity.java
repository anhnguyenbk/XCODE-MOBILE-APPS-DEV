package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.DateTimeHelper;
import com.xcode.mobile.smilealarm.DateTimePickerFragment;
import com.xcode.mobile.smilealarm.NotificationHelper;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesActivity;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesHandler;
import com.xcode.mobile.smilealarm.tunemanager.Tune;

public class AlarmPointInfoManagementActivity extends AppCompatActivity
        implements View.OnClickListener, DateTimePickerFragment.OnTimeSetListener {

    private final int CHOOSE_RECOMMEND_TUNE = 2;
    private final int CHOOSE_USER_TUNE = 1;
    private AlarmPoint _keyValue;
    private View _date;
    private View _timeMorning;
    private View _timeEvening;
    private View _tune;
    private View _isIncVol;
    private View _isRepeat;
    private View _repeatGroup;
    private View _isProtected;
    private View _btnSave;
    private ToggleButton[] _repeatDays = new ToggleButton[7];
    private DateTimePickerFragment _timePicker;
    private Date _dateValue;

    private RecommendedTunesHandler _recommendedTunesHandler = RecommendedTunesHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm_point_info_management);

        // Android Studio
        ViewHelper.setupToolbar(this);

        _date = findViewById(R.id.alarm_date);
        _timeMorning = findViewById(R.id.alarm_time_morning);
        _timeEvening = findViewById(R.id.alarm_time_evening);
        _tune = findViewById(R.id.alarm_tune);
        _isIncVol = findViewById(R.id.alarm_increasing_vol);
        _isRepeat = findViewById(R.id.alarm_repeat);
        _repeatGroup = findViewById(R.id.alarm_repeat_day);
        _isProtected = findViewById(R.id.alarm_protected);
        _btnSave = findViewById(R.id.alarm_save);
        View btnRemove = findViewById(R.id.alarm_remove);

        for (int i = 0; i < _repeatDays.length; i++) {
            switch (i) {
                // alarmPoint.repeat : 0-6
                case 0:
                    _repeatDays[i] = (ToggleButton) findViewById(R.id.alarm_repeat_sun);
                    break;

                case 1:
                    _repeatDays[i] = (ToggleButton) findViewById(R.id.alarm_repeat_mon);
                    break;

                case 2:
                    _repeatDays[i] = (ToggleButton) findViewById(R.id.alarm_repeat_tue);
                    break;

                case 3:
                    _repeatDays[i] = (ToggleButton) findViewById(R.id.alarm_repeat_wed);
                    break;

                case 4:
                    _repeatDays[i] = (ToggleButton) findViewById(R.id.alarm_repeat_thu);
                    break;

                case 5:
                    _repeatDays[i] = (ToggleButton) findViewById(R.id.alarm_repeat_fri);
                    break;

                case 6:
                    _repeatDays[i] = (ToggleButton) findViewById(R.id.alarm_repeat_sat);
                    break;

                default:
                    break;
            }

            _repeatDays[i].setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    _repeatDays[DateTimeHelper.GetDayOfWeek(_dateValue) - 1].setChecked(true);
                }
            });
        }

        _isRepeat.setOnClickListener(this);
        _timeMorning.setOnClickListener(this);
        _timeEvening.setOnClickListener(this);
        _tune.setOnClickListener(this);
        _isIncVol.setOnClickListener(this);
        _isProtected.setOnClickListener(this);
        _btnSave.setOnClickListener(this);
        btnRemove.setOnClickListener(this);

        _timePicker = new DateTimePickerFragment();

        prepareData(getIntent());
    }

    private void prepareData(Intent data) {
        Calendar c = Calendar.getInstance();
        int hour_1 = -1, minute_1 = -1, hour_2 = -1, minute_2 = -1;

        _keyValue = (AlarmPoint) data.getExtras().getSerializable(ActivityConstant.VALUE_NAME_VAPLIST_APINFO);

        // Date
        _dateValue = _keyValue.getSQLDate();
        c.setTime(_dateValue);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        _date.setTag(new int[]{year, month, day});
        ((TextView) _date)
                .setText(new DateFormatSymbols(Locale.getDefault()).getMonths()[month] + " " + day + ", " + year);

        // Time (in the morning or in the evening)
        Time time01 = _keyValue.getSQLTimePoint(1);
        Time time02 = _keyValue.getSQLTimePoint(2);

        if (time01 == null && time02 == null) {
            this.setTitle(R.string.add_alarm_title);
            ViewHelper.disableButton((Button) _btnSave);
        } else {
            if (time01 != null) {
                c = DateTimeHelper.GetCalendarFromSQLDateTime(_dateValue, time01);
                hour_1 = c.get(Calendar.HOUR_OF_DAY);
                minute_1 = c.get(Calendar.MINUTE);
            }

            if (time02 != null) {
                c = DateTimeHelper.GetCalendarFromSQLDateTime(_dateValue, time02);
                hour_2 = c.get(Calendar.HOUR_OF_DAY);
                minute_2 = c.get(Calendar.MINUTE);
            }
            this.setTitle(R.string.alarm_info_title);
        }
        _timeMorning.setTag(new int[]{hour_1, minute_1});
        _timeEvening.setTag(new int[]{hour_2, minute_2});
        DateTimeHelper.SetTime(this, (TextView) _timeMorning, hour_1, minute_1);
        DateTimeHelper.SetTime(this, (TextView) _timeEvening, hour_2, minute_2);

        // Alarm Tune
        Tune tune = _recommendedTunesHandler.getTuneFromRecommendList(_keyValue.get_tuneId());
        _tune.setTag(tune);
        ((TextView) _tune).setText(tune.get_name());

        // Increasing volume
        ((CheckBox) _isIncVol).setChecked(_keyValue.isFadeIn());

        // Repeat
        ((CheckBox) _isRepeat).setChecked(_keyValue.isRepeat());

        // Repeat List
        List<Boolean> repeatList = _keyValue.getRepeatList();
        for (int i = 0; i < 7; i++) {
            _repeatDays[i].setChecked(repeatList.get(i));
        }

        if (!((CheckBox) _isRepeat).isChecked()) {
            _repeatGroup.setVisibility(View.GONE);
        }

        // Protected
        ((CheckBox) _isProtected).setChecked(_keyValue.isProtected());
    }

    public void btnSave_onClick(View v) {
        // collect data

        // Date cannot be changed

        int[] time1 = (int[]) _timeMorning.getTag();
        int hour1 = time1[0];
        int minute1 = time1[1];

        int[] time2 = (int[]) _timeEvening.getTag();
        int hour2 = time2[0];
        int minute2 = time2[1];

        // check if input Time is after current time
        Boolean isValid = true;
        Time t1 = null, t2 = null;

        Calendar currentTime = Calendar.getInstance();
        Calendar alarmPointTime = Calendar.getInstance();
        alarmPointTime.setTime(_dateValue);

        // Time in the morning
        if (hour1 != -1 && minute1 != -1) {
            alarmPointTime.set(Calendar.HOUR_OF_DAY, hour1);
            alarmPointTime.set(Calendar.MINUTE, minute1);
            alarmPointTime.set(Calendar.SECOND, 0);
            alarmPointTime.set(Calendar.MILLISECOND, 0);
            if (alarmPointTime.compareTo(currentTime) <= 0)
                isValid = false;
            else {
                t1 = new Time(alarmPointTime.getTimeInMillis());
            }
        }

        // Time in the evening
        if (hour2 != -1 && minute2 != -1) {
            alarmPointTime.set(Calendar.HOUR_OF_DAY, hour2);
            alarmPointTime.set(Calendar.MINUTE, minute2);
            alarmPointTime.set(Calendar.SECOND, 0);
            alarmPointTime.set(Calendar.MILLISECOND, 0);
            if (alarmPointTime.compareTo(currentTime) <= 0)
                isValid = false;
            else {
                t2 = new Time(alarmPointTime.getTimeInMillis());
            }
        }

        if (isValid) {
            Tune tune = (Tune) _tune.getTag();

            boolean isIncVol = ((CheckBox) _isIncVol).isChecked();
            boolean isProtected = ((CheckBox) _isProtected).isChecked();
            List<Boolean> repeatDays = new ArrayList<Boolean>();
            for (ToggleButton btn : _repeatDays) {
                repeatDays.add(btn.isChecked());
            }

            if (t1 != null)
                _keyValue.setTimePoint(t1, 1);
            if (t2 != null)
                _keyValue.setTimePoint(t2, 2);

            _keyValue.set_tuneId(tune.get_keyId());
            if (!isProtected)
                _keyValue.removeProtect();
            _keyValue.setProperties(isIncVol, isProtected, _keyValue.isColor());
            _keyValue.setRepeat(repeatDays);

            Intent i = new Intent();
            i.putExtra(ActivityConstant.VALUE_NAME_VAPLIST_APINFO, _keyValue);
            setResult(ActivityConstant.RESULTCODE_VAPLIST_APINFO_SAVE, i);
            finish();
        } else {
            ShowErrorTimeNotification();
            ViewHelper.disableButton((Button) _btnSave);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            // Date cannot be changed

            case R.id.alarm_time_morning:
                _timePicker.setView((TextView) _timeMorning, true);
                _timePicker.show(getFragmentManager(), "timePicker");

                break;

            case R.id.alarm_time_evening:
                _timePicker.setView((TextView) _timeEvening, true);
                _timePicker.show(getFragmentManager(), "timePicker");
                break;

            case R.id.alarm_tune:
                new ChooseTunePopupDialog().show(getSupportFragmentManager(), "chooseTuneDialog");
                break;

            case R.id.alarm_repeat:
                if (((CheckBox) _isRepeat).isChecked()) {
                    _repeatGroup.setVisibility(View.VISIBLE);
                    // day of week of the date will be chosen
                    _repeatDays[DateTimeHelper.GetDayOfWeek(_dateValue) - 1].setChecked(true);
                } else {
                    _repeatGroup.setVisibility(View.GONE);
                    // reset repeatList
                    for (ToggleButton btn : _repeatDays) {
                        btn.setChecked(false);
                    }
                }
                break;

            case R.id.alarm_save:
                btnSave_onClick(v);
                break;

            case R.id.alarm_remove:
                Intent i = new Intent();
                setResult(ActivityConstant.RESULTCODE_VAPLIST_APINFO_REMOVE, i);
                finish();
                break;

            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_RECOMMEND_TUNE) {
                int i = data.getIntExtra(ActivityConstant.VALUE_NAME_APINFO_RECOMLIST, 0);
                Tune tune = _recommendedTunesHandler.get_recommendTunes().get(i);
                ((TextView) _tune).setText(tune.get_name());
                _tune.setTag(tune);
            } else if (requestCode == CHOOSE_USER_TUNE) {
                Uri selectedAudio = data.getData();
                File tuneFile = new File(selectedAudio.getPath());
                Tune tune = new Tune(tuneFile.getName(), selectedAudio.toString());
                _tune.setTag(tune);
                ((TextView) _tune).setText(tune.get_name());

                // save new music to user's list
                _recommendedTunesHandler.addTuneToRecommendList(tune);
                Toast.makeText(getApplicationContext(), tune.get_name() + " was added to Your Recommended Tune List",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onTimeSet(TextView timeView, TimePicker view, int hourOfDay, int minute) {
        if (timeView != null) {
            Boolean isValid = true;

            // check if input Time is valid to period of day
            if (timeView.getId() == R.id.alarm_time_morning) {
                if (hourOfDay > 12) {
                    isValid = false;
                }
            } else if (timeView.getId() == R.id.alarm_time_evening) {
                if (hourOfDay <= 12) {
                    isValid = false;
                }
            }

            if (isValid) {
                // check if input Time is after current time
                Calendar currentTime = Calendar.getInstance();
                Calendar alarmPointTime = Calendar.getInstance();
                alarmPointTime.setTime(_dateValue);
                alarmPointTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                alarmPointTime.set(Calendar.MINUTE, minute);
                alarmPointTime.set(Calendar.SECOND, 0);
                alarmPointTime.set(Calendar.MILLISECOND, 0);
                if (alarmPointTime.compareTo(currentTime) <= 0)
                    isValid = false;

                if (isValid) {
                    timeView.setTag(new int[]{hourOfDay, minute});
                    DateTimeHelper.SetTime(this, timeView, hourOfDay, minute);
                    ViewHelper.enableButton((Button) _btnSave);
                } else {
                    ShowErrorTimeNotification();
                }
            }
        }
    }

    private void ShowErrorTimeNotification() {
        NotificationHelper.ShowError(this, getString(R.string.notification_apInfo_input_time_error));
    }

    private class ChooseTunePopupDialog extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View _dialog = inflater.inflate(R.layout.choose_tune_popup_layout, container, false);
            getDialog().setTitle("Choose Tune From:");
            Button recommend = (Button) _dialog.findViewById(R.id.choose_tune_recommend);
            recommend.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(AlarmPointInfoManagementActivity.this, RecommendedTunesActivity.class);

                    // we need to getActivity()
                    // * if we start something from Dialog
                    getActivity().startActivityForResult(intent, CHOOSE_RECOMMEND_TUNE);

                    dismiss();
                }
            });

            Button mytune = (Button) _dialog.findViewById(R.id.choose_tune_mytune);
            mytune.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    try {
                        Intent audioPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        audioPickerIntent.setType("audio/*");
                        // we need to getActivity()
                        // * if we start something from Dialog
                        getActivity().startActivityForResult(audioPickerIntent, CHOOSE_USER_TUNE);
                    } catch (Exception e) {
                        new AlertDialog.Builder(getActivity()).setTitle("Error")
                                .setMessage("No application could explore audio file!")
                                .setPositiveButton(android.R.string.yes, null)
                                .setIcon(android.R.drawable.ic_dialog_alert).show();
                    }
                    dismiss();
                }
            });
            return _dialog;
        }
    }
}
