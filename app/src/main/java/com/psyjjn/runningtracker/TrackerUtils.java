package com.psyjjn.runningtracker;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Commonly used methods put into this class such as conversion methods
 */
public class TrackerUtils {
    public double convertMtoKM(double distance){
        return Math.round((distance / 1000) * 100.0) / 100.0;
    }
    public double convertMStoHRS(long timeInMs){
        return ((double)timeInMs / (1000 * 60 * 60));
    }
    public int[] differenceBetweenTimes(long latestMillis, long startMillis){
        long elapsedMillis = latestMillis - startMillis;
        int elapsedMs = (int)(elapsedMillis % 60);
        int elapsedSeconds = (int)(elapsedMillis / 1000) % 60;
        int elapsedMinutes = (int)((elapsedMillis / (1000 * 60)) % 60);
        int elapsedHours = (int)((elapsedMillis / (1000 * 60 * 60)) % 24);
        return new int[] {
                elapsedMs,
                elapsedSeconds,
                elapsedMinutes,
                elapsedHours
        };
    }
    public int[] msToUnits(long millis){
        long elapsedMillis = millis;
        int elapsedMs = (int)(elapsedMillis % 60);
        int elapsedSeconds = (int)(elapsedMillis / 1000) % 60;
        int elapsedMinutes = (int)((elapsedMillis / (1000 * 60)) % 60);
        int elapsedHours = (int)((elapsedMillis / (1000 * 60 * 60)) % 24);
        return new int[] {
                elapsedMs,
                elapsedSeconds,
                elapsedMinutes,
                elapsedHours
        };
    }
    public Date convert8601toDate(String rawActivityDate){
        SimpleDateFormat rawDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date rawDate = null;
        try {
            rawDate = rawDateFormat.parse(rawActivityDate);
        }catch(Exception e){}
        return rawDate;
    }
}
