package com.psyjjn.runningtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    /**
     * Creates new database with the activities table
     *
     * @param context
     */
    public DBHelper(Context context) {
        super(context, "activityTrackerDB", null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE activities (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, type VARCHAR(128) NOT NULL, distance DOUBLE NOT NULL, elevation DOUBLE NOT NULL, startdatetime VARCHAR(128) NOT NULL, enddatetime VARCHAR(128) NOT NULL, origin DOUBLE, destination DOUBLE, destinationText VARCHAR(128), rating INTEGER, avgspeed , comments VARCHAR(128));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}