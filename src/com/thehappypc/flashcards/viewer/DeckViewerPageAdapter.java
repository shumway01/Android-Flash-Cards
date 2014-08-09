/**
 * 
 */
package com.thehappypc.flashcards.viewer;

import com.thehappypc.flashcards.data.FlashCard;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * @author Steve
 *
 */
public class DeckViewerPageAdapter extends FragmentStatePagerAdapter {
	
	private FlashCard[] mCardArray;

	public DeckViewerPageAdapter(FragmentManager fm, FlashCard[] array) {
		super(fm);
		mCardArray = array;
	}

    /* (non-Javadoc)
	 * @see android.support.v4.app.FragmentStatePagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int index) {
		return DeckViewerCardFragment.newInstance(mCardArray[index]);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCardArray.length;
	}

}
