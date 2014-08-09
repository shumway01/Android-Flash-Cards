/**
 * 
 */
package com.thehappypc.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * A class that implements a deck of cards. Cards may be added to and deleted from the
 * deck. The top card can be retrieved from the deck.
 * 
 * @author Steve Shumway
 *
 */
public class CardDeck {
	
	private int mNextCardId = 0;
	
	private ArrayList<Card> mCards;
	
	/**
	 * Default constructor.
	 */
	public CardDeck() {		
		mCards = new ArrayList<Card>();
	}
	
	/**
	 * Constructor which allocates a deck with an initially-specified capacity.
	 * @param The size of the deck.
	 */
	public CardDeck(int size) {		
		mCards = new ArrayList<Card>(size);
	}
	
	/**
	 * Return the card at the argument position.
	 * @param position
	 * @return null if the position exceeds the number of items
	 */
	public Card get(int position) {
		return mCards.get(position);
	}
	
	/**
	 * Shuffles the deck of cards.
	 */
	public void shuffle() {		
		Collections.shuffle(mCards);
	}	
	
	/**
	 * Shuffle the deck, preserving its original order. The shuffled cards are
	 * returned by filling in the argument array, which must be at least CardDeck.size()
	 * items in length.
	 * @param array An array of Card objects, which should be the same size
	 * as the deck.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Card> void shuffle(T[] cards) {
		ArrayList<Card> cardCopy = null;
		try {
			cardCopy = (ArrayList<Card>) mCards.clone();
		} catch (ClassCastException e) {
		}
		if (cardCopy != null && cards.length >= mCards.size()) {
			Collections.shuffle(cardCopy);
			cardCopy.toArray(cards);
		}
	}
	
	/**
	 * Add a card to the deck.
	 * @param card
	 * @return true if the card was added, false otherwise.
	 */
	public boolean addCard(Card card) {
		if (card.getId() == Card.CARD_ID_NOT_SET)
			card.setId(generateId());
		return mCards.add(card);
	}
	
	/**
	 * Add a Collection of cards to the deck.
	 * @param card
	 * @return true if the card was modify, false otherwise.
	 */
	public boolean addCards(Collection<? extends Card> list) {
		boolean ret = false;
		for (Card card : list) {
			if (addCard(card) == true)
				ret = true;
		}
		return ret;		
	}

	/**
	 * Remove the specified card from the deck.
	 * @param card
	 * @return true if the card was removed, false otherwise.
	 */
	public boolean removeCard(Card card) {
		return mCards.remove(card);
	}
	
	/**
	 * Remove the card at the specified position from the deck.
	 * @param position
	 * @return The removed card.
	 */
	public Card removeCard(int position) {
		return mCards.remove(position);
	}

	/**
	 * Remove all cards from the deck.
	 * @param
	 * @return
	 */
	public void removeAllCards() {
		mCards.clear();
	}

	/**
	 * Return an Iterator that can be used to iterator over the cards
	 * in the deck. It is strongly encouraged that you treat the deck
	 * as read-only when using the iterator.
	 * @param
	 * @return
	 */
	public Iterator<Card> iterator() {
		return mCards.iterator();
	}
	
	public int size() {
		return mCards.size();
	}

	/**
	 * Return a copy of the deck of cards, as an Array of Card objects.
	 * @param contents
	 * @return An Array of Card objects.
	 */
	public <T extends Card> T[] getCardArray(T[] contents) {		
		return (mCards.toArray(contents));
	}
	
	/**
	 * Initialize the deck from the argument array of Card objects.
	 * @param cards
	 */
	protected <T extends Card> void setCards(T[] cards) {
		removeAllCards();
		mCards.addAll(Arrays.asList(cards));
		for (Card card : mCards) {
			if (card.getId() == Card.CARD_ID_NOT_SET)
				card.setId(generateId());
		}
	}
	
	private int generateId() {
		return mNextCardId++;
	}
}
