/**
 * 
 */
package com.thehappypc.flashcards.deckeditor;

import java.util.ArrayList;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.FlashCard;
import com.thehappypc.ui.CheckableListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * DeckAdapter is used to display a two-line View containing the question and answer of a card,
 * combined with a CheckBox that allows for batch operations on the list.
 * 
 * @author Steve
 *
 */
public class CardListAdapter extends ArrayAdapter<FlashCard> {
	
	private LayoutInflater mInflater = null;
	private Context mContext;
	
	public CardListAdapter(Context context, int resource, ArrayList<FlashCard> cards) {
		super(context, resource, cards);
		
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View newView = convertView;
		ViewHolder holder;

		FlashCard card = (FlashCard) getItem(position);

		if (null == convertView) {
			holder = new ViewHolder();
			newView = mInflater.inflate(R.layout.card_list_item, null);
			holder.checkBox = (CheckBox) newView.findViewById(R.id.listCheckBox);
			holder.question = (TextView) newView.findViewById(R.id.questionListView);
			holder.answer = (TextView) newView.findViewById(R.id.answerListView);
			newView.setTag(holder);			
		} else {
			holder = (ViewHolder) newView.getTag();
		}

		holder.question.setText("Q: " + card.getQuestion());
		holder.answer.setText("A: " + card.getAnswer());

		if (newView instanceof CheckableListItem)
			((CheckableListItem) newView).setItemPosition(position);
		
		return newView;
	}
	
	static class ViewHolder {	
		CheckBox checkBox;
		TextView question;
		TextView answer;
	}
}
