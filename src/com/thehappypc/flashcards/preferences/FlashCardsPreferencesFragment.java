package com.thehappypc.flashcards.preferences;

import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;

public class FlashCardsPreferencesFragment extends PreferenceFragment {
	
	private Preference mDbPref = null;
	private Preference mDeveloperPref = null;
	private Preference mVersionPref = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    // Load the preferences from an XML resource
	    addPreferencesFromResource(R.xml.flashcards_preferences);
	    	    
	    mDbPref = findPreference(
	    		FlashCardsPreferencesActivity.KEY_PREF_DELETE_DATABASE);
	    
	    mDeveloperPref = findPreference(
	    		FlashCardsPreferencesActivity.KEY_PREF_ENABLE_DEVELOPER);
	    if (mDeveloperPref instanceof TwoStatePreference)
	    	mDbPref.setEnabled(((TwoStatePreference) mDeveloperPref).isChecked());
	    
	    mDeveloperPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
		            Boolean b = (Boolean) newValue;
					mDbPref.setEnabled(b);
					return true;
				}
				return false;
			}
	    	
	    });
	    
	    mVersionPref = findPreference(
	    		FlashCardsPreferencesActivity.KEY_PREF_VERSION);
	    mVersionPref.setSummary(FlashCardApplication.VERSION);
    }
}
