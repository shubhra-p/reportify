package com.example.reportify.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SyncCompleteReceiver extends BroadcastReceiver {

    public static final String ACTION_SYNC_COMPLETED =
            "com.example.reportify.SYNC_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_SYNC_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context,
                    "Offline complaints synced successfully",
                    Toast.LENGTH_SHORT).show();
        }
    }
}