package com.thehappypc.flashcards.preferences;

import com.thehappypc.flashcards.FlashCardApplication;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

public class DeleteDatabaseDialogPreference extends DialogPreference {

	public DeleteDatabaseDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			FlashCardApplication.getInstance().deleteDatabase();
			Toast.makeText(getContext(), "Delete database", Toast.LENGTH_SHORT).show();
		}
		super.onDialogClosed(positiveResult);
	}

}
