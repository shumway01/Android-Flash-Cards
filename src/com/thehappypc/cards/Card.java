/**
 * 
 */
package com.thehappypc.cards;

/**
 * @author Steve
 *
 */
public class Card {
	
	public static final int CARD_ID_NOT_SET = -1;
	
	private int			mId = CARD_ID_NOT_SET;
	
	public int getId() {
		return mId;
	}
	
	public void setId(int id) {
		mId = id;
	}
	
	/**
	 * Compare this card against another for equality. Subclasses should
	 * call super.equals when overriding this method.
	 * @param card
	 * @return true if the id members of the cards match, false otherwise.
	 */
	public boolean equals(Card card) {
		return mId == card.getId();
	}
}
