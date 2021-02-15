package com.psyjjn.runningtracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.round;

public class TrackerService extends Service {
    private final IBinder binder = new MyBinder();
    MyBinder myBinder = new MyBinder();
    LocationManager locationManager;
    TrackerLocationListener locationListener;
    String currentType;
    int avgSpeed;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private int NOTIFICATION_ID = 001;

    /**
     * Override the onCreate method when service is created
     */
    @Override
    public void onCreate() {
        Log.d("g53mdp", "Service onCreate");
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new TrackerLocationListener();
        locationListener.init();
        try { locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5, // minimum time interval between updates
                5, // minimum distance between updates, in metres
                locationListener);
        } catch(SecurityException e) {
            Log.d("g53mdp", e.toString());
        }
        super.onCreate();
    }

    /**
     * Override the onStartCommand method when the service is started
     * & shows notification, also returns the correct flag so that
     * the service is not immediately restarted when destroyed
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("g53mdp", "Service OnStart");
        currentType = (String)intent.getExtras().get("type");
        createNotification();
        return Service.START_NOT_STICKY;
    }

    /**
     * Destroy service and stop music player, remove notification
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("g53mdp", "Service onDestroy");
        notificationManager.cancel(NOTIFICATION_ID);
        locationListener.stop();
        locationManager.removeUpdates(locationListener);

    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.d("g53mdp", "Service onBound");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("g53mdp", "Service onUnbind");
        //unBindService(mConnection);
        return super.onUnbind(intent);
    }

    /**
     * Stop service when the app is purposefully closed by user
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    public class MyBinder extends Binder
    {
        public void ActivityStop(Intent intent){
            avgSpeed = ((Long)intent.getExtras().get("avgSpeed")).intValue();
            Object result[] = locationListener.getData();
            Location destination = (Location)result[5];
            String destinationString = "";
            Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geoCoder.getFromLocation(destination.getLatitude(), destination.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Log.d("g53mdp", addresses.get(0) + "");
                    destinationString = addresses.get(0).getThoroughfare();
                }
            } catch (Exception e) {
                Log.d("g53mdp", e + "");
            }

            ContentValues dbVals = new ContentValues();
            dbVals.put(DBProviderContract.TYPE, currentType);
            dbVals.put(DBProviderContract.DISTANCE, Math.round(((double)result[0] / 1000) * 100.0) / 100.0);
            dbVals.put(DBProviderContract.ELEVATION, (double)result[1]);
            dbVals.put(DBProviderContract.STARTDATETIME, result[2].toString());
            dbVals.put(DBProviderContract.ENDDATETIME, result[3].toString());
            dbVals.put(DBProviderContract.DESTINATIONTEXT, destinationString);
            dbVals.put(DBProviderContract.RATING, 0);
            dbVals.put(DBProviderContract.AVGSPEED, avgSpeed);
            Uri lastRecipeUri = getContentResolver().insert(DBProviderContract.ACTIVITIES_URI, dbVals);
            Location origin = (Location)result[4];
            Log.d("g53mdp", result[0] + " -DISTANCE TOTAL " + result[1] + " -ELEVATION TOTAL " + result[2] + " -STARTTIME " + result[3] + " -ENDTIME " + origin.getLatitude() + " -ORIGIN ");
        }
        public LocalDateTime getStartDateTime(){
            return locationListener.startDateTime;
        }
        public double getTotalDistance(){
            return locationListener.distanceTotal;
        }
        public long getElapsedTime(){
            return locationListener.elapsedTime;
        }
        public double getElapsedDistance(){
            return locationListener.elapsedDistance;
        }
        public void updateNotification(String text){
            //mBuilder.setContentTitle(title);
            mBuilder.setContentText(text);
            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    /**
     * Handles the initial notification creation
     */
    public void createNotification(){
        final String CHANNEL_ID = "100";
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

        Intent notifIntent = new Intent(this, MainActivity.class);
        notifIntent.addCategory("android.intent.category.LAUNCHER");
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Tracking " + currentType)
                .setContentText("Tracking your activity")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        startForeground(NOTIFICATION_ID, mBuilder.build());
    }
}
