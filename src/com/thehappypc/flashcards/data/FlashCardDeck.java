package com.thehappypc.flashcards.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.thehappypc.cards.Card;
import com.thehappypc.cards.CardDeck;
import com.thehappypc.flashcards.storage.DeckLoader;

public class FlashCardDeck extends CardDeck {
	
	// Constants that can be used as key names for various Bundle
	// and Parcelable operations
	public final static String KEY_ID = "FlashCardDeck.Id";
	public final static String KEY_OBJECT = "FlashCardDeck.Object";
	public final static String KEY_NAME = "FlashCardDeck.Name";
	public final static String KEY_URI = "FlashCardDeck.Uri";
	public final static String KEY_VERSION = "FlashCardDeck.Version";
	public final static String KEY_ENCODING = "FlashCardDeck.Encoding";
	public final static String KEY_NUMTAGS = "FlashCardDeck.NumTags";
	public final static String KEY_TAGARRAY = "FlashCardDeck.TagArray";
	public final static String KEY_TAGLIST = "FlashCardDeck.TagList";
	public final static String KEY_CARDARRAY = "FlashCardDeck.CardArray";
	
	private DeckLoader mLoader = null;
	
	public final static int	DECK_ID_NOT_SET = -1;
	
	private int			mId = -1;
	private String		mName = null;
	private String		mUri = null;
	private String		mVersion = null;
	private String		mEncoding = null;
	private TagList		mTags = null;
	
	private final static String mNullString = new String("");
	
	public FlashCardDeck() {
		mName = mUri = mVersion = mEncoding = mNullString;		
		mTags = new TagList();
	}
	
	public void load() {
		if (mLoader != null)
			mLoader.load(this);
	}
	
	public boolean store() {
		boolean stored = false;
		
		if (mLoader != null)
			stored = mLoader.store(this);
		
		return stored;
	}
	
	public void delete() {
		if (mLoader != null)
			mLoader.delete(this);
	}
	
	public DeckLoader getLoader() {
		return mLoader;
	}
	
	public void setLoader(DeckLoader loader) {
		mLoader = loader;
	}
	
	public void setId(int id) {
		if (mId != -1 && mId != id)
			throw new UnsupportedOperationException("FlashCardDeck id cannot be changed");
		mId = id;
	}

	public int getId() {
		return mId;
	}
	
	public void setName(String name) {
		mName = name == null ? mNullString : name;
	}
	
	public String getName()	{
		return mName;
	}	
	
	public void setUri(String uri) {
		if (uri != null)
			mUri = uri.replace(" ", "_");
		else
			mUri = mNullString;
	}
	
	public String getUri() {
		return mUri;
	}
	
	public void setVersion(String version) {
		mVersion = version == null ? mNullString : version;
	}
	
	public String getVersion() {
		return mVersion;
	}
	
	public void setEncoding(String encoding) {
		mEncoding = encoding == null ? mNullString : encoding;
	}
	
	public String getEncoding() {
		return mEncoding;
	}
	
	public void resetCounters() {
		Iterator<? extends Card> iterator = iterator();
		
		try {
			do {
				FlashCard card = (FlashCard) iterator.next();
				card.resetAnswerCounters();
			} while (true);
		} catch (NoSuchElementException e) {				
		}		
	}
	
	/**
	 * Add a tag to the deck's list of tags.
	 * @param tag
	 */
	public void addTag(String tag) {
		mTags.add(tag);
	}
	
	/**
	 * Adds the tags in the argument TagList to the deck's list of tags.
	 * Any existing tags are retained, so the result of this operation is
	 * the superset of the old and new tags.
	 * @param tagList
	 */
	public void addTag(Collection<? extends String> collection) {
		mTags.addAll(collection);
	}
	
	public void removeTag(String tag) {
		mTags.remove(tag);
	}
	
	public int countTags() {
		return mTags.size();
	}
	
	public void clearTags() {
		mTags.clear();
	}
	
	public boolean containsTag(String tag) {
		return mTags.contains(tag);
	}
	
	/**
	 * Return a [shallow] copy of the deck's tags.
	 * @return
	 */
	public TagList cloneTags() {
		return (TagList) mTags.clone();
	}
		
}