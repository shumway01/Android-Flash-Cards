package com.thehappypc.flashcards.data;

import java.util.ArrayList;
import java.util.Collection;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A Parcelable ArrayList of String objects, in which duplicates are not allowed.
 * 
 * @author Steve
 *
 */
public class TagList extends ArrayList<String> implements Parcelable {
	
	private static final long serialVersionUID = 1L;
	
	public TagList() {
		super();
	}
	
	public TagList(String[] sArray) {
		fromArray(sArray);
	}
	
	/**
	 * Add a new tag to the TagList. 
	 * 
	 * @param tag The tag to add. If null or not unique, the method makes no changes.
	 * @return true if the TagList was modified
	 */
	@Override
	public boolean add(String tag) {
		boolean added = false;
		if (tag != null && !contains(tag))
			added = super.add(tag);
		return added;
	}
		
	/**
	 * Add a new tag to the TagList. 
	 * 
	 * @param tag The tag to add. If null or not unique, the method makes no changes.
	 * @return true if the TagList was modified
	 */
	@Override
	public void add(int index, String tag) {
		if (tag != null && !contains(tag))
			super.add(index, tag);
	}
		
	/**
	 * Add a Collection of tags to the TagList. Potential duplicates are removed
	 * from the argument Collection before it is added to the list.
	 * 
	 * @param collection The tags to add.
	 * @return true if the TagList was modified (in which case the listener is notified)
	 */
	@Override
	public boolean addAll(Collection<? extends String> collection) {
		boolean added = false;
		for (String s : collection) {
			if (add(s))
				added = true;	// assignment is cheaper than test, then assignment
		}
		return added;		
	}
	
	/**
	 * Add a Collection of tags to the TagList at the argument position. Potential
	 * duplicates are removed from the argument Collection before it is added to the list.
	 * 
	 * @param collection The tags to add.
	 * @return true if the TagList was modified
	 */
	@Override
	public boolean addAll(int index, Collection<? extends String> collection) {
		for (String s : collection)
			remove(s);
		return super.addAll(index, collection);
	}
	
	/**
	 * Add an array of Strings to the TagList. The list is cleared and replaced by the
	 * contents of the argument array. Duplicates are filtered out during the insertion
	 * process.
	 * 
	 * @param array The tags to add.
	 */
	public void fromArray(String[] array) {
		clear();
		for (String s : array)
			add(s);
	}
	
	// Parcelable methods
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		String[] sArray = new String[size()];
		toArray(sArray);
		dest.writeInt(sArray.length);
		dest.writeStringArray(sArray);
	}
	
    public static final Parcelable.Creator<TagList> CREATOR
    		= new Parcelable.Creator<TagList>() {
    	
		public TagList createFromParcel(Parcel in) {
		    return new TagList(in);
		}
		
		public TagList[] newArray(int size) {
		    return new TagList[size];
		}
	};
	
	private TagList(Parcel in) {
		int size = in.readInt();
		String[] sArray = new String[size];
		in.readStringArray(sArray);
		fromArray(sArray);
	}
	
}
