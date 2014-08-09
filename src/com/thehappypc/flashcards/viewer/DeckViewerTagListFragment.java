package com.thehappypc.flashcards.viewer;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.adapters.TagListAdapter;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.flashcards.data.TagList;
import com.thehappypc.flashcards.listeners.OnTagsSelectedListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class DeckViewerTagListFragment extends DialogFragment {
	
	private AlertDialog		mDialog = null;
	
	private TagList			mTagList = null;
	private TagListAdapter	mTagAdapter = null;
	private String			mDeckName = null;
	
	private GridView		mTagView = null;
	
	private OnTagsSelectedListener	mOnTagsSelectedListener = null;
	
	public static DeckViewerTagListFragment newInstance(FlashCardDeck deck) {
		DeckViewerTagListFragment fragment = new DeckViewerTagListFragment();
		
		Bundle args = new Bundle();
		args.putParcelable(FlashCardDeck.KEY_TAGLIST, deck.cloneTags());
		args.putString(FlashCardDeck.KEY_NAME, deck.getName());
		fragment.setArguments(args);	// retained across create/destroy
		
		return fragment;
	}
	    	
    @SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	Bundle	args = getArguments();
    	mDeckName = args.getString(FlashCardDeck.KEY_NAME);
    	mTagList = (TagList) args.getParcelable(FlashCardDeck.KEY_TAGLIST);
    	
    	setRetainInstance(true);
    	
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.viewer_taglist_dialog, null);
        TextView textView = (TextView) v.findViewById(R.id.tagListText);
    	String msg = String.format(
    			getResources().getString(R.string.viewer_taglist_format),
    			mDeckName);        
        textView.setText(msg);
        
        mTagView = (GridView) v.findViewById(R.id.tagGridView);
		mTagView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

		mTagAdapter = new TagListAdapter(
				getActivity(),
				R.layout.tag_list_item,
				mTagList,
				null);
		mTagView.setAdapter(mTagAdapter);
		mTagView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(
						TagListAdapter.getCheckedTags(mTagView).size() > 0);
			}
			
		});
		
        builder.setView(v)
        	   .setTitle(R.string.viewer_taglist_title)
        	   .setPositiveButton(R.string.viewer_show_selected, new DialogInterface.OnClickListener() {
        		   @Override
                   public void onClick(DialogInterface dialog, int id) {
        			   mOnTagsSelectedListener.onTagsSelected(
        					   TagListAdapter.getCheckedTags(mTagView));
                   }
        	   })
               .setNegativeButton(R.string.viewer_show_all, new DialogInterface.OnClickListener() {
            	   @Override
            	   public void onClick(DialogInterface dialog, int id) {            	   
               	   }
               });
                        
        // Create the AlertDialog object and return it
        mDialog = builder.create();
        
        return mDialog;
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
    }
    
    public void setOnTagsSelectedListener(OnTagsSelectedListener listener) {
    	mOnTagsSelectedListener = listener;
    }
    
}
