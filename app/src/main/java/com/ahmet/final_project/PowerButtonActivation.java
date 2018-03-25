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
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PowerButtonActivation extends BroadcastReceiver implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

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

    int notificationChannel = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

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
    private List peersList = new ArrayList();
    public WifiP2pDevice wifiP2pDevice;
    WifiP2pInfo wifiP2pInfo;

    WifiP2pManager wifiP2pManager;
    Channel channel;

    Context mContext;
    public static String deviceName = "";
    public static String gOIP = "";

    @Override
    public void onReceive(final Context context, Intent intent) {
        home = new Home();
        db = new DbHandler(context);

        this.mContext = context;
        // intent filters for wifi p2p broadcast receiver
        IntentFilter intentFilter2 = new IntentFilter();

        //wifi p2p status
        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // checks for the change of the available peers.
        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // checks the state of the connectivity
        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // checks for device's status
        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //register p2p broadcast receiver
        BroadcastReceiver p2pBroadcastReceiver = new P2PBroadcastReceiver(wifiP2pManager, channel, PowerButtonActivation.this);
        context.getApplicationContext().registerReceiver(p2pBroadcastReceiver, intentFilter2);

        wifiP2pManager = (WifiP2pManager) context.getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(context, context.getApplicationContext().getMainLooper(), null);

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

        //check these out for service discovery so it can discover peers in the background all the time
        //https://stackoverflow.com/questions/26300889/wifi-p2p-service-discovery-works-intermittently
//        https://stackoverflow.com/questions/29734632/wi-fi-p2p-inform-all-peers-available-of-some-event/29769877#29769877
//        https://developer.android.com/training/connect-devices-wirelessly/nsd-wifi-direct.html
        //https://stackoverflow.com/questions/48248292/alternative-to-discovering-peers-with-wifi-direct-as-it-requires-both-phones-run
        if (notificationClicked != null) {
//            db.OnOPen();
//            //get location
//            GetLocation(context);
//
//            //get the data from db
//            String contact1 = db.getContact1().toString();
//            String message = db.getMessage().toString();
//            String name = db.getName().toString();
//
//            if (finalLocation == null) {
//
//            } else {
//                UpdateLocation(context);
//            }
//
//            String sent = "SENT";
//            String delivered = "DELIVERED";
//
//            //initialise pending intents for broadcast receiver to listen for whenever the message is sent and delivered
//            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(
//                    sent), PendingIntent.FLAG_UPDATE_CURRENT
//            );
//            PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(context, 0,
//                    new Intent(delivered), PendingIntent.FLAG_UPDATE_CURRENT
//            );
//            //register the receivers
//            context.getApplicationContext().registerReceiver(sentMessageBroadcastReceiver, new IntentFilter(sent));
//            context.getApplicationContext().registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(delivered));
//            //send the message
//            home.SendMessage(name, message, contact1, sentPendingIntent, deliveredPendingIntent, latitude, longitude);
//            // /https://developer.android.com/training/notify-user/build-notification.html
//            //Sending notification after a successful sent message
//            //SendUpdateMessage(context);
//
//            db.onClose();


            //discover the devices
            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    home.Notification(context, notificationChannel, R.drawable.ic_tick, Color.RED, "Success", "Discovery Initiated");


                }

                @Override
                public void onFailure(int reasonCode) {
                    home.Notification(context, notificationChannel, R.drawable.ic_tick, Color.RED, "Error", "Discovery Failed - " + reasonCode);

                }
            });
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

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        // changing peers when available
        peersList.clear();
        //add it to the list and show it in a message box
        peersList.addAll(wifiP2pDeviceList.getDeviceList());
//        showMessage("Peers", peersList.toString());

        for (int i = 0; i < peersList.size(); i++) {
            wifiP2pDevice = (WifiP2pDevice) peersList.get(i);
//            String deviceName=wifiP2pDevice.deviceName;
//            int devicestatus=wifiP2pDevice.status;


            //setup the connection and obtain peer
            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
            wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
            wifiP2pConfig.wps.setup = WpsInfo.PBC;
            //peer to be client
//            wifiP2pConfig.groupOwnerIntent = 15;

        }
        //connect to the peer
        Connect();
        if (peersList.size() == 0) {
            home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "Error", "No peers available");
            return;
        }


    }

    public void Connect() {

        //try connecting
        wifiP2pManager.createGroup(channel, new ActionListener() {

            @Override
            public void onSuccess() {
//                System.out.println("Connection established");
//                home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "Success", "Connection Established");

            }

            @Override
            public void onFailure(int reason) {
//                System.out.println("Connection failed " + reason);
//                home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "Error", "Connection failed: " + reason);

            }
        });

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        this.wifiP2pInfo = wifiP2pInfo;

        WifiP2pDevice device = this.wifiP2pDevice;

//        setDeviceName(device.deviceName.toString());
//        setgOIP(wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());

//        home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "Success", "Peer device name: " + device.deviceName +
//                    ", Group Owner IP: " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());

        home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "SOS", "HELP!");

        //when the connection is successful a group will be formed with no password request
        //the group owner will be the one who started discovery and be able to send message
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            //Shows the connected device and shows the group owner ip address in notification

//            System.out.println("Peer device name: " + device.deviceName + ", Group Owner IP: " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());
//            home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "SOS", "HELP!");


            //the other device will be the client in this case and receive the message
        } else if (wifiP2pInfo.groupFormed) {
            //Shows the connected device and shows the group owner ip address in notification

//            System.out.println("Peer device name: " + device.deviceName + ", Group Owner IP: " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());
//            home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "Success", "Peer device name: " + device.deviceName +
//                    ", Group Owner IP: " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());
//            home.Notification(mContext, notificationChannel, R.drawable.ic_tick, Color.RED, "SOS", "HELP!");

        }

//        final Button disconnect = (Button) findViewById(R.id.disconnectButton);
//        disconnect.setVisibility(View.VISIBLE);
//
//        disconnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //remove from the group
//                wifiP2pManager.removeGroup(channel, new ActionListener() {
//
//                    @Override
//                    public void onFailure(int reasonCode) {
//                        showMessage("Error", "Disconnection failed " + reasonCode);
//
//                    }
//
//                    @Override
//                    public void onSuccess() {
//                        deviceName.setText("Disconnected");
//                        groupOwner.setText("No Group Owner");
//                        peersList.clear();
//
//                        disconnect.setVisibility(View.GONE);
//                    }
//
//                });
//            }
//        });
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getgOIP() {
        return this.gOIP;
    }

    public void setgOIP(String gOIP) {
        this.gOIP = gOIP;
    }


}
