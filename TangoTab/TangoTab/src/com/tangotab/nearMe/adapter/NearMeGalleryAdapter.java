package com.tangotab.nearMe.adapter;

import com.tangotab.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
/**
 * Gallery adapter used for scrolling the Casual and Upscale.
 * 
 * @author dillip.lenka
 *
 */
public class NearMeGalleryAdapter extends BaseAdapter
{
	private Context ctx;
	int[] pics= {R.drawable.allselector,R.drawable.casual_selector,R.drawable.upscale_selector};
	
	public NearMeGalleryAdapter(Context c,int[] pics) {
		ctx = c;
		this.pics=pics;
		
	}

	@Override
	public int getCount() {
		return pics.length;
	}

	@Override
	public Object getItem(int arg0) {
		
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2)
	{
		ImageView iv = new ImageView(ctx);
		iv.setImageResource(pics[arg0]);
		System.out.println("GALLERYADASPER"+pics[arg0]);
		iv.setScaleType(ImageView.ScaleType.FIT_XY);
		iv.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		iv.setBackgroundResource(pics[arg0]);
		return iv;
	}

}
