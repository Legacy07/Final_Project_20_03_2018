package com.ahmet.final_project;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;



/**
 * A simple {@link Fragment} subclass.
 */
public class WiFiP2PPeersDiscoveryFragmentList extends ListFragment implements PeerListListener {


    public WiFiP2PPeersDiscoveryFragmentList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.wifi_p2p_discovery_fragment_list_layout, container, false);

        return view;
    }

    //discovering available peers around
    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

    }
}
