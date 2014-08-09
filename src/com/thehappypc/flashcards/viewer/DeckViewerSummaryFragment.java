package com.thehappypc.flashcards.viewer;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.flashcards.data.FlashCardDeck;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DeckViewerSummaryFragment extends DialogFragment {
	
	private TextView mIncorrectView;
	private TextView mCorrectView;
	private TextView mSkippedView;
	private TextView mGuessView;
	
	private FlashCard[] mCardArray = null;
	
	public static DeckViewerSummaryFragment newInstance(FlashCard[] array) {
		DeckViewerSummaryFragment fragment = new DeckViewerSummaryFragment();
		
		Bundle args = new Bundle();
		args.putParcelableArray(FlashCardDeck.KEY_CARDARRAY, array);
		fragment.setArguments(args);	// retained across create/destroy
		
		return fragment;
	}
	    	
    @SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	Bundle args = getArguments();
    	mCardArray = (FlashCard[]) args.getParcelableArray(FlashCardDeck.KEY_CARDARRAY);
    	
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.viewer_summary_dialog, null);
        builder.setView(v)
        	   .setTitle(R.string.summary_title)
        	   .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   getActivity().finish();
                  }
                });
        
        mCorrectView = (TextView) v.findViewById(R.id.correctValue);
        mIncorrectView = (TextView) v.findViewById(R.id.incorrectValue);
        mGuessView = (TextView) v.findViewById(R.id.guessValue);
        mSkippedView = (TextView) v.findViewById(R.id.skippedValue);   
        
        updateCounts();
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
    
    private void updateCounts() {
        int correct = 0;
        int incorrect = 0;
        int skipped = 0;
        int guessed = 0;
        
        for (FlashCard card : mCardArray) {        	
        	switch (card.getStatus()) {
        	case CORRECT:
        		correct++;
        		break;
        	case INCORRECT:
        		incorrect++;
        		break;
        	case SKIPPED:
        		skipped++;
        		break;
        	case GUESSED:
        		guessed++;
        		// fallthrough
         	case NOTVIEWED:
       		default:
       			break;
        	}
        }
        mCorrectView.setText(String.valueOf(correct));
        mIncorrectView.setText(String.valueOf(incorrect));
        mGuessView.setText(String.valueOf(guessed));
        mSkippedView.setText(String.valueOf(skipped));            	
    }
}
