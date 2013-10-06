package com.tojaj.android.rouming.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tojaj.android.rouming.provider.RoumingContract.Jokes;
import com.tojaj.android.rouming.provider.RoumingContract.Metadata;
import com.tojaj.android.rouming.provider.RoumingContract.Pictures;
import com.tojaj.android.rouming.provider.RoumingDatabase.Tables;

public class RoumingProvider extends ContentProvider {
    private static final String TAG = "RoumingProvider";

    private RoumingDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int METADATA = 100;
    private static final int PICTURES = 200;
    private static final int PICTURES_ID = 201;
    private static final int JOKES = 300;
    private static final int JOKES_ID = 301;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RoumingContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "metadata", METADATA);
        matcher.addURI(authority, "pictures", PICTURES);
        matcher.addURI(authority, "pictures/*", PICTURES_ID);
        matcher.addURI(authority, "jokes", JOKES);
        matcher.addURI(authority, "jokes/*", JOKES_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new RoumingDatabase(getContext());
        return true;
    }

    private void deleteDatabase() {
        mOpenHelper.close();
        Context context = getContext();
        RoumingDatabase.deleteDatabase(context);
        mOpenHelper = new RoumingDatabase(getContext());
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
        case METADATA:
            return Metadata.CONTENT_TYPE;
        case PICTURES:
            return Pictures.CONTENT_TYPE;
        case PICTURES_ID:
            return Pictures.CONTENT_ITEM_TYPE;
        case JOKES:
            return Jokes.CONTENT_TYPE;
        case JOKES_ID:
            return Jokes.CONTENT_ITEM_TYPE;

        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        Log.d(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection)
                + ")");

        String defaultSortOrder = null;
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // TODO: Check projection if contains only existing columns

        final int match = sUriMatcher.match(uri);
        switch (match) {
        case METADATA:
            defaultSortOrder = Metadata.DEFAULT_SORT;
            builder.setTables(Tables.METADATA);
            break;
        case PICTURES_ID:
            builder.appendWhere(RoumingContract.Pictures._ID + "="
                    + uri.getLastPathSegment());
        case PICTURES:
            defaultSortOrder = Pictures.DEFAULT_SORT;
            builder.setTables(Tables.PICTURES);
            break;
        case JOKES_ID:
            builder.appendWhere(RoumingContract.Jokes._ID + "="
                    + uri.getLastPathSegment());
        case JOKES:
            defaultSortOrder = Jokes.DEFAULT_SORT;
            builder.setTables(Tables.JOKES);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = defaultSortOrder;
        }

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Log.d(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");

        long id;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
        case METADATA:
            id = db.insert(Tables.METADATA, null, values);
            break;
        case PICTURES:
            id = db.insert(Tables.PICTURES, null, values);
            break;
        case JOKES:
            id = db.insert(Tables.JOKES, null, values);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        if (id < 0) {
            // something went wrong
            throw new IllegalArgumentException("Insertion failed - used URI : "
                    + uri);
        }

        // notify all listeners of changes and return itemUri:
        Uri itemUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(itemUri, null);
        return itemUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        Log.d(TAG, "bulk insert to uri: " + uri.toString());

        int numInserted = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
        case PICTURES:
            db.beginTransaction();
            try {
                // standard SQL insert statement, that can be reused
                String statement_str = "insert into " + Tables.PICTURES + "("
                        + RoumingContract.Pictures.TIME + ","
                        + RoumingContract.Pictures.NAME + ","
                        + RoumingContract.Pictures.DETAIL_URL + ","
                        + RoumingContract.Pictures.PICTURE_URL + ","
                        + RoumingContract.Pictures.SIZE + ","
                        + RoumingContract.Pictures.LIKES + ","
                        + RoumingContract.Pictures.DISLIKES + ","
                        + RoumingContract.Pictures.COMMENTS + ") values "
                        + "(?,?,?,?,?,?,?,?);";
                Log.d(TAG, "Compiling statement: " + statement_str);
                SQLiteStatement insert = db.compileStatement(statement_str);

                for (ContentValues value : values) {
                    insert.bindLong(1,
                            value.getAsLong(RoumingContract.Pictures.TIME));
                    insert.bindString(2,
                            value.getAsString(RoumingContract.Pictures.NAME));
                    insert.bindString(3, value
                            .getAsString(RoumingContract.Pictures.DETAIL_URL));
                    insert.bindString(4, value
                            .getAsString(RoumingContract.Pictures.PICTURE_URL));
                    insert.bindLong(5,
                            value.getAsLong(RoumingContract.Pictures.SIZE));
                    insert.bindLong(6,
                            value.getAsLong(RoumingContract.Pictures.LIKES));
                    insert.bindLong(7,
                            value.getAsLong(RoumingContract.Pictures.DISLIKES));
                    insert.bindLong(8,
                            value.getAsLong(RoumingContract.Pictures.COMMENTS));
                    long id = insert.executeInsert();
                    Log.d(TAG, "Inserted id " + Long.toString(id));
                }
                db.setTransactionSuccessful();
                numInserted = values.length;
            } finally {
                db.endTransaction();
            }
            break;

        case JOKES:
            db.beginTransaction();
            try {
                // standard SQL insert statement, that can be reused
                String statement_str = "insert into " + Tables.JOKES + "("
                        + RoumingContract.Jokes.TIME + ","
                        + RoumingContract.Jokes.NAME + ","
                        + RoumingContract.Jokes.TEXT + ","
                        + RoumingContract.Jokes.CATEGORY + ","
                        + RoumingContract.Jokes.GRADE + ") values "
                        + "(?,?,?,?,?);";
                Log.d(TAG, "Compiling statement: " + statement_str);
                SQLiteStatement insert = db.compileStatement(statement_str);

                for (ContentValues value : values) {
                    insert.bindLong(1,
                            value.getAsLong(RoumingContract.Jokes.TIME));
                    insert.bindString(2,
                            value.getAsString(RoumingContract.Jokes.NAME));
                    insert.bindString(3, value
                            .getAsString(RoumingContract.Jokes.TEXT));
                    insert.bindString(4, value
                            .getAsString(RoumingContract.Jokes.CATEGORY));
                    insert.bindLong(5,
                            value.getAsLong(RoumingContract.Jokes.GRADE));
                    long id = insert.executeInsert();
                    Log.d(TAG, "Inserted id " + Long.toString(id));
                }
                db.setTransactionSuccessful();
                numInserted = values.length;
            } finally {
                db.endTransaction();
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Unsupported URI for bulk insertion: " + uri);
        }

        // notify all listeners of changes
        getContext().getContentResolver().notifyChange(uri, null);
        return numInserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        Log.d(TAG, "delete(uri=" + uri + ")");

        int delCount = 0;

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        if (uri == RoumingContract.BASE_CONTENT_URI) {
            // Handle whole database deletes
            Log.d(TAG, "Deleting a whole database");
            deleteDatabase();
            getContext().getContentResolver().notifyChange(uri, null);
            return 1;
        }

        String idStr;
        String where;

        switch (sUriMatcher.match(uri)) {
        case METADATA:
            delCount = db.delete(Tables.METADATA, selection, selectionArgs);
            break;
        case PICTURES:
            delCount = db.delete(Tables.PICTURES, selection, selectionArgs);
            break;
        case PICTURES_ID:
            idStr = uri.getLastPathSegment();
            where = Pictures._ID + "=" + idStr;
            if (!TextUtils.isEmpty(selection)) {
                where += " AND " + selection;
            }
            delCount = db.delete(Tables.PICTURES, where, selectionArgs);
            break;
        case JOKES:
            delCount = db.delete(Tables.JOKES, selection, selectionArgs);
            break;
        case JOKES_ID:
            idStr = uri.getLastPathSegment();
            where = Jokes._ID + "=" + idStr;
            if (!TextUtils.isEmpty(selection)) {
                where += " AND " + selection;
            }
            delCount = db.delete(Tables.JOKES, where, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // notify all listeners of changes:
        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }
}