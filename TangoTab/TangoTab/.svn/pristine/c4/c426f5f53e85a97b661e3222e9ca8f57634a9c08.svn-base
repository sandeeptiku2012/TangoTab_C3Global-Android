package com.tangotab.nearMe.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;
/**
 * Gallery used  for displaying dinning style.
 * @author dillip.lenka
 *
 */
public class NearMeGallery extends Gallery{

	int mSelection = 0;

	public NearMeGallery(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
	   return e2.getX() < e1.getX();
	}

	private boolean isScrollingRight(MotionEvent e1, MotionEvent e2) {
	   return e2.getX() > e1.getX();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	       float velocityY) {
	 boolean leftScroll = isScrollingLeft(e1, e2);
	   boolean rightScroll = isScrollingRight(e1, e2);

	   if (rightScroll) {
	       if (mSelection != 0)             
	           setSelection(--mSelection, true);
	   } else if (leftScroll) {

	       if (mSelection != getCount() - 1)
	           setSelection(++mSelection, true);
	   }
	   return false;
	}




}
