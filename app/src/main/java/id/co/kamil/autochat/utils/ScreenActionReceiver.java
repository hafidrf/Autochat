package id.co.kamil.autochat.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class ScreenActionReceiver extends BroadcastReceiver {

    private String TAG = "ScreenActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {


        String action = intent.getAction();


        Intent i = new Intent(action);
        // Data you need to pass to activity

        if(Intent.ACTION_SCREEN_ON.equals(action))
        {
            //Log.d(TAG, "screen is on...");
            i.putExtra("message", "screen is on...");

        }

        else if(Intent.ACTION_SCREEN_OFF.equals(action))
        {
            //Log.d(TAG, "screen is off...");
            i.putExtra("message", "screen is off...");
        }

        else if(Intent.ACTION_USER_PRESENT.equals(action))
        {
            //Log.d(TAG, "screen is unlock...");
            i.putExtra("message", "screen is unlock...");
        }
        //context.sendBroadcast(i);

    }

    public IntentFilter getFilter(){
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        return filter;
    }
}
