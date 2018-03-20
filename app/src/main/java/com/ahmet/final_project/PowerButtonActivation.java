package com.ahmet.final_project;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PowerButtonActivation extends BroadcastReceiver {

    //to check if the screen is off or not
    public boolean screenState;
    //counting the amount of times the power button is pressed
    private static int countLocked = 0;
    DbHandler db;
    private static final int PERMISSION_REQUEST_LOCATION = 0;

    LocationManager locationManager;
    double longitude = 0;
    double latitude = 0;
    int countTimer = 0;
    long time1;
    long time2;


    Location location;
    String networkProvider = "";

    String notificationTitle = "Successfully Message Sent";
    String notificationBody = "";
    String updateMessageTitle = "Successfully Update Message is Sent";

    BroadcastReceiver deliveredMessageBroadcastReceiver = new DeliveredMessageBroadcastReceiver();
    BroadcastReceiver sentMessageBroadcastReceiver = new SentMessageBroadcastReceiver();
    String finalLocation = null;

    Home home;
    String[] time = new String[2];

    @Override
    public void onReceive(Context context, Intent intent) {
        home = new Home();
        db = new DbHandler(context);

//        //get the data from db
//        String contact1 = db.getContact1().toString();
//        String message = db.getMessage().toString();
//        String name = db.getName().toString();
//
//        //send the sms if screen is on or off
//        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//            countLocked++;
//            screenState = false;
//
//        }
//        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//            countLocked++;
//            screenState = true;
//
//        }

//        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
//            try {
//                home.LockScreenNotification();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(context, "Error" + e, Toast.LENGTH_LONG).show;
//
//            }
//
//        }
        //if the power button is pressed 3 times then trigger the app

        //check later - https://stackoverflow.com/questions/44127283/press-power-button-multiple-times-calls-emergency-contact-android
//        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//            countLocked++;
//            screenState = true;
//
//            if (countLocked == 1) {
//                time1 = Calendar.getInstance().getTimeInMillis();
//
//            }
//            if (countLocked == 2) {
//                time2 = Calendar.getInstance().getTimeInMillis();
//
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.UK);
//                Date date1 = null;
//                Date date2 = null;
//                try {
//                    //https://stackoverflow.com/questions/5369682/get-current-time-and-date-on-android
//                    //https://stackoverflow.com/questions/16516888/how-to-get-current-date-time-in-milliseconds-in-android
//                    //https://stackoverflow.com/questions/10364383/how-to-transform-currenttimemillis-to-a-readable-date-format
//
//                    //error here on 120 saying 'Unparseable date: ' fix this and make it work
//                    date1 = simpleDateFormat.parse(String.valueOf(time1));
//                    date2 = simpleDateFormat.parse(String.valueOf(time2));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                long difference = date2.getTime() - date1.getTime();
//
//                if (difference < 10000) {
//                    Toast.makeText(context, "Under 4 seconds", Toast.LENGTH_LONG).show();
////                    db.OnOPen();
////                    //get location
////                    GetLocation(context);
////                    if (finalLocation == null) {
////
////                    } else {
////                        UpdateLocation(context);
////                    }
////
////                    String sent = "SENT";
////                    String delivered = "DELIVERED";
////
////                    //initialise pending intents for broadcast receiver to listen for whenever the message is sent and delivered
////                    PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(
////                            sent), 0);
////                    PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, 0,
////                            new Intent(delivered), 0);
////                    //register the receivers
////                    context.getApplicationContext().registerReceiver(sentMessageBroadcastReceiver, new IntentFilter(sent));
////                    context.getApplicationContext().registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(delivered));
////                    //send the message
////                    home.SendMessage(name, message, number, sentPendingIntent, deliveredPendingIntent, latitude, longitude);
////                    // /https://developer.android.com/training/notify-user/build-notification.html
////                    //Sending notification after a successful sent message
////                    //SendUpdateMessage(context);
////
////                    db.onClose();
//
//                    countLocked = 0;
//
//                }
//            }
//        }
        String notificationClicked = intent.getStringExtra("Clicked");
//        notificationBody = "Message sent to: " + contact1;

        //location manager helps to get current location
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (notificationClicked != null) {
            db.OnOPen();
            //get location
            GetLocation(context);

            //get the data from db
            String contact1 = db.getContact1().toString();
            String message = db.getMessage().toString();
            String name = db.getName().toString();

            if (finalLocation == null) {

            } else {
                UpdateLocation(context);
            }

            String sent = "SENT";
            String delivered = "DELIVERED";

            //initialise pending intents for broadcast receiver to listen for whenever the message is sent and delivered
            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                    sent), 0);
            PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(delivered), 0);
            //register the receivers
            context.getApplicationContext().registerReceiver(sentMessageBroadcastReceiver, new IntentFilter(sent));
            context.getApplicationContext().registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(delivered));
            //send the message
            home.SendMessage(name, message, contact1, sentPendingIntent, deliveredPendingIntent, latitude, longitude);
            // /https://developer.android.com/training/notify-user/build-notification.html
            //Sending notification after a successful sent message
            //SendUpdateMessage(context);

            db.onClose();
        } else {
        }

        //broadcast receiver on why it may take some time to receive actions
        //"Even in the case of normal broadcasts, the system may in some situations revert to delivering the broadcast one receiver at a time.
        // In particular, for receivers that may require the creation of a process, only one will be run at a time to avoid overloading the
        // system with new processes. In this situation, however, the non-ordered semantics hold: these receivers still cannot return results
        // or abort their broadcast."
        //https://developer.android.com/reference/android/content/BroadcastReceiver.html

        //send the screen state information and update the background service from service update class
        Intent i = new Intent(context, BackgroundServiceUpdate.class);
        //clear the tasks above of the stack and not restart a new task but simply open the app when unlocked
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_RECEIVER_FOREGROUND);
        i.putExtra("screen_state", screenState);
        //start the service essentially from background service update class
        context.startService(i);
        context.sendBroadcast(i);
    }

    //check it out - https://android.jlelse.eu/detecting-sending-sms-on-android-8a154562597f

