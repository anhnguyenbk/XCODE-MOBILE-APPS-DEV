package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.DateTimeHelper;
import com.xcode.mobile.smilealarm.NotificationHelper;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;

public class CaldroidAlarmPointListFragment extends CaldroidFragment {
    private AlarmPointListHandler _alarmPointListHandler;
    private AlarmPoint _keyValue;
    private Button _btnRemove;
    private HashMap<Date, AlarmPoint> _selectedAlarmPointList;
    private Boolean _isRisingPlan;

    public CaldroidAlarmPointListFragment() {
        super();
    }

    public CaldroidAlarmPointListFragment(Boolean isRisingPlan, Button btnRemove) {
        super();
        _keyValue = null;

        _isRisingPlan = isRisingPlan;

        _alarmPointListHandler = new AlarmPointListHandler(_isRisingPlan);

        _selectedAlarmPointList = new HashMap<Date, AlarmPoint>();


        this.extraData.clear();
        this.extraData.put(StringKey.ALP_LIST, _alarmPointListHandler.getCurrentList());
        this.extraData.put(StringKey.SELECTED_LIST, _selectedAlarmPointList);

        _btnRemove = btnRemove;

        _btnRemove.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                removeAllItemsAction();
            }
        });

        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(java.util.Date utilDate, View view) {

                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                if (!DateTimeHelper.IsPast(sqlDate)) {

                    AlarmPoint selected = _alarmPointListHandler.getAlarmPoint(sqlDate);
                    if (selected == null || selected.getSQLDate().compareTo(sqlDate) > 0) {
                        selected = new AlarmPoint(sqlDate);
                    }

                    if (_selectedAlarmPointList.containsKey(selected.getSQLDate())) {
                        _selectedAlarmPointList.remove(selected.getSQLDate());
                    } else {
                        _selectedAlarmPointList.put(selected.getSQLDate(), selected);
                    }

                    extraData.put(StringKey.SELECTED_LIST, _selectedAlarmPointList);
                    refreshView();

                    if (_selectedAlarmPointList.size() > 0) ViewHelper.enableButton(_btnRemove);
                    else ViewHelper.disableButton(_btnRemove);
                }
            }

            @Override
            public void onLongClickDate(java.util.Date utilDate, View view) {

                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                if (DateTimeHelper.IsNowAndFuture(sqlDate)) {

                    _keyValue = _alarmPointListHandler.getAlarmPoint(sqlDate);
                    if (_keyValue == null || _keyValue.getSQLDate().compareTo(sqlDate) > 0) {
                        _keyValue = new AlarmPoint(sqlDate);

                        if (_isRisingPlan) {
                            _keyValue.setColor();
                        }
                    }

                    Intent i = new Intent(getActivity(), AlarmPointInfoManagementActivity.class);
                    i.putExtra(ActivityConstant.VALUE_NAME_VAPLIST_APINFO, _keyValue);
                    startActivityForResult(i, ActivityConstant.REQUESTCODE_VAPLIST_APINFO);
                } else {
                    NotificationHelper.ShowError(getActivity(),
                            getString(R.string.notification_apInfo_input_time_error));
                }
            }

            @Override
            public void onCaldroidViewCreated() {
                super.onCaldroidViewCreated();
                Toast.makeText(getActivity().getApplicationContext(),
                        "Long click on cell to edit\nShort click on cell to select", Toast.LENGTH_LONG).show();
            }

        };
        this.setCaldroidListener(listener);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
        return new CaldroidAlarmPointListAdapter(getActivity(), month, year, getCaldroidData(), extraData);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityConstant.REQUESTCODE_VAPLIST_APINFO) {

            if (resultCode == ActivityConstant.RESULTCODE_VAPLIST_APINFO_SAVE) {
                _keyValue = (AlarmPoint) data.getExtras().getSerializable(ActivityConstant.VALUE_NAME_VAPLIST_APINFO);
                _alarmPointListHandler.saveAlarmPoint(_keyValue, getActivity());
                _selectedAlarmPointList.clear();
                ViewHelper.disableButton(_btnRemove);
            }

            if (resultCode == ActivityConstant.RESULTCODE_VAPLIST_APINFO_REMOVE) {
                removeAlarmPoint(_keyValue.getSQLDate());
                _selectedAlarmPointList.clear();
                ViewHelper.disableButton(_btnRemove);
            }

            // put will decide to add or edit HashMap automatically
            this.extraData.put(StringKey.ALP_LIST, _alarmPointListHandler.getCurrentList());
            this.extraData.put(StringKey.SELECTED_LIST, _selectedAlarmPointList);
            this.refreshView();
        }
    }

    private int removeAlarmPointList() {

        int count = 0;

        for (Map.Entry<Date, AlarmPoint> entry : _selectedAlarmPointList.entrySet()) {

            java.sql.Date date = entry.getKey();

            if (removeAlarmPoint(date)) {
                count++;
            }
        }

        _selectedAlarmPointList.clear();
        this.extraData.put(StringKey.ALP_LIST, _alarmPointListHandler.getCurrentList());
        this.extraData.put(StringKey.SELECTED_LIST, _selectedAlarmPointList);
        this.refreshView();

        return count;
    }

    private void removeAllItemsAction() {
        if (_selectedAlarmPointList.size() > 0) {

            NotificationHelper.ShowConfirmation(CaldroidAlarmPointListFragment.this.getActivity(),
                    new NotificationHelper.Predicate() {
                        public void yesFunction(DialogInterface dialog, int which) {
                            if (removeAlarmPointList() != 0) {
                                Toast.makeText(
                                        CaldroidAlarmPointListFragment.this.getActivity().getApplicationContext(),
                                        "Remove Successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(
                                        CaldroidAlarmPointListFragment.this.getActivity().getApplicationContext(),
                                        "No Alarm Points to remove!", Toast.LENGTH_SHORT).show();
                            }
                            ViewHelper.disableButton(_btnRemove);
                        }

                        public void noFunction(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }, getResources().getString(R.string.dialog_title_confirmation),
                    getResources().getString(R.string.confirmation_remove));
        }
    }

    private Boolean removeAlarmPoint(java.sql.Date date) {
        return _alarmPointListHandler.removeAlarmPoint(date, getActivity()) == ReturnCode.OK;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vrp_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.action_vrp_remove_all);
        boolean visible = false;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            visible = true;
        }
        item.setVisible(visible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_vrp_remove_all:
                removeAllItemsAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
