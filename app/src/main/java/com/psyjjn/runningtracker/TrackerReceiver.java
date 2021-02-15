package com.psyjjn.runningtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * This class extends broadcast receiver and displays notifications when the user starts
 * a run or walk
 */
public class TrackerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("g53mdpr", intent.getAction() + " RECEIVER");
        if (intent.getAction().equals("com.runningtracker.START_RUN")){
            Toast.makeText(context, "Starting Run", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals("com.runningtracker.START_WALK")){
            Toast.makeText(context, "Starting Walk", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals("com.runningtracker.UPDATE_DB")){
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        }

    }
}
