package com.thehappypc.flashcards.cardeditor;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.adapters.TagListAdapter;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.data.TagList;
import com.thehappypc.flashcards.preferences.FlashCardsPreferencesActivity;
import com.thehappypc.util.Committable;
import com.thehappypc.util.Validateable;

import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Fragment that displays the properties of an individual card. There are three ways to
 * instantiate this Fragment.
 * 	1) Call the class newInstance() method passing a card and TagList as arguments
 *  2) Call Fragment.instantiate with an argument Bundle containing two Parcelables:
 *  	a TagList of available tags and a FlashCard
 *  3) Create the fragment passing in savedInstanceState, containing the two Parcelables
 *  	mentioned above
 * The Fragment is responsible for displaying the data contained in the argument card
 * and for performing error checking on the fields of the UI when its validate()
 * hook method is called.
 * 
 * @author Steve
 *
 */
public class CardPropertiesFragment extends Fragment implements Validateable, Committable {
	
	private TagListAdapter mTagAdapter; 	// Deck's tag list, wrapped by an Adapter
	
	// Local reference to a FlashCard, which will be used as a container for
	// the values in the UI. The card is built as a Parcelable and is never
	// seen by our calling activity (see commit()).
	private FlashCard mCard = null;
	private TagList mAvailableTags = null;
	private TagList mSelectedTags = null;
	
	private TextView mQuestionView;
	private TextView mHintView;
	private TextView mAnswerView;
	private TextView mExplanationView;
	private GridView mTagGridView;
	
	private View mHintContainer;
	private View mExplanationContainer;
	
	public static CardPropertiesFragment newInstance(FlashCard card, TagList availableTags) {
		CardPropertiesFragment fragment = new CardPropertiesFragment();
		
		Bundle args = new Bundle();
		args.putParcelable(FlashCard.KEY_OBJECT, card);
		args.putParcelable(FlashCardDeck.KEY_TAGLIST, availableTags);
		fragment.setArguments(args);	// retained across create/destroy

		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.card_properties_fragment, container, false);
		
		Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		if (bundle != null) {
			mCard = (FlashCard) bundle.getParcelable(FlashCard.KEY_OBJECT);
			mAvailableTags = (TagList) bundle.getParcelable(FlashCardDeck.KEY_TAGLIST);
			mSelectedTags = (TagList) bundle.getParcelable(FlashCard.KEY_TAGLIST);
		}
		if (mCard == null)
			mCard = new FlashCard();
		if (mAvailableTags == null)
			mAvailableTags = new TagList();
		if (mSelectedTags == null)
			mSelectedTags = mCard.cloneTags();
		
		mQuestionView = (TextView) rootView.findViewById(R.id.questionEditText);
		mHintView = (TextView) rootView.findViewById(R.id.hintEditText);
		mAnswerView = (TextView) rootView.findViewById(R.id.answerEditText);
		mExplanationView = (TextView) rootView.findViewById(R.id.explanationEditText);
		mTagGridView = (GridView) rootView.findViewById(R.id.tagGridView);
		
		mTagGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		
		// Cache the layout containers (children are a TextView, a divider View, and
		// an EditTextView for each, so we can display/hide them according to Settings
		mHintContainer = rootView.findViewById(R.id.hintContainer);
		mExplanationContainer = rootView.findViewById(R.id.explanationContainer);	
		
		displayFlashCardValues(mCard);
		
		mTagAdapter = new TagListAdapter(
									getActivity(),
									R.layout.tag_list_item,
									mAvailableTags,
									mSelectedTags);
		mTagGridView.setAdapter(mTagAdapter);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		SharedPreferences sharedPref =
				PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		// Only show Hints if enabled in Settings
		boolean enableHints = sharedPref.getBoolean(
					FlashCardsPreferencesActivity.KEY_PREF_ENABLE_HINTS, false);
		mHintContainer.setVisibility(enableHints ? View.VISIBLE : View.GONE);
		
		// Only show Explanations if enabled in Settings
		boolean enableExplanations = sharedPref.getBoolean(
					FlashCardsPreferencesActivity.KEY_PREF_ENABLE_EXPLANATIONS, false);
		mExplanationContainer.setVisibility(enableExplanations ? View.VISIBLE : View.GONE);	
		
		super.onResume();		
	}
	
	/**
	 * Save the state of the UI in a Bundle
	 */
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		saveFlashCardValues(mCard);
		bundle.putParcelable(FlashCard.KEY_OBJECT, mCard);
		bundle.putParcelable(FlashCardDeck.KEY_TAGLIST,	mAvailableTags);
		bundle.putParcelable(
					FlashCard.KEY_TAGLIST,
					TagListAdapter.getCheckedTags(mTagGridView));
	}
	
	/**
	 * Validate hook method. Ensure the user has entered the two required
	 * text fields: the question and the answer.
	 */
	@Override
	public boolean validate() {
		String question = mQuestionView.getText().toString();
		if (question == null || question.length() == 0) {
			Toast.makeText(getActivity(), R.string.error_missing_question, Toast.LENGTH_LONG).show();
			return false;
		}
		
		String answer = mAnswerView.getText().toString();
		if (answer == null || answer.length() == 0) {
			Toast.makeText(getActivity(), R.string.error_missing_answer, Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}

	/**
	 * Commit hook method. The argument must be the FlashCard into which
	 * the values the user entered will be stored. The member variable mCard
	 * is not passed up to the calling activity.
	 */
	@Override
	public boolean commit(Object arg) {
		saveFlashCardValues((FlashCard)arg);
		return true;
	}
	
	/**
	 * Retrieve the values from the argument FlashCard and populate the
	 * fields of the user interface
	 * @param card
	 */
	private void displayFlashCardValues(FlashCard card) {
		String s;
		if ((s = card.getQuestion()) != null)
			mQuestionView.setText(s);
		if ((s = card.getHint()) != null)
			mHintView.setText(s);
		if ((s = card.getAnswer()) != null)
			mAnswerView.setText(s);
		if ((s = card.getExplanation()) != null)
			mExplanationView.setText(s);
	}
	
	/**
	 * Retrieve the values from the user interface and populate the
	 * argument FlashCard.
	 * @param card
	 */
	private void saveFlashCardValues(FlashCard card) {
		card.setQuestion(mQuestionView.getText().toString());
		card.setAnswer(mAnswerView.getText().toString());
		card.setHint(mHintView.getText().toString());
		card.setExplanation(mExplanationView.getText().toString());
		card.clearTags();
		card.addTags(TagListAdapter.getCheckedTags(mTagGridView));
	}

}
