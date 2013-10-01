package com.tojaj.android.rouming;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tojaj.android.rouming.service.RoumingSyncService;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements
		Preference.OnPreferenceClickListener,
		Preference.OnPreferenceChangeListener,
		SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "SettingsFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		Preference pref;
		pref = (Preference) findPreference("clear_cache_button");
		pref.setOnPreferenceClickListener(this);
	}

	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	/*
	 * Interfaces implementations
	 */
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// Used to provide a button like behavior

		if (preference.getKey().equals("clear_cache_button")) {
			// Clear cache
			Log.d(TAG, "Clear cache buttton pressed");
			ImageLoader.getInstance().clearDiscCache();
			Toast.makeText(getActivity(), R.string.cache_cleared,
					Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// Could be use to check option before stored to the shared preference
		return true;
	}
	
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("background_update")
				|| key.equals("background_update_frequency_preference")) {
			// Reschedule update service start
			Log.d(TAG, "Rescheduling service start");
			// Start Rouming service only for reschedule
			RoumingSyncService.scheduleNextUpdate(getActivity());
		}
    }
}

