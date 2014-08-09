package com.thehappypc.flashcards.listeners;

import com.thehappypc.flashcards.data.FlashCard;

/**
 * Interface implemented by objects that need to be notified in a change
 * in the composition of a set of FlashCards.
 * 
 * @author Steve
 *
 */
public interface OnCardListChangedListener {

	public abstract void onFlashCardListChanged(FlashCard[] cards);
}
