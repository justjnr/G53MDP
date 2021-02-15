package com.psyjjn.runningtracker;

import android.net.Uri;

public class DBProviderContract {
    public static final String AUTHORITY = "com.psyjjn.runningtracker.DBProvider";

    public static final Uri ACTIVITIES_URI = Uri.parse("content://"+AUTHORITY+"/activities");
    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");

    public static final String _ID = "_id";
    public static final String TYPE = "type";
    public static final String DISTANCE = "distance";
    public static final String ELEVATION = "elevation";
    public static final String STARTDATETIME = "startdatetime";
    public static final String ENDDATETIME = "enddatetime";
    public static final String ORIGIN = "origin";
    public static final String DESTINATION = "destination";
    public static final String DESTINATIONTEXT = "destinationText";
    public static final String RATING = "rating";
    public static final String AVGSPEED = "avgspeed";
    public static final String COMMENTS = "comments";

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/DBProvider.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/DBProvider.data.text";
}
