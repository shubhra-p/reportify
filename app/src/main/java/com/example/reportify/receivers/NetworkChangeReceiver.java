package com.example.reportify.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.reportify.services.ComplaintSyncService;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (isConnected(context)) {

            Intent serviceIntent =
                    new Intent(context, ComplaintSyncService.class);

            context.startForegroundService(serviceIntent);
        }
    }

    private boolean isConnected(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }

        return false;
    }
}