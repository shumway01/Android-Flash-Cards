package com.thehappypc.flashcards.deckeditor;

import java.util.Iterator;

import com.thehappypc.cards.Card;
import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.adapters.TagListAdapter;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.data.TagList;
import com.thehappypc.flashcards.deckeditor.CreateTagFragment.CreateTagDialogListener;
import com.thehappypc.util.Committable;
import com.thehappypc.util.Validateable;

import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.MultiChoiceModeListener;

/**
 * Fragment that displays the properties of a card deck. There are three ways to
 * instantiate this Fragment.
 * 	1) Call the class newInstance() method that takes a deck as arguments
 *  2) Call Fragment.instantiate with an argument Bundle containing the deck's id,
 *  	name, and its Taglist (as a Parcelable)
 *  3) Create the fragment passing in savedInstanceState, containing the Bundle
 *  	arguments mentioned above
 *  
 * @author Steve
 *
 */
public class DeckPropertiesFragment extends Fragment
				implements Validateable, Committable, CreateTagDialogListener {
	
	private Context mContext;
	
	private int			mDeckId = FlashCardDeck.DECK_ID_NOT_SET;
	private TagList		mTagList = null;
		
	// Widgets
	private TextView	mNameView;	
	private GridView	mTagGridView;
	
	private TagListAdapter	mTagAdapter = null;
	private ActionMode		mActionMode = null;
	
	// Removing deleted tags from the set of cards is potentially an
	// expensive operation. To avoid having to validate each card's tags
	// each time we commit, cache the set of deleted tags when the user
	// performs the delete and run through the card tags only as necessary.
	private static final String KEY_DELETED_TAGS = "DeckPropertiesFragment.DeletedTags";
	private TagList			mDeletedTags = null;
	
	private static final String KEY_CHECKED_TAGS = "DeckPropertiesFragment.CheckedTags";
	private TagList			mCheckedTags = null;
	
	public static DeckPropertiesFragment newInstance(int deckId) {
		DeckPropertiesFragment fragment = new DeckPropertiesFragment();
		
		Bundle args = new Bundle();
		args.putInt(FlashCardDeck.KEY_ID, deckId);
		fragment.setArguments(args);	// retained across create/destroy
		
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		View rootView = inflater.inflate(
				R.layout.deck_properties_fragment, container, false);
		
		setRetainInstance(true);
		
		mContext = getActivity();
		
		mNameView = (TextView) rootView.findViewById(R.id.nameView);
		mTagGridView = (GridView) rootView.findViewById(R.id.deckTagGridView);
		
		FlashCardDeck deck = null;
		
		Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		if (bundle != null) {
			mDeckId = bundle.getInt(FlashCardDeck.KEY_ID, FlashCardDeck.DECK_ID_NOT_SET);
			deck = FlashCardApplication.getInstance().loadDeck(mDeckId);
			String name = bundle.getString(FlashCardDeck.KEY_NAME, "");
			if (deck != null && name.length() > 0)
				deck.setName(name);
			mTagList = bundle.getParcelable(FlashCardDeck.KEY_TAGLIST);
			if (deck != null && mTagList != null) {
				deck.clearTags();
				deck.addTag(mTagList);
			}
			mDeletedTags = bundle.getParcelable(KEY_DELETED_TAGS);
			mCheckedTags = bundle.getParcelable(KEY_CHECKED_TAGS);
		}

		if (deck == null)
			deck = new FlashCardDeck();
		if (mTagList == null)
			mTagList = deck.cloneTags();
		if (mDeletedTags == null)
			mDeletedTags = new TagList();
			
		mNameView.setText(deck.getName());
		
		// Respond to long press by selecting the card and enter
		// action mode to allow batch operations
		mTagGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		mTagGridView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mActionMode = mode;
		        mode.setTitle(mContext.getString(R.string.action_mode_title));	            
	    		mode.getMenuInflater().inflate(R.menu.deck_tags_actionmode_menu, menu);	            
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {				
	            int nChecked = mTagGridView.getCheckedItemCount();
	            
	            // TODO How often are these recreated? Can we cache references
	            // to them?
	            MenuItem deleteOneItem = menu.findItem(R.id.menu_delete_tag);
	            MenuItem deleteMultItem = menu.findItem(R.id.menu_delete_tags);
	    		
	            if (deleteOneItem != null)
	            	deleteOneItem.setVisible(nChecked == 1);
	            if (deleteMultItem != null)
	            	deleteMultItem.setVisible(nChecked > 1);

				return true;
			}

			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		        mode.setTitle(mContext.getString(R.string.action_mode_title));
		        mode.setSubtitle(mContext.getString(R.string.action_mode_one_selected));
		        
		        return onOptionsItemSelected(item); // forward to unify processing
		    }

			@Override
			public void onDestroyActionMode(ActionMode mode) {		
				mActionMode = null;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
		        int selectCount = mTagGridView.getCheckedItemCount();		        
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
		
		mTagAdapter = new TagListAdapter(mContext, R.layout.tag_list_item, mTagList, null);            
		mTagGridView.setAdapter(mTagAdapter);
		
		setHasOptionsMenu(true);
		
		return rootView;
	}
	
	@Override
	public void setUserVisibleHint(boolean visible) {
		super.setUserVisibleHint(visible);
    	
		if (visible) {
			// Reselect checked tags to recreate CAB
			if (mCheckedTags != null) {
				mTagAdapter.setNotifyOnChange(false);
				for (int i = 0; i < mTagGridView.getCount(); i++) {
					String tag = (String) mTagGridView.getItemAtPosition(i);
					if (mCheckedTags.contains(tag))
						mTagGridView.setItemChecked(i, true);
				}
				mTagAdapter.notifyDataSetChanged();
			}
		} else {
			if (mTagGridView != null)
				mCheckedTags = TagListAdapter.getCheckedTags(mTagGridView);
			if (mActionMode != null)
				mActionMode.finish();
		}
	}
	
	/**
	 * Save UI state. This includes the id of the deck we were working on (which
	 * could be DECK_ID_NOT_SET if we were creating a deck), the name of deck (the
	 * user may have entered a new name which may not yet be saved), the current
	 * set of tags (again, the user may have changed the set but not yet saved them),
	 * and any tags the user deleted prior to commit.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(FlashCardDeck.KEY_ID, mDeckId);
		outState.putString(FlashCardDeck.KEY_NAME, mNameView.getText().toString());
		outState.putParcelable(FlashCardDeck.KEY_TAGLIST, mTagList);
		outState.putParcelable(KEY_DELETED_TAGS, mDeletedTags);
    	outState.putParcelable(KEY_CHECKED_TAGS, TagListAdapter.getCheckedTags(mTagGridView));
	}
	
	/**
	 * Process a new tag from CreateTagFragment
	 */
	@Override
	public void onDialogPositiveClick(String tagName) {
		if (tagName != null && tagName.length() > 0)
			mTagAdapter.add(tagName);
		else
			Toast.makeText(mContext, R.string.error_missing_tag, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Validate the data in UI prior to commit. Ensure there is a name and that
	 * if it has been changed, it is unique in the database.
	 * @return true if a name exists and is either unchanged or unique, false otherwise
	 */
	@Override
	public boolean validate() {
		String name = mNameView.getText().toString();
		if (name == null || name.length() == 0) {
			Toast.makeText(mContext, R.string.error_missing_deck_name, Toast.LENGTH_LONG).show();
			return false;
		}
		
		FlashCardDeck deck = FlashCardApplication.getInstance().loadDeck(name);
		if (deck != null && mDeckId != deck.getId()) {
			String msg = String.format(
					mContext.getString(R.string.error_duplicate_deck_name), name);
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}

	@Override
	public boolean commit(Object arg) {
		FlashCardDeck deck = (FlashCardDeck) arg;
		TagList deckTags = mTagAdapter.getTags();
		
		deck.setName(mNameView.getText().toString());
		deck.clearTags();
		deck.addTag(deckTags);
		
		if (mDeletedTags.size() > 0) {
			Iterator<Card> cardIt = deck.iterator();
			while (cardIt.hasNext()) {
				FlashCard card = (FlashCard) cardIt.next();
				card.removeTags(mDeletedTags);
			}
			mDeletedTags.clear();
		}
		
		FlashCardApplication.getInstance().storeDeck(deck);
		
		// If the cached Id indicated the deck's id had not been set, we just
		// created a new deck 
		if (mDeckId == FlashCardDeck.DECK_ID_NOT_SET) {
			mDeckId = deck.getId();
		}
				
		return true;
	}

    // Menu item processing
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.deck_params_menu, menu);
	}
	
	/**
	 * Handle options and ActionMode menu item processing for those items
	 * completely under control of this fragment (tag creation and deletion).
	 * All other items are handled by the calling Activity by forwarding them
	 * up to the superclass.
	 * @param The MenuItem the user selected
	 * @return true if the fragment consumed the selection, the return from
	 * the superclass onOptionsItemSelected method otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create_tag:
	        CreateTagFragment dialog = CreateTagFragment.newInstance();
	        dialog.setCreateTagDialogListener(DeckPropertiesFragment.this);
	        dialog.show(getFragmentManager(), "CreateTagFragment");		        			
			return true;
        case R.id.menu_delete_tag:
        case R.id.menu_delete_tags:
        	confirmDeleteTags();
        	return true;
		}
		return super.onOptionsItemSelected(item);
	}	
	    
    private void confirmDeleteTags() {
    	int	numTags = TagListAdapter.getCheckedTags(mTagGridView).size();
    	String msg;
    	
    	if (numTags == 1)
	    	msg = getResources().getString(R.string.delete_tag_format);
    	else
    		msg = String.format(
    				getResources().getString(R.string.delete_multiple_tags_format),
    				numTags);
    	if (numTags > 0) {
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
    	    	.setTitle(R.string.delete_tag_title)
    	        .setMessage(msg)
    	        .setPositiveButton(R.string.delete_tag_ok, new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	                performDeleteTags();
    	                dialog.dismiss();
	                }
	            })
    	        .setNegativeButton(R.string.delete_tag_cancel,new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	                dialog.dismiss();
	                }
	            });
    	    AlertDialog alert = builder.create();
    	    alert.show();    		
    	}
    }
    
    // TODO This could potentially be a long-running operation, so post
    // it in a Runnable
    private void performDeleteTags() {
    	TagList deletedTags = TagListAdapter.getCheckedTags(mTagGridView);
    	
		// Cache the removal(s) to be processed in commit. Note that
    	// the addition of tags to the cached list must be cumulative,
    	// as the user may perform multiple deletion operations prior
    	// to the commit.
		mDeletedTags.addAll(deletedTags);	// dups filtered by TagList.addAll()
		
    	// Remove each tag from the adapter which is displaying
    	// the deck's tags
    	mTagAdapter.setNotifyOnChange(false);    	
    	Iterator<String> tagIt = deletedTags.iterator();
    	while (tagIt.hasNext()) {
    		String tag = tagIt.next();
    		mTagAdapter.remove(tag);
    	}    	
    	mTagAdapter.notifyDataSetChanged();
    	
    	if (mActionMode != null)
    		mActionMode.finish();
    }
 
}
