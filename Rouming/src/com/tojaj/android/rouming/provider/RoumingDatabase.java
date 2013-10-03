package com.tojaj.android.rouming.provider;

import com.tojaj.android.rouming.provider.RoumingContract.MetadataColumns;
import com.tojaj.android.rouming.provider.RoumingContract.PicturesColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class RoumingDatabase extends SQLiteOpenHelper {
    private static final String TAG = "RoumingDatabase";

    private static final String DATABASE_NAME = "rouming.db";

    private static final int DATABASE_VERSION = 1;

    private static final String PICTURES_ROWS_TO_KEEP = "1000";

    // private final Context mContext;

    interface Tables {
        String METADATA = RoumingContract.PATH_METADATA;
        String PICTURES = RoumingContract.PATH_PICTURES;
    }

    RoumingDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating tables");

        String sql;

        // Tables

        sql = "CREATE TABLE " + Tables.METADATA + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MetadataColumns.LAST_UPDATE + " INTEGER)";
        db.execSQL(sql);
        Log.d(TAG, "SQL Executed: " + sql);

        sql = "CREATE TABLE " + Tables.PICTURES + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + PicturesColumns.TIME
                + " INTEGER," + PicturesColumns.NAME
                + " TEXT UNIQUE ON CONFLICT REPLACE,"
                + PicturesColumns.DETAIL_URL + " TEXT,"
                + PicturesColumns.PICTURE_URL + " TEXT," + PicturesColumns.SIZE
                + " INTEGER," + PicturesColumns.LIKES + " INTEGER,"
                + PicturesColumns.DISLIKES + " INTEGER,"
                + PicturesColumns.COMMENTS + " INTEGER)";
        db.execSQL(sql);
        Log.d(TAG, "SQL Executed: " + sql);

        // Triggers

        sql = "CREATE TRIGGER trigger_metadata_keep_only_one_line "
                + "AFTER INSERT ON metadata BEGIN "
                + "DELETE FROM metadata WHERE "
                + "last_update < (SELECT MAX(last_update) FROM metadata);"
                + "END;";
        db.execSQL(sql);
        Log.d(TAG, "SQL Executed: " + sql);

        sql = "CREATE TRIGGER trigger_picutres_keep_limited_number_of_records "
                + "AFTER INSERT ON pictures BEGIN "
                + "DELETE FROM pictures WHERE "
                + "_id < ((SELECT MAX(_id) FROM pictures) - "
                + PICTURES_ROWS_TO_KEEP + ");" + "END;";
        db.execSQL(sql);
        Log.d(TAG, "SQL Executed: " + sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database " + DATABASE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.METADATA);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.PICTURES);
        onCreate(db);
    }

    public static void deleteDatabase(Context context) {
        Log.d(TAG, "Deleting database " + DATABASE_NAME);
        context.deleteDatabase(DATABASE_NAME);
    }
}
