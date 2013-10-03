package com.tojaj.android.rouming;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.tojaj.android.rouming.R;
import com.tojaj.android.rouming.service.RoumingSyncService;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
        PicturesFragment.OnPictureClickedListener {

    static final String TAG = "ROUMING-OBRAZKY";

    private boolean two_pane_layout = false;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private class DrawerMenuItem {
        private String mName;
        private Class<?> mFragmentClass;
        private String mFragmentId;

        public DrawerMenuItem(String name, Class<?> fragmentClass,
                String fragmentId) {
            this.mName = name;
            this.mFragmentClass = fragmentClass;
            this.mFragmentId = fragmentId;
        }

        public String toString() {
            return mName;
        }

        public String getFragmentId() {
            return mFragmentId;
        }

        public Fragment getInstance() {
            Fragment instance = null;

            try {
                instance = (Fragment) mFragmentClass.getConstructor()
                        .newInstance();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return instance;
        }
    }

    private ArrayList<DrawerMenuItem> mDrawerMenuItems;

    private PictureFragment mPictureFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Check whether the activity is using two pane layout
        // TODO

        // Prepare drawer menu

        mDrawerMenuItems = new ArrayList<DrawerMenuItem>();
        mDrawerMenuItems.add(new DrawerMenuItem(
                getString(R.string.fragment_pictures), PicturesFragment.class,
                "pictures_fragment"));
        mDrawerMenuItems.add(new DrawerMenuItem(
                getString(R.string.fragment_preference),
                SettingsFragment.class, "fragment_preference"));

        // Prepare drawer

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<DrawerMenuItem>(this,
                R.layout.drawer_list_item, mDrawerMenuItems));

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, // host Activity
                mDrawerLayout, // DrawerLayout object
                R.drawable.ic_drawer, // nav drawer image to replace 'Up' caret
                R.string.drawer_open, // "open drawer" description for
                                      // accessibility
                R.string.drawer_close // "close drawer" description for
                                      // accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        selectItem(0);

        // Check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Log.e(TAG, "No Network available");
            Toast.makeText(this, "No connection available", Toast.LENGTH_SHORT)
                    .show();
        } else {
            start_sync_service();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fm.popBackStack();
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
        }
    }

    /*
     * Helper methods
     */

    void start_sync_service() {
        // Start Rouming service that should sync the local database (cache)
        Intent intent = new Intent(this, RoumingSyncService.class);
        intent.putExtra(RoumingSyncService.EXTRA_FORCE_SYNC, true);
        startService(intent);
    }

    /*
     * Drawer + action bar stuff
     */

    /** Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content
        // view
        // boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        // menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_refresh:
            start_sync_service();
            return true;
        case R.id.action_settings:
            selectItem(1);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on
        // position
        DrawerMenuItem drawerMenuItem = mDrawerMenuItems.get(position);
        String tag = drawerMenuItem.getFragmentId();

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);

        if (fragment == null) {
            fragment = drawerMenuItem.getInstance();
            if (fragment == null) {
                Log.e(TAG, "Cannot instantiate " + drawerMenuItem.toString());
                return;
            }
        }

        // Insert the fragment by replacing any existing fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // transaction.addToBackStack(null);
        transaction.commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(drawerMenuItem.toString());
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /*
     * Interface for PicturesFragment.OnPictureClickedListener
     */

    @Override
    public void OnPictureClicked(String local_uri, String origin_uri,
            String name) {
        if (local_uri != null) {
            // TODO: dualpane support
            Intent intent = new Intent(this, PictureActivity.class);
            intent.putExtra(PictureActivity.EXTRA_LOCAL_URI, local_uri);
            intent.putExtra(PictureActivity.EXTRA_ORIGIN_URI, origin_uri);
            intent.putExtra(PictureActivity.EXTRA_NAME, name);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_image_available,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
