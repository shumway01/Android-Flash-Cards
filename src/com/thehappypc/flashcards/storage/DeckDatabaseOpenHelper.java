package com.thehappypc.flashcards.storage;

import java.util.ArrayList;

import com.thehappypc.flashcards.FlashCardApplication;
import com.thehappypc.flashcards.data.FlashCardDeck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
		
public class DeckDatabaseOpenHelper extends SQLiteOpenHelper {
	final public static String TABLE_NAME = "flashCardDecks";
	final public static String _ID = "_id";
	final public static String DECK_NAME = "name";
	
	final private static String DECK_COUNT = "count";
	final private static String DECK_VERSION = "version";
	final private static String DECK_URI = "uri";
	final private static String DECK_ENCODING = "encoding";
	final private static String[] columns = {
		_ID, DECK_NAME, DECK_COUNT, DECK_VERSION, DECK_URI, DECK_ENCODING};
	
	final private static int DECK_ID_INDEX = 0;
	final private static int DECK_NAME_INDEX = 1;
//	final private static int DECK_COUNT_INDEX = 2;
	final private static int DECK_VERSION_INDEX = 3;
	final private static int DECK_URI_INDEX = 4;
	final private static int DECK_ENCODING_INDEX = 5;

	final private static String CREATE_CMD =
		"CREATE TABLE " + TABLE_NAME + " (" + _ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ DECK_NAME + " TEXT UNIQUE NOT NULL, "
			+ DECK_COUNT + " INTEGER, "
			+ DECK_VERSION + " TEXT, "
			+ DECK_URI + " TEXT, "
			+ DECK_ENCODING + " TEXT)";

	final private static String PATH_NAME = "flash_cards_db";
	final private static Integer VERSION = 1;
	final private Context mContext;

	public DeckDatabaseOpenHelper(Context context) {
		super(context, PATH_NAME, null, VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_CMD);
		Toast.makeText(mContext, "Created database table", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// N/A
	}
	
	public void deleteDatabase() {
		mContext.deleteDatabase(PATH_NAME);
		Toast.makeText(mContext, "Deleted database", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * CRUD - Create, read, update, delete
	 */
	public void addDeck(FlashCardDeck deck) {
		if (deck.getLoader() == null)
			FlashCardApplication.getInstance().initDeckLoader(deck);
		
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DeckDatabaseOpenHelper._ID, deck.getId());
		values.put(DeckDatabaseOpenHelper.DECK_NAME, deck.getName());
		values.put(DeckDatabaseOpenHelper.DECK_COUNT, deck.size());
		values.put(DeckDatabaseOpenHelper.DECK_VERSION, deck.getVersion());
		values.put(DeckDatabaseOpenHelper.DECK_URI, deck.getUri());
		values.put(DeckDatabaseOpenHelper.DECK_ENCODING, deck.getEncoding());

		db.insert(DeckDatabaseOpenHelper.TABLE_NAME, null, values);
		db.close();		
	}
	
	public boolean deckExists(String deckName) {
		SQLiteDatabase db = getReadableDatabase();		
		Cursor cursor = db.query(
				DeckDatabaseOpenHelper.TABLE_NAME,			// table name
				new String[] { _ID, DECK_NAME },			// columns to return
				DeckDatabaseOpenHelper.DECK_NAME + "=?",	// selection
				new String[] { deckName },					// selection args
				null,										// groupBy
				null,										// having
				null);										// orderBy
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.close();
			db.close();
			return true;
		}
		return false;
	}
	
	public FlashCardDeck getDeck(String deckName) {
		SQLiteDatabase db = getReadableDatabase();
		FlashCardDeck deck = null;
		
		Cursor cursor = db.query(
				DeckDatabaseOpenHelper.TABLE_NAME,			// table name
				DeckDatabaseOpenHelper.columns,				// columns to return
				DeckDatabaseOpenHelper.DECK_NAME + "=?",	// selection
				new String[] { deckName },					// selection args
				null,										// groupBy
				null,										// having
				null);										// orderBy		
		try {
			if (cursor != null && cursor.moveToFirst()) {
				deck = new FlashCardDeck();
				initDeckFromCursor(deck, cursor);
				FlashCardApplication.getInstance().initDeckLoader(deck);
			}
		} finally {
			cursor.close();
			db.close();
		}		
		return deck;
	}
	
	public FlashCardDeck getDeck(int deckId) {
		FlashCardDeck deck = null;
		
		if (deckId != FlashCardDeck.DECK_ID_NOT_SET) {
			SQLiteDatabase db = getReadableDatabase();
			
			Cursor cursor = db.query(
					DeckDatabaseOpenHelper.TABLE_NAME,			// table name
					DeckDatabaseOpenHelper.columns,				// columns to return
					DeckDatabaseOpenHelper._ID + "=?",			// selection
					new String[] { String.valueOf(deckId) },	// selection args
					null,										// groupBy
					null,										// having
					null);										// orderBy		
			try {
				if (cursor != null && cursor.moveToFirst()) {
					deck = new FlashCardDeck();
					initDeckFromCursor(deck, cursor);
					FlashCardApplication.getInstance().initDeckLoader(deck);
				}
			} finally {
				cursor.close();
				db.close();
			}		
		}
		
		return deck;
	}
	
	private void initDeckFromCursor(FlashCardDeck deck, Cursor cursor) {
		if (cursor != null && deck != null) {
			deck.setId(cursor.getInt(DECK_ID_INDEX));
			deck.setName(cursor.getString(DECK_NAME_INDEX));
			deck.setVersion(cursor.getString(DECK_VERSION_INDEX));
			deck.setUri(cursor.getString(DECK_URI_INDEX));
			deck.setEncoding(cursor.getString(DECK_ENCODING_INDEX));
		}
	}
	
	public void updateDeck(FlashCardDeck deck) {
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(DeckDatabaseOpenHelper._ID, deck.getId());
		values.put(DeckDatabaseOpenHelper.DECK_NAME, deck.getName());
		values.put(DeckDatabaseOpenHelper.DECK_COUNT, deck.size());
		values.put(DeckDatabaseOpenHelper.DECK_VERSION, deck.getVersion());
		values.put(DeckDatabaseOpenHelper.DECK_URI, deck.getUri());
		values.put(DeckDatabaseOpenHelper.DECK_ENCODING, deck.getEncoding());

		db.replace(DeckDatabaseOpenHelper.TABLE_NAME, null, values);
		db.close();		
	}
	
	public void deleteDeck(FlashCardDeck deck) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(DeckDatabaseOpenHelper.TABLE_NAME,
				DeckDatabaseOpenHelper._ID + "=?",
				new String[] { String.valueOf(deck.getId()) });
		db.close();		
	}
	
	public ArrayList<FlashCardDeck>getAllDecks() {
        ArrayList<FlashCardDeck> deckList = new ArrayList<FlashCardDeck>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
 
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        if (cursor != null && cursor.moveToFirst()) {
        	try {
	            do {
	                FlashCardDeck deck = new FlashCardDeck();
	                initDeckFromCursor(deck, cursor);
	    			FlashCardApplication.getInstance().initDeckLoader(deck);
	                deckList.add(deck);
	            } while (cursor.moveToNext());
        	} catch(Exception e) {
        		e.printStackTrace();
        		deckList.clear();
        		deckList = null;
        	} finally {
		        cursor.close();
		        db.close();		
        	}
        }
        return deckList;
	}
	
	/**
	 * Return a count of the number of decks in the database.
	 * @param text
	 */
    public int getDeckCount() {
    	int count = -1;
        String countQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
