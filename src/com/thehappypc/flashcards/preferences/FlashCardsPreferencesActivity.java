package com.thehappypc.flashcards.preferences;

import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

public class FlashCardsPreferencesActivity extends Activity {
	
	/**
	 * Preference keys - The keys are initialized from resources in the strings.xml
	 * file to avoid having to manually keep the strings file and this file in sync
	 * (pretty cool, huh?). There is a comment in the strings file that indicates
	 * that these strings should not be localized.
	 */
	public static final String KEY_PREF_ENABLE_HINTS =
			FlashCardApplication.getInstance()
				.getResources().getString(R.string.pref_key_enable_hints);
	public static final String KEY_PREF_ENABLE_EXPLANATIONS = 
			FlashCardApplication.getInstance()
				.getResources().getString(R.string.pref_key_enable_explanations);
	public static final String KEY_PREF_ENABLE_SCORING =
			FlashCardApplication.getInstance()
				.getResources().getString(R.string.pref_key_enable_scoring);
	public static final String KEY_PREF_ENABLE_AUTO_SHUFFLE =
			FlashCardApplication.getInstance()
				.getResources().getString(R.string.pref_key_enable_auto_shuffle);
	public static final String KEY_PREF_DELETE_DATABASE =
			FlashCardApplication.getInstance()
				.getResources().getString(R.string.pref_key_delete_database);
	public static final String KEY_PREF_ENABLE_DEVELOPER =
			FlashCardApplication.getInstance()
				.getResources().getString(R.string.pref_key_enable_developer);
	public static final String KEY_PREF_VERSION =
			FlashCardApplication.getInstance()
				.getResources().getString(R.string.pref_key_version);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.preferences_activity);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);		
		actionBar.setTitle(R.string.action_bar_title_settings);
        
        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        FlashCardsPreferencesFragment mPrefsFragment = new FlashCardsPreferencesFragment();
        mFragmentTransaction.add(R.id.preferencesContainer, mPrefsFragment);
        mFragmentTransaction.commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
