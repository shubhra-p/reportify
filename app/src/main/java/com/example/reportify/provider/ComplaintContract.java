package com.example.reportify.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ComplaintContract {

    private ComplaintContract() {}

    public static final String AUTHORITY =
            "com.example.reportify.provider";

    public static final Uri BASE_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);

    public static final String PATH_COMPLAINTS = "complaints";

    public static final class ComplaintEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon()
                        .appendPath(PATH_COMPLAINTS)
                        .build();

        public static final String TABLE_NAME = "complaints";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_USER_ID = "userId";
        public static final String COLUMN_PROVIDER_ID = "providerId";
        public static final String COLUMN_SERVICE_TYPE = "serviceType";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_URGENCY = "urgency";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_SYNCED = "synced";
    }
}