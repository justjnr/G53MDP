package com.psyjjn.runningtracker;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackerLocationListener implements LocationListener {
    double elevationTotal;
    double distanceTotal, elapsedDistance;
    Location currentLocation, newLocation, origin, destination;
    LocalDateTime startDateTime, endDateTime;
    boolean initFlag, runFlag;
    long currentTime, newTime, elapsedTime;

    /**
     * Initialised values to be used
     */
    public void init(){
        startDateTime = getDateTime();
        elevationTotal = 0;
        distanceTotal = 0;
        elapsedDistance = 0;
        currentLocation = new Location("");
        currentLocation.setLatitude(0);
        currentLocation.setLongitude(0);
        newLocation = new Location("");
        newLocation.setLatitude(0);
        newLocation.setLongitude(0);
        origin = new Location("");
        origin.setLatitude(0);
        origin.setLongitude(0);
        destination = new Location("");
        destination.setLatitude(0);
        destination.setLongitude(0);
        currentTime = System.currentTimeMillis();
        newTime = System.currentTimeMillis();
        elapsedTime = 0;
        initFlag = true;
        runFlag = true;
    }

    /**
     * Stop updating values reset values
     */
    public void stop(){
        runFlag = false;
        distanceTotal = 0;
        elevationTotal = 0;
    }

    /**
     * Returns data such as total distance and elevation
     * @return - object array containing all values
     */
    public Object[] getData(){
        endDateTime = getDateTime();
        destination = newLocation;

        long timeInMillis = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timeInMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        Log.d("g53mdp", destination + "");

        return new Object[] {
                distanceTotal,
                elevationTotal,
                startDateTime,
                endDateTime,
                origin,
                destination
        };
    }

    /**
     * Gets current date and time
     * @return - LocalDateTime type
     */
    public LocalDateTime getDateTime(){
        return LocalDateTime.now();
    }

    /**
     * Updates when GPS changes location, calculate current totals and set new locations
     * @param location
     */
    @Override
    public void onLocationChanged(Location location){
        if (runFlag){
            if (initFlag){
                newTime = System.currentTimeMillis();
                newLocation.setLatitude(location.getLatitude());
                newLocation.setLongitude(location.getLongitude());
                newLocation.setAltitude(location.getAltitude());
                origin = newLocation;
            }

            currentTime = newTime;
            currentLocation.setLatitude(newLocation.getLatitude());
            currentLocation.setLongitude(newLocation.getLongitude());
            currentLocation.setAltitude(newLocation.getAltitude());

            newTime = System.currentTimeMillis();
            newLocation.setLatitude(location.getLatitude());
            newLocation.setLongitude(location.getLongitude());
            newLocation.setAltitude(location.getAltitude());

            distanceTotal += currentLocation.distanceTo(newLocation);
            elevationTotal += newLocation.getAltitude() - currentLocation.getAltitude();
            elapsedDistance = currentLocation.distanceTo(newLocation);
            elapsedTime = newTime - currentTime;
            initFlag = false;
            Log.d("g53mdp", distanceTotal + "");
            //Log.d("g53mdp", newTime - currentTime + "");
            //Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        // information about the signal, i.e. number of satellites
        Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
    }
    @Override
    public void onProviderEnabled(String provider){
        Log.d("g53mdp", "onProviderEnabled: " + provider);
    }
    @Override
    public void onProviderDisabled(String provider){
        Log.d("g53mdp", "onProviderDisabled: " + provider);
    }
}
