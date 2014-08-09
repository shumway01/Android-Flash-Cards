package com.thehappypc.flashcards.launcher;

import java.util.List;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.FlashCardDeck;
import com.thehappypc.ui.CheckableListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class LauncherAdapter extends ArrayAdapter<FlashCardDeck> {
	
	private Context mContext;
	private LayoutInflater mInflater;

	public LauncherAdapter(Context context, int textViewResourceId,
			List<FlashCardDeck> decks) {
		super(context, textViewResourceId, decks);

		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}
	
	/**
	 * Display an icon and name for the deck at the given position.
	 */
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		View newView = convertView;
		ViewHolder holder;

		if (null == convertView) {
			holder = new ViewHolder();
			newView = mInflater.inflate(R.layout.launcher_item, null);
			holder.checkBox = (CheckBox) newView.findViewById(R.id.launcherSelect);
			holder.iconView = (ImageView) newView.findViewById(R.id.launcherIcon);
			holder.nameView = (TextView) newView.findViewById(R.id.launcherName);			
			newView.setTag(holder);			
		} else {
			holder = (ViewHolder) newView.getTag();
		}
		
		FlashCardDeck deck = getItem(position);
		holder.nameView.setText(deck.getName());
		
		if (newView instanceof CheckableListItem)
			((CheckableListItem) newView).setItemPosition(position);
		
		return newView;
	}
	
	static class ViewHolder {	
		CheckBox checkBox;
		ImageView iconView;
		TextView nameView;
	}
}
