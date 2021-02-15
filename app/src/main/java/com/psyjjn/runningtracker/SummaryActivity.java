package com.psyjjn.runningtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {
    TrackerUtils trackerUtils = new TrackerUtils();
    List<Object> dateList = new ArrayList<>();
    LinearLayout linearLayout;
    LayoutInflater inflater;

    /**
     * Sets custom font and sizes and assigns views
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        TextView title = findViewById(R.id.textView5);
        View inflatedView = getLayoutInflater().inflate(R.layout.template_layout_summary, null);
        TextView dateTitle = inflatedView.findViewById(R.id.summaryDateTimeView);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/TrackerFont-Demi.ttf");
        title.setTypeface(typeface);
        title.setLetterSpacing((float)-0.025);
        dateTitle.setTypeface(typeface);
        dateTitle.setLetterSpacing((float)-0.025);

        Cursor cursor = getDBColumn(null , DBProviderContract.STARTDATETIME);
        linearLayout = findViewById(R.id.summaryLayout);
        inflater = LayoutInflater.from(this);

        setAllDates(cursor);
    }

    /**
     * Returns a date with zeroed hours, minutes and seconds
     * so that it can be compared with
     * @param date - date passed in that you want to zero out the time
     * @return - date object with new values
     */
    protected Date getDateWithoutTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        return date;
    }

    /**
     * Method that looks at all dates within the database and determines for each date, the total metrics
     * for all activities within each date.
     * @param cursor - database cursor to pass in
     */
    protected void setAllDates(Cursor cursor){
        Date lastDate = null;
        if (cursor.moveToFirst()) {
            do {
                Log.d("g53mdp", cursor.getString(cursor.getColumnIndex(DBProviderContract.STARTDATETIME)) + " DATE" + cursor.getString(cursor.getColumnIndex(DBProviderContract._ID)) + "ID");
                Date rawStartDate = trackerUtils.convert8601toDate(cursor.getString(cursor.getColumnIndex(DBProviderContract.STARTDATETIME)));
                //Log.d("g53mdp", getDateWithoutTime(rawStartDate) + " NOTIMEDATE");
                if (lastDate == null || getDateWithoutTime(rawStartDate).before(getDateWithoutTime(lastDate))) {
                    dateList.add(getDateWithoutTime(rawStartDate));
                } else if (lastDate == null || getDateWithoutTime(rawStartDate).after(getDateWithoutTime(lastDate))){
                    dateList.add(getDateWithoutTime(rawStartDate));
                } else {}
                lastDate = rawStartDate;
            } while (cursor.moveToNext());
        }

        for (int i = 0, j = 0; i < dateList.size(); i++, ++j){
            Date date = (Date)dateList.get(i);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            Date datePlusOne = cal.getTime();
            double sumDistance = 0;
            double sumElevation = 0;
            int sumAvgSpeed = 0;
            long sumDurationMs = 0;
            int tResults = 0;
            if (cursor.moveToFirst()) {
                do {
                    Date rawStartDate = trackerUtils.convert8601toDate(cursor.getString(cursor.getColumnIndex(DBProviderContract.STARTDATETIME)));
                    Date rawEndDate = trackerUtils.convert8601toDate(cursor.getString(cursor.getColumnIndex(DBProviderContract.ENDDATETIME)));
                    if (rawStartDate.after(date) && rawStartDate.before(datePlusOne)){
                        sumDurationMs += rawEndDate.getTime() - rawStartDate.getTime();
                        tResults++;
                        sumDistance += Double.valueOf(cursor.getString(cursor.getColumnIndex(DBProviderContract.DISTANCE)));
                        sumElevation += Double.valueOf(cursor.getString(cursor.getColumnIndex(DBProviderContract.ELEVATION)));
                        sumAvgSpeed += Integer.valueOf(cursor.getString(cursor.getColumnIndex(DBProviderContract.AVGSPEED)));
                        Log.d("g53mdp", rawStartDate + " " + i + " ID");
                    }
                } while (cursor.moveToNext());
                Log.d("g53mdp", sumDurationMs + " duration");
                populateView(sumDistance, sumElevation, (sumAvgSpeed / tResults), sumDurationMs, date);
            }
            Log.d("g53mdp", dateList.get(i) + " DATES");
        }
    }

    /**
     * Return cursor for the columns and sort order requested
     * @param columns - selected columns to return
     * @param columnSort - sort by this column
     * @return - returns cursor containing selection
     */
    protected Cursor getDBColumn(String[] columns, String columnSort){
        return getContentResolver().query(DBProviderContract.ACTIVITIES_URI, columns, null, null, columnSort + " DESC");
    }

    /**
     * Populate the LinearContainer with the template layout
     * @param tDistance - total distance for the day
     * @param tElev - total elevation gain for the day
     * @param aSpeed - average speed for all activity for the day
     * @param tDuration - total duration of all activity during the day
     * @param date - the date for which to display totals
     */
    protected void populateView(double tDistance, double tElev, int aSpeed, long tDuration, Date date){
        View view = inflater.inflate(R.layout.template_layout_summary, linearLayout, false);
        TextView totalDistanceView = view.findViewById(R.id.totalDistanceView);
        TextView totalElevationView = view.findViewById(R.id.totalElevationView);
        TextView totalAvgSpeedView = view.findViewById(R.id.totalAvgSpeedView);
        TextView dateTitle = view.findViewById(R.id.summaryDateTimeView);
        TextView totalTime = view.findViewById(R.id.totalDurationView);

        int[] timeArray = trackerUtils.msToUnits(tDuration);
        String secondsText = String.format("%02d", timeArray[1]);
        String minutesText = String.format("%02d", timeArray[2]);
        String hoursText = String.format("%01d", timeArray[3]);
        String elapsedTime = hoursText + ":" + minutesText + ":" + secondsText;
        totalTime.setText(elapsedTime);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/TrackerFont-Demi.ttf");
        dateTitle.setTypeface(typeface);
        dateTitle.setLetterSpacing((float)-0.025);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy");
        String dateTime = dateFormat.format(date);

        totalElevationView.setText(tElev + "");
        totalAvgSpeedView.setText(aSpeed + "");
        totalDistanceView.setText(String.format("%.2f", tDistance));
        dateTitle.setText(dateTime);
        linearLayout.addView(view);
    }
}
