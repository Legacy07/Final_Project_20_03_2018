package com.ahmet.final_project;


import android.Manifest;
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
import android.widget.Toast;


public class Permission_Fragment extends Fragment {

    Button next;

    private static final int PERMISSION_REQUEST_LOCATION = 0;


    public Permission_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_permission_, container, false);

        next = (Button) view.findViewById(R.id.permissionNextButton);

        goNextPage();

        return view;
    }


    public void goNextPage() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //opens the permission Fragment
                Setup_1 setup1 = new Setup_1();
                FragmentManager manager = getFragmentManager();
                //replacing the fragment inside the layout
                manager.beginTransaction().replace(R.id.layout_Fragment, setup1).addToBackStack(null).commit();
            }
        });
    }

}
