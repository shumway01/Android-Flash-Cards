package com.thehappypc.flashcards.storage;

import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;

import android.content.Context;

public class TestLoader extends DeckLoader {
	
	private final String[] questions = {
			"2+2", "2+3", "4x7", "5x9", "6-2", "7-9"		
	};
	
	private final String[] answers = {
			"4", "5", "28", "45", "4", "-2"
	};
	
	private final String[] tags = {
			"Addition", "Addition", "Multiplication", "Multiplication", "Subtraction", "Subtraction"
	};
	
	private final String DECK_NAME = "Elementary Math";
	
	private static TestLoader mSingleton = null;
	
	private TestLoader() {
		super();
	}
	
	public static TestLoader getInstance() {
		if (mSingleton == null)
			mSingleton = new TestLoader();
		return mSingleton;
	}
	
	public boolean init(Context context, FlashCardDeck deck) {
		deck.setLoader(this);
		return true;
	}

	@Override
	public boolean load(FlashCardDeck deck) {
		deck.setName(DECK_NAME);
		deck.setVersion("0.1");
		deck.setEncoding("XML");
		for (int i = 0; i < questions.length; i++) {
			FlashCard card = new FlashCard();
			
			card.setQuestion(questions[i]);
			card.setAnswer(answers[i]);
			card.addTag(tags[i]);
			deck.addTag(tags[i]);	// build taglist from superset of card tags
			
			deck.addCard(card);
		}
		return true;
	}

	@Override
	public boolean store(FlashCardDeck deck) {
		// Not implemented
		return true;
	}
	
	@Override
	public boolean delete(FlashCardDeck deck) {
		// Not implemented
		return true;
	}
}
