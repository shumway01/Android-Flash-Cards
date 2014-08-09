package com.thehappypc.flashcards.listeners;

import com.thehappypc.flashcards.data.TagList;

/**
 * Interface implemented by objects that need to be notified when a
 * set of tags is selected.
 * 
 * @author Steve
 *
 */
public interface OnTagsSelectedListener {

	public abstract void onTagsSelected(TagList tagList);
}
