package com.thehappypc.flashcards;

import java.util.ArrayList;
import ua.com.vassiliev.androidfilebrowser.FileBrowserActivity;

import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.preferences.FlashCardsPreferencesActivity;
import com.thehappypc.flashcards.storage.DeckDatabaseOpenHelper;
import com.thehappypc.flashcards.storage.DeckLoader;
import com.thehappypc.flashcards.storage.DeckXmlLoader;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * FlashCardApplication provides a central singleton Application object that
 * the various Activities (create, edit, view) can use to perform tasks such
 * as querying the database and keep track of the active card deck.
 * 
 * @author Steve
 *
 */
public class FlashCardApplication extends Application {
	
	/**
	 * Constants
	 */		
	public static final String VERSION = "Build 14-08-08.002 v0.2";
	
	public static final int REQUEST_IMPORT_FILE = 0;
	public static final int REQUEST_EXPORT_DIR = 1;
	
	public static final String INTENT_PARENT_CLASSNAME = "FlashCardApplication.ParentClass";
	public static final String INTENT_PARENT_EXTRAS = "FlashCardApplication.ParentExtras";
	
	private static final String LOG_TAG = "FlashCards";
	
	private static FlashCardApplication mSingleton;
	
	private DeckDatabaseOpenHelper mDbHelper;
	
	private int mNextId = -1;
		
	public static FlashCardApplication getInstance() {
		return mSingleton;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mSingleton = this;
		
		// Create a new DatabaseHelper
		mDbHelper = new DeckDatabaseOpenHelper(this);
		getNextAvailableId();
	}
	
	public void initDeckLoader(FlashCardDeck deck) {
		String encoding = deck.getEncoding();
		if (encoding == null || encoding.length() == 0 || encoding.equals(DeckXmlLoader.ENCODING_XML))
			DeckXmlLoader.getInstance().init(this, deck);
	}
		
	/**
	 * Return a FlashCardDeck by looking up its database entry by ID
	 * and retrieving it from the backing store indicated in the entry.
	 * 
	 * @return The deck corresponding to the given ID, or null if no such
	 * deck exists.
	 */
	public FlashCardDeck loadDeck(int deckId) {
		FlashCardDeck deck = null;
		
		if (deckId != FlashCardDeck.DECK_ID_NOT_SET) {
			deck = mDbHelper.getDeck(deckId);
			if (deck.getLoader() == null)
				initDeckLoader(deck);
			deck.load();
		}
		
		return deck;
	}
	
	/**
	 * Return a FlashCardDeck by looking up its database entry by name
	 * and retrieving it from the backing store indicated in the entry.
	 * 
	 * @return The deck corresponding to the given name, or null if no such
	 * deck exists.
	 */
	public FlashCardDeck loadDeck(String deckName) {
		FlashCardDeck deck = mDbHelper.getDeck(deckName);
		
		if (deck != null) {
			if (deck.getLoader() == null)
				initDeckLoader(deck);
			deck.load();
		}
		
		return deck;
	}
	
	public void storeDeck(FlashCardDeck deck) {
		if (deck.getId() == -1) {
			deck.setId(getNextAvailableId());
			mDbHelper.addDeck(deck);
		} else
			mDbHelper.updateDeck(deck);
		if (deck.getLoader() == null)
			initDeckLoader(deck);
		deck.store();	// write to backing store
	}
	
	public void deleteDeck(FlashCardDeck deck) {
		mDbHelper.deleteDeck(deck);		
		if (deck.getLoader() == null)
			initDeckLoader(deck);
		deck.delete();
	}
	
	/**
	 * Read a deck from somewhere in the file system and add it to the database.
	 * @param fileName
	 * @return true if the deck was successfully read and added, false otherwise
	 */
	public boolean importDeck(String fileName) {
		FlashCardDeck deck = new FlashCardDeck();
		deck.setId(getNextAvailableId());
		initDeckLoader(deck);
		
		// We want to read the deck from the specified filename, so trick
		// the deck's associated loader by changing the deck's URI to that
		// of the desired location, then set it back when we're done.
		String oldUri = deck.getUri();		
		deck.setUri("file:" + fileName);
		deck.load();
		deck.setUri(oldUri);
		
		// Check for duplicate name and modify as necessary. We will generate
		// names of the form "<Name> (n).fcd" where "n" is an integer starting at 1
		// until we find a unique name.
		// TODO Take a deck name of the form <Name> (n) and generate one named
		// <Name> (n+1)
		String baseName = deck.getName();
		String newName = baseName;
		int copyNumber = 1;
		while (mDbHelper.deckExists(newName) == true) {
			newName = baseName + " (" + copyNumber++ + ")" + DeckXmlLoader.XML_FILE_EXT;
		}
		if (copyNumber > 1) {
			deck.setName(newName);
			String s = String.format(
					getResources().getString(R.string.import_duplicate_format),
					baseName, newName);
			Toast.makeText(this, s, Toast.LENGTH_LONG).show();
		}
		
		// Add the deck to the database and store it in our application's
		// private storage
		storeDeck(deck);
		
		return true;
	}
	
