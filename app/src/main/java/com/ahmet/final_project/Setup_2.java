package com.ahmet.final_project;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Setup_2 extends Fragment {

    Button nextButton;
    Button backButton;
    Button addContactB1;

    Spinner loc_update;
    Spinner loc_refresh;
    Spinner loc_history;

    TextView contactText1;
    TextView contactText2;
    TextView contactText3;


    private Uri contactUri;
    private String contactID;
    private static final int CONTACT_REQUEST_CODE = 1;

    private static final int PERMISSION_REQUEST_CONTACTS = 0;


    public Setup_2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup_2, container, false);

        nextButton = (Button) view.findViewById(R.id.setup2NextButton);
        backButton = (Button) view.findViewById(R.id.setup2BackButton);
        addContactB1 = (Button) view.findViewById(R.id.addContactButton1);

        loc_update = (Spinner) view.findViewById(R.id.updateSpinner);
        loc_refresh = (Spinner) view.findViewById(R.id.refreshSpinner);
        loc_history = (Spinner) view.findViewById(R.id.historySpinner);

        contactText1 = (TextView) view.findViewById(R.id.contactTextview1);
        contactText2 = (TextView) view.findViewById(R.id.contactTextview2);
        contactText3 = (TextView) view.findViewById(R.id.contactTextview3);

        //https://developer.android.com/guide/topics/ui/controls/spinner.html
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.location_update, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loc_update.setAdapter(adapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.refresh_location, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loc_refresh.setAdapter(adapter2);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getActivity(),
                R.array.location_history, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loc_history.setAdapter(adapter3);

        int permissionContacts = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);

        if (permissionContacts == PackageManager.PERMISSION_GRANTED) {
        } else {
            //ask for permission if access is not given
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CONTACTS);

        }

        Main main = (Main) getActivity();

        //get the static data to show in fields if the user decides to go back
