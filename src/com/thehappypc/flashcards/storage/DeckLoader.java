package com.thehappypc.flashcards.storage;

import com.thehappypc.flashcards.data.FlashCardDeck;

import android.content.Context;

public abstract class DeckLoader {
	
	private DeckLoaderListener mListener = null;
	
	public abstract boolean init(Context context, FlashCardDeck deck);
	public abstract boolean load(FlashCardDeck deck);
	public abstract boolean store(FlashCardDeck deck);
	public abstract boolean delete(FlashCardDeck deck);
	
	public void setFlashCardDeckLoaderListener(
			DeckLoaderListener listener) {
		mListener = listener;		
	}
	
	public DeckLoaderListener getFlashCardDeckLoaderListener() {
		return mListener;
	}
	
}
