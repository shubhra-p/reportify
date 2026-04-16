package com.example.reportify.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.reportify.models.Complaint;

import java.util.ArrayList;
import java.util.List;

public class ComplaintDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "complaints_db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "complaints";

    public ComplaintDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + "id TEXT PRIMARY KEY,"
                + "userId TEXT,"
                + "providerId TEXT,"
                + "serviceType TEXT,"
                + "title TEXT,"
                + "description TEXT,"
                + "status TEXT,"
                + "urgency TEXT,"
                + "timestamp LONG,"
                + "synced INTEGER DEFAULT 0"
                + ")";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert complaint locally
    public void insertComplaint(Complaint complaint) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id", complaint.getComplaintId());
        values.put("userId", complaint.getUserId());
        values.put("providerId", complaint.getProviderId());
        values.put("serviceType", complaint.getServiceType());
        values.put("title", complaint.getTitle());
        values.put("description", complaint.getDescription());
        values.put("status", complaint.getStatus());
        values.put("urgency", complaint.getUrgency());
        values.put("timestamp", complaint.getTimestamp());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Get unsynced complaints
    public List<Complaint> getUnsyncedComplaints() {

        List<Complaint> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE synced = 0",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                Complaint complaint = new Complaint(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getLong(8)
                );

                list.add(complaint);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    // Mark complaint as synced
    public void markAsSynced(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("synced", 1);

        db.update(TABLE_NAME, values, "id=?", new String[]{id});
        db.close();
    }
}