//        String update = main.getLoc_update().toString();
//        String refresh = main.getLoc_refresh().toString();
//        String history = main.getLoc_history().toString();
        String contact = main.getContact1().toString();
        //set the number
        contactText1.setText(contact);


        AddContact1();
        goNextPage();
        goBack();
        return view;
    }

    public void goNextPage() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sContactcheck = contactText1.getText().toString();
                if (sContactcheck.equals("")) {
                    //if empty then output toast
                    Toast.makeText(getActivity(), "At least 1 contact needs to be selected", Toast.LENGTH_LONG).show();
                } else {
                    Main main = (Main) getActivity();
                    //adding the input fields to the related setter in main activity,
                    String setLoca_update = loc_update.getSelectedItem().toString();
                    main.setLoc_update(setLoca_update);

                    String setLoca_refresh = loc_refresh.getSelectedItem().toString();
                    main.setLoc_refresh(setLoca_refresh);

                    String setLoca_history = loc_history.getSelectedItem().toString();
                    main.setLoc_history(setLoca_history);

                    //opens the next setup
                    Setup_3 setup_3 = new Setup_3();
                    FragmentManager manager = getFragmentManager();
                    //replacing the fragment inside the layout
                    manager.beginTransaction().replace(R.id.layout_Fragment, setup_3).commit();
                }
            }
        });
    }

    public void goBack() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //opens the previous page
                Setup_1 setup_1 = new Setup_1();
                FragmentManager manager = getFragmentManager();
                //replacing the fragment inside the layout
                manager.beginTransaction().replace(R.id.layout_Fragment, setup_1).addToBackStack(null).commit();
            }
        });
    }

    //getting the grant from the user and handling the response
    @Override
    public void onRequestPermissionsResult(int request, String[] permission, int[] grant) {
        super.onRequestPermissionsResult(request, permission, grant);
        switch (request) {

            case PERMISSION_REQUEST_CONTACTS: {
                if (grant.length >= 0 && grant[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getActivity(), "Application will not fully function if you don't grant permissions", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    //adapted from https://gist.github.com/evandrix/7058235
    //and https://developer.android.com/reference/android/provider/ContactsContract.Data.html
    //https://developer.android.com/training/contacts-provider/retrieve-names.html#NameMatch

    private void AddContact1() {

        addContactB1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //opens the contacts list
//                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), CONTACT_REQUEST_CODE);
                //opens contact list and allows the user to select limited multiple contacts
                Intent contactList = new Intent("intent.action.INTERACTION_TOPMENU");
                contactList.putExtra("additional", "phone-multi");
                contactList.putExtra("maxRecipientCount", 4);
                contactList.putExtra("FromMMS", true);

                startActivityForResult(contactList, CONTACT_REQUEST_CODE);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//
// comparing the request code to allow picking contacts
//        if (requestCode == CONTACT_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
//            //it gets the data of the selected contact
//            contactUri = data.getData();
//        }
//
//        RetrieveContact();

        Main main = (Main) getActivity();

        if (requestCode == CONTACT_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {

            Bundle bundle = data.getExtras();

            String result = bundle.getString("result");
            ArrayList<String> contacts = bundle.getStringArrayList("result");

            //if max amount of contacts arent selected then only add the ones that are selected
            if (contacts.size() == 1) {
                //only add the number and trim the rest which shows the id.
                String contact = contacts.get(0).toString();
                String sContact = contact.substring(contact.indexOf(";") + 1);
                sContact.trim();
                //add it to contact text view
                contactText1.setText(sContact);
                //add it to the setter
                main.setContact1(sContact);
            } else {

            }
            if (contacts.size() == 2) {
                //only add the number and trim the rest which shows the id.
                String contact = contacts.get(0).toString();
                String sContact = contact.substring(contact.indexOf(";") + 1);
                sContact.trim();
                //add it to contact text view
                contactText1.setText(sContact);
                //add it to the setter
                main.setContact1(sContact);

                //only add the number and trim the rest which shows the id.
                String contact2 = contacts.get(1).toString();
                String sContact2 = contact.substring(contact2.indexOf(";") + 1);
                sContact2.trim();
                //add it to contact text view
                contactText2.setText(sContact2);
                //add it to the setter
                main.setContact2(sContact2);
            } else {

            }
            if (contacts.size() == 3) {
                //only add the number and trim the rest which shows the id.
                String contact = contacts.get(0).toString();
                String sContact = contact.substring(contact.indexOf(";") + 1);
                sContact.trim();
                //add it to contact text view
                contactText1.setText(sContact);
                //add it to the setter
                main.setContact1(sContact);

                //only add the number and trim the rest which shows the id.
                String contact2 = contacts.get(1).toString();
                String sContact2 = contact2.substring(contact2.indexOf(";") + 1);
                sContact2.trim();
                //add it to contact text view
                contactText2.setText(sContact2);
                //add it to the setter
                main.setContact2(sContact2);

                //only add the number and trim the rest which shows the id.
                String contact3 = contacts.get(2).toString();
                String sContact3 = contact3.substring(contact3.indexOf(";") + 1);
                sContact3.trim();
                //add it to contact text view
                contactText3.setText(sContact3);
                //add it to the setter
                main.setContact3(sContact3);
            } else {

            }

        }

    }

    //get the contact name and number, save it in database
    private void RetrieveContact() {

        String contactNumber = null;
        String contactName = null;

        //query the contact id
        Cursor cursorID = getActivity().getContentResolver().query(contactUri, new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            //get the contact id
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

//        // use cursor to query contacts
//        Cursor cursorName = getActivity().getContentResolver().query(contactUri, null, null, null, null);
//
//        if (cursorName.moveToFirst()) {
//            //get the name of the selected contact
//            contactName = cursorName.getString(cursorName.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//        }
//
//        cursorName.close();

        // get mobile number for the selected contact using the contact id
        Cursor cursorMobileNumber = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorMobileNumber.moveToFirst()) {
            //get the number of the selected contact
            contactNumber = cursorMobileNumber.getString(cursorMobileNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorMobileNumber.close();

        Main main = (Main) getActivity();
        //adding the contact number in main so it can then be added in the db later on.
        String contactNo = contactNumber;
        main.setContact1(contactNo);

        contactText1.setText(contactNumber);

        //showMessage("Contact", "Name: " + contactName + " " + "Number: " + contactNumber);
    }

    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
}
