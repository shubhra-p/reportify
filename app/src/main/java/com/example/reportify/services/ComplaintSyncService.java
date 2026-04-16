package com.example.reportify.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.example.reportify.R;
import com.example.reportify.database.ComplaintDbHelper;
import com.example.reportify.models.Complaint;

import java.util.List;

public class ComplaintSyncService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        syncComplaints();
        return START_NOT_STICKY;
    }

    private void syncComplaints() {

        ComplaintDbHelper dbHelper = new ComplaintDbHelper(this);
        List<Complaint> unsynced = dbHelper.getUnsyncedComplaints();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        for (Complaint complaint : unsynced) {

            firestore.collection("complaints")
                    .document(complaint.getComplaintId())
                    .set(complaint)
                    .addOnSuccessListener(unused ->
                            dbHelper.markAsSynced(complaint.getComplaintId())
                    );
        }
        Intent broadcastIntent = new Intent("com.example.reportify.SYNC_COMPLETED");

        sendBroadcast(broadcastIntent);

        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}