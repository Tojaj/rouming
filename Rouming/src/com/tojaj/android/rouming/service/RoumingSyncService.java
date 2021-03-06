package com.tojaj.android.rouming.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tojaj.android.rouming.Rouming;
import com.tojaj.android.rouming.Rouming.RoumingJoke;
import com.tojaj.android.rouming.Rouming.RoumingPictureHref;
import com.tojaj.android.rouming.provider.RoumingContract;
import com.tojaj.android.rouming.provider.RoumingContract.Jokes;
import com.tojaj.android.rouming.provider.RoumingContract.Metadata;
import com.tojaj.android.rouming.provider.RoumingContract.Pictures;

public class RoumingSyncService extends IntentService {

    static final String TAG = "RoumingSyncService";
    static final long MIN_MINUTES_BETWEEN_UPDATES = 5;
    private SharedPreferences mSharedPrefs;
    private static final SimpleDateFormat TIMEDATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm", Locale.US);

    // Intent extra parameters
    public static final String EXTRA_FORCE_SYNC = "force_sync";

    public RoumingSyncService() {
        super("RoumingSyncService");
    }

    private synchronized static String millisToString(long milis) {
        // We use a single instance variable with SimpleDateFormat
        // which is definitely not thread-safe, thus this method
        // use the "synchronized" statement
        return TIMEDATE_FORMAT.format(new Date(milis));
    }

    private boolean databaseDataExpired() {
        ContentResolver cr = getContentResolver();
        // cr.delete(RoumingContract.BASE_CONTENT_URI, null, null);

        Cursor c = cr.query(RoumingContract.Metadata.CONTENT_URI,
                new String[] { RoumingContract.Metadata.LAST_UPDATE, }, null,
                null, null);

        long current_time = System.currentTimeMillis();
        long last_update = 0;

        if (c.moveToFirst()) {
            last_update = c.getLong(c
                    .getColumnIndex(RoumingContract.Metadata.LAST_UPDATE));
        }

        if (last_update != 0) {
            Log.d(TAG, "Time of the last db update "
                    + millisToString(last_update));
        } else {
            Log.w(TAG, "First sync of the db");
        }

        if (current_time
                - TimeUnit.MINUTES.toMillis(MIN_MINUTES_BETWEEN_UPDATES) < last_update) {
            // Database is up to date
            return false;
        }

        // Update should be done
        return true;
    }

    private boolean networkConnectionAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;

        String update_preference = mSharedPrefs.getString(
                "background_update_preference", "UPDATE_WIFI_ONLY");
        if (update_preference.equals("UPDATE_WIFI_ONLY")) {
            Log.d(TAG, "Using wifi only");
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } else {
            Log.d(TAG, "Using any network");
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        }

        return true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d(TAG, "Rouming update service starting...");

        // Make a schedule for a next update
        scheduleNextUpdate(this);

        boolean force = intent.getBooleanExtra(EXTRA_FORCE_SYNC, false);
        if (!force && !mSharedPrefs.getBoolean("background_update", true)) {
            // Service is not started with force "flag" and
            // moreover automatic background updates are disabled
            Log.d(TAG, "Update canceled");
            return;
        }

        if (!databaseDataExpired()) {
            Log.d(TAG, "Too soon to another update");
            return;
        }

        if (!networkConnectionAvailable()) {
            Log.w(TAG, "No Network available");
            // Make a schedule for a next update (sooner)
            scheduleNextUpdate(this, true);
            return;
        }

