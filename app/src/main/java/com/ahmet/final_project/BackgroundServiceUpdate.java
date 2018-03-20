package com.ahmet.final_project;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


public class BackgroundServiceUpdate extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    DbHandler db;

    @Override
    public void onCreate() {
        super.onCreate();
        //registering the states/actions
        //Specifying the type of intent, screen on and off
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        //adding screen off to trigger when the screen is off
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        //for startup service
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        //checks if the user wakes up the device
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);

        //Broadcast receiver to register the screen state
        BroadcastReceiver broadcastReceiver = new PowerButtonActivation();
        registerReceiver(broadcastReceiver, intentFilter);

        String sent = "SENT";
        String delivered = "DELIVERED";

        //Broadcast receiver to check if messages are sent
        BroadcastReceiver sentMessageBroadcastReceiver = new SentMessageBroadcastReceiver();
        registerReceiver(sentMessageBroadcastReceiver, new IntentFilter(sent));

        //Broadcast receiver to check if messages are delivered
        BroadcastReceiver deliveredMessageBroadcastReceiver = new DeliveredMessageBroadcastReceiver();
        registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(delivered));

        // intent filters for wifi p2p broadcast receiver
//        IntentFilter intentFilter2 = new IntentFilter();
//        //wifi p2p status
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        // checks for the change of the available peers.
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        // checks the state of the connectivity
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        // checks for device's status
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//
//        BroadcastReceiver p2pBroadcastReceiver = new P2PBroadcastReceiver();
//        registerReceiver(p2pBroadcastReceiver, intentFilter2);



    }
    //non-removable notification to send messages when clicked via the broadcast receiver
    public void LockScreenNotification() {

        String notfirsttime = "1";

        db = new DbHandler(getApplicationContext());
        db.OnOPen();
        String notFirstTimeTrue = db.getNotFirstTime(notfirsttime);

        //if it's not first time then build the notification
        if (notfirsttime.equals(notFirstTimeTrue)) {

        //https://developer.android.com/training/notify-user/build-notification.html#notify
        Intent intent = new Intent(this, PowerButtonActivation.class);
        //passing this value to powerbuttonactivation class so it gets called when clicked
        intent.putExtra("Clicked", "NotificationClicked");
        //pendingintent will give permission to notification manager to use the specified service and apply actions via the broadcast receiver
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //https://developer.android.com/guide/topics/ui/notifiers/notifications.html#lockscreenNotification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_tick)
                        .setColor(Color.RED)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setContentTitle("Send Emergency message now")
                        //user cannot remove the notification
                        .setOngoing(true)
                        .setPriority(Notification.PRIORITY_MAX);
        //apply the action
        builder.setContentIntent(pendingIntent);

        //Show the notification
        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
        } else {
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new DbHandler(getApplicationContext());
        db.OnOPen();
        LockScreenNotification();
        db.onClose();
        //restart the service if its killed
        return START_STICKY;
    }

}

