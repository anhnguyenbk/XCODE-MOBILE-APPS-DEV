package com.xcode.mobile.smilealarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.UUID;

import com.xcode.mobile.smilealarm.alarmpointmanager.AlarmPointListHandler;

public class RingOffReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // GET AND PROCESS INTENT'S DATA
        boolean isMorning = intent.getAction().compareTo(ActivityConstant.ACTION_START_GAME) == 0;
        UUID tuneId = (UUID) intent.getExtras().getSerializable(ActivityConstant.VALUE_NAME_AP_TUNE_ID);
        Boolean isFadeIn = intent.getBooleanExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, true);

        AlarmPointListHandler.ProcessAfterRingOff(context, isMorning);

        // SEND TO NEW ACTIVITY
        Intent i = new Intent();

        if (isMorning) {
            i.setClassName("com.xcode.mobile.smilealarm", "com.xcode.mobile.smilealarm.AlarmActivity");
            i.setAction(intent.getAction());
        } else {
            i.setClassName("com.xcode.mobile.smilealarm", "com.xcode.mobile.smilealarm.listmanager.ViewVLTCheckListActivity");
            i.setAction(intent.getAction());
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ID, tuneId);
        i.putExtra(ActivityConstant.VALUE_NAME_AP_TUNE_ATTR, isFadeIn);
        context.startActivity(i);
    }
}
