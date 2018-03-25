package com.ahmet.final_project;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity implements OnMapReadyCallback/* WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener*/ {

    Button sosButton;
    Button showButton;
    Button testB;
    Button p2pButton;
    Button locationUpdateButton;
    Button contactsettingButton;
    Button activationButton;
    ImageButton activationSettinginformationButton;

    TextView contact1TextV;
    TextView contact2TextV;
    TextView contact3TextV;

    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final int CONTACT_REQUEST_CODE = 1;

    private Uri contactUri;
    private String contactID;

    TextView contact1TextView;
    TextView contact2TextView;
    TextView contact3TextView;

    String sFinalContact1 = "";
    String sFinalContact2 = "";
    String sFinalContact3 = "";

    double longitude = 0;
    double latitude = 0;
    List<Address> addresses;
    DbHandler db;
    String finalLocation = null;
    int countTimer = 0;
    private GoogleMap mMap;
    LatLng currentLocation = null;

    //notification builder parameters
    int channelIdSendMessage = 1;
    final int channelidUpdate = 2;
    String sendMessageTitle = "Successfully Message Delivered";
    String sendMessageBody = "Message delivered to: ";
    final String updateMessageTitle = "Successfully Update Message is Delivered";
    String networkSSID = null;

    String whatsappMessage = "";
    BroadcastReceiver broadcastReceiver;

    BroadcastReceiver sentMessageBroadcastReceiver = new SentMessageBroadcastReceiver();
    BroadcastReceiver deliveredMessageBroadcastReceiver = new DeliveredMessageBroadcastReceiver();
    BroadcastReceiver p2pBroadcastReceiver;

    public boolean isWifiP2pEnabled = false;

    Channel channel;
    WifiP2pManager wifiP2pManager;
    public WifiP2pDevice wifiP2pDevice;
    WifiP2pInfo wifiP2pInfo;

    ProgressDialog progressDialog = null;

    private List peersList = new ArrayList();
    private WifiP2pManager.PeerListListener peerListListener;

    public static final String TAG = "TAG";

    // intent filters for wifi p2p broadcast receiver
    IntentFilter intentFilter2 = new IntentFilter();

    String contact1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment
                .getMapAsync(this);

        sosButton = (Button) findViewById(R.id.sosButton);
        showButton = (Button) findViewById(R.id.showButton);
        testB = (Button) findViewById(R.id.testButton);
        p2pButton = (Button) findViewById(R.id.p2pButton);

        locationUpdateButton = (Button) findViewById(R.id.locationSettingsButton);
        contactsettingButton = (Button) findViewById(R.id.contactsButton);
        activationButton = (Button) findViewById(R.id.activationButton);

        contact1TextV = (TextView) findViewById(R.id.contactSetting1Textview);
        contact2TextV = (TextView) findViewById(R.id.contactSetting2Textview);
        contact3TextV = (TextView) findViewById(R.id.contactSetting3Textview);

        db = new DbHandler(this);
        contact1 = db.getContact1().toString();

        //open location update setting dialog
        LocationUpdateSetting();
        //opens contact setting dialog
        ContactSetting();
        //opens activation setting dialog
        ActivationSetting();

        //Service//
        //Specifying the type of intent, screen on and off
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        //adding screen off to trigger when the screen is off
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        //for startup service
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        //checks if the user wakes up the device
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        //Broadcast receiver to register the screen state
        broadcastReceiver = new PowerButtonActivation();
        registerReceiver(broadcastReceiver, intentFilter);
        whatsappMessage = "I'm " + "ahmet" + " and " + "I'm in danger" + " My location is: "
                //if comma doesnt work then use encoding for the link
                + "http://maps.google.com/?q=" + latitude + "," + longitude + "";
        Show();

        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the data from db
                String contact = db.getContact1().toString();
                String message = db.getMessage().toString();
                String name = db.getName().toString();

                String SENT = "SENT";
                String DELIVERED = "DELIVERED";

                //initialise pending intents for broadcast receiver to listen for whenever the message is sent and delivered
                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(Home.this, 0, new Intent(
                        SENT), PendingIntent.FLAG_UPDATE_CURRENT
                );
                PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(Home.this, 0,
                        new Intent(DELIVERED), PendingIntent.FLAG_UPDATE_CURRENT
                );
                //register receivers so it can start receiving and create an intent
                registerReceiver(sentMessageBroadcastReceiver, new IntentFilter(SENT));
                registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(DELIVERED));

                //sending the message
                SendMessage(name, message, contact, sentPendingIntent, deliveredPendingIntent, latitude, longitude);
                // /https://developer.android.com/training/notify-user/build-notification.html
                //SendUpdateMessage(Home.this);
                //build notification after every update
                //Notification(Home.this, 2, R.drawable.ic_tick, Color.RED, updateMessageTitle, sendMessageBody + contact);
                db.onClose();

            }
        });

        //check it out https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
        testB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                //turn on the wifi service
                wifiManager.setWifiEnabled(true);
                Handler handler = new Handler();
                //delay for 5 seconds to turn wifi on
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //declare wifi configuration to pick up available wifi around
                        WifiConfiguration wifiConfiguration = new WifiConfiguration();
                        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                        //only open networks
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        //add the network
                        int networkID = wifiManager.addNetwork(wifiConfiguration);

                        //if there is an available open network then connect
                        if (networkID != -1) {
                            wifiManager.disconnect();
                            //enable and connect to network
                            wifiManager.enableNetwork(networkID, true);
                            wifiManager.reconnect();

                            Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "Unable to connect!", Toast.LENGTH_LONG).show();

                        }

