package com.thehappypc.flashcards.storage;

import com.thehappypc.flashcards.data.FlashCardDeck;

public interface DeckLoaderListener {
	
	public void onFlashCardDeckLoaded(FlashCardDeck deck);

}