        // Do update
        new UpdateTask(this).execute(this);
    }

    /*
     * Schedule of the next update
     */

    public static void scheduleNextUpdate(Context context) {
        scheduleNextUpdate(context, false);
    }

    public static void scheduleNextUpdate(Context context,
            boolean last_update_fails) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, RoumingSyncService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Cancel all already planed updates
        Log.d(TAG, "Canceling all planed starts of the RoumingSyncService");
        alarmManager.cancel(pendingIntent);

        if (!prefs.getBoolean("background_update", true)) {
            // Automatic background updates are disabled
            Log.d(TAG, "Background updates are disabled - nothing to do");
            return;
        }

        long interval;

        if (last_update_fails) {
            Log.d(TAG, "Update failed, the next one will be scheduled soon");
            interval = AlarmManager.INTERVAL_HALF_HOUR;
        } else {
            String strinterval = prefs.getString(
                    "background_update_frequency_preference",
                    "INTERVAL_HALF_DAY");
            interval = AlarmManager.INTERVAL_HALF_DAY;

            if (strinterval.equals("INTERVAL_FIFTEEN_MINUTES"))
                interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            else if (strinterval.equals("INTERVAL_HALF_HOUR"))
                interval = AlarmManager.INTERVAL_HALF_HOUR;
            else if (strinterval.equals("INTERVAL_HOUR"))
                interval = AlarmManager.INTERVAL_HOUR;
            else if (strinterval.equals("INTERVAL_HALF_DAY"))
                interval = AlarmManager.INTERVAL_HALF_DAY;
            else if (strinterval.equals("INTERVAL_DAY"))
                interval = AlarmManager.INTERVAL_DAY;
        }

        long nextUpdateTimeMillis = System.currentTimeMillis() + interval;
        Log.d(TAG, "Next update will take place at "
                + millisToString(nextUpdateTimeMillis) + " Current is: "
                + millisToString(System.currentTimeMillis()));
        alarmManager.setInexactRepeating(AlarmManager.RTC,
                nextUpdateTimeMillis, interval, pendingIntent);
    }

    /*
     * Async task
     */

    public class UpdateTask extends
            AsyncTask<Context, Void, Void> {

        Context mContext;
        public boolean mOnline;
        long mUpdateStartTime;

        private ArrayList<RoumingPictureHref> mRoumingPictureHrefs;
        private ArrayList<RoumingJoke> mRoumingJokes;

        public UpdateTask(Context ctx) {
            // Now set context
            mContext = ctx;
        }

        @Override
        protected void onPreExecute() {
            mUpdateStartTime = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(
                Context... contexts) {
            mRoumingPictureHrefs = Rouming.getRoumingPictureHrefs(contexts[0]);
            mRoumingJokes = new ArrayList<RoumingJoke>();

            for (int x = 1; x < 4; x++) {
                ArrayList<RoumingJoke> jokes;
                jokes = Rouming.getRoumingJokes(contexts[0], x);
                if (jokes != null) {
                    mRoumingJokes.addAll(jokes);
                } else {
                    Log.e(TAG, "Cannot get rouming jokes for page " + String.valueOf(x));
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void none) {
            boolean roumingPicturesHrefStatus = true;
            boolean roumingJokesStatus = true;

            ArrayList<ContentValues> values;
            ContentResolver resolver = getContentResolver();

            // Rouming pictures

            values.clear();
            for (RoumingPictureHref href : mRoumingPictureHrefs) {
                Log.d(TAG, "Url: " + href.pic_url);
                ContentValues newValues = new ContentValues();

                newValues.put(Pictures.TIME, href.unixtime);
                newValues.put(Pictures.NAME, href.name);
                newValues.put(Pictures.DETAIL_URL, href.url.toString());
                newValues.put(Pictures.PICTURE_URL, href.pic_url.toString());
                newValues.put(Pictures.SIZE, href.size);
                newValues.put(Pictures.LIKES, href.likes);
                newValues.put(Pictures.DISLIKES, href.dislikes);
                newValues.put(Pictures.COMMENTS, href.comments);

                values.add(newValues);
            }

            if (values.size() > 0) {
                // Bulk insert
                int num = resolver.bulkInsert(
                        RoumingContract.Pictures.CONTENT_URI,
                        values.toArray(new ContentValues[values.size()]));
                Log.d(TAG, "Number of inserted items: " + Integer.toString(num));
            } else {
                roumingJokesStatus = false;
            }

            // Rouming jokes

            values = new ArrayList<ContentValues>();
            for (RoumingJoke joke : mRoumingJokes) {
                Log.d(TAG, "Joke: " + joke.name);
                ContentValues newValues = new ContentValues();

                newValues.put(Jokes.TIME, joke.unixtime);
                newValues.put(Jokes.NAME, joke.name);
                newValues.put(Jokes.TEXT, joke.text);
                newValues.put(Jokes.CATEGORY, joke.category;
                newValues.put(Jokes.GRADE, joke.grade);

                values.add(newValues);
            }

            if (values.size() > 0) {
                // Bulk insert
                int num = resolver.bulkInsert(
                        RoumingContract.Jokes.CONTENT_URI,
                        values.toArray(new ContentValues[values.size()]));
                Log.d(TAG, "Number of inserted jokes: " + Integer.toString(num));
            } else {
                roumingPicturesHrefStatus = false;
            }

            // Check status

            if (roumingPicturesHrefStatus || roumingJokesStatus) {
                // If at least update of one table was success
                // Update time of last update
                ContentValues mNewValues = new ContentValues();
                mNewValues.put(Metadata.LAST_UPDATE, mUpdateStartTime);
                Uri mNewUri = resolver.insert(Metadata.CONTENT_URI, mNewValues);
                Log.d(TAG, "New update successful uri: " + mNewUri.toString());
            } else {
                // Update failed, plan the next one sooner
                scheduleNextUpdate(mContext, true);
            }
        }
    }
}
