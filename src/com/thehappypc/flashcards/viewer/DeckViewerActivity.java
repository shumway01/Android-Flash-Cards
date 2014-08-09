/**
 * 
 */
package com.thehappypc.flashcards.viewer;

import java.util.ArrayList;
import java.util.Iterator;

import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.data.FlashCard.FlashCardStatus;
import com.thehappypc.flashcards.data.TagList;
import com.thehappypc.flashcards.listeners.OnTagsSelectedListener;
import com.thehappypc.flashcards.preferences.FlashCardsPreferencesActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * The DeckViewerActivity is the main Activity for the user to run through the deck
 * of cards in "quiz" mode. The Activity is responsible for handling all card-to-card
 * interactions, whereas the Fragment it contains is responsible for transitions between
 * the various parts of the individual card.
 * 
 * @author Steve
 *
 */
public class DeckViewerActivity extends FragmentActivity implements OnTagsSelectedListener {
	
   /**
     * The pager widget, which handles animation and allows swiping horizontally to
     * access previous and next flash cards.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;   
    
    private FlashCard[] mCardArray = null;
    
    private static final String KEY_CARD_INDEX = "DeckViewerActivity.Index";
    private int				mCardIndex = -1;
    private FlashCardDeck	mDeck = null;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer_activity);

        int deckId = FlashCardDeck.DECK_ID_NOT_SET;
        
        // There are two ways of initializing this Activity: call it with a deck
        // ID in either the calling Intent or savedInstanceState, or call it with
        // an array of FlashCards (and an index of the currently-displayed card).
        // Calling with an array and index via savedInstanceState allows the viewer
        // to preserve the shuffled order of the cards across screen orientation changes.   
        
        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if (extras != null) {
        	deckId = extras.getInt(FlashCardDeck.KEY_ID);
        	mCardArray = (FlashCard[]) extras.getParcelableArray(FlashCardDeck.KEY_CARDARRAY);
        	mCardIndex = extras.getInt(DeckViewerActivity.KEY_CARD_INDEX, 0);
        }
        
        if (mCardArray == null) {
        	// Load the deck corresponding to the deck ID we were handed, shuffle it
        	// if that preference is enabled, and create the array of cards that will
        	// be used by this Activity and its Fragments from now on. 
        	
        	mDeck = FlashCardApplication.getInstance().loadDeck(deckId);    
        	
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			boolean autoShuffle = sharedPref.getBoolean(
						FlashCardsPreferencesActivity.KEY_PREF_ENABLE_AUTO_SHUFFLE, true);
			
			mCardArray = new FlashCard[mDeck.size()];
			
			if (autoShuffle)
				mDeck.shuffle(mCardArray);
			else
				mDeck.getCardArray(mCardArray);
			
			if (mDeck != null && mDeck.countTags() > 0)
				showTagSelectionDialog(mDeck);
			
			mCardIndex = 0;
        }
                      
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        
        mPager.setOnPageChangeListener(new OnPageChangeListener() {
        	
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int index) {
		        // Keep track of which card is being displayed, so that if
	        	// the user rotates the device we can show that same card.

				mCardIndex = index;
				
				// We are now viewing the card. If the user doesn't display the answer
				// and click on one of the score recording buttons, we need to treat
				// this card as having been skipped. If the user clicks a score button,
				// the status will be updated accordingly.
				
				if (mCardArray[index].getStatus() == FlashCardStatus.NOTVIEWED)
					mCardArray[index].setStatus(FlashCardStatus.SKIPPED);
			}
        });
        
        mPagerAdapter = new DeckViewerPageAdapter(getSupportFragmentManager(), mCardArray);
        	
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mCardIndex);	// could be middle of deck, on rotate...
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);    	
    	
		// Inflate the menu; scoring, card advancement, hint and explanation buttons
    	// will be added and have their visibility managed by the Fragment.
    	
		getMenuInflater().inflate(R.menu.viewer_menu, menu);    	
		return true;    	
    }
    
    /**
     * Respond to menu touches. Note that although the visibility of
     * scoring and advancement buttons is managed by the Fragment, the
     * processing of their touches is handled here in the Activity because
     * the Activity's job is to manage transitions between cards; any
     * of these buttons causes the next card in the deck to be displayed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	FlashCard card = mCardArray[mCardIndex];
    	
    	switch (item.getItemId()) {
		case R.id.menu_next:
			doAdvance();
			return true;
		case R.id.menu_correct:
			card.countCorrect();
			doAdvance();
			return true;
		case R.id.menu_guess:
			card.countGuessed();
			doAdvance();
			return true;
		case R.id.menu_wrong:
			card.countIncorrect();
			doAdvance();
			return true;
    	case R.id.menu_settings:
    		FlashCardApplication.startPreferencesActivity(this);
    		return true;
    	}
    	return false;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	outState.putParcelableArray(FlashCardDeck.KEY_CARDARRAY, mCardArray);
    	outState.putInt(DeckViewerActivity.KEY_CARD_INDEX, mCardIndex);
    }
    
    /**
     * Handle a press of the device's "back" button. Show the previous card in
     * the deck, or return to the Launcher Activity if we are at the first card.
     */
    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
        	mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

	private void doAdvance() {
		int nextIndex = mPager.getCurrentItem() + 1;
		
		if (nextIndex == mCardArray.length) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			boolean scoringEnabled = sharedPref.getBoolean(
						FlashCardsPreferencesActivity.KEY_PREF_ENABLE_SCORING, false);
			if (scoringEnabled)
		        // Create an instance of the dialog fragment and show it
				DeckViewerSummaryFragment.newInstance(mCardArray)
		        		.show(getSupportFragmentManager(), "DeckViewerSummaryFragment");
			else {
				new DeckViewerFinishFragment()
						.show(getSupportFragmentManager(), "DeckViewerFinishFragment");				
			}
		} else
			mPager.setCurrentItem(nextIndex);		
	}
	
	/**
	 * Create an instance of the tag selection dialog, register a listener to
	 * be informed of the set of tags the user selects, and display the dialog.
	 * @param deck
	 */
	private void showTagSelectionDialog(FlashCardDeck deck) {		
		DeckViewerTagListFragment fragment =
				DeckViewerTagListFragment.newInstance(deck);
		
		fragment.setOnTagsSelectedListener(this);
		fragment.show(getSupportFragmentManager(), "PagerTagListDialogFragment");		
	}

	@Override
	public void onTagsSelected(TagList tagList) {
		// Loop through all cards, add only those with matching tags to
		// the adapter's array. The card is displayed if any of its tags
		// match any in the selected set.
		
		ArrayList<FlashCard> filteredDeck = new ArrayList<FlashCard>();
		for (FlashCard card : mCardArray) {
			Iterator<String> it = tagList.iterator();
			while (it.hasNext()) {
				String tag = it.next();
				if (card.containsTag(tag)) {
					filteredDeck.add(card);
					break;	// no need to check more tags
				}
			}
		}
		
		if (filteredDeck.size() > 0) {
			mCardArray = new FlashCard[filteredDeck.size()];
			filteredDeck.toArray(mCardArray);
			
			// Recreate the adapter
	        mPagerAdapter = new DeckViewerPageAdapter(getSupportFragmentManager(), mCardArray);    	
	        mPager.setAdapter(mPagerAdapter);
		} else {
			// No cards matched the filter
			Toast.makeText(this, R.string.viewer_no_matching_cards, Toast.LENGTH_LONG).show();
			showTagSelectionDialog(mDeck);
		}
	}
   
}
