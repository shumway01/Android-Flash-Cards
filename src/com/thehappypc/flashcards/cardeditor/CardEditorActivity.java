package com.thehappypc.flashcards.cardeditor;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.cardeditor.CardPropertiesFragment;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.data.TagList;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Class of Activity responsible for creating a new or editing an existing FlashCard.
 * The Activity should be called with an Intent containing the following extra arguments:
 *	 A TagList containing the available tags the card can be tagged with
 * 
 * The Activity gets the data for the new card from the user, validates it, then
 * passes the FlashCard object back to the calling Activity via the setResult method
 * in that method's argument Intent. The calling Activity is then responsible for
 * doing the actual attachment to the FlashCardDeck. If the user chose the "save
 * and create" option, this Activity passes back the value "true" accessible via
 * the key KEY_CREATE_ANOTHER in the setResult Intent.
 * 
 */
public class CardEditorActivity extends FragmentActivity {
	
	public static String KEY_CREATE_ANOTHER;
		
	private TagList		mAvailableTags;
	private FlashCard	mCard;

	private CardPropertiesFragment mFragment = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if (extras != null) {
        	mAvailableTags = (TagList) extras.getParcelable(FlashCardDeck.KEY_TAGLIST);
        	mCard = (FlashCard) extras.getParcelable(FlashCard.KEY_OBJECT);
        }
        if (mAvailableTags == null)
        	mAvailableTags = new TagList();
		
		setContentView(R.layout.card_editor_activity);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
        if (mCard == null) {
        	actionBar.setTitle(R.string.action_bar_title_add_card);
        	mCard = new FlashCard();
        } else
        	actionBar.setTitle(R.string.action_bar_title_modify_card);
		
		mFragment = CardPropertiesFragment.newInstance(mCard, mAvailableTags);
		
		// TODO Should the creation of the fragment be inside this test?
		// General task: understand savedInstanceState better and complete
		// its implementation
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.cardEditorContainer, mFragment)
					.commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
    	outState.putParcelable(FlashCardDeck.KEY_TAGLIST, mAvailableTags);
    	outState.putParcelable(FlashCard.KEY_OBJECT, mCard);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.card_params_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		switch (item.getItemId()) {
		case android.R.id.home:			
		case R.id.menu_save:
			// Save the card and go back to Deck Editor activity
			doSaveAction(false);
			return true;
		case R.id.menu_save_next:
			// Save the card and indicate the caller should create
			// us again
			doSaveAction(true);
			return true;
		case R.id.menu_discard:
			// Cancel and return to the Deck Editor activity
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Helper function that performs error checking and saves the attributes entered
	 * in the associated editor UI.
	 * @return True if the card was saved, false if any error occurred.
	 */
	private boolean doSaveAction(boolean createAnother) {
		if (mFragment.validate() && mFragment.commit(mCard)) {
			Intent data = new Intent();
			if (createAnother)
				data.putExtra(KEY_CREATE_ANOTHER, true);
			data.putExtra(FlashCard.KEY_OBJECT, mCard);
			setResult(RESULT_OK, data);
			finish();
	        
			return true;	// not reached
		}
		return false;
	}
}
