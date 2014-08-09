package com.thehappypc.flashcards.adapters;

import java.text.Collator;
import java.util.Iterator;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.data.TagList;
import com.thehappypc.ui.CheckableListItem;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * An Adpater that displays a list of tag names and check boxes, enabling the user
 * to select from a defined set of tags.
 * 
 * @author Steve
 *
 */
public class TagListAdapter extends ArrayAdapter<String> {
	
	private Context mContext;
	private LayoutInflater mInflater;
	
	private TagList	mTagList;		// the data source
	private TagList mSelectedTags;	// initially-selected tags (can be null)
	
	private Collator mCollator = Collator.getInstance();
	
	public TagListAdapter(Context context, int resource, TagList tagList, TagList selectedTags) {	
		super(context, resource, tagList);
		
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mTagList = tagList;
		mSelectedTags = selectedTags;
		sortTags();
	}
	
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View newView = convertView;
		ViewHolder holder;

		String tag = (String) getItem(position);

		if (null == convertView) {
			holder = new ViewHolder();
			newView = mInflater.inflate(R.layout.tag_list_item, parent, false);
			holder.checkBox = (CheckBox) newView.findViewById(R.id.tagCheckBox);
			holder.tagName = (TextView) newView.findViewById(R.id.tagTextView);
			if (newView instanceof CheckableListItem) {
				CheckableListItem checkableView = (CheckableListItem) newView;
				checkableView.setItemPosition(position);
				checkableView.setChecked(
						mSelectedTags != null && mSelectedTags.contains(tag));
			}			
			newView.setTag(holder);			
		} else {
			holder = (ViewHolder) newView.getTag();
			if (newView instanceof CheckableListItem)
				((CheckableListItem) newView).setItemPosition(position);
		}
		
		holder.tagName.setText(tag);
		
		return newView;
	}
	
	static class ViewHolder {	
		CheckBox checkBox;
		TextView tagName;
	}
	
	// In order for checked items to be rendered properly in the face of insertions
	// and deletions, we need to ensure the adapter has stable ids. We can't use
	// position as the position of any given tag may change, so use the item/tag's
	// hash code.
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}
	
	/**
	 * Populate the adapter with the tag names contained in the argument TagList.
	 * @param tagList
	 */
	public void sortTags() {
		setNotifyOnChange(false);
		sort(mCollator);
		notifyDataSetChanged();
	}
	
	/**
	 * Return a copy of the Adapter's tags.
	 * @return A shallow copy of the Adapter's data source (TagList).
	 */
	public TagList getTags() {
		return (TagList) mTagList.clone();
	}
	
	@Override
	public void add(String tag) {
		// Sanity check arg
		if (tag == null || tag.length() == 0)
			return;
		
		Iterator<String> it = mTagList.iterator();
		int		position = 0;
		boolean	done = false;
		
		while (!done && it.hasNext()) {
			String listElement = it.next();
			int	result = mCollator.compare(listElement, tag);
			if (result == 0) {
				// Duplicate entry -- ignore it
				done = true;
			} else if (result > 0) {
				// Our TagList is kept in ascending sorted order, so the first
				// list element we find that compares greater than the tag to
				// be inserted indicates our insertion position.
				super.insert(tag, position);
				done = true;
			}
			position++;	// could use else, but just incrementing is cheaper...
		}
		if (!done)
			// New tag compares greater than last entry, so add it to the end
			super.add(tag);
	}
	
	/**
	 * Convenience method to return a TagList containing all checked tags. This
	 * should probably be a method attached to an instance of the adapter, but
	 * the most expedient way of getting this information is through the ListView
	 * to which the adapter is attached. Rather than duplicate this functionality
	 * in each fragment that uses one of these adapters (there are currently at least
	 * three such places), it made more sense to put it here as a class method.
	 * 
	 * @param listView The ListView containing the items that may be checked.
	 * @return A TagList containing only the checked items.
	 */
    static public TagList getCheckedTags(AbsListView listView) {
    	TagList checkedTags = new TagList();
    	SparseBooleanArray itemPositions = listView.getCheckedItemPositions();
    	int	positions = listView.getCheckedItemCount();
    	
    	for (int i = 0; i < listView.getCount() && positions > 0; i++) {
    		if (itemPositions.get(i)) {
    			Object o = listView.getItemAtPosition(i);
    			if (o instanceof String)
    				checkedTags.add((String) o);
    			positions--;	// optimization -- done when all checked have been counted
    		}
    	}
    	return checkedTags;
    }
    
}