//                        if (wifiManager.reconnect()) {
//                            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
//                        }
                    }
                }, 5000);

                Handler handler2 = new Handler();

                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //send whatsapp message after 5 seconds
                        try {
                            //FOR OTHER SOCIAL MEDIA - https://stackoverflow.com/questions/35972329/how-to-open-specific-contact-chat-screen-in-various-popular-chat-social-networks

                            //http://howdygeeks.com/send-whatsapp-message-unsaved-number-android/

                            //whatsapp tos --
                            //Be aware that the following actions are in violation of our Terms of Service:
                            //Using an automated system or an unauthorized / unofficial client application to send messages through WhatsApp.

                            //so it can only open the chat
                            //the user needs to press send thats all, not %100 automated
                            PackageManager packageManager = getPackageManager();
                            Intent intent1 = new Intent(Intent.ACTION_VIEW);
                            //the url allows to send message to the phone number
                            String message = "https://api.whatsapp.com/send?phone=" + contact1 + "&text=" + URLEncoder.encode(whatsappMessage, "UTF-8");
                            //get to whatsapp and send the data when its open
                            intent1.setPackage("com.whatsapp");
                            intent1.setData(Uri.parse(message));
                            //if there is whatsapp, open it
                            if (intent1.resolveActivity(packageManager) != null) {
                                startActivity(intent1);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 10000);

            }
        });

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //initialise wifi p2p service and channel for connection
//        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
//
//        //wifi p2p status
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        // checks for the change of the available peers.
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        // checks the state of the connectivity
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        // checks for device's status
//        intentFilter2.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

////        //listening for nearby peers and updating it
//        peerListListener = new WifiP2pManager.PeerListListener() {
//            @Override
//            public void onPeersAvailable(WifiP2pDeviceList peerList) {
//
//                //remove the progressdialog
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                // changing peers when available
//                peersList.clear();
//                //add it to the list and show it in a message box
//                peersList.addAll(peerList.getDeviceList());
//                showMessage("Peers", peersList.toString());
////                Toast.makeText(Home.this, "Peers found!", Toast.LENGTH_LONG).show();
//
//                if (peersList.size() == 0) {
//                    Toast.makeText(Home.this, "No peers available", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }
//        };


        //Try this if it doesnt work for peerlistener -- https://stackoverflow.com/questions/24357892/how-to-solve-discovering-other-device-via-wi-fi-android-api

        //first try peer listener -- https://stackoverflow.com/questions/17530885/android-java-wifi-direct-peer-list

        //adapted by -- https://developer.android.com/guide/topics/connectivity/wifip2p.html
        p2pButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //adapted from ---
//                https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
//                https://developer.android.com/guide/topics/connectivity/wifip2p.html
//                https://android.googlesource.com/platform/development/+/master/samples/WiFiDirectDemo/

