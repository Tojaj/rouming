package com.tojaj.android.rouming;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class PictureActivity extends Activity {

	public static final String EXTRA_LOCAL_URI = "local_uri";
	public static final String EXTRA_ORIGIN_URI = "origin_uri";
	public static final String EXTRA_NAME = "name";

	private static final String FRAGMENT_TAG = "picture_f";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = getIntent();
		String local_uri = intent.getStringExtra(EXTRA_LOCAL_URI);
		String origin_uri = intent.getStringExtra(EXTRA_ORIGIN_URI);
		String name = intent.getStringExtra(EXTRA_NAME);
		
	    FragmentManager fragmentManager = getFragmentManager();
	    Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
	    // If the Fragment is non-null, then it is currently being
	    // retained across a configuration change.
	    if (fragment == null) {
	    	fragment = PictureFragment.newInstance(local_uri, origin_uri, name);
	    }
	    
	    FragmentTransaction transaction = fragmentManager.beginTransaction();
	    transaction.replace(R.id.activity_picture_fragment_container, fragment, FRAGMENT_TAG);
	    //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	    transaction.commit();
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.picture, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = NavUtils.getParentActivityIntent(this);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			NavUtils.navigateUpTo(this, intent);
			//NavUtils.navigateUpFromSameTask(this);
			//onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
