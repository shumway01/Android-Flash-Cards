/**
 * 
 */
package com.thehappypc.flashcards.data;

import java.util.Arrays;
import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;

import com.thehappypc.cards.Card;

/**
 * @author Steve Shumway
 *
 */
public class FlashCard extends Card implements Parcelable {
	
	// Constants that can be used as key names for various Bundle
	// and Parcelable operations
	public static String KEY_OBJECT = "FlashCard.Object";
	public static String KEY_ID = "FlashCard.Id";
	public static String KEY_QUESTION = "FlashCard.Question";
	public static String KEY_HINT = "FlashCard.Hint";
	public static String KEY_ANSWER = "FlashCard.Answer";
	public static String KEY_EXPLANATION = "FlashCard.Explanation";
	public static String KEY_TAGLIST = "FlashCard.TagList";
	public static String KEY_NUMTAGS = "FlashCard.NumTags";
	public static String KEY_TAGARRAY = "FlashCard.TagArray";
	
	// Member variables that comprise the meat of the card
	private String mQuestion;
	private String mHint;
	private String mAnswer;
	private String mExplanation;
	private TagList mTags;
	
	// Grading and scoring member variables
	private int mCorrectAnswers = 0;
	private int mIncorrectAnswers = 0;
	private int mGuessedAnswers = 0;
	private int mSkippedAnswers = 0;
	
	public enum FlashCardStatus {
		NOTVIEWED,
		SKIPPED,
		CORRECT,
		INCORRECT,
		GUESSED
	};
	private FlashCardStatus mStatus = FlashCardStatus.NOTVIEWED;
	
	public FlashCard() {
		mQuestion = mHint = mAnswer = mExplanation = null;
		mTags = new TagList();
	}
	
	public FlashCard(String question, String answer) {
		mQuestion = question;
		mAnswer = answer;
		mHint = mExplanation = null;
		mTags = new TagList();
	}
	
	public void setQuestion(String question) {
		mQuestion = question;
	}
	
	public String getQuestion()	{
		return mQuestion;
	}
	
	public void setHint(String hint) {
		mHint = hint != null ? hint : "";
	}

	public String getHint() {
		return mHint;
	}

	public void setAnswer(String answer) {
		mAnswer = answer;
	}
	
	public String getAnswer() {
		return mAnswer;
	}
	
	public void setExplanation(String explanation) {
		mExplanation = explanation != null ? explanation : "";
	}

	public String getExplanation() {
		return mExplanation;
	}
	
	/**
	 * Add a tag to the card's list of tags.
	 * @param tag
	 */
	public void addTag(String tag) {
		mTags.add(tag);
	}
	
	/**
	 * Adds the tags in the argument TagList to the card's list of tags.
	 * Any existing tags are retained, so the result of this operation is
	 * the superset of the old and new tags.
	 * @param tagList
	 */
	public void addTags(TagList tagList) {
		Iterator<String> it = tagList.iterator();
		while (it.hasNext())
			mTags.add(it.next());
	}
	
	public void removeTag(String tag) {
		mTags.remove(tag);
	}
	