//                if (!isWifiP2pEnabled) {
//                    //if wifi isnt enabled turn on wifi
//                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                    //turn on the wifi service
//                    wifiManager.setWifiEnabled(true);
//                } else {
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                    progressDialog = ProgressDialog.show(Home.this, "Press back to cancel", "Searching for nearby peers",
//                            true, true, new DialogInterface.OnCancelListener() {
//
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                }
//                            });
//                    //discover the devices
//                    wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//
//                        @Override
//                        public void onSuccess() {
//                            Toast.makeText(Home.this, "Started Discovery for nearby peers",
//                                    Toast.LENGTH_SHORT).show();
//
//                        }
//
//                        @Override
//                        public void onFailure(int reasonCode) {
//                            Toast.makeText(Home.this, "Discovery Failed : " + reasonCode,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });

//                    //remove the progress dialog
//                    if (progressDialog != null && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                    //setup the connection
//                    WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
//                    wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
//                    wifiP2pConfig.wps.setup = WpsInfo.PBC;
//
//
//                    //progress dialog
//                    progressDialog = ProgressDialog.show(Home.this, "Press back to cancel",
//                            "Connecting to :" + wifiP2pDevice.deviceAddress, true, true
//                    );
//
//                    //try connecting
//                    wifiP2pManager.connect(channel, wifiP2pConfig, new ActionListener() {
//
//                        @Override
//                        public void onSuccess() {
//
//                        }
//
//                        @Override
//                        public void onFailure(int reason) {
//                            showMessage("Error", "Couldn't connect: " + reason);
//                        }
//                    });

//                }
            }
        });


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        GetLocation();

        boolean is_gps_provider_enabled = false;
        //location manager helps to get current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //setting boolean values for either gps or network availability
        is_gps_provider_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!is_gps_provider_enabled) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Location Services");
            builder.setMessage("Please turn on location services.");
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int id) {
                    //open location services setting
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialogInterface.cancel();
                }
            });
            builder.show();

        }
        if (finalLocation == null) {
            //default is network provider
            finalLocation = LocationManager.NETWORK_PROVIDER;


        } else {
            UpdateLocation();
        }
        db.onClose();
    }


    public void onResume() {
        super.onResume();
        //check if location services is turned off
        boolean is_gps_provider_enabled = false;
        //location manager helps to get current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //setting boolean values for either gps or network availability
        is_gps_provider_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!is_gps_provider_enabled) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Location Services");
            builder.setMessage("Please turn on location services.");
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int id) {
                    //open location services setting
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialogInterface.cancel();
                }
            });
            builder.show();

        }
        //register p2p broadcast receiver
//        p2pBroadcastReceiver = new P2PBroadcastReceiver(wifiP2pManager, channel, Home.this);
//        registerReceiver(p2pBroadcastReceiver, intentFilter2);
    }

    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(p2pBroadcastReceiver);
    }

    //setting to toggle if wifi p2p is turned on or not
    public void WifiP2PEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;

    }

    //callback for listing the found peers
//    @Override
//    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
//        //remove the progressdialog
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
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
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setCancelable(true);
//        builder.setTitle("Found Peers");
//        builder.setMessage("Connect to this peer?" + "\n " + peersList.toString());
//        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialogInterface, int id) {
//                //setup the connection
//                WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
//                wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
//                wifiP2pConfig.wps.setup = WpsInfo.PBC;
//                //connect to the peer
//                Connect();
//
//            }
//        });
//        builder.show();
//        if (peersList.size() == 0) {
//            Toast.makeText(Home.this, "No peers available", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//
//    }

//    public void Connect() {
//        //remove the progress dialog
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//
//        //progress dialog
//        progressDialog = ProgressDialog.show(Home.this, "Press back to cancel",
//                "Connecting to :" + wifiP2pDevice.deviceAddress, true, true
//        );
//
//        //try connecting
//        wifiP2pManager.createGroup(channel, new ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                showMessage("Success", "Connected!");
//
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                showMessage("Error", "Couldn't connect: " + reason);
//
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//            }
//        });
//
//    }

    //callback for peers who are connected and decide the responsibility for group owner and clients
