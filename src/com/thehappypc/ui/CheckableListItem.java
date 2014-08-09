package com.thehappypc.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;

/**
 * This class creates a RelativeLayout that is Checkable. It is intended to be used as
 * the basis for items containing CheckBoxes in any implementation of AbsListView, such
 * as a ListView or a GridView. It can be inflated from an XML file; when doing so,
 * it will automatically connect itself to a CheckBox and an AbsListView by traversing
 * its children and parents, respectively. You must however call setItemPosition from
 * an external source in order to establish the position of the list item within the list.
 * 
 * @author Steve
 *
 */
public class CheckableListItem extends RelativeLayout implements Checkable {
	
	private CheckBox	mCheckBox;	
	private AbsListView mListView;	// our parent
	
	static private final int	NO_POSITION = -1;
	// our position within the GridView/Adapter, set by calling setItemPosition
	private int 				mPosition = NO_POSITION;
	
	private boolean				mSetCheckedOnAttach = false;

	public CheckableListItem(Context context) {
		super(context);
	}
	
	public CheckableListItem(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}
	
	public CheckableListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
	}
	
	/**
	 * Record the item's position within the list. The list's adapter is not
	 * available to us given the level of abstraction used by this class; otherwise
	 * we could determine this automatically.
	 * 
	 * @param position
	 */
	public void setItemPosition(int position) {
		mPosition = position;
	}
	
	/**
	 * We override this method to automatically traverse up the parent hierarchy
	 * until we find our containing AbsListView, or run out of parents. This method
	 * was chosen because we have been attached to our parent's hierarchy when it
	 * is called.
	 */
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		ViewParent p = getParent();
		while (mListView == null && p != null) {
			if (p instanceof AbsListView)
				mListView = (AbsListView) p;
			else
				p = p.getParent();
		}
		// If setChecked was called before we were attached,
		// finish that processing now.
		if (mSetCheckedOnAttach)
			setChecked(mSetCheckedOnAttach);
		// For all modes other than MODAL (i.e., clicking on check box starts
		// a contextual action mode), make the check box ignore clicks so that
		// clicking anywhere on the item is equivalent to clicking the check box.
		if (mListView.getChoiceMode() != AbsListView.CHOICE_MODE_MULTIPLE_MODAL)
			mCheckBox.setClickable(false);
	}
	
	/**
	 * We override this method to traverse our child hierarchy and maintain a
	 * reference to the first (should be only!) CheckBox we find.
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mCheckBox = findCheckBox(this);
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				setChecked(isChecked);		
			}			
		});
	}
	
	@Override
	public void setChecked(boolean checked) {
		if (mCheckBox.isChecked() != checked)
			mCheckBox.setChecked(checked);
        setBackgroundColor(android.R.attr.activatedBackgroundIndicator);
        if (mPosition != NO_POSITION && mListView != null) {
        	mListView.setItemChecked(mPosition, checked);
        } else
        	mSetCheckedOnAttach = checked;
	}

	@Override
	public boolean isChecked() {
		return mCheckBox.isChecked();
	}

	@Override
	public void toggle() {
        setChecked(!isChecked());
	}
	
	/**
	 * Recursive class helper function that returns the single CheckBox from a
	 * tree of Views. The traversal is depth-first.
	 * 
	 * @param The root of the hierarchy to search
	 * @return The [first] CheckBox View in the hierarchy, or null if none was found.
	 */
	private static CheckBox findCheckBox(View v) {
		CheckBox checkBox = null;
		if (v instanceof CheckBox)
			checkBox = (CheckBox) v;
		else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount() && checkBox == null; i++)
				checkBox = findCheckBox(vg.getChildAt(i));				
		}
		return checkBox;		
	}
}
