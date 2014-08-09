package com.thehappypc.flashcards.deckeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.Assert;

import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.listeners.OnCardListChangedListener;
import com.thehappypc.util.Committable;
import com.thehappypc.util.Validateable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.AbsListView.MultiChoiceModeListener;

public class CardListFragment extends ListFragment
							  implements OnCardListChangedListener,
							  Validateable, Committable {
	
	/**
	 * The calling Activity is required to implement this interface. Its
	 * onFlashCardSelected() method will be called when the user selects
	 * a flash card from this list displayed by this fragment. The card
	 * selected is returned to the Activity via the method's argument.
	 * 
	 * We implement things this way to handle the case in which multiple
	 * fragments may be displayed on the screen simultaneously, so we defer
	 * the decision about laying out the selected card's fragment to the
	 * calling Activity.
	 * 
	 * @author Steve
	 */
	public interface OnFlashCardSelectedListener {
		public void onFlashCardSelected(FlashCard card);
	}
	OnFlashCardSelectedListener mSelectionListener = null;
	
	public interface OnFlashCardCreationRequestedListener {
		public void onFlashCardCreationRequestedListener();
	}
	OnFlashCardCreationRequestedListener mCreationListener = null;
	
	public interface OnFlashCardListChangedListener {
		public void onFlashCardListChanged(FlashCard[] cardArray);
	}
	OnFlashCardListChangedListener mCardChangeListener = null;
		
	private ListView				mListView = null;
	private CardListAdapter			mAdapter = null;	
	private ArrayList<FlashCard>	mCards = new ArrayList<FlashCard>();
    private ArrayList<FlashCard>	mCheckedCards = null;
	
	private Context					mContext = null;
	
	private int						mDeckId = FlashCardDeck.DECK_ID_NOT_SET;
	
	private ActionMode		mActionMode = null;
	
	public static CardListFragment newInstance(int deckId) {
		CardListFragment fragment = new CardListFragment();
		
		Bundle args = new Bundle();
		args.putInt(FlashCard.KEY_ID, deckId);
		fragment.setArguments(args);	// retained across create/destroy
	
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.card_list_fragment, container, false);
		
		setRetainInstance(true);
				
		mContext = getActivity();
		
		mListView = (ListView) rootView.findViewById(android.R.id.list);
		
		Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		if (bundle != null) {
        	int deckId = bundle.getInt(FlashCardDeck.KEY_ID, FlashCardDeck.DECK_ID_NOT_SET);
        	if (deckId != FlashCardDeck.DECK_ID_NOT_SET) {
	        	FlashCardDeck deck = FlashCardApplication.getInstance().loadDeck(deckId);
	    		FlashCard[] cardArray = new FlashCard[deck.size()];
	    		deck.getCardArray(cardArray);
	    		mCards.clear();
				mCards.addAll(Arrays.asList(cardArray));
        	}
		}
		
		// Respond to long press by selecting the card and enter
		// action mode to allow batch operations
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				FlashCardApplication.log("CardListFragment.onCreateActionMode");
				mActionMode = mode;
		        mode.setTitle("Select Items");
	            
	    		mode.getMenuInflater().inflate(R.menu.card_list_actionmode_menu, menu);
	            
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				FlashCardApplication.log("LauncherActivity.onPrepareActionMode");
				
	            int nChecked = mListView.getCheckedItemCount();
	            
	            // TODO How often are these recreated? Can we cache references
	            // to them?
	            MenuItem deleteOneItem = menu.findItem(R.id.menu_delete_card);
	            MenuItem deleteMultItem = menu.findItem(R.id.menu_delete_cards);
	            MenuItem modifyItem = menu.findItem(R.id.menu_modify_card);
	    		
	            if (deleteOneItem != null)
	            	deleteOneItem.setVisible(nChecked == 1);
	            if (deleteMultItem != null)
	            	deleteMultItem.setVisible(nChecked > 1);
	            if (modifyItem != null)
	            	modifyItem.setVisible(nChecked == 1);
	            
				return true;
			}

			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				FlashCardApplication.log("onActionItemClicked");
		        mode.setTitle(mContext.getString(R.string.action_mode_title));
		        mode.setSubtitle(mContext.getString(R.string.action_mode_one_selected));
		        
		        ArrayList<FlashCard> checkedCards = getCheckedCards();
		        
				switch (item.getItemId()) {
		        case R.id.menu_delete_card:
		        case R.id.menu_delete_cards:
		        	confirmDeleteCards(checkedCards);
		        	return true;
		        case R.id.menu_modify_card:
		        	// Note that the only way we can get to this menu item is if only
		        	// one card is selected, even though the list supports multiple
		        	// selections...
		        	Assert.assertTrue(checkedCards.size() == 1);
		        	mSelectionListener.onFlashCardSelected(checkedCards.get(0));
		        	return true;
		 		}
				return false;
		    }

			@Override
			public void onDestroyActionMode(ActionMode mode) {		
				mActionMode = null;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				FlashCardApplication.log("onItemCheckedStateChanged");
		        int selectCount = mListView.getCheckedItemCount();
		        
		        switch (selectCount) {
		        case 1:
			        mode.setSubtitle(mContext.getString(R.string.action_mode_one_selected));
		            break;
		        default:
		        	String s = String.format(
		        			mContext.getString(R.string.action_mode_multiple_selected),
		        			selectCount);
			        mode.setSubtitle(s);
		            break;
		        }
		        mode.invalidate();
			}			
		});
		
		mAdapter = new CardListAdapter(container.getContext(), 0, mCards);
		setListAdapter(mAdapter);
		
		setHasOptionsMenu(true);
		
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(FlashCardDeck.KEY_ID, mDeckId);
	}
	
	@Override
	public void setUserVisibleHint(boolean visible) {
		super.setUserVisibleHint(visible);
    	
		if (visible) {
			// Reselect checked cards to recreate CAB/ActionMode
			if (mCheckedCards != null) {
				mAdapter.setNotifyOnChange(false);
				for (int i = 0; i < mListView.getCount(); i++) {
					FlashCard card = (FlashCard) mListView.getItemAtPosition(i);
					if (mCheckedCards.contains(card))
						mListView.setItemChecked(i, true);
					else
						mListView.setItemChecked(i, false);
				}
				mAdapter.notifyDataSetChanged();
			}
		} else {
			if (mListView != null)
				mCheckedCards = getCheckedCards();
			if (mActionMode != null)
				mActionMode.finish();
		}
	}
	
	/**
	 * We override onAttach to ensure that our calling Activity is capable of
	 * handling callbacks need to occur when a card is selected and when the user
	 * wants to create a new card. The calling Activity is responsible for managing
	 * the display of Fragments, so it needs to be the entity responsible for
	 * displaying the required fragment in the appropriate location on the screen.
	 * Doing things this way avoids explicitly tight coupling between this Fragment
	 * and the calling Activity.
	 */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface so it can be notified when the user
        // selects a card. If not, it throws an exception
        try {
            mSelectionListener = (OnFlashCardSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFlashCardSelectedListener");
        }
        
        // This makes sure that the container activity has implemented
        // the callback interface so it can handle the case in which the
        // user wants to create a new card/cards. If not, it throws an exception
       try {
            mCreationListener =
            		(OnFlashCardCreationRequestedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFlashCardCreationRequestedListener");
        }
        
        ((DeckEditorActivity) activity).setOnCardListChangedListener(this);
    }
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.card_list_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}	
	
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		v.setSelected(true);
		l.setItemChecked(position, true);
		mSelectionListener.onFlashCardSelected(mCards.get(position));				    	
    }
    
    // In order for mAdapter.addAll to work, the adapter must have been
    // initialized with an ArrayList, not an array
    public void onFlashCardListChanged(FlashCard[] cardArray) {
    	mCards.clear();
		mCards.addAll(Arrays.asList(cardArray));
    	mAdapter.setNotifyOnChange(false);
    	mAdapter.clear();
    	mAdapter.addAll(cardArray);
    	mAdapter.notifyDataSetChanged();
    }
    
    private ArrayList<FlashCard> getCheckedCards() {
    	ArrayList<FlashCard> checkedCards = new ArrayList<FlashCard>();    	
    	SparseBooleanArray itemPositions = mListView.getCheckedItemPositions();
    	int	positions = mListView.getCheckedItemCount();
    	
    	for (int i = 0; i < mListView.getCount() && positions > 0; i++) {
    		if (itemPositions.get(i)) {
    			Object o = mListView.getItemAtPosition(i);
    			if (o instanceof FlashCard) {
    				checkedCards.add((FlashCard) o);
    			}
    			positions--;	// optimization -- done when all checked have been counted
    		}
    	}
    	return checkedCards;
    }
    
    // Menu item processing
    
    private void confirmDeleteCards(ArrayList<FlashCard> cards) {
    	int	numCards = cards.size();
    	String msg;
    	if (numCards == 1)
	    	msg = getResources().getString(R.string.delete_card_format);
    	else
    		msg = String.format(
    				getResources().getString(R.string.delete_multiple_cards_format),
    				numCards);
    	if (numCards > 0) {
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
    	    	.setTitle(R.string.delete_card_title)
    	        .setMessage(msg)
    	        .setPositiveButton(R.string.delete_card_ok, new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	                performDeleteCards();
    	                dialog.dismiss();
	                }
	            })
    	        .setNegativeButton(R.string.delete_card_cancel,new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	                dialog.dismiss();
	                }
	            });
    	    AlertDialog alert = builder.create();
    	    alert.show();    		
    	}
    }
    
    private void performDeleteCards() {
    	Iterator<FlashCard> it = getCheckedCards().iterator();
    	FlashCard card = null;
    	while (it.hasNext()) {
    		card = it.next();
    		mCards.remove(card);
    	}
    	
    	mAdapter.notifyDataSetChanged();
    	if (mActionMode != null)
    		mActionMode.finish();
    }
 
    /**
     * Validate hook method. Currently does nothing.
     * @return Always returns true.
     */
	@Override
	public boolean validate() {
		return true;
	}    
	
	/**
     * Commit hook method. When asked to commit, push our current list of cards into
     * the supplied deck object. The deck will be committed to backing store by the
     * calling Activity.
     * @param arg A FlashCardDeck to which we attach our current list of cards.
     * @return true if successful, false otherwise.
     */
	@Override
	public boolean commit(Object arg) {
		FlashCardDeck deck = (FlashCardDeck) arg;
		deck.removeAllCards();
		deck.addCards(mCards);
		
		FlashCardApplication.getInstance().storeDeck(deck);
		
		return true;
	}
}
