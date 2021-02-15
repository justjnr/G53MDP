package com.psyjjn.runningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityViewActivity extends AppCompatActivity {

    int currentActivityId;
    String currentActivityType;
    TrackerUtils trackerUtils = new TrackerUtils();
    boolean editMode = false;

    TrackerReceiver trackerReceiver = new TrackerReceiver();
    IntentFilter filter = new IntentFilter("com.runningtracker.UPDATE_DB");

    /**
     * Set event listeners, fonts and titles
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activity);

        Bundle bundle = getIntent().getExtras();
        currentActivityId = bundle.getInt("activityId");
        Log.d("g53mdp", currentActivityId + "");
        String currentRecipeIdStr = String.valueOf(currentActivityId);

        TextView title = findViewById(R.id.textView9);
        TextView dateTitle = findViewById(R.id.textView12);
        TextView avgTitle = findViewById(R.id.textView14);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/TrackerFont-Demi.ttf");
        //Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/TrackerFont-Medium.ttf");
        title.setTypeface(typeface);
        title.setLetterSpacing((float)-0.025);
        dateTitle.setTypeface(typeface);
        dateTitle.setLetterSpacing((float)-0.025);
        avgTitle.setTypeface(typeface);
        avgTitle.setLetterSpacing((float)-0.025);

        final EditText editRating = findViewById(R.id.editText2);
        final EditText editComments = findViewById(R.id.commentsEditText);
        disableEditText(editRating);
        disableEditText(editComments);

        /**
         * Enable editTexts to be editable and save to db on finish
         */
        final FloatingActionButton buttonEdit = findViewById(R.id.editButton);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editMode){
                    enableEditText(editRating);
                    enableEditText(editComments);
                    buttonEdit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00C853")));
                    /**
                     * Open source icons sourced from : https://material.io/resources/icons/?style=baseline
                     */
                    buttonEdit.setImageResource(R.drawable.baseline_check_circle_white_48dp);

                    try{
                        unregisterReceiver(trackerReceiver);
                    }catch(Exception e){}

                    editMode = true;
                } else {
                    disableEditText(editRating);
                    disableEditText(editComments);
                    String ratingString = editRating.getText().toString();
                    updateDBData(Integer.valueOf(ratingString), editComments.getText().toString());
                    buttonEdit.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFAB40")));
                    /**
                     * Open source icons sourced from : https://material.io/resources/icons/?style=baseline
                     */
                    buttonEdit.setImageResource(R.drawable.baseline_edit_white_48dp);

                    Intent bIntent = new Intent("com.runningtracker.UPDATE_DB");
                    registerReceiver(trackerReceiver, filter);
                    sendBroadcast(bIntent);

                    editMode = false;
                }
            }
        });

        final FloatingActionButton buttonDelete = findViewById(R.id.deleteButton);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDBData();
                finish();
            }
        });

        /**
         * Returns all database info and puts all information into appropriate views
         */
        Cursor cursor = getDBData();
        if (cursor.moveToFirst()) {
            currentActivityType = cursor.getString(cursor.getColumnIndex(DBProviderContract.TYPE));
            title.setText("This " + cursor.getString(cursor.getColumnIndex(DBProviderContract.TYPE)));
            Date rawEndDate = trackerUtils.convert8601toDate(cursor.getString(cursor.getColumnIndex(DBProviderContract.ENDDATETIME)));
            Date rawStartDate = trackerUtils.convert8601toDate(cursor.getString(cursor.getColumnIndex(DBProviderContract.STARTDATETIME)));
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy");
            String dateTime = dateFormat.format(rawStartDate);
            dateTitle.setText(dateTime);

            String rawDistance = cursor.getString(cursor.getColumnIndex(DBProviderContract.DISTANCE));
            //long distance = Math.round(convertMtoKM(Double.parseDouble(rawDistance)));
            TextView distanceText = findViewById(R.id.distanceView);
            distanceText.setText(rawDistance);

            //long diffInMillis = rawEndDate.getTime() - rawStartDate.getTime();
            int[] timeArray = trackerUtils.differenceBetweenTimes(rawEndDate.getTime(), rawStartDate.getTime());
            String secondsText = String.format("%02d", timeArray[1]);
            String minutesText = String.format("%02d", timeArray[2]);
            String hoursText = String.format("%01d", timeArray[3]);
            TextView durationText = findViewById(R.id.durationView);
            durationText.setText(hoursText + ":" + minutesText + ":" + secondsText);
            Log.d("g53mdp", rawEndDate.getTime() + "");
            Log.d("g53mdp", timeArray[0] + " " + timeArray[1] + " " + timeArray[2] + " " + timeArray[3] + "");

            TextView elevationText = findViewById(R.id.elevationView);
            elevationText.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract.ELEVATION)));
            TextView coarseLocationText = findViewById(R.id.coarseLocationView);
            if (cursor.getString(cursor.getColumnIndex(DBProviderContract.DESTINATIONTEXT)) == null){
                coarseLocationText.setText("No Location");
            } else {
                Log.d("g53mdp", ":" + cursor.getString(cursor.getColumnIndex(DBProviderContract.DESTINATIONTEXT)) + ":");
                coarseLocationText.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract.DESTINATIONTEXT)));
            }

            editComments.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract.COMMENTS)));
            editRating.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract.RATING)));
            TextView avgSpeedText = findViewById(R.id.avgSpeedView);
            avgSpeedText.setText(cursor.getString(cursor.getColumnIndex(DBProviderContract.AVGSPEED)));
        }

        /**
         * Calculate average speed metrics to display
         */
        Cursor cursor2 = getDBColumn(DBProviderContract.AVGSPEED);
        if (cursor2.moveToFirst()) {
            int i = 0;
            int sumAvgs = 0;
            do {
                i++;
                sumAvgs += Integer.valueOf(cursor2.getString(cursor2.getColumnIndex(DBProviderContract.AVGSPEED)));
                Log.d("g53mdp", cursor2.getString(cursor2.getColumnIndex(DBProviderContract.AVGSPEED)));
            } while (cursor2.moveToNext());

            double ratioAvgSpeed = (double)Integer.valueOf(cursor.getString(cursor.getColumnIndex(DBProviderContract.AVGSPEED))) / (sumAvgs / i);
            TextView avgSpeedText = findViewById(R.id.avgSpeedCompView);
            TextView avgSpeedSubText = findViewById(R.id.textView15);
            if (ratioAvgSpeed >= 1.00){
                TextView speedUnit = findViewById(R.id.textView16);
                speedUnit.setVisibility(View.GONE);
                avgSpeedSubText.setText("average speed");
                avgSpeedText.setText("All time best");
            } else {
                String ratioAvgText = String.format("%.2f", ratioAvgSpeed);
                avgSpeedText.setText(ratioAvgText);
                avgSpeedSubText.setText("Avg. speed of previous " + currentActivityType.toLowerCase() + "s");
            }
        }

        /**
         * Calculate distance metrics to display
         */
        Cursor cursor3 = getDBColumn(DBProviderContract.DISTANCE);
        if (cursor3.moveToFirst()) {
            double highestDistance = Double.valueOf(cursor3.getString(cursor3.getColumnIndex(DBProviderContract.DISTANCE)));
            double currentDistance = Double.valueOf(cursor.getString(cursor.getColumnIndex(DBProviderContract.DISTANCE)));
            TextView distanceCompText = findViewById(R.id.allTimeDistanceView);
            TextView distanceSubText = findViewById(R.id.textView18);
            if (highestDistance - currentDistance <= 0.00){
                TextView distanceUnit = findViewById(R.id.textView17);
                distanceCompText.setText("Longest");
                distanceSubText.setText(currentActivityType.toLowerCase() + " so far");
                distanceUnit.setVisibility(View.GONE);
            } else {
                String newVal = String.format("%.2f", highestDistance - currentDistance);
                distanceSubText.setText("Shorter than your longest " + currentActivityType.toLowerCase());
                distanceCompText.setText(newVal);
            }
        }
    }

    /**
     * Unregister broadcast receiver on destroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(trackerReceiver);
        } catch (Exception e) {}

    }
    /**
     * Get current activity data with all fields
     * @return
     */
    protected Cursor getDBData(){
        return getContentResolver().query(DBProviderContract.ACTIVITIES_URI, null, DBProviderContract._ID + "=?", new String[] { String.valueOf(currentActivityId) }, null);
    }

    /**
     * Get specific database column
     * @param column - column to access
     * @return
     */
    protected Cursor getDBColumn(String column){
        return getContentResolver().query(DBProviderContract.ACTIVITIES_URI, new String[] { column }, DBProviderContract.TYPE + "=?", new String[] { currentActivityType }, column + " DESC");
    }

    /**
     * Deletes item from database
     */
    protected void deleteDBData(){
        getContentResolver().delete(DBProviderContract.ACTIVITIES_URI, DBProviderContract._ID + "=?", new String[] { String.valueOf(currentActivityId) });
    }

    /**
     * Updates row in database
     * @param ratingInt - rating to update
     * @param comments - comment to update
     */
    protected void updateDBData(int ratingInt, String comments){
        ContentValues updateVals = new ContentValues();
        updateVals.put(DBProviderContract.RATING, ratingInt);
        updateVals.put(DBProviderContract.COMMENTS, comments);
        getContentResolver().update(DBProviderContract.ACTIVITIES_URI, updateVals, DBProviderContract._ID + "=?", new String[] { String.valueOf(currentActivityId) });
    }

    /**
     * Disable clicking on the editText
     * @param editText - editText to disable click for
     */
    protected void disableEditText(EditText editText) {
        editText.setEnabled(false);
        editText.setFocusable(false);
        //editText.setKeyListener(null);
        editText.setCursorVisible(false);
        editText.setAlpha(1);
    }

    /**
     * Enable clicking on the editText
     * @param editText - editText to enable click for
     */
    protected void enableEditText(EditText editText) {
        editText.setEnabled(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        //editText.setKeyListener(null);
        editText.setCursorVisible(true);
        editText.setAlpha((float)0.25);
    }
}
