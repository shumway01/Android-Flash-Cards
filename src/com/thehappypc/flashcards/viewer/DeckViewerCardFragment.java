package com.thehappypc.flashcards.viewer;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.preferences.FlashCardsPreferencesActivity;

import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * The class implements the Fragment responsible for the display of one FlashCard in the
 * deck. It mediates between the question, hint, answer, and explanation views.
 * 
 * @author Steve
 *
 */
public class DeckViewerCardFragment extends Fragment {
	
	private static final String KEY_DISPLAYED_CHILD_TAG = "DeckViewerCardFragment.DisplayTag";
	private static final int QUESTION_TAG = 0;
	private static final int ANSWER_TAG = 1;
	
	boolean mScoringEnabled;
	boolean mHintsEnabled;
	boolean mExplanationsEnabled;
	
	private ViewFlipper mFlipper = null;
	
	private TextView mQuestionView = null;
	private TextView mAnswerView = null;
	
	private MenuItem mCorrectItem = null;
	private MenuItem mGuessItem = null;
	private MenuItem mWrongItem = null;
	private MenuItem mNextItem = null;
	private MenuItem mHintItem = null;
	private MenuItem mExplanationItem = null;
	
	private FlashCard mCard;
	
	public static DeckViewerCardFragment newInstance(FlashCard card) {
		DeckViewerCardFragment fragment = new DeckViewerCardFragment();
		
		Bundle args = new Bundle();
		args.putParcelable(FlashCard.KEY_OBJECT, card);
		fragment.setArguments(args);	// retained across create/destroy
		
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.viewer_card_fragment, container, false);
		View v;
		
		int displayedChildTag = -1;
		
		Bundle args = savedInstanceState != null ? savedInstanceState : getArguments();
		if (args != null) {
			mCard = args.getParcelable(FlashCard.KEY_OBJECT);
			displayedChildTag = args.getInt(KEY_DISPLAYED_CHILD_TAG);
		}
		if (displayedChildTag == -1)
			displayedChildTag = 0;
	
		mFlipper = (ViewFlipper) rootView.findViewById(R.id.qaSwitcherView);
		
		mQuestionView = (TextView) rootView.findViewById(R.id.questionView);
		mQuestionView.setText(mCard.getQuestion());
		
		// The ViewFlipper is showing a hierarchy rooted in a LinearLayout. Assign
		// a tag to that View so we can query the flipper to determine whether it
		// is showing the question side or the answer side.
		v = rootView.findViewById(R.id.questionChild);
		v.setTag(QUESTION_TAG);
		
		mAnswerView = (TextView) rootView.findViewById(R.id.answerView);
		mAnswerView.setText(mCard.getAnswer());
		v = rootView.findViewById(R.id.answerChild);	// See above
		v.setTag(ANSWER_TAG);
		
		mFlipper.setDisplayedChild(displayedChildTag);
		
		setHasOptionsMenu(true);
		
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(FlashCard.KEY_OBJECT, mCard);
		View displayedChild = mFlipper.getChildAt(mFlipper.getDisplayedChild());
		outState.putInt(KEY_DISPLAYED_CHILD_TAG, (Integer) displayedChild.getTag());
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.viewer_fragment_menu, menu);
		
		mCorrectItem = menu.findItem(R.id.menu_correct);
		mWrongItem = menu.findItem(R.id.menu_wrong);
		mGuessItem = menu.findItem(R.id.menu_guess);
		mNextItem = menu.findItem(R.id.menu_next);
		mHintItem = menu.findItem(R.id.menu_hint);
		mExplanationItem = menu.findItem(R.id.menu_explain);
		
		setMenuItemVisibility();
	}
	
	@Override
	public void onResume() {
		// Set states of scoring items as user may have gone to
		// Preferences and then come back here
		// Shuffle the deck if auto-shuffle is on
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mScoringEnabled = sharedPref.getBoolean(
					FlashCardsPreferencesActivity.KEY_PREF_ENABLE_SCORING, false);
		mHintsEnabled = sharedPref.getBoolean(
				FlashCardsPreferencesActivity.KEY_PREF_ENABLE_HINTS, false);		
		mExplanationsEnabled = sharedPref.getBoolean(
				FlashCardsPreferencesActivity.KEY_PREF_ENABLE_EXPLANATIONS, false);
		
		setMenuItemVisibility();
		
		super.onResume();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;
		
		switch (item.getItemId()) {
		case R.id.menu_flip:
			// NB: If we ever have more then the question and answer views
			// to display this will need to be modified...
			if (getDisplayedChildTag() == QUESTION_TAG)
				mFlipper.setDisplayedChild(ANSWER_TAG);
			else
				mFlipper.setDisplayedChild(QUESTION_TAG);
			setMenuItemVisibility();			
			handled = true;
			break;
		case R.id.menu_hint:
			showHintOrExplanationDialog(R.string.hint_title, mCard.getHint());
			handled = true;
			break;
		case R.id.menu_explain:
			showHintOrExplanationDialog(R.string.explanation_title, mCard.getExplanation());
			handled = true;
			break;
		}
		
		return handled;		
	}
	
	/**
	 * This method is overridden as something of a hack to handle the
	 * case in which the user is scrolling backwards through questions
	 * already viewed. In this case, we want to display the question,
	 * not the answer (which is what the ViewPager/Adapter combo will
	 * show as it reuses the fragment). There is no other Fragment life-
	 * cycle method I could find where I could intercept this event.
	 */
	@Override
	public void setUserVisibleHint(boolean isVisible) {
		super.setUserVisibleHint(isVisible);
		if (!isVisible && mFlipper != null)
			mFlipper.setDisplayedChild(QUESTION_TAG);			
	}
	
	// Helper methods
	
	private int getDisplayedChildTag() {
		int	displayedChild = mFlipper.getDisplayedChild();
		View child = mFlipper.getChildAt(displayedChild);
		return (Integer) child.getTag();
	}
	
	private void setMenuItemVisibility() {
		if (mCorrectItem == null)
			return;	// menus not created yet
		
		switch (getDisplayedChildTag()) {
		case QUESTION_TAG:
			mHintItem.setVisible(mHintsEnabled && mCard.getHint().length() > 0);
			mExplanationItem.setVisible(false);
			mCorrectItem.setVisible(false);
			mGuessItem.setVisible(false);
			mWrongItem.setVisible(false);
			mNextItem.setVisible(false);
			break;
		case ANSWER_TAG:
			mExplanationItem.setVisible(mExplanationsEnabled && mCard.getExplanation().length() > 0);
			mHintItem.setVisible(false);
			if (mScoringEnabled) {
				mCorrectItem.setVisible(true);
				mGuessItem.setVisible(true);
				mWrongItem.setVisible(true);
				mNextItem.setVisible(false);
			} else {
				mCorrectItem.setVisible(false);
				mGuessItem.setVisible(false);
				mWrongItem.setVisible(false);
				mNextItem.setVisible(true);			
			}
			break;
		}				
	}
	
	private void showHintOrExplanationDialog(int titleResource, String msgText) {
	    // Use the Builder class for convenient dialog construction
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    
	    builder.setTitle(titleResource)
	    		.setMessage(msgText)
	    		.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   dialog.dismiss();
	              }
	            });
	    
	    // Create and show the AlertDialog object
	    builder.create().show();
	}
}
