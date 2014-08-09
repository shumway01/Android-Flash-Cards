package com.thehappypc.flashcards.launcher;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Assert;

import ua.com.vassiliev.androidfilebrowser.FileBrowserActivity;

import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.deckeditor.DeckEditorActivity;
import com.thehappypc.flashcards.viewer.DeckViewerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * The main Activity for creating/editing/viewing flash cards. This Activity
 * displays a grid of available flash card decks, with an additional deck icon
 * adorned with a "plus" sign enabling the user to create a new deck.
 * 
 * Pressing a deck's icon brings up the Viewer activity.
 * Pressing the "Create Deck" icon brings up the Deck Creator activity.
 * Long pressing a deck or checking the deck's CheckBox populates the
 * Activity's options/actions menu, which allows the user to edit, delete,
 * and export the deck.
 * 
 * The Activity's options menu also allows the user to import a deck.
 * 
 * @author Steve
 *
 */
public class LauncherActivity extends Activity {
		
	/**
	 * Variables
	 */
	private GridView mGridView = null;
	private LauncherAdapter mAdapter = null;
	
	private FlashCardApplication mApplication = null;
	private ActionMode mActionMode = null;
	
	private SharedPreferences mPrefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mApplication = FlashCardApplication.getInstance();
		PreferenceManager.setDefaultValues(this, R.xml.flashcards_preferences, false);
		mPrefs = mApplication.getPreferences();
		
		setContentView(R.layout.launcher_activity);
		
		mGridView = (GridView) findViewById(R.id.launcher);
		
		// Respond to long press by selecting the deck and enter
		// action mode to allow batch operations
		mGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		mGridView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				FlashCardApplication.log("LauncherActivity.onCreateActionMode");
				mActionMode = mode;
				
	            // Inflate a menu resource providing context menu items
				MenuInflater inflater = mode.getMenuInflater();
		        inflater.inflate(R.menu.launcher_actionmode_menu, menu);
		        
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				FlashCardApplication.log("LauncherActivity.onPrepareActionMode");
		        mode.setTitle("Select Items");
		        
		        // Adjust display of contextual items based on checked item count
		        
		        MenuItem mi;
		        int	nChecked = mGridView.getCheckedItemCount();
		        
	        	mi = menu.findItem(R.id.menu_delete_deck);
	        	if (mi != null)
	        		mi.setVisible(nChecked == 1);
	        	mi = menu.findItem(R.id.menu_delete_decks);
	        	if (mi != null)
	        		mi.setVisible(nChecked != 1);
	        	
	        	mi = menu.findItem(R.id.menu_export_deck);
	        	if (mi != null)
	        		mi.setVisible(nChecked == 1);
	        	mi = menu.findItem(R.id.menu_export_decks);
	        	if (mi != null)
	        		mi.setVisible(nChecked != 1);

	        	mi = menu.findItem(R.id.menu_modify_deck);
	        	if (mi != null)
	        		mi.setVisible(nChecked == 1);

