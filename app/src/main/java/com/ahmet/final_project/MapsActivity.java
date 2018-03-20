package com.ahmet.final_project;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longitude = 0;
    double latitude = 0;

    DbHandler db;

    private static final int PERMISSION_REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        db = new DbHandler(getApplicationContext());
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //https://developers.google.com/maps/documentation/android-api/current-place-tutorial
    //https://javapapers.com/android/android-show-current-location-on-map-using-google-maps-api/
    //https://stackoverflow.com/questions/21403496/how-to-get-current-location-in-google-map-android
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //get refresh location
        final int loc_refresh = (Integer.parseInt(db.getLocation_Refresh().toString()) * 1000) * 60;

        int permissionLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        //location manager helps to get current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            try {
                try {
                    //using network provider service to get an accurate estimation of the location
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    //get the values of lat long
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    //use lat long to define the location
                    LatLng currentLocation = new LatLng(latitude, longitude);
                    //add a marker of the gathered location
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current location"));
                    //moves the screen towards the marker with a zoom level of 15 showing streets
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } catch (Exception ec) {
                    ec.printStackTrace();
                    showMessage("Error occurred", ec.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }

                };
                //updates the location and the level of zoom level. 20 is buildings level
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, loc_refresh, 20, locationListener);
            } catch (
                    Exception exce)

            {
                exce.printStackTrace();
                showMessage("Error occurred", exce.getMessage());
            }


        } else {
            //ask for permission if access is not given
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

        }


        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                    // Toast.makeText(this.getApplicationContext(), "Application will not fully function if you don't grant permissions", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
}
