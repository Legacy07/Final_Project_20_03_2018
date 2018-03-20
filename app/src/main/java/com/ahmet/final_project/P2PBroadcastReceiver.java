package com.ahmet.final_project;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

public class P2PBroadcastReceiver extends BroadcastReceiver {


    private WifiP2pManager manager;
    private Channel channel;
    private Home home;

    private WifiP2pManager.PeerListListener peerListListener;

    //getting the connection from the activity to listen
    public P2PBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                Home home) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.home = home;

//        this.peerListListener = peerListListener1;

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            //checking if wifi p2p is supported and turned on
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //set to true
                home.WifiP2PEnabled(true);
            } else {
                home.WifiP2PEnabled(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            //fetch list -- https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html#fetch
            //finding the peers
            if (manager != null) {
                manager.requestPeers(channel, home);
            }
            Log.d(Home.TAG, "Nearby peers have changed");


        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }

    }
}
