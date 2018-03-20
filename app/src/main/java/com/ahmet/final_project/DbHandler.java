package com.ahmet.final_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHandler extends SQLiteOpenHelper {

    //Database name
    public static final String DATABASE_NAME = "Database.db";
    //table name
    public static final String TABLE_NAME = "User_Information";
    //column names
    public static final String COL_1 = "Name";
    public static final String COL_2 = "Message";
    public static final String COL_3 = "Contact_Number1";
    public static final String COL_4 = "Contact_Number2";
    public static final String COL_5 = "Contact_Number3";
    public static final String COL_6 = "Contact_Number4";
    public static final String COL_7 = "Location_Update";
    public static final String COL_8 = "Refresh_Location";
    public static final String COL_9 = "Location_History";
    public static final String COL_10 = "Power_Button";
    public static final String COL_11 = "NotFirstTime";

    public SQLiteDatabase db;

    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //execute sql to create columns within a table
        try {
            db.execSQL("create table " + TABLE_NAME + " (NAME TEXT NOT NULL," +
                    "Message TEXT, Contact_Number1 TEXT, Contact_Number2 TEXT, Contact_Number3 TEXT, Contact_Number4 TEXT," +
                    " Location_Update TEXT, Refresh_Location TEXT, Location_History TEXT, " +
                    "Power_Button TEXT, NotFirstTime TEXT);");
        } catch (Exception ex) {
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    public void OnOPen() {
        db = getWritableDatabase();
    }


    //inserting data
    public boolean AddFromSetup(String name, String message, String contact1, String contact2, String contact3, String contact4,
                                String loc_update, String loc_refresh, String loc__history, String power_press, String notfirsttime) {

        SQLiteDatabase db = this.getWritableDatabase();
        //its like an array, empty values which new data be inserted
        ContentValues contentValues = new ContentValues();
        //adding it to columns
        contentValues.put(COL_1, name);
        contentValues.put(COL_2, message);
        contentValues.put(COL_3, contact1);
        contentValues.put(COL_4, contact2);
        contentValues.put(COL_5, contact3);
        contentValues.put(COL_6, contact4);
        contentValues.put(COL_7, loc_update);
        contentValues.put(COL_8, loc_refresh);
        contentValues.put(COL_9, loc__history);
        contentValues.put(COL_10, power_press);
        contentValues.put(COL_11, notfirsttime);

        //insert into database
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;

    }

    public boolean UpdateLocationUpdates(String name, String loc_update, String loc_refresh, String loc__history){

        SQLiteDatabase db = this.getWritableDatabase();
        //its like an array, empty values which new data be inserted
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COL_1, name);
        //put it into these columns
        contentValues.put(COL_7, loc_update);
        contentValues.put(COL_8, loc_refresh);
        contentValues.put(COL_9, loc__history);
        //update the database where name is the same
        long result = db.update(TABLE_NAME, contentValues, "Name = ?", new String[]{name});
        return result > 0 ;
    }
    public boolean UpdateActivation(String name, String power_press){

        SQLiteDatabase db = this.getWritableDatabase();
        //its like an array, empty values which new data be inserted
        ContentValues contentValues = new ContentValues();
        //put it into the column to update
        contentValues.put(COL_10, power_press);
        //update the database where name is the same
        long result = db.update(TABLE_NAME, contentValues, "Name = ?", new String[]{name});
        return result > 0 ;
    }

    //method to get notfirsttime result which is column 11
    public String getNotFirstTime(String notFirstTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find not first time result related to it
        Cursor cursor = db.query(TABLE_NAME, columns, COL_11 + "='" + notFirstTime + "'", null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting quantity from column 11
            String getNotFirstTime = cursor.getString(10);
            return getNotFirstTime;
        }
        return null;
    }

    //method to get name which is in column 1
    public String getName() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching the table
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting name from column 1
            String getName = cursor.getString(0);
            return getName;
        }
        return null;
    }

    //method to get message which is column 2
    public String getMessage() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find message
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting message from column 2
            String getMessage = cursor.getString(1);
            return getMessage;
        }
        return null;
    }

    //method to get first contact which is column 3
    public String getContact1() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find contact
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting contact 1 from column 3
            String getContact = cursor.getString(2);
            return getContact;
        }
        return null;
    }
    //method to get second contact which is column 4
    public String getContact2() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find contact
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting contact 2 from column 4
            String getContact = cursor.getString(3);
            return getContact;
        }
        return null;
    }
    //method to get third contact which is column 5
    public String getContact3() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find contact
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting contact 3 from column 5
            String getContact = cursor.getString(4);
            return getContact;
        }
        return null;
    }
    //update contacts
    public boolean UpdateContacts(String name, String contact1, String contact2, String contact3){

        SQLiteDatabase db = this.getWritableDatabase();
        //its like an array, empty values which new data be inserted
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COL_1, name);
        //put it into these columns
        contentValues.put(COL_3, contact1);
        contentValues.put(COL_4, contact2);
        contentValues.put(COL_5, contact3);
        //update the database where name is the same
        long result = db.update(TABLE_NAME, contentValues, "Name = ?", new String[]{name});
        return result > 0 ;

    }


    //method to get location update value which is column 7
    public String getLocation_Update() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find location update
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting loc update from column 7
            String getLocationUpdate = cursor.getString(6);
            return getLocationUpdate;
        }
        return null;
    }

    //method to get location refresh value which is column 8
    public String getLocation_Refresh() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find location refresh
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting loc update from column 8
            String getRefresh = cursor.getString(7);
            return getRefresh;
        }
        return null;
    }

    //method to get location history value which is column 9
    public String getLocation_History() {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //searching to find location history value
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            //getting loc history from column 9
            String getRefresh = cursor.getString(8);
            return getRefresh;
        }
        return null;
    }

    //retrieving the data
    //cursor class is an interface that is used to get data from database using resultset
    public String getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        //get all the columns
        String[] columns = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11};
        //query
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();
        //get the data in columns
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String message = cursor.getString(1);
            String contact1 = cursor.getString(2);
            String contact2 = cursor.getString(3);
            String contact3 = cursor.getString(4);
            String contact4 = cursor.getString(5);
            String loc_update = cursor.getString(6);
            String loc_refresh = cursor.getString(7);
            String loc_history = cursor.getString(8);
            String power = cursor.getString(9);
            String notfirsttime = cursor.getString(10);


            //output the data which is in a buffer
            buffer.append("Name: " + name + "\n" + "Message: " + message + "\n" + "Contact 1: " + contact1 + "\n" +
                    "Contact 2: " + contact2 + "\n" + "Contact 3: " + contact3 + "\n" + "Contact 4: " + contact4 + "\n" +
                    "Location Update in every: " + loc_update + "\n" + "Refresh Location in every: " + loc_refresh + "\n" +
                    "Location History: " + loc_history + "\n" +
                    "Power: " + power + "\n" + "Not first time: " + notfirsttime + "\n\n");

        }
        return buffer.toString();

    }

    protected void onClose() {

        //closing database connection
        if (db != null)
            db.close();
    }

}
