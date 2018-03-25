package com.ahmet.final_project;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class P2PBroadcastReceiver extends BroadcastReceiver {


    private WifiP2pManager manager;
    private Channel channel;
    private PowerButtonActivation powerButtonActivation;
    Home home;
    String deviceName = "";
    String gOIP = "";

    private WifiP2pManager.PeerListListener peerListListener;

    //getting the connection from the activity to listen
    public P2PBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                PowerButtonActivation powerButtonActivation) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.powerButtonActivation = powerButtonActivation;

//        this.peerListListener = peerListListener1;

    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        home = new Home();

        //https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            //checking if wifi p2p is supported and turned on
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //set to true
//                backgroundServiceUpdate.WifiP2PEnabled(true);
            } else {
//                backgroundServiceUpdate.WifiP2PEnabled(false);
            }

        } //checks for changed peers in the list
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            //fetch list -- https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html#fetch
            //finding the peers
            if (manager != null) {
                manager.requestPeers(channel, powerButtonActivation);
            }

        }//checks for connections
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            deviceName = powerButtonActivation.getDeviceName();
            gOIP = powerButtonActivation.getgOIP();

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                manager.requestConnectionInfo(channel, powerButtonActivation);
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        home.Notification(context, 2, R.drawable.ic_tick, Color.RED, "SOS Message", /*"Peer device name: " +*/ deviceName +
//                        /*", Group Owner IP: "*/ ", " + gOIP);
//                    }
//                }, 5000);


            } else {
//                //disconnection
//                home.Notification(context, 2, R.drawable.ic_tick, Color.RED, "Disconnected", "Peer device name: " + deviceName +
//                        ", Group Owner IP: " + gOIP);
            }

        }//checks for wifi state change
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }

    }
}
