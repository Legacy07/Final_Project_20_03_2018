package com.ahmet.final_project;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

//Broadcast receiver listening for the sms delivering action and output message depending on the result
public class DeliveredMessageBroadcastReceiver extends BroadcastReceiver {
    String notificationTitle = "Successfully Message Delivered";
    String notificationBody = "Message delivered to: ";
    String updateMessageTitle = "Update Message Delivered";

    int countTimer = 0;
    double latitude = 0;
    double longitude = 0;
    String contact1 = "";
    Home home;
    DbHandler db;

    LocationManager locationManager;
    String finalLocation = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        home = new Home();
        db = new DbHandler(context);
        contact1 = db.getContact1().toString();

        //get location
        GetLocation(context);


        // receiving sms -- https://stackoverflow.com/questions/17720965/perfom-an-action-on-getting-specific-text-in-sms-in-android

        //might help for receiving from specific contact -- https://web.archive.org/web/20121022021217/http://mobdev.olin.edu/mobdevwiki/FrontPage/Tutorials/SMS%20Messaging

        switch (getResultCode()) {
            //if message is delivered then notify the user
            case Activity.RESULT_OK:
                db.OnOPen();
                //Sending notification after a successful sent message
                home.Notification(context, 1, R.drawable.ic_tick, Color.RED, notificationTitle, notificationBody + contact1);

                //if gps or network are off do nothing else send update message also getting the location update
                if (finalLocation == null) {

                } else {
                    UpdateLocation(context);
                }
//                SendUpdateMessage(context);

//                delay for 5 seconds
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //send message
                        SendUpdateMessage(context);
                    }
                }, 5000);

                db.onClose();
                break;
            case Activity.RESULT_CANCELED:
                home.Notification(context, 1, R.drawable.ic_tick, Color.RED, "Unable to deliver the message", "Message couldn't be sent to: " + contact1);

                break;
        }
    }

    public void SendUpdateMessage(final Context context) {
        final int loc_update = Integer.parseInt(db.getLocation_Update().toString()) * 1000;

        try {
            //https://developer.android.com/reference/java/util/TimerTask.html
            //timer to send location updates
            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //it will send 5 updates
                    if (countTimer == 2) {
                        timer.cancel();
                        timer.purge();

                    } else {
                        //send the sms
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(contact1, null, "Location Update: "
                                + "http://maps.google.com/?q=" + latitude + "," + longitude + "", null, null);
                        //build notification after every update
                        home.Notification(context, 2, R.drawable.ic_tick, Color.RED, updateMessageTitle, notificationBody + contact1);
                        countTimer++;
                    }
                }
                //sends every interval of whatever the user chose at the setup
            }, loc_update, loc_update);
        } catch (Exception exc) {
            exc.printStackTrace();
            //showMessage("Error occurred", exc.getMessage());
        }

    }

    //get the current location
    public void GetLocation(Context context) {

        boolean is_gps_provider_enabled = false;
        boolean is_network_provider_enabled = false;

        Location gpsLocation = null;
        Location networkLocation = null;

        //location manager helps to get current location
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //setting boolean values for either gps or network availability
        is_gps_provider_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        is_network_provider_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //location permission
        int permissionLocation = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

            //https://developer.android.com/guide/topics/location/strategies.html
            //https://developer.android.com/reference/android/location/LocationManager.html#getBestProvider(android.location.Criteria,%20boolean)
            try {
                if (is_gps_provider_enabled) {
                    //using network provider service
                    gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (is_network_provider_enabled) {
                    //using network provider service
                    networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                //if gps and network providers are in use then get the best accuracy and use that to provide location
                if (gpsLocation != null && networkLocation != null) {
                    //checking if network has a better accuracy first, if true then use the network provided service
                    if (gpsLocation.getAccuracy() > networkLocation.getAccuracy()) {
                        //get the values to send via message
                        latitude = networkLocation.getLatitude();
                        longitude = networkLocation.getLongitude();
                    } else {   //otherwise use gps provider
                        latitude = gpsLocation.getLatitude();
                        longitude = gpsLocation.getLongitude();
                    }
                }
                //if one of the provider is disabled then use the other
                else {
                    if (gpsLocation != null) {
                        latitude = gpsLocation.getLatitude();
                        longitude = gpsLocation.getLongitude();
                    } else if (networkLocation != null) {
                        latitude = networkLocation.getLatitude();
                        longitude = networkLocation.getLongitude();
                    }
                }

            } catch (Exception ec) {
                ec.printStackTrace();
            }
            //https://developer.android.com/training/location/display-address.html
            //geocoder reverse engineers the location values into addresses
//            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//            try {
//                //convert lat and long values for a single address
//                addresses = geocoder.getFromLocation(
//                        latitude,
//                        longitude,
//                        1);
//            } catch (IOException e) {
//                e.printStackTrace();
//                showMessage("Error occurred", e.getMessage());
//            }

        } else

        {
            //ask for permission if access is not given
            // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

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