//    @Override
//    public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
//        //update this
//
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//        this.wifiP2pInfo = wifiP2pInfo;
//
//        WifiP2pDevice device = this.wifiP2pDevice;
//        final TextView deviceName = (TextView) findViewById(R.id.connectedPeersTextView);
//
//        final TextView groupOwner = (TextView) findViewById(R.id.groupOwnerTextView);
//
//        //when the connection is successful a group will be formed with no password request
//        //the group owner will be the one who started discovery and be abe to send message
//        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
//
////            //Shows the connected device
//            deviceName.setText("Device Name: " + device.deviceName);
////            // shows the group owner ip address
//            groupOwner.setText("Group Owner IP - " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());
//
//            //the other device will be the client in this case and receive the message
//        } else if (wifiP2pInfo.groupFormed) {
////            //Shows the connected device name
//            deviceName.setText("Device Name: " + device.deviceName.toString());
////
////            //shows the group owner ip address
//            groupOwner.setText("Group Owner IP - " + wifiP2pInfo.groupOwnerAddress.getHostAddress().toString());
//        }
//
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
//    }

    //check it out for getting location if no internet
    //showing google map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //check it out -- https://developers.google.com/maps/documentation/android-api/map
        mMap = googleMap;
        //get refresh location
        final int loc_refresh = (Integer.parseInt(db.getLocation_Refresh().toString()) * 1000) * 60;

        int permissionLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        //location manager helps to get current location
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //using network provider service to get an accurate estimation of the location
        final Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        //default provider is network for location updates
        finalLocation = LocationManager.NETWORK_PROVIDER;
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {

            try {
                //get the values of lat long
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                //use lat long to define the location
                currentLocation = new LatLng(latitude, longitude);
                //add a marker of the gathered location
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current location"));
                //moves the screen towards the marker with a zoom level of 15 showing streets
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } catch (Exception ec) {
                ec.printStackTrace();
                showMessage("Error occurred", ec.getMessage());
            }

            try

            {
                final LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        LatLng currentLocation = new LatLng(latitude, longitude);
                        //clear the marker and add the updated marker
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current location"));
                        //moves the screen towards the marker with a zoom level of 15 showing streets
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

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
                locationManager.requestLocationUpdates(finalLocation, loc_refresh, 20, locationListener);
            } catch (Exception exce) {
                exce.printStackTrace();
                showMessage("Error occurred", exce.getMessage());
            }


        } else {
            //ask for permission if access is not given
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

        }


        ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshMapButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get new lat long values to update location
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                LatLng currentLocation = new LatLng(latitude, longitude);
                //clear the marker and add the updated marker
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current location"));
                //moves the screen towards the marker with a zoom level of 15 showing streets
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

            }
        });
        db.onClose();

    }


    //testing purposes, showing user data
    public void Show() {
        db = new DbHandler(getApplicationContext());

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Show all data
                    String data = db.getAllData();
                    showMessage("User Information", data);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Error", e.getMessage());
                }

            }
        });
    }

    /// CHECK IT OUT FOR Mesh networking -- https://developer.android.com/things/sdk/apis/lowpan.html
    //CHECK THIS OUT == https://stackoverflow.com/questions/11589642/sms-delivery-report-in-android
    //check if message is sent https://stackoverflow.com/questions/19439820/notify-if-a-message-you-sent-was-sent-successfully-or-not-in-android
    public void SendMessage(String name, String message, String contact, PendingIntent sentIntent, PendingIntent deliveryIntent, double latitude, double longitude) {

        boolean is_gps_provider_enabled = false;
        //location manager helps to get current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //setting boolean values for either gps or network availability
        is_gps_provider_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!is_gps_provider_enabled) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Location Services");
            builder.setMessage("Please turn on location services.");
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int id) {
                    //open location services setting
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialogInterface.cancel();
                }
            });
            builder.show();

        } else {
            if (contact == null || message == null || name == null) {

            } else {
                try {
                    //use this to send if the sms text is long
                    //ArrayList<String> texts = smsManager.divideMessage(text);
                    // smsManager.sendMultipartTextMessage(phone, null, texts, null, null)

                    //send the sms
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(contact, null, "I'm " + name + " and " + message + " My location is: "
                            //if comma doesnt work then use encoding for the link
                            + "http://maps.google.com/?q=" + latitude + "," + longitude + "", sentIntent, deliveryIntent);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }
        }
    }

    public void Notification(Context context, int channelID, int icon, int color, String title, String body) {
        //setting the ui of the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setColor(color)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setContentTitle(title)
                        .setContentText(body);
        //Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(channelID, builder.build());

    }

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
//                        countTimer++;
//                    }
//                }
//                //sends every interval of whatever the user chose at the setup
//            }, loc_update, loc_update);
//        } catch (Exception exc) {
//            exc.printStackTrace();
//        }
//
//    }

