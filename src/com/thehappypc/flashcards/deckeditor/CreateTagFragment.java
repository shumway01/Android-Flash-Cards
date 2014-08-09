package com.thehappypc.flashcards.deckeditor;

import com.thehappypc.flashcards.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

public class CreateTagFragment extends DialogFragment {
	
	private EditText mNewTagView;
	
    /* The UI object that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     */
    public interface CreateTagDialogListener {
        public void onDialogPositiveClick(String newTagName);
    }
        
    // Use this instance of the interface to deliver action events
    CreateTagDialogListener mListener;
    
    public static CreateTagFragment newInstance() {
    	return new CreateTagFragment();
    }
    
    public void setCreateTagDialogListener(CreateTagDialogListener listener) {
        mListener = listener;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Pass null as the parent view because its going in the dialog layout
        View v = getActivity().getLayoutInflater().inflate(R.layout.create_tag_dialog, null);
        mNewTagView = (EditText) v.findViewById(R.id.newTagView);
       
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());      
        builder.setView(v)
			.setTitle(R.string.create_tag)
			.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
						Editable editableName = mNewTagView.getText();
						if (mListener != null && editableName != null)
							mListener.onDialogPositiveClick(editableName.toString());
				}
           })
           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   // User cancelled the dialog
               }
           });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
