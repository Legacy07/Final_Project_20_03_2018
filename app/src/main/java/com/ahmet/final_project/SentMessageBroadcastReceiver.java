package com.ahmet.final_project;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

//Broadcast receiver listening for the sms sending action and output message depending on the result
public class SentMessageBroadcastReceiver extends BroadcastReceiver {
    String notificationTitle = "Successfully Message Delivered";
    String notificationBody = "Message delivered to: ";
    String updateMessageTitle = "Update Message Delivered ";

    Home home;
    DbHandler db;

    double latitude = 0;
    double longitude = 0;
    int countTimer = 0;
    int countTimer1 = 0;
    String name = "";
    String message = "";
    String contact1 = "";
    String whatsappMessage = "";

    String networkSSID = null;

    Location location;
    LocationManager locationManager;
    AirplaneModeBroadcastReceiver airplaneModeBroadcastReceiver;

    BroadcastReceiver deliveredMessageBroadcastReceiver = new DeliveredMessageBroadcastReceiver();
    String finalLocation = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        home = new Home();

        db = new DbHandler(context);

        //get the data from db
        contact1 = db.getContact1().toString();
        message = db.getMessage().toString();
        name = db.getName().toString();
        whatsappMessage = "I'm " + name + " and " + message + " My location is: "
                //if comma doesnt work then use encoding for the link
                + "http://maps.google.com/?q=" + latitude + "," + longitude + "";

        String sent = "SENT";
        String delivered = "DELIVERED";
        //initialise pending intents for broadcast receiver to listen for whenever the message is sent and delivered
        final PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                sent), 0);
        final PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(delivered), 0);

        context.registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(delivered));

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                db.onClose();
                break;
            //and if somewhat, in any f these errors occur then re-send the message within an interval
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                //failed somehow, cannot tell how
                try {
                    GetLocation(context);
                    if (finalLocation == null) {

                    } else {
                        UpdateLocation(context);
                    }
                    db.OnOPen();
                    //https://developer.android.com/reference/java/util/TimerTask.html
                    //timer to try re-sending the message after 5 seconds
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //will stop after it sent
                            if (countTimer == 1) {
                                timer.cancel();
                                timer.purge();
                                db.onClose();
                            } else {
                                //send the message
                                home.SendMessage(name, message, contact1, sentPendingIntent, deliveredPendingIntent, latitude, longitude);
                                countTimer++;
                                db.onClose();
                            }
                        }
                        //sends after 5 seconds.
                    }, 5000);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                //service is probably not available at the moment
                //Check IT OUT -- https://stackoverflow.com/questions/8818290/how-do-i-connect-to-a-specific-wi-fi-network-in-android-programmatically
                //https://infoqueue.wordpress.com/2014/07/14/join-a-wifi-network-programmatically-in-android/
                //Check it OUT receiver -- https://stackoverflow.com/questions/15088507/open-app-when-connect-with-wifi
                //https://stackoverflow.com/questions/32517188/automatically-connect-to-wifi-network-in-android
                //https://stackoverflow.com/questions/8271681/how-to-scan-find-and-connect-to-an-open-wifi-ap-programatically
                //https://stackoverflow.com/questions/30889089/android-connect-to-open-wifi-programmatically-by-name-which-is-best-solution
                //https://stackoverflow.com/questions/6141185/android-connect-to-wifi-without-human-interaction
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                //turn on the wifi service
                wifiManager.setWifiEnabled(true);

                //declare wifi configuration to pick up available wifi around
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                //only open networks
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                //add network
                int networkID = wifiManager.addNetwork(wifiConfiguration);
                //enable and connect to network
                wifiManager.enableNetwork(networkID, true);
                wifiManager.reconnect();

                try {
                    GetLocation(context);
                    if (finalLocation == null) {

                    } else {
                        UpdateLocation(context);
                    }
                    //FOR OTHER SOCIAL MEDIA - https://stackoverflow.com/questions/35972329/how-to-open-specific-contact-chat-screen-in-various-popular-chat-social-networks

                    //http://howdygeeks.com/send-whatsapp-message-unsaved-number-android/

                    //whatsapp tos --
                    //Be aware that the following actions are in violation of our Terms of Service:
                    //Using an automated system or an unauthorized / unofficial client application to send messages through WhatsApp.

                    //so it can only open the chat
                    //the user needs to press send thats all, not %100 automated
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent1 = new Intent(Intent.ACTION_VIEW);
                    //the url allows to send message to the phone number
                    String message = "https://api.whatsapp.com/send?phone=" + contact1 + "&text=" + URLEncoder.encode(whatsappMessage, "UTF-8");
                    //get to whatsapp and send the data when its open
                    intent1.setPackage("com.whatsapp");
                    intent1.setData(Uri.parse(message));
                    //if there is whatsapp, open it
                    if (intent1.resolveActivity(packageManager) != null) {
                        context.startActivity(intent1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                //error in pdu, something wrong in transmission process
                Toast.makeText(context, "PDU Error!", Toast.LENGTH_SHORT)
                        .show();
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                //the phone is in airplane mode
                try {
                    // Open the airplane mode settings to turn it off
                    Intent i = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(i);

                    //if airplane mode is turned off then send message
                    airplaneModeBroadcastReceiver = new AirplaneModeBroadcastReceiver() {
                        @Override
                        public void airplaneModeChanged(boolean enabled) {
                            GetLocation(context);
                            if (finalLocation == null) {

                            } else {
                                UpdateLocation(context);
                            }
                            //handler to delay process
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //send the message and notify the user
                                    db.OnOPen();

                                    home.SendMessage(name, message, contact1, sentPendingIntent, deliveredPendingIntent, latitude, longitude);
                                    home.Notification(context, 3, R.drawable.ic_tick, Color.RED, notificationTitle, notificationBody + contact1);
                                    db.onClose();
                                }
                                //after 5 seconds
                            }, 5000);
                        }
                    };
                    //register broadcast receiver to listen for user action
                    airplaneModeBroadcastReceiver.Register(context);


                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }

    }

//    public void SendUpdateMessage(final Context context) {
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
//                        home.Notification(context, 2, R.drawable.ic_tick, Color.RED, updateMessageTitle, notificationBody + number);
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
