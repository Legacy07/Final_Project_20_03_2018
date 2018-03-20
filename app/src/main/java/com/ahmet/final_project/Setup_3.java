package com.ahmet.final_project;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


public class Setup_3 extends Fragment {

    Button finish;
    Button backButton;
    Spinner power_button_press_spinner;

    private static final int PERMISSION_REQUEST_LOCATION = 0;

    DbHandler db;

    public Setup_3() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup_3, container, false);

        db = new DbHandler(getActivity());

        finish = (Button) view.findViewById(R.id.finishButtonSetup3);
        backButton = (Button) view.findViewById(R.id.setup3BackButton);

        power_button_press_spinner = (Spinner) view.findViewById(R.id.power_spinner);

        //https://developer.android.com/guide/topics/ui/controls/spinner.html
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.power_button_press, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        power_button_press_spinner.setAdapter(adapter);

        int permissionLocation = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);


        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
        } else {
            //ask for permission if access is not given
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);

        }

        finishSetup();
        goBack();
        return view;
    }

    public void finishSetup() {
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Main main = (Main) getActivity();
                    //adding the input fields to the related setter in main activity,
                    String setPower = power_button_press_spinner.getSelectedItem().toString();
                    main.setPower_press(setPower);

                    //getting the static data from getters in main activity set from the previous setup pages and adding it to the database.
                    String sName = main.getName();
                    String sMessage = main.getMessage();
                    String sContact = main.getContact1();
                    String sContact2 = main.getContact2();
                    String sContact3 = main.getContact3();
                    String sContact4 = main.getContact4();
                    String sLoc_update = main.getLoc_update();
                    String sLoc_refresh = main.getLoc_refresh();
                    String sLoc_history = main.getLoc_history();
                    String sPower = main.getPower_press();
                    String isnotfirsttime = "1";
                    boolean insertedData = db.AddFromSetup(sName, sMessage, sContact, sContact2, sContact3, sContact4, sLoc_update, sLoc_refresh, sLoc_history, sPower, isnotfirsttime);
                    if (insertedData == true) {

                        //complete setup and open home page
                        startActivity(new Intent(getActivity(), Home.class));
                        getActivity().finish();
                    }
// else {
//                Toast.makeText(getActivity(), "Data couldn't be added!", Toast.LENGTH_LONG).show();
//
//            }
                } catch (Exception exc) {
                    exc.printStackTrace();
                    showMessage("Error", exc.getMessage());
                }

            }
        });
    }

    public void goBack() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //opens the previous page
                Setup_2 setup_2 = new Setup_2();
                FragmentManager manager = getFragmentManager();
                //replacing the fragment inside the layout
                manager.beginTransaction().replace(R.id.layout_Fragment, setup_2).addToBackStack(null).commit();
            }
        });
    }

    //getting the grant from the user and handling the response
    @Override
    public void onRequestPermissionsResult(int request, String[] permission, int[] grant) {
        super.onRequestPermissionsResult(request, permission, grant);
        switch (request) {

            case PERMISSION_REQUEST_LOCATION: {
                if (grant.length >= 0 && grant[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getActivity(), "Application will not fully function if you don't grant permissions", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

}
