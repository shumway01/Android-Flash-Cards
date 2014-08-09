package com.thehappypc.flashcards.defunct;

import java.util.ArrayList;

import com.thehappypc.flashcards.R;
import com.thehappypc.flashcards.cardeditor.CardPropertiesFragment;
import com.thehappypc.flashcards.data.FlashCard;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CardViewPagerActivity extends FragmentActivity {
		
	private ViewPager				mViewPager;
	private CardPropertiesAdapter	mPropsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.card_props_pager);
		setContentView(mViewPager);
		
		mPropsAdapter = new CardPropertiesAdapter(this, mViewPager);
		mViewPager.setAdapter(mPropsAdapter);				
		mViewPager.setCurrentItem(0);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("card", mViewPager.getCurrentItem());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.deck_params_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		switch (item.getItemId()) {
		case R.id.menu_discard:
			finish();	// quit the Activity
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Adapter that populates the ViewPager of cards.
	 * @author Steve
	 *
	 */
	public static class CardPropertiesAdapter extends FragmentStatePagerAdapter
		implements ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ViewPager mViewPager;
		private final ArrayList<CardInfo> mCardInfoList = new ArrayList<CardInfo>();
		
		static class CardInfo {
			CardPropertiesFragment	fragment = null;
			int						position = -1;
			FlashCard				card = null;
		}
		
		public CardPropertiesAdapter(FragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
			
			CardInfo cardInfo = new CardInfo();
			cardInfo.position = 0;
			cardInfo.card = new FlashCard();
			mCardInfoList.add(cardInfo);
		}
		
		@Override
		public int getCount() {
			// Always allow swipe to commit last card and
			// create a new one.
			return mCardInfoList.size() + 1;
		}
		
		@Override
		public Fragment getItem(int position) {
			CardInfo cardInfo;
			if (position == mCardInfoList.size()) {
				cardInfo = new CardInfo();
				cardInfo.position = position;
				cardInfo.card = new FlashCard();
				mCardInfoList.add(cardInfo);
				notifyDataSetChanged();
			} else	
				cardInfo = mCardInfoList.get(position);
			/*
			if (cardInfo.fragment == null) {
				cardInfo.fragment = CardPropertiesFragment.newInstance(
												cardInfo.card,
												((FlashCardDeck) cardInfo.card.getDeck()).cloneTags());
				// cardInfo.fragment.displayFlashCardValues(cardInfo.card);
			}
			*/
			return cardInfo.fragment;
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}
		
		@Override
		public void onPageSelected(int position) {			
			Toast.makeText(mContext, "Page " + String.valueOf(position) + " Selected", Toast.LENGTH_SHORT).show();
			//mCardInfoList.get(position).fragment.doSaveAction();
		}
		
		@Override
		public void onPageScrollStateChanged(int state) {
		}
	}
}
