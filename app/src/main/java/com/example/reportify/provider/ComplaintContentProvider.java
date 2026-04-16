package com.example.reportify.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.reportify.database.ComplaintDbHelper;

public class ComplaintContentProvider extends ContentProvider {

    private ComplaintDbHelper dbHelper;

    private static final int COMPLAINTS = 100;
    private static final int COMPLAINT_ID = 101;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(
                ComplaintContract.AUTHORITY,
                ComplaintContract.PATH_COMPLAINTS,
                COMPLAINTS
        );

        uriMatcher.addURI(
                ComplaintContract.AUTHORITY,
                ComplaintContract.PATH_COMPLAINTS + "/#",
                COMPLAINT_ID
        );
    }

    @Override
    public boolean onCreate() {
        dbHelper = new ComplaintDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor;

        switch (uriMatcher.match(uri)) {

            case COMPLAINTS:
                cursor = db.query(
                        ComplaintContract.ComplaintEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case COMPLAINT_ID:
                String id = uri.getLastPathSegment();
                cursor = db.query(
                        ComplaintContract.ComplaintEntry.TABLE_NAME,
                        projection,
                        "id=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        cursor.setNotificationUri(
                getContext().getContentResolver(),
                uri
        );

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id = db.insert(
                ComplaintContract.ComplaintEntry.TABLE_NAME,
                null,
                values
        );

        if (id == -1) {
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsUpdated = db.update(
                ComplaintContract.ComplaintEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsDeleted = db.delete(
                ComplaintContract.ComplaintEntry.TABLE_NAME,
                selection,
                selectionArgs
        );

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}