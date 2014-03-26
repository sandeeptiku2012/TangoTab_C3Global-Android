package com.tangotab.nearMe.adapter;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.tangotab.R;
import com.tangotab.VolleySingleton;
import com.tangotab.core.utils.ImageLoader;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.nearMe.vo.DealsDetailVo;

/**
 * User defined adapter class will be used for  display the list view for list of deal in near me Tab
 * 
 * <bR> Class : NearMeListAdapter
 * <br>Layout : nearme.xml
 * 
 *  @author Dillip.Lenka
 *
 */
public class NearMeListAdapter extends BaseAdapter 
{
	/*
	 * Meta Definitions
	 */
	private ImageLoader imageLoader;
	private Context context;
	private final List<DealsDetailVo> dealsDetailList;
	/*
	 * UI Widgets
	 */
	private LayoutInflater layoutInflater;
	private TextView businessname;
	private TextView dealname;
	private TextView date,newDeals;
	private TextView nodealsavailable;
	private String country;
	private RotateAnimation rotate;
	private boolean status;
	Activity activity;


	//private String 
	
	/**
	 * Constructor for nearmelist adapter.
	 * @param context
	 * @param dealsDetailList
	 * @param locationSearch
	 */
	
	
	public NearMeListAdapter(Context context,List<DealsDetailVo> dealsDetailList,String country,boolean status,Activity activity) 
	{
		this.context=context;
		this.activity= activity;
		this.dealsDetailList = dealsDetailList;
		this.country = country;
		imageLoader = new ImageLoader(context.getApplicationContext());		
		layoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		rotate= (RotateAnimation)AnimationUtils.loadAnimation(context,R.anim.rot);
		this.status=status;
		 
	}
	
	
	@Override
	public int getCount()
	{
		return dealsDetailList.size();
	}

	@Override
	public Object getItem(int arg0)
	{
		return null;
	}

	@Override
	public long getItemId(int arg0) 
	{
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup viewGroup) 
		{
			if(convertView==null)
			{
				convertView=layoutInflater.inflate(R.layout.dealslistitems, null);
			}
			DealsDetailVo  dealsDetailVo= dealsDetailList.get(position);
			String noDeals = dealsDetailVo.getNoOfdeals();
			if (noDeals.contains("'"))
				noDeals = noDeals.replace("'", "");
			int no_deals =0;
			try
			{
			 no_deals = Integer.parseInt(noDeals);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			String sdate = dealsDetailVo.getDealAvailableStartDate();			
			String rest[] = sdate.split("-");
			String rest1;
			if (rest.length > 2)
				rest1 = rest[1] + "-" + rest[2] + "-" + rest[0];
			else
				rest1 = rest[1] + "-" + rest[0];
	
			// Refer to TextView from Layout
			businessname = (TextView) convertView.findViewById(R.id.businessname);
			businessname.setText(dealsDetailVo.getBusinessName());
			NetworkImageView dealImg = (NetworkImageView)convertView.findViewById(R.id.image);
			dealImg.setImageUrl(dealsDetailVo.getImageUrl(),VolleySingleton.getInstance(context).getImageLoader());
			dealImg.setDefaultImageResId(R.drawable.image_bg);
			/*ImageView image = (ImageView) convertView.findViewById(R.id.image);
			imageLoader.DisplayImage(dealsDetailVo.getImageUrl(), image,activity);*/	
			dealname = (TextView) convertView.findViewById(R.id.dealname);
			
			dealname.setText(dealsDetailVo.getDealName());
			
			nodealsavailable = (TextView) convertView.findViewById(R.id.nodealsavailable);
			String drivingDistance=dealsDetailVo.getDrivingDistance();
			String milesOrKm = " miles";
			StringBuilder dealsAvailable = new StringBuilder();
					
			if(!ValidationUtil.isNullOrEmpty(country) && country.equalsIgnoreCase("Canada")){
				drivingDistance = String.valueOf(new DecimalFormat("##.##").format(Float.parseFloat(drivingDistance)*1.61));
				milesOrKm = " Km";
			}
			
			nodealsavailable.setText(dealsAvailable.append(drivingDistance).append(milesOrKm).append(", ").append(dealsDetailVo.getStartTime()).append(" - ").append(dealsDetailVo.getEndTime()));
			newDeals=(TextView) convertView.findViewById(R.id.myImageViewText);
		    String availableDeals=dealsDetailVo.getNoDealsAvailable();
		    if(availableDeals.length()>1){
			newDeals.setText("  "+dealsDetailVo.getNoDealsAvailable()+""+"\n"+"LEFT");
		    }
		    else{
		    	newDeals.setText("   "+dealsDetailVo.getNoDealsAvailable()+"\n"+"LEFT");	
		    }
			
			
			 newDeals.startAnimation(rotate);
			convertView.setTag(dealsDetailVo);
			return convertView;		
	}
	
}
