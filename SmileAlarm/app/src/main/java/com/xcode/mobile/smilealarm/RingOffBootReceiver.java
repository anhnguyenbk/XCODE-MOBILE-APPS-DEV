package com.xcode.mobile.smilealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xcode.mobile.smilealarm.alarmpointmanager.AlarmPointListHandler;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
// BEGIN_INCLUDE(autostart)
public class RingOffBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            new AlarmPointListHandler(false).RegisterAllAlarmPoint(context);
    }
}
//END_INCLUDE(autostart)