	public void removeTags(TagList tagList) {
		mTags.removeAll(tagList);
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
	 * Return a [shallow] copy of the card's tags.
	 * @return
	 */
	public TagList cloneTags() {
		return (TagList) mTags.clone();
	}
		
	public void resetAnswerCounters() {
		mCorrectAnswers = 0;
		mGuessedAnswers = 0;
		mIncorrectAnswers = 0;
		mSkippedAnswers = 0;
	}
	
	/**
	 * Record a correct answer. If this is not the first time we've recorded
	 * an answer for this card, "undo" the previous record.
	 * 
	 */
	public void countCorrect() {
		switch (mStatus) {
		case NOTVIEWED:
			mCorrectAnswers++;
			break;
		case SKIPPED:
			mSkippedAnswers--;
			mCorrectAnswers++;
			break;
		case CORRECT:
			// card was already marked as answered correctly, so
			// do not increment/adjust counters
			break;
		case INCORRECT:
			mIncorrectAnswers--;
			mCorrectAnswers++;
			break;
		case GUESSED:
			mGuessedAnswers--;
			mCorrectAnswers++;
			break;
		default:
			break;
		}
		mStatus = FlashCardStatus.CORRECT;
	}
	
	/**
	 * Record an incorrect answer. If this is not the first time we've recorded
	 * an answer for this card, "undo" the previous record.
	 * 
	 */
	public void countIncorrect() {
		switch (mStatus) {
		case NOTVIEWED:
			mIncorrectAnswers++;
			break;
		case SKIPPED:
			mSkippedAnswers--;
			mIncorrectAnswers++;
			break;
		case CORRECT:
			mCorrectAnswers--;	// should have stuck with previous answer!
			mIncorrectAnswers++;
			break;
		case INCORRECT:
			// card was already marked as answered correctly, so
			// do not increment/adjust counters
			break;
		case GUESSED:
			mGuessedAnswers--;	// that's what you get for guessing!
			mIncorrectAnswers++;
			break;
		default:
			break;
		}
		mStatus = FlashCardStatus.INCORRECT;
	}
	
	/**
	 * Record a guessed answer. If this is not the first time we've recorded
	 * an answer for this card, "undo" the previous record.
	 * 
	 */
	public void countGuessed() {
		switch (mStatus) {
		case NOTVIEWED:
			mGuessedAnswers++;
			break;
		case SKIPPED:
			mSkippedAnswers--;
			mGuessedAnswers++;
			break;
		case CORRECT:
			mCorrectAnswers--;
			mGuessedAnswers++;
			break;
		case INCORRECT:
			mIncorrectAnswers--;
			mGuessedAnswers++;
			break;
		case GUESSED:
			// card was already marked as answered correctly, so
			// do not increment/adjust counters
			break;
		default:
			break;
		}
		mStatus = FlashCardStatus.GUESSED;
	}
	
	/**
	 * Record a skipped answer. If this is not the first time we've recorded
	 * an answer for this card, "undo" the previous record.
	 * 
	 */
	public void countSkipped() {
		switch (mStatus) {
		case NOTVIEWED:
			mSkippedAnswers++;
			break;
		case SKIPPED:
			// card was already marked as skipped, so
			// do not increment/adjust counters
			break;
		case CORRECT:
			mCorrectAnswers--;
			mSkippedAnswers++;
			break;
		case INCORRECT:
			mIncorrectAnswers--;
			mSkippedAnswers++;
			break;
		case GUESSED:
			mGuessedAnswers--;
			mSkippedAnswers++;
			break;
		default:
			break;
		}
		mStatus = FlashCardStatus.SKIPPED;
	}
	
	public int getCorrectAnswerCount() {
		return mCorrectAnswers;
	}
	
	public int getIncorrectAnswerCount() {
		return mIncorrectAnswers;
	}
	
	public int getGuessedAnswerCount() {
		return mGuessedAnswers;
	}
	
	public int getSkippedAnswserCount() {
		return mSkippedAnswers;
	}
	
	/**
	 * Set the status of the card. The status tracks whether the card has
	 * been displayed and the result of when it was last answered.
	 * @param status
	 */
	public void setStatus(FlashCardStatus status) {
		mStatus = status;
	}
	
	public FlashCardStatus getStatus() {
		return mStatus;
	}
	
	@Override
	public boolean equals(Card card) {
		boolean isEqual = super.equals(card) && card instanceof FlashCard;
		if (isEqual) {
			FlashCard flashCard = (FlashCard) card;
			isEqual = mAnswer.equals(flashCard.getAnswer()) &&
					  mQuestion.equals(flashCard.getQuestion()) &&
					  mHint.equals(flashCard.getHint()) &&
					  mExplanation.equals(flashCard.getExplanation());
			if (isEqual) {
				TagList otherTags = flashCard.cloneTags();
				isEqual = mTags.equals(otherTags);					
			}			
		}
		return isEqual;
	}

	// Parcelable methods
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(getId());
		dest.writeString(mQuestion);
		dest.writeString(mHint);
		dest.writeString(mAnswer);
		dest.writeString(mExplanation);
		dest.writeParcelable(mTags, flags);
	}

	public static final Parcelable.Creator<FlashCard> CREATOR
			= new Parcelable.Creator<FlashCard>() {
		public FlashCard createFromParcel(Parcel in) {
		    return new FlashCard(in);
		}
		
		public FlashCard[] newArray(int size) {
		    return new FlashCard[size];
		}
	};
		
	private FlashCard(Parcel in) {
		setId(in.readInt());
		mQuestion = in.readString();
		mHint = in.readString();
		mAnswer = in.readString();
		mExplanation = in.readString();
		mTags = in.readParcelable(TagList.class.getClassLoader());
	}
	
	public static FlashCard[] toFlashCards(Parcelable[] pArray) {
		FlashCard[] resultArray = null;
		if (pArray != null)
		    resultArray = Arrays.copyOf(pArray, pArray.length, FlashCard[].class);
		return resultArray;
	}
}
