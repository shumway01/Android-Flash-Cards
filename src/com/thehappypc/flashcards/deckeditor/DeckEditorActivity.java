package com.thehappypc.flashcards.deckeditor;

import java.util.ArrayList;
import java.util.Iterator;

import com.thehappypc.cards.Card;
import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.cardeditor.CardEditorActivity;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.listeners.OnCardListChangedListener;
import ua.com.vassiliev.androidfilebrowser.FileBrowserActivity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * The DeckEditorActivity is the central repository and rendezvous point for all
 * Fragments and Activities that deal with displaying and modifying the various
 * attributes of a FlashCardDeck. It internally maintains a FlashCardDeck object
 * and uses callback methods to receive change requests from the various UI pieces
 * that manipulate the various attributes. These include:
 * 
 * 	Changing the deck's name
 * 		FlashCardDeckRenamedListener.onFlashCardDeckRenamed
 * 	Changing the set of tags associated with the deck (adding, removing, renaming)
 * 		addDeckTag, removeDeckTag, renameDeckTag
 * 		onFlashCard
 * 	Adding and removing cards
 * 	Changing any of the attributes of a card
 * 
 * @author Steve
 *
 */
public class DeckEditorActivity extends FragmentActivity
					implements CardListFragment.OnFlashCardSelectedListener,
					CardListFragment.OnFlashCardCreationRequestedListener {
		
	private OnCardListChangedListener mCardListChangedListener = null;
	
	private ViewPager	mViewPager;
    private TabsAdapter	mTabsAdapter;
    
    private FlashCardDeck	mDeck = null;
    private int				mDeckId = FlashCardDeck.DECK_ID_NOT_SET;
        
    // Activity request codes for startActivityForResult/onActivityResult
    private static final int FILEBROWSER_CODE = 1;
    private static final int CREATECARD_CODE = 2;
    private static final int MODIFYCARD_CODE = 3;
        
    private static final int TAB_POSITION_NONE = -1;
    private static final int TAB_POSITION_DECK = 0;
    private static final int TAB_POSITION_CARDS = 1;
    
    private int mOverrideCurrentTabItem = TAB_POSITION_NONE;
    
    private static String TAB_KEY = "tab";	// for saveInstanceState
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	int	titleResource;
    	
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.deck_editor_activity);
        mViewPager = (ViewPager) findViewById(R.id.editorPager);
        
        // Look up the deck by id from the database. If no id was passed in,
        // we are creating and editing a new deck.
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if (bundle != null) {
        	mDeckId = bundle.getInt(FlashCardDeck.KEY_ID, FlashCardDeck.DECK_ID_NOT_SET);        	
        	mDeck = FlashCardApplication.getInstance().loadDeck(mDeckId);
        }
        
        if (mDeck == null) {
        	titleResource = R.string.action_bar_title_create_deck;
        	mDeck = new FlashCardDeck();        	
        } else
	        titleResource = R.string.action_bar_title_editor;
        
        final ActionBar bar = getActionBar();
		bar.setTitle(titleResource);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayHomeAsUpEnabled(true);		
		bar.setDisplayShowHomeEnabled(true);
		
        // TODO Larger screens should show the properties on the left and
        // the card list on the right
        mTabsAdapter = new TabsAdapter(this, mViewPager);
        
        Bundle deckPropsArgs = new Bundle();
        deckPropsArgs.putInt(FlashCardDeck.KEY_ID, mDeck.getId());
        mTabsAdapter.addTab(TAB_POSITION_DECK, bar.newTab().setText(R.string.tab_properties),
                DeckPropertiesFragment.class, deckPropsArgs);
        
        Bundle cardListArgs = new Bundle();
        cardListArgs.putInt(FlashCardDeck.KEY_ID, mDeck.getId());
        mTabsAdapter.addTab(TAB_POSITION_CARDS, bar.newTab().setText(R.string.tab_cards),
                CardListFragment.class, cardListArgs);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FlashCardDeck.KEY_ID, mDeck.getId());
        outState.putInt(TAB_KEY, getActionBar().getSelectedNavigationIndex());
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(TAB_KEY, 0));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.deck_editor_menu, menu);    	
		return true;    	
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.	
		switch (item.getItemId()) {
		case android.R.id.home:
			if (doSaveDeckProperties() && doSaveCardList())
				finish();
			return true;
		case R.id.menu_add_card:
			startCardEditorActivity(null);
			return true;
		case R.id.menu_export:
			FlashCardApplication.startExportActivity(this);
			return true;
		case R.id.menu_discard:
			finish();	// quit the Activity
			return true;
        case R.id.menu_settings:
        	FlashCardApplication.startPreferencesActivity(this);
        	return true;
    	default:
    		Toast.makeText(this, "Activity ignored menu item", Toast.LENGTH_SHORT).show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final FragmentActivity mFragmentActivity;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(FragmentActivity activity, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mFragmentActivity = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(int position, ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(position, info);
            mActionBar.addTab(tab, position);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }
        
        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mFragmentActivity, info.clss.getName(), info.args);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        // Handle validation as the user attempts to move away from a page. This is
        // a little tricky, because the order in which these hook methods are called
        // varies according to whether the page change was initiated by clicking on a
        // tab or with a swipe.
        //
        // In the case of a swipe, the order of the method calls is this:
        //		onPageSelected(<new index>)
        //		onTabUnselected(<old index>)
        //		onTabSelected(<new index>)
        //
        // whereas in the case of a tab click, the order is:
        //		onTabUnselected(<old index>)
        //		onTabSelected(<new index>)
        //		onPageSelected(<new index>)
        //
        // We can differentiate between the two by comparing the indices of
        // the selected tab vs. the selected page.
        
        @Override
        public void onPageSelected(int position) {
        	if (mOverrideCurrentTabItem != TAB_POSITION_NONE) {
	    		int	index = mOverrideCurrentTabItem;
	    		mOverrideCurrentTabItem = TAB_POSITION_NONE;        	       		
        		mActionBar.setSelectedNavigationItem(index);   
        	} else if (mActionBar.getSelectedNavigationIndex() != position) {
        		if (position == TAB_POSITION_DECK) {
	        		// This is a swipe to the deck properties; validate
	        		// and commit the card list and return to it if necessary.
            		if (doSaveCardList() == false) {
            			mOverrideCurrentTabItem = TAB_POSITION_CARDS;
            			mViewPager.setCurrentItem(mOverrideCurrentTabItem);
            		} else
		        		mActionBar.setSelectedNavigationItem(position);   
        		} else if (position == TAB_POSITION_CARDS) {
	        		// This is a swipe to the card list; validate and commit
	        		// the deck properties and return to it if necessary.
            		if (doSaveDeckProperties() == false) {
            			mOverrideCurrentTabItem = TAB_POSITION_DECK;
            			mViewPager.setCurrentItem(mOverrideCurrentTabItem);
            		} else  			
		        		mActionBar.setSelectedNavigationItem(position);   
        		} // else what?
        	}
        }
        
        @Override
        public void onPageScrollStateChanged(int state) {
        }

        /**
         * TODO Moving back and forth between the deck properties tab and the card tab
         * works as follows: when navigating away from the properties tab, all fields
         * in the properties form should be saved to the underlying deck. When moving
         * away from the card tab, all values in the card form should be preserved so
         * that navigating back restores them.
         */
        @Override
        public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
        	int	position = tab.getPosition();
        	
        	FlashCardApplication.log("onTabSelected(" + position + ")");
        	
        	if (mOverrideCurrentTabItem != TAB_POSITION_NONE) {
        		int	index = mOverrideCurrentTabItem;
        		mOverrideCurrentTabItem = TAB_POSITION_NONE;        	       		
        		mViewPager.setCurrentItem(index);
        	} else if (mViewPager.getCurrentItem() != position) {
        		if (position == TAB_POSITION_DECK) {
	        		// This is a touch on the deck properties tab; validate
	        		// and commit the card list and return to it if necessary.
            		if (doSaveCardList() == false) {
            			mOverrideCurrentTabItem = TAB_POSITION_CARDS;
            			postTabReset(mOverrideCurrentTabItem);
	                } else
            			mViewPager.setCurrentItem(position);   
        		} else if (position == TAB_POSITION_CARDS) {
	        		// This is a touch on the card list tab; validate and commit
	        		// the deck properties and return to it if necessary.
            		if (doSaveDeckProperties() == false) {
            			mOverrideCurrentTabItem = TAB_POSITION_DECK;
            			postTabReset(mOverrideCurrentTabItem);
            		} else  			
            			mViewPager.setCurrentItem(position);   
        		} // else what?
        	}
        }

        @Override
        public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
        }
    	
		private void postTabReset(final int index) {
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                public void run() {
	        		mActionBar.setSelectedNavigationItem(index);   
                }
            };
            handler.postDelayed(runnable, 10);
		}
    }

    /**
     * Called when a card is selected. The Activity needs to know when an individual
     * card is to be displayed as the Activity is responsible for managing the display
     * of the various fragments (depending on screen size and orientation).
     */
	@Override
	public void onFlashCardSelected(FlashCard card) {
		startCardEditorActivity(card);
	}
	
	/**
	 * Commit the deck to the database and its backing store [XML] file.
	 * @return true if the save completed, false otherwise
	 */
	public boolean doSaveDeckProperties() {
		 DeckPropertiesFragment deckFragment =
				 (DeckPropertiesFragment) mTabsAdapter.instantiateItem(mViewPager, TAB_POSITION_DECK);				 
		if (deckFragment.validate() && deckFragment.commit(mDeck)) {    		
			Toast.makeText(this, R.string.status_deck_saved, Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	/**
	 * Commit the deck to the database and its backing store [XML] file.
	 * @return true if the save completed, false otherwise
	 */
	public boolean doSaveCardList() {
		 CardListFragment listFragment =
				 (CardListFragment) mTabsAdapter.instantiateItem(mViewPager, TAB_POSITION_CARDS);				 
		if (listFragment.validate() && listFragment.commit(mDeck)) {
			Toast.makeText(this, R.string.status_deck_saved, Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	/**
	 * Create and add a new card to the deck.
	 */
	public void startCardEditorActivity(FlashCard card) {
		Intent intent = new Intent(this, CardEditorActivity.class);
		if (card != null)
			intent.putExtra(FlashCard.KEY_OBJECT, (Parcelable) card);
		intent.putExtra(FlashCardDeck.KEY_TAGLIST, (Parcelable) mDeck.cloneTags());
		startActivityForResult(intent, card != null ? MODIFYCARD_CODE : CREATECARD_CODE);		
	}

	/**
	 * Receive the result from an Activity we initiated (either the FileBrowser for
	 * the "export" action or the CreateCardActivity.
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		switch (requestCode) {
    		case FILEBROWSER_CODE:
    			doExportDeck(data);
    			break;
    		case CREATECARD_CODE:
    			getCreateCardResult(data);
    			break;
    		case MODIFYCARD_CODE:
    			getModifyCardResult(data);
    			break;
			default:
				break;	// Convenience for setting a breakpoint...
    		}
    	}
	}
    	
	private void doExportDeck(Intent data) {
		String dirName = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);		
		FlashCardApplication.getInstance().exportDeck(mDeck, dirName);
	}
	
	private void getCreateCardResult(Intent data) {
		FlashCard card = (FlashCard) data.getParcelableExtra(FlashCard.KEY_OBJECT);
		// Commit the new card to the deck
		mDeck.addCard(card);
		
		if (mCardListChangedListener != null) {
			FlashCard[] cards = new FlashCard[mDeck.size()];
			mDeck.getCardArray(cards);
			mCardListChangedListener.onFlashCardListChanged(cards);
		}
		
		boolean createAnother = data.getBooleanExtra(CardEditorActivity.KEY_CREATE_ANOTHER, false);
		if (createAnother)
			// The user pressed save and create, so invoke the activity again
			startCardEditorActivity(null);
	}
	
	private void getModifyCardResult(Intent data) {
		FlashCard newCard = (FlashCard) data.getParcelableExtra(FlashCard.KEY_OBJECT);
		
		// Find the card with the given ID and stuff the new values into it
		Iterator<Card> it = mDeck.iterator();
		while (it.hasNext()) {
			FlashCard oldCard = (FlashCard) it.next();
			if (oldCard.getId() == newCard.getId()) {
				oldCard.setQuestion(newCard.getQuestion());
				oldCard.setHint(newCard.getHint());
				oldCard.setAnswer(newCard.getAnswer());
				oldCard.setExplanation(newCard.getExplanation());
				oldCard.clearTags();
				oldCard.addTags(newCard.cloneTags());
				break;
			}
		}
				
		if (mCardListChangedListener != null) {
			FlashCard[] cards = new FlashCard[mDeck.size()];
			mDeck.getCardArray(cards);
			mCardListChangedListener.onFlashCardListChanged(cards);
		}
		
		boolean createAnother = data.getBooleanExtra(CardEditorActivity.KEY_CREATE_ANOTHER, false);
		if (createAnother)
			// The user pressed save and create, so invoke the activity again
			startCardEditorActivity(null);
	}
	
	@Override
	public void onFlashCardCreationRequestedListener() {
		startCardEditorActivity(null);
	}
        	
	// Setters for various listeners
	public void setOnCardListChangedListener(OnCardListChangedListener listener) {
		mCardListChangedListener = listener;
	}

}