				return true;
			}

			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				FlashCardApplication.log("onActionItemClicked");
		        mode.setTitle("Select Items");
		        mode.setSubtitle("One item selected");
		        
		        return handleMenuAction(item);
		    }

			@Override
			public void onDestroyActionMode(ActionMode mode) {		
				mActionMode = null;
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				FlashCardApplication.log("onItemCheckedStateChanged");
		        int selectCount = mGridView.getCheckedItemCount();
		        
		        switch (selectCount) {
		        case 1:
		            mode.setSubtitle("One item selected");
		            break;
		        default:
		            mode.setSubtitle("" + selectCount + " items selected");
		            break;
		        }
		        mode.invalidate();
			}			
		});
		
		// Respond to touch when not in action mode by displaying the
		// deck's cards in the DeckViewerActivity
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				startViewerActivity(mAdapter.getItem(position));
			}			
		});

		mAdapter = new LauncherAdapter(this, 0, new ArrayList<FlashCardDeck>());
		mGridView.setAdapter(mAdapter);
	}
	
	@Override
	protected void onPause() {
        super.onPause();

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.commit();
    }
	
	@Override
	protected void onResume() {
		FlashCardApplication.log("LauncherActivity.onResume()");
		super.onResume();
		
		reloadDecks();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.launcher_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return handleMenuAction(item);
	}	
	
    /**
     * Process result returned the various sub-activities we launch.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
    		if (requestCode == FlashCardApplication.REQUEST_IMPORT_FILE) {
	    		String pathName = data.getStringExtra(
	    				FileBrowserActivity.returnFileParameter);
	    		if (mApplication.importDeck(pathName)) {
	    			reloadDecks();
	    		}
    		} else if (requestCode == FlashCardApplication.REQUEST_EXPORT_DIR) {
    			String dirName = data.getStringExtra(
    					FileBrowserActivity.returnDirectoryParameter);
    			ArrayList<FlashCardDeck> deckList = getCheckedDecks();
    			Iterator<FlashCardDeck> it = deckList.iterator();
    			while (it.hasNext()) {
    				FlashCardDeck deck = it.next();
    				mApplication.exportDeck(deck, dirName);
    			}
    		}
    	}
    }
    	
    private ArrayList<FlashCardDeck> getCheckedDecks() {
    	ArrayList<FlashCardDeck> checkedDecks = new ArrayList<FlashCardDeck>();    	
    	SparseBooleanArray itemPositions = mGridView.getCheckedItemPositions();
    	int	positions = mGridView.getCheckedItemCount();
    	
    	for (int i = 0; i < mGridView.getCount() && positions > 0; i++) {
    		if (itemPositions.get(i)) {
    			Object o = mGridView.getItemAtPosition(i);
    			if (o instanceof FlashCardDeck) {
    				checkedDecks.add((FlashCardDeck) o);
    			}
    			positions--;	// optimization -- done when all checked have been counted
    		}
    	}
    	return checkedDecks;
    }
    
    // Menu item processing
    
    // Helper method to unify options and action item menu processing
	private boolean handleMenuAction(MenuItem item) {
        ArrayList<FlashCardDeck> checkedDecks = getCheckedDecks();
        
		switch (item.getItemId()) {
		case R.id.menu_create_deck:
			startEditorActivity(null);
			break;
        case R.id.menu_delete_deck:
        case R.id.menu_delete_decks:
        	confirmDeleteDecks(checkedDecks);
        	break;
		case R.id.menu_import_deck:
			startImportActivity();
			break;
        case R.id.menu_export_deck:
        case R.id.menu_export_decks:
        	FlashCardApplication.startExportActivity(this);
        	break;
        case R.id.menu_modify_deck:
        	// Note that the only way we can get to this menu item is if only
        	// one deck is selected, even though the grid supports multiple
        	// selections...
        	Assert.assertTrue(checkedDecks.size() == 1);
        	startEditorActivity(checkedDecks.get(0));
        	break;
        case R.id.menu_settings:
        	FlashCardApplication.startPreferencesActivity(this);
        	break;
		}
		return true;
	}
	
    private void startImportActivity() {
		Intent intent = new Intent(LauncherActivity.this, FileBrowserActivity.class);
		intent.setAction(FileBrowserActivity.INTENT_ACTION_SELECT_FILE);
		intent.putExtra(
				FileBrowserActivity.startDirectoryParameter,
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
		startActivityForResult(intent, FlashCardApplication.REQUEST_IMPORT_FILE);    	
    }
    
    private void startEditorActivity(FlashCardDeck deck) {
		Intent intent = new Intent(this, DeckEditorActivity.class);
		if (deck != null)
			intent.putExtra(FlashCardDeck.KEY_ID, deck.getId());
		startActivity(intent);	   	
    }
    
	private void startViewerActivity(FlashCardDeck deck) {
		if (deck.size() > 0) {
			Intent intent = new Intent(this, DeckViewerActivity.class);
			intent.putExtra(FlashCardDeck.KEY_ID, deck.getId());
			startActivity(intent);	
		} else {
	    	String msg = String.format(
	    			getResources().getString(R.string.viewer_no_cards_format),
	    			deck.getName());
    	    AlertDialog.Builder builder = new AlertDialog.Builder(this)
		    	.setTitle(R.string.viewer_no_cards_title)
		        .setMessage(msg)
		        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		                dialog.dismiss();
	                }
	            });	
    	    AlertDialog alert = builder.create();
    	    alert.show();    		
		}
	}
	
    private void confirmDeleteDecks(ArrayList<FlashCardDeck> decks) {
    	int	numDecks = decks.size();
    	String msg;
    	if (numDecks == 1)
	    	msg = String.format(
	    			getResources().getString(R.string.delete_deck_format),
	    			decks.get(0).getName());
    	else
    		msg = String.format(
    				getResources().getString(R.string.delete_multiple_decks_format),
    				numDecks);
    	if (numDecks > 0) {
    	    AlertDialog.Builder builder = new AlertDialog.Builder(this)
    	    	.setTitle(R.string.delete_deck_title)
    	        .setMessage(msg)
    	        .setPositiveButton(R.string.delete_deck_ok, new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	                performDeleteDecks();
    	                dialog.dismiss();
	                }
	            })
    	        .setNegativeButton(R.string.delete_deck_cancel,new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int which) {
    	                dialog.dismiss();
	                }
	            });
    	    AlertDialog alert = builder.create();
    	    alert.show();    		
    	}
    }
    
    private void performDeleteDecks() {
    	Iterator<FlashCardDeck> it = getCheckedDecks().iterator();
    	FlashCardDeck deck;
    	while (it.hasNext()) {
    		deck = it.next();
    		mApplication.deleteDeck(deck);
    	}
    	reloadDecks();
    	if (mActionMode != null)
    		mActionMode.finish();
    }
    
	private void reloadDecks() {
		mAdapter.setNotifyOnChange(false);
		mAdapter.clear();
		mAdapter.addAll(mApplication.getAllDecks());
		mAdapter.notifyDataSetChanged();		
	}
}