//check to get best estimate -- https://developer.android.com/guide/topics/location/strategies.html
//if gps is disabled then turn it on -- https://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled

//CHECK THIS FOR DIFFERENT LOCATION CAPTURING SERVICES
//https://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-on-a/3145655#3145655

//check it out!! - for different providers -- https://stackoverflow.com/questions/6694391/android-get-current-location-of-user-without-using-gps-or-internet
//FUSED LOCATION - a better and imporved location capturing provider - https://developer.android.com/training/location/index.html
//https://developer.android.com/training/location/retrieve-current.html

//if gps and netowrk are off -- https://developers.google.com/maps/documentation/geolocation/intro#overview
//and this -- http://android-coding.blogspot.co.uk/2011/06/convert-celllocation-to-real-location.html
//check it out to check if things are on -- https://hedgehogjim.wordpress.com/2013/04/03/android-network-locates-when-enabled-is-not-enabled/

    //why to use fused location - https://stackoverflow.com/questions/33022662/android-locationmanager-vs-google-play-services

    //cell tower db for accessing location without gps - http://opencellid.org/#action=database.downloadDatabase

    //get the current location
    public void GetLocation() {

        boolean is_gps_provider_enabled = false;
        boolean is_network_provider_enabled = false;

        Location gpsLocation = null;
        Location networkLocation = null;

        //location manager helps to get current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //setting boolean values for either gps or network availability
        is_gps_provider_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        is_network_provider_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //location permission
        int permissionLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
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
                showMessage("Error occurred", ec.getMessage());
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

        }

    }

    public void UpdateLocation() {

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        final int loc_update = Integer.parseInt(db.getLocation_Update().toString()) * 1000;
        //location permission
        int permissionLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
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
                showMessage("Error occurred", exce.getMessage());
            }
        } else

        {
            //ask for permission if access is not given
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

        }

    }

    //getting the grant from the user and handling the response
    @Override
    public void onRequestPermissionsResult(int request, String[] permission, int[] grant) {
        super.onRequestPermissionsResult(request, permission, grant);
        //CHECK .... the grant length if it gives an error and search for multiple permissions
        switch (request) {

            case PERMISSION_REQUEST_LOCATION: {
                if (grant.length >= 0 && grant[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showMessage("Grant Permission", "Application will not fully function if you don't grant permissions.");
                }

            }

        }
    }

    //building alert dialog to show dialog messages
    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    //Fix this later
    public void ContactSetting() {
        db.OnOPen();
        //get name from db so it can update using this
        final String name = db.getName().toString();

        final String dbContact1 = db.getContact1().toString();
        final String dbContact2 = db.getContact2().toString();
        final String dbContact3 = db.getContact3().toString();


        contactsettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new alert dialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
                //inflate the dialog layout to import it into alert dialog in order to view
                LayoutInflater layoutInflater = getLayoutInflater();
                //when inflated, then able to view the settings screen
                View view1 = layoutInflater.inflate(R.layout.contact_setting_screen, null);
                //initialise buttons and textviews
                Button contact1Button = (Button) view1.findViewById(R.id.contactSettingaddContact1Button);

                contact1TextView = (TextView) view1.findViewById(R.id.contactSetting1Textview);
                contact2TextView = (TextView) view1.findViewById(R.id.contactSetting2Textview);
                contact3TextView = (TextView) view1.findViewById(R.id.contactSetting3Textview);

//                contact1TextView.setText(dbContact1);
//                contact2TextView.setText(dbContact2);
//                contact3TextView.setText(dbContact3);

                try {
                    //add contacts to textview
                    contact1Button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //opens contact list and allows the user to select limited multiple contacts
                            Intent contactList = new Intent("intent.action.INTERACTION_TOPMENU");
                            contactList.putExtra("additional", "phone-multi");
                            contactList.putExtra("maxRecipientCount", 3);
                            contactList.putExtra("FromMMS", true);

                            startActivityForResult(contactList, CONTACT_REQUEST_CODE);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Error", e.getMessage());
                }
                //set custom layout in dialog
                dialog.setView(view1);

                dialog.setCancelable(true);
                //create clickable buttons
                dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            //update location update
                            boolean updatedData = db.UpdateContacts(name, sFinalContact1, sFinalContact2, sFinalContact3);

                            if (updatedData == true) {

                                showMessage("Information", "Data Updated!");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showMessage("Error", ex.getMessage());

                        }

                    }
                });
                dialog.setNegativeButton("Cancel", null);

                final AlertDialog alert = dialog.create();
                alert.show();
                db.onClose();
            }
        });

    }

    public void LocationUpdateSetting() {
        db.OnOPen();
        //get name from db so it can update using this
        final String name = db.getName().toString();

        locationUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
                //inflate the dialog layout to import it into alert dialog in order to view
                LayoutInflater layoutInflater = getLayoutInflater();
                //when inflated, then able to view the settings screen
                View view1 = layoutInflater.inflate(R.layout.location_update_setting_screen, null);
                //initialise spinner
                final Spinner spinnerUpdate = (Spinner) view1.findViewById(R.id.locationUpdateSettingUpdateSpinner);
                final Spinner spinnerHistory = (Spinner) view1.findViewById(R.id.locationUpdateSettingHistorySpinner);
                final Spinner spinnerRefresh = (Spinner) view1.findViewById(R.id.locationUpdateSettingRefreshSpinner);
                ImageButton locationsettinginformationbutton = (ImageButton) view1.findViewById(R.id.locationupdateSettingInformationButton);

                //information dialog box
                locationsettinginformationbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
                        //inflate the dialog layout to import it into alert dialog in order to view
                        LayoutInflater layoutInflater = getLayoutInflater();
                        //when inflated, then able to view the settings screen
                        View view1 = layoutInflater.inflate(R.layout.information_dialog_box, null);

                        EditText informationText = (EditText) view1.findViewById(R.id.informationEditText);
                        //providing information
                        String information = getResources().getString(R.string.location_update_Information);
                        informationText.setText(information);
                        //set custom layout in dialog
                        dialog.setView(view1);

                        dialog.setCancelable(false);
                        //create clickable button
                        dialog.setPositiveButton("Okay", null);

                        final AlertDialog alert = dialog.create();
                        alert.show();
                    }
                });

                try {
                    //https://developer.android.com/guide/topics/ui/controls/spinner.html
                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                            R.array.location_update, android.R.layout.simple_spinner_item);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerUpdate.setAdapter(adapter);

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getApplicationContext(),
                            R.array.refresh_location, android.R.layout.simple_spinner_item);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerRefresh.setAdapter(adapter2);

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getApplicationContext(),
                            R.array.location_history, android.R.layout.simple_spinner_item);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerHistory.setAdapter(adapter3);

                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Error", e.getMessage());
                }
                //set custom layout in dialog
                dialog.setView(view1);

                dialog.setCancelable(true);
                //create clickable buttons
                dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //get from spinners
                        String setLoca_update = spinnerUpdate.getSelectedItem().toString();
                        String setLoca_refresh = spinnerRefresh.getSelectedItem().toString();
                        String setLoca_history = spinnerHistory.getSelectedItem().toString();
                        try {
                            //update location update
                            boolean updatedData = db.UpdateLocationUpdates(name, setLoca_update, setLoca_refresh, setLoca_history);

                            if (updatedData == true) {

                                showMessage("Information", "Data Updated!");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showMessage("Error", ex.getMessage());

                        }
                    }
                });
                dialog.setNegativeButton("Cancel", null);

                final AlertDialog alert = dialog.create();
                alert.show();
                db.onClose();
            }
        });

    }

    public void ActivationSetting() {
        db.OnOPen();
        final String name = db.getName().toString();
        activationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
                //inflate the dialog layout to import it into alert dialog in order to view
                LayoutInflater layoutInflater = getLayoutInflater();
                //when inflated, then able to view the settings screen
                View view1 = layoutInflater.inflate(R.layout.activation_setting_screen, null);
                //initialise spinner
                final Spinner spinnerPowerbutton = (Spinner) view1.findViewById(R.id.activation_setting_power_spinner);
                //initialise information button
                activationSettinginformationButton = (ImageButton) view1.findViewById(R.id.activationSettingInformationButton);

                //information dialog box
                activationSettinginformationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder dialog = new AlertDialog.Builder(Home.this);
                        //inflate the dialog layout to import it into alert dialog in order to view
                        LayoutInflater layoutInflater = getLayoutInflater();
                        //when inflated, then able to view the settings screen
                        View view1 = layoutInflater.inflate(R.layout.information_dialog_box, null);

                        EditText informationText = (EditText) view1.findViewById(R.id.informationEditText);
                        //providing information
                        String information = getResources().getString(R.string.setup3_description);
                        informationText.setText(information);
                        //set custom layout in dialog
                        dialog.setView(view1);

                        dialog.setCancelable(false);
                        //create clickable button
                        dialog.setPositiveButton("Okay", null);

                        final AlertDialog alert = dialog.create();
                        alert.show();
                    }
                });

                try {
                    //https://developer.android.com/guide/topics/ui/controls/spinner.html
                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                            R.array.power_button_press, android.R.layout.simple_spinner_item);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPowerbutton.setAdapter(adapter);

                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Error", e.getMessage());
                }
                //set custom layout in dialog
                dialog.setView(view1);

                dialog.setCancelable(true);
                //create clickable buttons
                dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String setPowerbuttonSpinnervalue = spinnerPowerbutton.getSelectedItem().toString();

                        //update db
                        boolean updated = db.UpdateActivation(name, setPowerbuttonSpinnervalue);
                        if (updated == true) {
                            showMessage("Information", "Successfully updated!");
                        }

                    }
                });
                dialog.setNegativeButton("Cancel", null);

                final AlertDialog alert = dialog.create();
                alert.show();
                db.onClose();
            }
        });

    }

    //adapted from https://gist.github.com/evandrix/7058235
    //and https://developer.android.com/reference/android/provider/ContactsContract.Data.html
    //https://developer.android.com/training/contacts-provider/retrieve-names.html#NameMatch
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //comparing the request code to allow picking contacts
        if (requestCode == CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {

            //it gets the data of the selected contact
            Bundle bundle = data.getExtras();

            String result = bundle.getString("result");
            ArrayList<String> contacts = bundle.getStringArrayList("result");

            //if max amount of contacts arent selected then only add the ones that are selected
            if (contacts.size() == 1) {
                //if uk numbers are like; 00440123456789
                String mobileRegex = "^0{2}[0-9]{11,}";

                //only add the number and trim the rest which shows the id.
                String contact = contacts.get(0).toString();
                String sContact = contact.substring(contact.indexOf(";") + 1);
                sContact.trim();

//                if (sContact.matches(mobileRegex)) {
//                    //remove the first 2 numbers which are "00" and add "+"
//                    String newContact = sContact.substring(2);
//
//                    newContact = "+" + newContact;
//                    contact1TextView.setText(newContact);
//
//                    sFinalContact1 = newContact;
//
//
//                } else {
//                    //add it to contact textview
                contact1TextView.setText(sContact);

                sFinalContact1 = sContact;
//                }
            } else {

            }
            if (contacts.size() == 2) {
                //only add the number and trim the rest which shows the id.
                String contact = contacts.get(0).toString();
                String sContact = contact.substring(contact.indexOf(";") + 1);
                sContact.trim();
                //add it to contact text view
                contact1TextView.setText(sContact);

                sFinalContact1 = sContact;

                //only add the number and trim the rest which shows the id.
                String contact2 = contacts.get(1).toString();
                String sContact2 = contact2.substring(contact2.indexOf(";") + 1);
                sContact2.trim();
                //add it to contact text view
                contact2TextView.setText(sContact2);

                sFinalContact2 = sContact2;


            } else {

            }
            if (contacts.size() == 3) {
                //only add the number and trim the rest which shows the id.
                String contact = contacts.get(0).toString();
                String sContact = contact.substring(contact.indexOf(";") + 1);
                sContact.trim();
                //add it to contact text view
                contact1TextView.setText(sContact);

                sFinalContact1 = sContact;


                //only add the number and trim the rest which shows the id.
                String contact2 = contacts.get(1).toString();
                String sContact2 = contact2.substring(contact2.indexOf(";") + 1);
                sContact2.trim();
                //add it to contact text view
                contact2TextView.setText(sContact2);

                sFinalContact2 = sContact2;


                //only add the number and trim the rest which shows the id.
                String contact3 = contacts.get(2).toString();
                String sContact3 = contact3.substring(contact3.indexOf(";") + 1);
                sContact3.trim();
                //add it to contact text view
                contact3TextView.setText(sContact3);

                sFinalContact3 = sContact3;


            } else {

            }

        }

    }

}