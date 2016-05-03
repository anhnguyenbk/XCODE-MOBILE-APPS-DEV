package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.xcode.mobile.smilealarm.NotificationHelper;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;

public class ViewRisingPlanActivity extends AppCompatActivity {

    private CaldroidAlarmPointListFragment _vAPListFragment;
    private Button btnRemoveAll;
    private Button btnConnect;
    private Boolean _isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_alarm_point_list_layout);

        ViewHelper.setupToolbar(this);

        btnConnect = (Button) findViewById(R.id.btn_VAPL_01);
        btnConnect.setText(R.string.btn_VAPL_Connect);

        btnRemoveAll = (Button) findViewById(R.id.btn_VAPL_02);
        btnRemoveAll.setText(R.string.btn_VAPL_Remove);
        btnRemoveAll.setVisibility(View.VISIBLE);
        ViewHelper.disableButton(btnRemoveAll);

        _vAPListFragment = new CaldroidAlarmPointListFragment(true, btnRemoveAll);

        if (savedInstanceState != null) {
            _vAPListFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.calendar, _vAPListFragment);
        ft.commit();

        _isConnected = false;

        btnConnect.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                ConnectAction();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_vrp_connect);
        boolean visible = false;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            visible = true;
        }
        item.setVisible(visible);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_vrp_connect:
                ConnectAction();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void ShowNotification(boolean isSuccessful) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewRisingPlanActivity.this);
        builder.setTitle(R.string.dialog_title_result);
        if (isSuccessful) {
            builder.setMessage(R.string.notification_connect_successfully);
            _isConnected = true;
        } else {
            builder.setMessage(R.string.notification_connect_error);
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();

    }

    private void ConnectAction() {
        NotificationHelper.ShowConfirmation(ViewRisingPlanActivity.this, new NotificationHelper.Predicate() {

            public void yesFunction(DialogInterface dialog, int which) {
                ShowNotification(RisingPlanHandler.Connect(getBaseContext()) == ReturnCode.OK);
            }

            public void noFunction(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        }, getString(R.string.dialog_title_confirmation), getString(R.string.confirmation_connect));
    }
}
