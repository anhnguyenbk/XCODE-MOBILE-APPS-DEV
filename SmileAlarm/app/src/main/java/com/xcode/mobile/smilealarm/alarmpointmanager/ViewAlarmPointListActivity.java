package com.xcode.mobile.smilealarm.alarmpointmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;

import com.xcode.mobile.smilealarm.ActivityConstant;
import com.xcode.mobile.smilealarm.NotificationHelper;
import com.xcode.mobile.smilealarm.R;
import com.xcode.mobile.smilealarm.ViewHelper;
import com.xcode.mobile.smilealarm.tunemanager.RecommendedTunesActivity;

public class ViewAlarmPointListActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences _settings;
    private DrawerLayout drawerLayout;
    private CaldroidAlarmPointListFragment _vAPListFragment;
    private Button btnRemoveAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_alarm_point_list_layout);
        setTitle(R.string.title_activity_view_alarm_point_list);

        setupToolbar();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.action_vapl_create_rising_plan:
                        intent = new Intent(ViewAlarmPointListActivity.this, CreateRisingPlanActivity.class);
                        startActivityForResult(intent, ActivityConstant.REQUESTCODE_VAPLIST_CREATE_R);
                        drawerLayout.closeDrawers();
                        return true;
                    case R.id.action_vapl_modify_recommended_tunes:
                        intent = new Intent(ViewAlarmPointListActivity.this, RecommendedTunesActivity.class);
                        intent.setAction(ActivityConstant.ACTION_MODIFY_TUNES);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        return true;
                }
                return false;
            }
        });

        _settings = getSharedPreferences(PREFS_NAME, 0);
        boolean firstTime = _settings.getBoolean("firstTimeMode", true);
        if (firstTime)
            firstTimeAction();

        btnRemoveAll = (Button) findViewById(R.id.btn_VAPL_01);
        btnRemoveAll.setText(R.string.btn_VAPL_Remove);
        ViewHelper.disableButton(btnRemoveAll);

        _vAPListFragment = new CaldroidAlarmPointListFragment(false, btnRemoveAll);

        if (savedInstanceState != null)
            _vAPListFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.calendar, _vAPListFragment);
        ft.commit();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.mipmap.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void firstTimeAction() {

        NotificationHelper.ShowConfirmation(this, new NotificationHelper.Predicate() {

            public void yesFunction(DialogInterface dialog, int which) {
                Intent intent = new Intent(ViewAlarmPointListActivity.this, CreateRisingPlanActivity.class);
                ViewAlarmPointListActivity.this.startActivityForResult(intent, ActivityConstant.REQUESTCODE_VAPLIST_CREATE_R);
            }

            public void noFunction(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        }, getString(R.string.dialog_title_confirmation), getString(R.string.confirmation_create_rising_plan));

        SharedPreferences.Editor editor = _settings.edit();
        editor.putBoolean("firstTimeMode", false);
        editor.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityConstant.REQUESTCODE_VAPLIST_CREATE_R) {
            if (resultCode == RESULT_OK) {
                _vAPListFragment.refreshView();
            }
        }
    }

}
