package com.thehappypc.flashcards.viewer;

import com.thehappypc.flashcards.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DeckViewerFinishFragment extends DialogFragment {
	
    @SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        builder.setMessage(R.string.finish_text)
        	   .setTitle(R.string.finish_title)
        	   .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   getActivity().finish();
                  }
                });
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
    
}
