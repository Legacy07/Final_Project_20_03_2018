package com.ahmet.final_project;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;

public class AirplaneModeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //https://stackoverflow.com/questions/4319212/how-can-one-detect-airplane-mode-on-android
        //if airplane mode is turned off
        if (Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 0) {
            airplaneModeChanged(false);
        } else {
            airplaneModeChanged(true);
        }

    }

    public void Register(Context context) {
        //create intentfilter to listen for airplane mode change and register the receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        context.registerReceiver(this, intentFilter);
    }

    public void airplaneModeChanged(boolean enabled) {

    }

}
