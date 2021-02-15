package com.psyjjn.runningtracker;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Custom Cursor Adapter class to convert dates from the database before putting them into the text views
 * Also changes icon image based on activity type
 */
public class TrackerAdapter extends CursorAdapter {
    TrackerUtils trackerUtils = new TrackerUtils();

    public TrackerAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.template_layout_activity, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView activityId = view.findViewById(R.id.activityId);
        TextView activityType = view.findViewById(R.id.activityType);
        TextView distanceView = view.findViewById(R.id.distanceView);
        TextView dateView = view.findViewById(R.id.dateTimeView);

        Date rawStartDate = trackerUtils.convert8601toDate(cursor.getString(cursor.getColumnIndex(DBProviderContract.STARTDATETIME)));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        String dateTime = dateFormat.format(rawStartDate);
        dateView.setText(dateTime);

        String currentType = cursor.getString(cursor.getColumnIndex(DBProviderContract.TYPE));
        AppCompatImageView imageView = view.findViewById(R.id.imageView2);

        /**
         * Open source icons sourced from : https://material.io/resources/icons/?style=baseline
         */
        if (currentType.equals("Run")){
            imageView.setImageResource(R.drawable.baseline_directions_run_black_48dp);
        } else {
            imageView.setImageResource(R.drawable.baseline_directions_walk_black_48dp);
        }

        activityId.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract._ID)));
        activityType.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract.TYPE)));
        distanceView.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract.DISTANCE)));
    }
}
