package com.ahmet.final_project;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class Setup_1 extends Fragment {

    Button nextButton;
    Button backButton;
    EditText nameText;
    EditText messageText;

    private static final int PERMISSION_REQUEST_SEND_SMS = 0;
    DbHandler db;


    public Setup_1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.setup_1, container, false);

        db = new DbHandler(getActivity());

        nextButton = (Button) view.findViewById(R.id.setup1NextButton);
        backButton = (Button) view.findViewById(R.id.setup1BackButton);
        nameText = (EditText) view.findViewById(R.id.nameEdittext);
        messageText = (EditText) view.findViewById(R.id.setMessageEditText);

        messageText.setEnabled(false);
        //get the permission from manifest to compare
        int permissionSMS = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS);

        if (permissionSMS == PackageManager.PERMISSION_GRANTED) {
        } else {
            //ask for permission if access is not given
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);

        }

        Main main = (Main) getActivity();

        //get the static data to show in fields if the user decides to go back
        String name = main.getName().toString();
        String message = main.getMessage().toString();
        //set the data in fields
        nameText.setText(name);
        messageText.setText(message);

        goNextPage();
        goBack();
        return view;
    }

    public void goNextPage() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String checkNameText = nameText.getText().toString();
                String message = messageText.getText().toString();

                //check if textboxes are empty
                if (checkNameText.equals("") || message.equals("")) {
                    //if empty then output toast
                    Toast.makeText(getActivity(), "Fields need to be filled in order to continue!", Toast.LENGTH_LONG).show();
                } else {

//                    //adding information to the following columns in that method
//                    boolean insertedData = db.addSetup1(checkNameText, checkNumberText, message);
//                    if (insertedData == true) {

                    Main main = (Main) getActivity();
                    //adding the input fields to the related setter in main activity,
                    // so at the very end of the setup it can be pulled from the getter to add into the db
                    String setName = checkNameText;
                    main.setName(setName);

                    String setMessage = message;
                    main.setMessage(setMessage);

                    //opens the next setup
                    Setup_2 setup_2 = new Setup_2();
                    FragmentManager manager = getFragmentManager();
                    //replacing the fragment inside the layout
                    manager.beginTransaction().replace(R.id.layout_Fragment, setup_2).addToBackStack(null).commit();

//                    } else {
//                        Toast.makeText(getActivity(), "Data couldn't be added!", Toast.LENGTH_LONG).show();
//
//                    }

                }
            }
        });
    }

    public void goBack() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //opens the previous page
                Permission_Fragment permission_fragment = new Permission_Fragment();
                FragmentManager manager = getFragmentManager();
                //replacing the fragment inside the layout
                manager.beginTransaction().replace(R.id.layout_Fragment, permission_fragment).addToBackStack(null).commit();

            }
        });
    }

    //check the link for if the user denies the permission
    //https://stackoverflow.com/questions/30719047/android-m-check-runtime-permission-how-to-determine-if-the-user-checked-nev
    //getting the grant from the user and handling the response
    @Override
    public void onRequestPermissionsResult(int request, String[] permission, int[] grant) {
        super.onRequestPermissionsResult(request, permission, grant);
        switch (request) {
            case PERMISSION_REQUEST_SEND_SMS: {
                if (grant.length >= 0 && grant[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getActivity(), "Application will not fully function if you don't grant permissions", Toast.LENGTH_LONG).show();
                }

            }
        }
    }


}
