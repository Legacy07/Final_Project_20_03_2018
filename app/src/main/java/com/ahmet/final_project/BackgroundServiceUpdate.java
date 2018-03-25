package com.ahmet.final_project;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class BackgroundServiceUpdate extends Service /*implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener*/ {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    DbHandler db;
    WifiP2pManager wifiP2pManager;
    Channel channel;
    Home home;
    private List peersList = new ArrayList();
    public WifiP2pDevice wifiP2pDevice;
    WifiP2pInfo wifiP2pInfo;

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

        //initialise wifi p2p service and channel for connection
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

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

//        //register p2p broadcast receiver
//        BroadcastReceiver p2pBroadcastReceiver = new P2PBroadcastReceiver(wifiP2pManager, channel, BackgroundServiceUpdate.this);
//        registerReceiver(p2pBroadcastReceiver, intentFilter2);

        home = new Home();
//        //discover the devices
//        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                home.Notification(getApplicationContext(), 1, R.drawable.ic_tick, Color.RED, "Success", "Discovery Initiated");
//
//
//            }
//
//            @Override
//            public void onFailure(int reasonCode) {
//                home.Notification(getApplicationContext(), 1, R.drawable.ic_tick, Color.RED, "Error", "Discovery Failed - " + reasonCode);
//
//            }
//        });

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

    //adapted from -- https://developer.android.com/guide/topics/connectivity/wifip2p.html
//    protected void onHandleIntent(Intent intent) {
//
//        //update this ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
//
//        String TAG = "Project";
//        final int SOCKET_TIMEOUT = 5000;
//        final String ACTION_SEND_FILE = "com.ahmet.final_project.SEND_FILE";
//        final String EXTRAS_FILE_PATH = "file_url";
//        final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
//        final String EXTRAS_GROUP_OWNER_PORT = "go_port";
//
//        Context context = getApplicationContext();
//        if (intent.getAction().equals(ACTION_SEND_FILE)) {
//            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
//            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
//            Socket socket = new Socket();
//            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
//
//            try {
//                Log.d(TAG, "Opening client socket - ");
//                socket.bind(null);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
//
//                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
//                OutputStream stream = socket.getOutputStream();
//                ContentResolver cr = context.getContentResolver();
//                InputStream is = null;
//                try {
//                    is = cr.openInputStream(Uri.parse(fileUri));
//                } catch (FileNotFoundException e) {
//                    Log.d(WiFiDirectActivity.TAG, e.toString());
//                }
//                DeviceDetailFragment.copyFile(is, stream);
//                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
//            } catch (IOException e) {
//                Log.e(WiFiDirectActivity.TAG, e.getMessage());
//            } finally {
//                if (socket != null) {
//                    if (socket.isConnected()) {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            // Give up
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//        }
//    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = new DbHandler(getApplicationContext());
        db.OnOPen();
        LockScreenNotification();
        db.onClose();
        //restart the service if its killed
        return START_STICKY;
    }

//    @Override
//    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
//        // changing peers when available
//        peersList.clear();
//        //add it to the list and show it in a message box
//        peersList.addAll(wifiP2pDeviceList.getDeviceList());
////        showMessage("Peers", peersList.toString());
//
//        for (int i = 0; i < peersList.size(); i++) {
//            wifiP2pDevice = (WifiP2pDevice) peersList.get(i);
////            String deviceName=wifiP2pDevice.deviceName;
////            int devicestatus=wifiP2pDevice.status;
//        }
//
//
//        //setup the connection
//        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
//        wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
//        wifiP2pConfig.wps.setup = WpsInfo.PBC;
//        //connect to the peer
//        Connect();
//        if (peersList.size() == 0) {
//            Toast.makeText(getApplicationContext(), "No peers available", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//
//    }

//    public void Connect() {
//
//        //try connecting
//        wifiP2pManager.createGroup(channel, new ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                home.Notification(getApplicationContext(), 1, R.drawable.ic_tick, Color.RED, "Success", "Connection Established");
//
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                home.Notification(getApplicationContext(), 1, R.drawable.ic_tick, Color.RED, "Error", "Connection Failed - " + reason);
//
//            }
//        });
//
//    }
//
//    @Override
//    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
//        this.wifiP2pInfo = wifiP2pInfo;
//
//        WifiP2pDevice device = this.wifiP2pDevice;
//
//        //when the connection is successful a group will be formed with no password request
//        //the group owner will be the one who started discovery and be abe to send message
//        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
//            //Shows the connected device and shows the group owner ip address in notification
//
//            home.Notification(getApplicationContext(), 1, R.drawable.ic_tick, Color.RED, "Connection Details",
//                    "Peer device name: " + device.deviceName + ", Group Owner IP: " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());
//
//            //the other device will be the client in this case and receive the message
//        } else if (wifiP2pInfo.groupFormed) {
//            //Shows the connected device and shows the group owner ip address in notification
//
//            home.Notification(getApplicationContext(), 1, R.drawable.ic_tick, Color.RED, "Connection Details",
//                    "Peer device name: " + device.deviceName + ", Group Owner IP: " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());
//        }

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


