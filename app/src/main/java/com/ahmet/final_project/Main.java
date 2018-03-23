package com.ahmet.final_project;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Main extends AppCompatActivity {

    Button button;
    Button showButton;

    DbHandler db;

    SharedPreferences firstTimeUser;

    //variables to hold information from setup fragments and ad it to the database
    private String name = "Ahmet";
    private String message = "I'm in danger, message me back as soon as possible.";
    private String contact1 = "+447740061788";
    private String contact2 = "";
    private String contact3 = "";
    private String contact4 = "";
    private String loc_update = "";
    private String loc_refresh = "";
    private String loc_history = "";
    private String power_press = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DbHandler(getApplicationContext());

//        //declare shared preference
//        firstTimeUser = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
//        //edit shared preference
//        SharedPreferences.Editor editor = firstTimeUser.edit();
//        //true for first time user.
//        boolean isFirst = firstTimeUser.getBoolean("first", true);
//
//        //if its first time, do nothing
//        if (isFirst) {
//
//            //change boolean value for not first time
//            editor.putBoolean("first", false);
//            editor.commit();
//
//        } else {
//
////            //if it's not first time then start with the home page
//            startActivity(new Intent(Main.this, Home.class));
//
//        }

        NotFirstTime();
        showButton = (Button) findViewById(R.id.showButton);
        button = (Button) findViewById(R.id.startButton);

        Show();
        startButton();
    }

    public void startButton() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set the content to a blank xml in order to replace the content with the fragments
                setContentView(R.layout.content_main);
                //opens the permission Fragment
                Permission_Fragment permission_fragment = new Permission_Fragment();

                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                //replacing the fragment inside the layout
                manager.beginTransaction().replace(R.id.layout_Fragment, permission_fragment).addToBackStack(null).commit();

            }
        });
    }

    public void Show() {
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


    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    //allows to check if the user have completed the setup
    public void NotFirstTime() {

        String notfirsttime = "1";

        db = new DbHandler(getApplicationContext());

        String notFirstTimeTrue = db.getNotFirstTime(notfirsttime);

        if (notfirsttime.equals(notFirstTimeTrue)) {
            //if it's not first time then start with the home page
            startActivity(new Intent(Main.this, Home.class));
            //destroys activity so the user cannot go back
            finish();
        } else {
        }
    }


    //getters and setters

    public String getName() {
        return this.name;
    }

    public void setName(String sName) {
        this.name = sName;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String sMessage) {
        this.message = sMessage;
    }

    public String getContact1() {
        return this.contact1;
    }

    public void setContact1(String sContact1) {
        this.contact1 = sContact1;
    }

    public String getContact2() {
        return this.contact2;
    }

    public void setContact2(String sContact2) {
        this.contact2 = sContact2;
    }

    public String getContact3() {
        return this.contact3;
    }

    public void setContact3(String sContact3) {
        this.contact3 = sContact3;
    }

    public String getContact4() {
        return this.contact4;
    }

    public void setContact4(String sContact4) {
        this.contact4 = sContact4;
    }

    public String getLoc_update() {
        return this.loc_update;
    }

    public void setLoc_update(String sLoc_update) {
        this.loc_update = sLoc_update;
    }

    public String getLoc_refresh() {
        return this.loc_refresh;
    }

    public void setLoc_refresh(String sLoc_refresh) {
        this.loc_refresh = sLoc_refresh;
    }

    public String getLoc_history() {
        return this.loc_history;
    }

    public void setLoc_history(String sLoc_history) {
        this.loc_history = sLoc_history;
    }

    public String getPower_press() {
        return this.power_press;
    }

    public void setPower_press(String sPower_press) {
        this.power_press = sPower_press;
    }
}