//    public void SendUpdateMessage(final Context context) {
//        final String number = db.getNumber().toString();
//        final int loc_update = Integer.parseInt(db.getLocation_Update().toString()) * 1000;
//
//        try {
//            //https://developer.android.com/reference/java/util/TimerTask.html
//            //timer to send location updates
//            final Timer timer = new Timer();
//            timer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    //it will send 5 updates
//                    if (countTimer == 2) {
//                        timer.cancel();
//                        timer.purge();
//
//                    } else {
//                        //send the sms
//                        SmsManager smsManager = SmsManager.getDefault();
//                        smsManager.sendTextMessage(number, null, "Location Update: "
//
//                                + "http://maps.google.com/?q=" + latitude + "," + longitude + "", null, null);
//                        //build notification after every update
//                        home.Notification(context, 2, R.drawable.ic_tick, Color.RED, updateMessageTitle, notificationBody);
//                        countTimer++;
//                    }
//                }
//                //sends every interval of whatever the user chose at the setup
//            }, loc_update, loc_update);
//        } catch (Exception exc) {
//            exc.printStackTrace();
//            //showMessage("Error occurred", exc.getMessage());
//        }
//
//    }

    //get the current location
    public void GetLocation(Context context) {

        //location permission
        int permissionLocation = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

            //https://developer.android.com/guide/topics/location/strategies.html
            //https://developer.android.com/reference/android/location/LocationManager.html#getBestProvider(android.location.Criteria,%20boolean)
            try {
                //using network provider service
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                //get the values to send via message
                latitude = location.getLatitude();
                longitude = location.getLongitude();

            } catch (Exception ec) {
                ec.printStackTrace();
            }

        } else

        {
            //ask for permission if access is not given
            //ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

        }

    }

    public void UpdateLocation(Context context) {

        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        final int loc_update = Integer.parseInt(db.getLocation_Update().toString()) * 1000;
        //location permission
        int permissionLocation = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            try

            {
                final LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            finalLocation = LocationManager.NETWORK_PROVIDER;
                        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            finalLocation = LocationManager.GPS_PROVIDER;
                        }

                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            finalLocation = LocationManager.NETWORK_PROVIDER;
                        } else if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            finalLocation = LocationManager.GPS_PROVIDER;
                        }
                    }

                };
                //updates the location and the level of zoom level. 20 is buildings level
                locationManager.requestLocationUpdates(finalLocation, loc_update, 20, locationListener);
            } catch (
                    Exception exce)

            {
                exce.printStackTrace();
            }
        } else

        {
            //ask for permission if access is not given
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

        }

    }
}