	public boolean exportDeck(FlashCardDeck deck, String dirName) {
		boolean	exported = false;
		
		// We want to write the deck to the specified directory, so trick
		// the deck's associated loader by changing the deck's URI to that
		// of the desired location, then set it back when we're done.
		String oldUri = deck.getUri();	
		DeckLoader oldLoader = deck.getLoader();
		String oldEncoding = deck.getEncoding();
		
		deck.setUri("file:" + dirName + "/" + deck.getName() + DeckXmlLoader.XML_FILE_EXT);
		deck.setLoader(DeckXmlLoader.getInstance());
		deck.setEncoding(DeckXmlLoader.ENCODING_XML);
		
		exported = deck.store();
		
		deck.setUri(oldUri);
		deck.setLoader(oldLoader);
		deck.setEncoding(oldEncoding);
		
		deck.setUri(oldUri);
		
    	String msg = exported ?
			String.format(
    			getResources().getString(R.string.deck_exported_format),
    			deck.getName(), dirName) :
			String.format(
    			getResources().getString(R.string.deck_not_exported_format),
    			deck.getName(), dirName);
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		
		return exported;		
	}

	public ArrayList<FlashCardDeck> getAllDecks() {
		ArrayList<FlashCardDeck> deckList = mDbHelper.getAllDecks();
		for (FlashCardDeck deck : deckList) {
			if (deck.getLoader() == null)
				initDeckLoader(deck);
			deck.load();
		}
		return deckList;
	}

	/**
	 * Return a count of the number of decks in the database.
	 * @param text
	 */
	public int countDecks() {
		return mDbHelper.getDeckCount();
	}
	
	public void deleteDatabase() {
		mNextId = -1;
		mDbHelper.deleteDatabase();
	}
	
	public SharedPreferences getPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs;		
	}
	
	/**
	 * Start the Activity that displays preferences. This Activity can be initiated
	 * from many screens in the application, so it is accessed via the application object.
	 * 
	 * @param parent The parent Activity, from which we derive its class name and
	 * pass that as an Extra to the Intent. In this manner, the preference Activity
	 * can handle the up affordence properly and return control back to the parent
	 * Activity if the user presses it.
	 */
	public static void startPreferencesActivity(Activity parent) {
		Intent intent = new Intent(parent, FlashCardsPreferencesActivity.class);
		intent.putExtra(
				FlashCardApplication.INTENT_PARENT_CLASSNAME,
				parent.getClass().getCanonicalName());
		intent.putExtra(
				FlashCardApplication.INTENT_PARENT_EXTRAS,
				parent.getIntent().getExtras());
		parent.startActivity(intent);	   			
	}

	/**
	 * Start the Activity that exports card decks. This Activity can be initiated
	 * from different in the application, so it is accessed via the application object.
	 * Note that the calling Activity needs to do its own results processing via
	 * onActivityResult (which can call into the application's exportDeck) since it
	 * knows how to determine which decks were selected for export via the UI (I
	 * suppose I could get tricky and pass around a callback object via the Intent
	 * to more loosely couple this...).
	 * 
	 * @param parent The parent Activity, from which we derive its class name and
	 * pass that as an Extra to the Intent. In this manner, the file browser Activity
	 * can handle the up affordence properly and return control back to the parent
	 * Activity if the user presses it.
	 */
    public static void startExportActivity(Activity parent) {
		Intent intent = new Intent(parent, FileBrowserActivity.class);
		intent.setAction(FileBrowserActivity.INTENT_ACTION_SELECT_DIR);
		intent.putExtra(
				FileBrowserActivity.startDirectoryParameter,
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
		intent.putExtra(
				FlashCardApplication.INTENT_PARENT_CLASSNAME,
				parent.getClass().getCanonicalName());
		intent.putExtra(
				FlashCardApplication.INTENT_PARENT_EXTRAS,
				parent.getIntent().getExtras());
		parent.startActivityForResult(intent, REQUEST_EXPORT_DIR);    	   	
    }
    	
	public static void navigateToParent(Activity sourceActivity) {
	    Intent intent = sourceActivity.getIntent();
	    String className = intent.getStringExtra(
	    		FlashCardApplication.INTENT_PARENT_CLASSNAME);
	    Bundle extras = intent.getBundleExtra(
	    		FlashCardApplication.INTENT_PARENT_EXTRAS);

	    Intent upIntent = null;
	    try {
	         upIntent = new Intent(sourceActivity, Class.forName(className));
	         if (extras != null)
	        	 upIntent.putExtras(extras);
	         NavUtils.navigateUpTo(sourceActivity, upIntent);
         } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void log(String text) {
    	Log.i(LOG_TAG, text);
    }
	
	private int getNextAvailableId() {
		if (mNextId == -1) {
			ArrayList<FlashCardDeck> list = mDbHelper.getAllDecks();
			if (list.size() == 0)
				mNextId = 0;
			else
				for (FlashCardDeck deck : list) {
					int	id = deck.getId();
					if (id >= mNextId)
						mNextId = id + 1;
				}
		}
		return mNextId++;
	}
	
}