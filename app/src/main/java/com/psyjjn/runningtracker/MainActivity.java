package com.psyjjn.runningtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TrackerService.MyBinder myService = null;
    Intent intent;
    ListView listView;
    Button buttonRun, buttonWalk;
    View inflatedView;

    List<Long> speedAvgs;
    boolean walkActive = false, runActive = false;

    static final int ACTIVITY_MAIN_REQUEST_CODE = 0;
    static final int ACTIVITY_SUMMARY_REQUEST_CODE = 2;
    static final int ACTIVITY_ACTIVITYVIEW_REQUEST_CODE = 1;

    TrackerReceiver trackerReceiver = new TrackerReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getActionBar().hide();

        /**
         * Set fonts and intialises variables
         */
        TextView textView = findViewById(R.id.textView);
        inflatedView = getLayoutInflater().inflate(R.layout.template_layout_activity, null);
        TextView distanceView = inflatedView.findViewById(R.id.distanceView);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/TrackerFont-Demi.ttf");
        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/TrackerFont-Medium.ttf");
        textView.setTypeface(typeface);
        textView.setLetterSpacing((float)-0.025);
        distanceView.setTypeface(typeface2);
        distanceView.setLetterSpacing((float)-0.025);
        Log.d("g53mdp", myService + "");
        listView = findViewById(R.id.listView);
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3){
                TextView tv = v.findViewById(R.id.activityId);
                String tvText = tv.getText().toString();
                int tvInt = new Integer(tvText).intValue();
                Bundle bundle = new Bundle();
                bundle.putInt("activityId", tvInt);
                Intent myIntent = new Intent(MainActivity.this, ActivityViewActivity.class);
                myIntent.putExtras(bundle);
                startActivityForResult(myIntent, ACTIVITY_ACTIVITYVIEW_REQUEST_CODE);
            }
        });

        CardView summaryCard = findViewById(R.id.summaryCard);
        summaryCard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent myIntent = new Intent(MainActivity.this, SummaryActivity.class);
                startActivityForResult(myIntent, ACTIVITY_SUMMARY_REQUEST_CODE);
            }
        });

        findViewById(R.id.currentActivityCard).setVisibility(View.GONE);
        findViewById(R.id.summaryCard).setVisibility(View.GONE);

        /**
         * Starts service and registers associated broadcast intent
         */
        buttonRun = findViewById(R.id.startRunButton);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!walkActive){
                    /*Intent myIntent = new Intent(MainActivity.this, NewRecipeActivity.class);
                    startActivityForResult(myIntent, ACTIVITY_NEWRECIPE_REQUEST_CODE);*/
                    Log.d("g53mdp", myService + "");
                    if (myService == null) {
                        runActive = true;
                        buttonRun.setText("Stop Run");
                        buttonRun.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#505050")));
                        TextView currentActivity = findViewById(R.id.currentActivityType);
                        currentActivity.setText("Current Run");
                        findViewById(R.id.currentActivityCard).setVisibility(View.VISIBLE);

                        IntentFilter filter = new IntentFilter("com.runningtracker.START_RUN");
                        registerReceiver(trackerReceiver, filter);
                        Intent bIntent = new Intent("com.runningtracker.START_RUN");
                        //bIntent.setAction();
                        bIntent.putExtra("type", "Run");
                        sendBroadcast(bIntent);

                        intent = new Intent(MainActivity.this, TrackerService.class);
                        intent.putExtra("type", "Run");
                        MainActivity.this.bindService(new Intent(MainActivity.this, TrackerService.class), serviceConnection, 0);
                        startService(intent);
                    } else {
                        myService.ActivityStop(intent.putExtra("avgSpeed", getAvgSpeed()));
                        buttonRun.setText("Start Run");
                        buttonRun.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
                        findViewById(R.id.currentActivityCard).setVisibility(View.GONE);
                        runActive = false;

                        unregisterReceiver(trackerReceiver);

                        stopThread();
                        stopService(intent);
                        getDB("_id", " ASC");
                    }
                }
            }
        });

        /**
         * Starts service and registers associated broadcast intent
         */
        buttonWalk = findViewById(R.id.startWalkButton);
        buttonWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!runActive) {
                    /*Intent myIntent = new Intent(MainActivity.this, NewRecipeActivity.class);
                    startActivityForResult(myIntent, ACTIVITY_NEWRECIPE_REQUEST_CODE);*/
                    Log.d("g53mdp", myService + "");
                    if (myService == null) {
                        walkActive = true;
                        buttonWalk.setText("Stop Walk");
                        buttonWalk.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF82B1FF")));
                        TextView currentActivity = findViewById(R.id.currentActivityType);
                        currentActivity.setText("Current Walk");
                        findViewById(R.id.currentActivityCard).setVisibility(View.VISIBLE);

                        IntentFilter filter = new IntentFilter("com.runningtracker.START_WALK");
                        registerReceiver(trackerReceiver, filter);
                        Intent bIntent = new Intent("com.runningtracker.START_WALK");
                        bIntent.putExtra("type", "Walk");
                        sendBroadcast(bIntent);

                        intent = new Intent(MainActivity.this, TrackerService.class);
                        intent.putExtra("type", "Walk");
                        MainActivity.this.bindService(new Intent(MainActivity.this, TrackerService.class), serviceConnection, 0);
                        startService(intent);
                    } else {
                        myService.ActivityStop(intent.putExtra("avgSpeed", getAvgSpeed()));
                        buttonWalk.setText("Start Walk");
                        buttonWalk.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2979FF")));
                        findViewById(R.id.currentActivityCard).setVisibility(View.GONE);
                        walkActive = false;

                        unregisterReceiver(trackerReceiver);

                        stopThread();
                        stopService(intent);

                        getDB("_id", " ASC");
                    }
                }
            }
        });
        getDB("_id", " ASC");

        /*if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            findViewById(R.id.warningCard).setVisibility(View.GONE);
            return;
        }*/


    }

    /**
     * Thread that updates current activity card with speed, distance and a timer
     */
    Thread progThread;
    public void runThread() {
        LocalDateTime startDateTime = myService.getStartDateTime();
        final long startMillis = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        speedAvgs = new ArrayList<>();
        progThread = new Thread() {
            String progressText;
            TextView timer = findViewById(R.id.currentTimer);
            TextView totalDistance = findViewById(R.id.totalDistance);
            TextView currentSpeed = findViewById(R.id.currentSpeed);
            TrackerUtils trackerUtils = new TrackerUtils();
            long lastSpeed = -1;
            @Override
            public void run() {
                while (progThread != null) {
                    try {
                        sleep(250);
                        int[] timeArray = trackerUtils.differenceBetweenTimes(System.currentTimeMillis(), startMillis);
                        //String millisText = String.format("%02d", timeArray[0]);
                        String secondsText = String.format("%02d", timeArray[1]);
                        String minutesText = String.format("%02d", timeArray[2]);
                        String hoursText = String.format("%01d", timeArray[3]);
                        final String elapsedTime = hoursText + ":" + minutesText + ":" + secondsText;
                        //Log.d("g53mdp", myService.getElapsedDistance() / ((double)myService.getElapsedTime() / 1000) + "");
                        //Log.d("g53mdp", "KM:" + convertMtoKM(myService.getElapsedDistance()) + "HOURS:" + convertMStoHRS(myService.getElapsedTime()) + "");
                        final long speed = Math.round(trackerUtils.convertMtoKM(myService.getElapsedDistance()) / trackerUtils.convertMStoHRS(myService.getElapsedTime()));
                        if (lastSpeed != speed){
                            speedAvgs.add(speed);
                        }
                        lastSpeed = speed;
                        final double distance = trackerUtils.convertMtoKM(myService.getTotalDistance());
                        //myService.updateNotification(elapsedTime);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentSpeed.setText(speed + "");
                                totalDistance.setText(distance + "");
                                timer.setText(elapsedTime);
                            }
                        });
                        //Log.d("g53mdp", "" + currentProgress);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        progThread.start();
    }

    /**
     * Method to interrupt the thread
     */
    public void stopThread(){
        progThread.interrupt();
        progThread = null;
    }

    /**
     * Calculates average speed of the current activity and returns
     * the average
     * @return - the average speed of the activity
     */
    public long getAvgSpeed(){
        long speedAvg = 0;
        for (int i = 0; i < speedAvgs.size(); i++){
            speedAvg += speedAvgs.get(i);
            Log.d("g53mdp", speedAvgs.get(i) + " AVGS");
        }
        Log.d("g53mdp", (speedAvg / (speedAvgs.size() + 1)) + " AVERAGE");
        return (speedAvg / (speedAvgs.size() + 1));
    }

    /**
     * Check if permission has been granted, if so hide the warning card
     */
    @Override
    public void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            findViewById(R.id.warningCard).setVisibility(View.GONE);
            return;
        }
    }

    /**
     * Gets database selection using the content resolver and passes it to
     * a custom Cursor Adapter to set relative dates and icons
     * @param sortParam - sort parameter
     * @param orderParam - order by (ascending or descending)
     */
    protected void getDB(String sortParam, String orderParam){
        Cursor cursor = getContentResolver().query(DBProviderContract.ACTIVITIES_URI, null, null, null, sortParam + orderParam);
        TrackerAdapter dataAdapter = new TrackerAdapter(this, cursor);
        if (dataAdapter.getCount() != 0){
            findViewById(R.id.welcomeCard).setVisibility(View.GONE);
            findViewById(R.id.summaryCard).setVisibility(View.VISIBLE);
        }
        listView.setAdapter(dataAdapter);
    }

    /**
     * Refreshes database view on other activity return
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_ACTIVITYVIEW_REQUEST_CODE){
            getDB("_id", " ASC");
        }
    }

    /**
     * Manage service connection
     */
    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("g53mdp", "MainActivity onServiceConnected");
            if (myService != null){
                MainActivity.this.unbindService(serviceConnection);
            }
            myService = (TrackerService.MyBinder)service;
            runThread();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            getDB("_id", " ASC");
            MainActivity.this.unbindService(serviceConnection);
            myService = null;
        }
    };

}
