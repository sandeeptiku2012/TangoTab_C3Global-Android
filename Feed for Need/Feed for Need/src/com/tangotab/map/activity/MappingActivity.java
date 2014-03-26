package com.tangotab.map.activity;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;
import com.tangotab.LocationManagerToggle;
import com.c3global.R;
import com.tangotab.calendar.activity.CalendarActivity;
import com.tangotab.calendar.utils.CalendarView;
import com.tangotab.claimOffer.activity.ClaimOfferActivity;
import com.tangotab.contactSupport.activity.ContactSupportActvity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.GeoCoderUtil;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.mainmenu.activity.MainMenuActivity;
import com.tangotab.map.overlay.OnSingleTapListener;
import com.tangotab.map.overlay.TapControlledMapView;
import com.tangotab.map.util.MapItemizedOverlay;
import com.tangotab.me.activity.MeActivity;
import com.tangotab.myOfferDetails.activity.AutoCheckinActivity;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.myOffers.activity.MyOffersActivity;
import com.tangotab.nearMe.activity.NearMeActivity;
import com.tangotab.nearMe.activity.NearMeGallery;
import com.tangotab.nearMe.adapter.NearMeGalleryAdapter;
import com.tangotab.nearMe.vo.DealsDetailVo;
import com.tangotab.search.activity.SearchActivity;
import com.tangotab.search.adapter.CustomWheelAdapter;
import com.tangotab.search.service.SearchService;
import com.tangotab.search.vo.SearchVo;
import com.tangotab.settings.activity.SettingsActivity;

/**
 * Map activity in order to display the Map.
 * 
 *<br> Class :MappingActivity
 * <br> layout:map.xml
 * 
 * @author Dillip.Lenka
 *
 */
public class MappingActivity extends MapActivity
{
	/* FIELDS */
	private int prevPos;
	private MapController mapController = null;
	private TapControlledMapView MView = null;
	GeoPoint point = null;
	//MapItemizedOverlay itemizedOverlay = null;
	MapItemizedOverlay itemizedOverlay1 = null;
	Button back = null;
	ArrayList<GeoPoint> itemAddress = null;
	private TextView dinningType,currentDate;
	private String userId;
	private double dev_lat ;
	private double dev_lang ;
	GeoPoint point1 = null;
	public Handler myUiUpdater;
	public TangoTab application;
	SimpleItemizedOverlay itemizedOverlay=null;
	private ProgressDialog mDialog;
	private int diningStyle;
	private RelativeLayout relativeLayoutTopBar;
	private int selectedPosition;
	private String diningId;
	private ArrayList<GeoPoint> geoPointsList=new ArrayList<GeoPoint>();
	private String[] dinningStyles=new String[]{"All","Breakfast","Lunch","Happy Hour","Dinner","Late"};
	int[] pics = {R.drawable.allselector,R.drawable.casual_selector,R.drawable.upscale_selector};
	private int[] topBarImgs=new int[]{R.drawable.white_one,R.drawable.white_two,R.drawable.white_three};
	private String selectedDate;
	private String dinningStyleType;
	private int mNewIndex;
	private int valueToSet;
	private boolean scrolling = false;
	private NearMeGallery ga;
	private int selectedposition;
	TextView locationInfo;
	String selectedLocation;
	String Address;
	private Timer t;
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.map);
						
		/*DisplayMetrics dm = getResources().getDisplayMetrics();
		 REL_SWIPE_MIN_DISTANCE = (int)(120.0f * dm.densityDpi / 160.0f + 0.5); 
	        REL_SWIPE_MAX_OFF_PATH = (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
	        REL_SWIPE_THRESHOLD_VELOCITY = (int)(200.0f * dm.densityDpi / 160.0f + 0.5);*/
		  relativeLayoutTopBar=(RelativeLayout)findViewById(R.id.relativeLayoutBackground);
		 /* final GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());
	        View.OnTouchListener gestureListener = new View.OnTouchListener() {
	            public boolean onTouch(View v, MotionEvent event) {
	            	return gestureDetector.onTouchEvent(event); 
	             
	            }};*/
	  //  relativeLayoutTopBar.setOnTouchListener(gestureListener);    
		dinningType=(TextView)findViewById(R.id.tvDinningType);
		application = (TangoTab) getApplication();
		
		
		
		itemAddress = new ArrayList<GeoPoint>();
		AppConstant.flagFormMaping = true;
		MView = (TapControlledMapView) findViewById(R.id.mapview);
		
	 	       		
		// dismiss balloon upon single tap of MapView 
		MView.setOnSingleTapListener(new OnSingleTapListener() {		
					@Override
					public boolean onSingleTap(MotionEvent e) {
						if(!ValidationUtil.isNull(itemizedOverlay))
						{
							itemizedOverlay.hideAllBalloons();
						}
						return true;
					}
				});		
	
		/**
		 * do auto check in
		 */
		application = (TangoTab) getApplication();
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,getApplicationContext(),offerList);
		activity.doCheckIn();
			
		SharedPreferences spf1 = getSharedPreferences("UserDetails", 0);
		String homeZipCode1 = spf1.getString("postal", "");
		String workZipCode1 = spf1.getString("workZip", "");
		String	alternateZipCode1 = spf1.getString("alternateZip", "");
		//SharedPreferences spc11 = getSharedPreferences("locationDetail", 0);
		//selectedLocation = spc11.getString("SelectedLocation","Current Location");
		
		SharedPreferences spc2 = getSharedPreferences("locationDetail", 0);
		selectedLocation = spc2.getString("currentLocation","");
		
		locationInfo = (TextView) findViewById(R.id.locationLabel);
		
		if (selectedLocation.equalsIgnoreCase("Current Location"))
		{
			locationInfo.setText(selectedLocation);
			//locationInfo.setText(stringBuilder.toString());
		} 
		else if (selectedLocation.equalsIgnoreCase("HomeZip")) 
		{
			locationInfo.setText(homeZipCode1);
			
		}
		else if (selectedLocation.equalsIgnoreCase("WorkZip")){
			locationInfo.setText(workZipCode1);
		}
		else if (selectedLocation.equalsIgnoreCase("alternateZipCode"))
		{
			
			if(!ValidationUtil.isNullOrEmpty(alternateZipCode1))
			{
				locationInfo.setText(ValidationUtil.capitalizeFully(alternateZipCode1,null));
			}
			else
			{
				locationInfo.setText(ValidationUtil.capitalizeFully(Address.toUpperCase(),null));
			}
		} 
		else
		{
			locationInfo.setText("Current Location");
		}
		
		
		Button topMenuBtn = (Button) findViewById(R.id.topMenuButton);
		Button meButton = (Button) findViewById(R.id.topMeMenuButton);
		Button nearMe = (Button) findViewById(R.id.topNearmeMenuMenuButton);
		Button topSearchBtn = (Button) findViewById(R.id.topSearchMenuButton);
		
		currentDate=(TextView)findViewById(R.id.tvCurrentDate);
		
		
		
		
		final SharedPreferences datePreferences = getSharedPreferences("NearMeDate", 0);
		
		String calenderText = datePreferences.getString("selectText", "");
		selectedDate = datePreferences.getString("selectedDate", "");
		if(!ValidationUtil.isNullOrEmpty(calenderText) && !ValidationUtil.isNullOrEmpty(selectedDate))
		{
			currentDate.setText(calenderText);
		}
		else
		{
			Calendar today = Calendar.getInstance();
			//StringBuilder currentDate = new StringBuilder();
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
			selectedDate = sdfDate.format(today.getTime());
			currentDate.setText("Today");
		}
		currentDate.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				Intent getDate=new Intent(MappingActivity.this,CalendarActivity.class);
				int year = datePreferences.getInt("year", 0);
				int month = datePreferences.getInt("month", 0);
				int day =datePreferences.getInt("day", 0);
				CalendarView.prevDay=day;
				CalendarView.prevMonth=month;
				CalendarView.prevYear=year;
			    startActivityForResult(getDate, AppConstant.GET_DATE_REQUEST_CODE);   
			    
			}
		});
		
		   final LinearLayout wheelLayout = (LinearLayout) findViewById(R.id.wheelLayout);
	       Button doneBtn = (Button) findViewById(R.id.doneBtn);
	       Button cancelBtn = (Button) findViewById(R.id.cancelBtn);

			final WheelView wheelView = (WheelView) findViewById(R.id.radiusWheelView);
			wheelView.setViewAdapter(new RadiusAdapter(this));
			wheelView.setVisibleItems(3);

			/**
			 * scroll change listener
			 */

			wheelView.addChangingListener(new OnWheelChangedListener() {
				public void onChanged(WheelView wheel, int oldValue, int newValue) {
					if (!scrolling) {
						populateRadius(wheel, dinningStyles, newValue);
					}
				}
			});

			/**
			 * scroll listener
			 */
			wheelView.addScrollingListener(new OnWheelScrollListener() {
				public void onScrollingStarted(WheelView wheel) {
					scrolling = true;
				}

				public void onScrollingFinished(WheelView wheel) {
					scrolling = false;
					populateRadius(wheel, dinningStyles, wheel.getCurrentItem());
					mNewIndex = wheel.getCurrentItem();
				}
			});
			wheelView.setCurrentItem(1);
			wheelView.setCurrentItem(0);
			wheelView.setCurrentItem(mNewIndex);
			/**
			 * distance onclick listener
			 */
			dinningType.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					diningStyle=datePreferences.getInt("diningStyle",1);

					if (diningStyle == 0)
						dinningStyleType = dinningStyles[0];
					if (diningStyle==1)
						dinningStyleType = dinningStyles[1];
					if (diningStyle==2)
						dinningStyleType = dinningStyles[2];
					if (diningStyle==3)
						dinningStyleType = dinningStyles[3];
					if (diningStyle==4)
						dinningStyleType = dinningStyles[4];				

					for (int i = 0; i < dinningStyles.length; i++) {
						if (dinningStyles[i].equalsIgnoreCase(dinningStyleType)) {
							valueToSet = i;
							break;
						}
					}
					wheelView.setCurrentItem(valueToSet);
					wheelLayout.setVisibility(View.VISIBLE);	
					application.getDealsList().clear();
					
				}

			});
			/*
			 * near me button onClick listener
			 */
			cancelBtn.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					wheelLayout.setVisibility(View.GONE);
					
				}
			});

			/**
			 * scroll done button listener
			 */
			doneBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dinningStyleType = dinningStyles[mNewIndex];
					
					SharedPreferences datePreferences = getSharedPreferences("NearMeDate", 0);
					SharedPreferences.Editor dateEdit = datePreferences.edit();
					dateEdit.putInt("diningStyle", mNewIndex);
					dateEdit.commit();
	    			
					getDealList(1, diningId,mNewIndex);
					wheelLayout.setVisibility(View.GONE);
	        		
				}
				
				
			});
		//menu button onclick listener.
				topMenuBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent meIntent=new Intent(MappingActivity.this, MainMenuActivity.class);
						meIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						storeSliderPosition();
						startActivity(meIntent);
					}
				});
					
		//me button onclick listener.
				meButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent meIntent=new Intent(MappingActivity.this, MeActivity.class);
						meIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						storeSliderPosition();
						startActivity(meIntent);
					}
				});		
		
		// near me button onclick listener		 
		nearMe.setOnClickListener(new OnClickListener() 
		{
			
			 @Override
	            public void onClick(View v)
	            	{  
	            	if(AppConstant.IS_CLAIM_OFFER_PAGE)
	        		{
	        			DealsDetailVo dealsDetailVo = application.getSelectDeal();
	        			String country = application.getCountry();
	        			ArrayList<DealsDetailVo> businessList = new ArrayList<DealsDetailVo>();
	        			for (DealsDetailVo dealsVo : application.getDealsList())
	        			{
	        				if (dealsDetailVo.businessName.equals(dealsVo.businessName)) {
	        					businessList.add(dealsVo);
	        				}

	        			}			
	        			Intent calimedIntent = new Intent(MappingActivity.this,ClaimOfferActivity.class);
	        			calimedIntent.putExtra("from", "nearme");
	        			calimedIntent.putExtra("selectDeal", dealsDetailVo);
	        			calimedIntent.putExtra("businessList", businessList);
	        			calimedIntent.putExtra("country", country);
	        			calimedIntent.putExtra("selectedPosition",AppConstant.SELECTED_POSITION);
	        			startActivity(calimedIntent);
	        		}
	            	else{
	            		Intent nearmeIntent=new Intent(MappingActivity.this, NearMeActivity.class);
	    				nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    				storeSliderPosition();
	    				startActivity(nearmeIntent);
	            	}
	            	}
			});

			
		
		//Top Search button onclick listener.
		topSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent searchIntent=new Intent(MappingActivity.this, SearchActivity.class);
				searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				storeSliderPosition();
				startActivity(searchIntent);
			}
		});
		 ga = (NearMeGallery)findViewById(R.id.nearMeGalleryView);
       
        prevPos = datePreferences.getInt("slidingPos", 0);
        ga.setAdapter(new NearMeGalleryAdapter(this,pics));
        ga.setSelection(prevPos);
        diningStyle=datePreferences.getInt("diningStyle",0);
        
        ga.setOnItemSelectedListener( new OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3)
            {	
            	
            	prevPos = datePreferences.getInt("slidingPos", 0);
                Log.v("Changed----->", ""+pos);
                if(itemizedOverlay!=null)
                	itemizedOverlay.hideAllBalloons();
                
                relativeLayoutTopBar.setBackgroundResource(topBarImgs[pos]);
                selectedPosition=pos+1;
                StringBuilder dinning= new StringBuilder();
                if(selectedPosition ==1)
                {               	
                	dinning.append("1").append(",").append("2").append(",").append("3").append(",").append("4");
                	diningId = dinning.toString();
                }
                else if(selectedPosition ==2){
                	dinning.append("2").append(",").append("3");
                	diningId = dinning.toString();
                }
                else
                {
                	dinning.append("4").append(",").append("5");
                	diningId = dinning.toString();
                }
               
                //application.getDealsList().clear();
                //finalDealList.clear();
               
                /**
                 * Retrieve all the deals
                 */
                if(!ValidationUtil.isNullOrEmpty(application.getDealsList()) && prevPos==pos)
                {
                	//finalDealList.clear();
                	//finalDealList.addAll(application.getDealsList());
                	dinningType.setText(dinningStyles[diningStyle]);
                	new MapListAsyncTask().execute();
                	/*getMapPosition(application.getDealsList());
                	myMapView();*/
                }
                else{
                	SharedPreferences.Editor edits11 = datePreferences.edit();
                	edits11.putInt("slidingPos", pos);
                	edits11.commit();
                	edits11=null;
                	getDealList(1,diningId,diningStyle);
                }      

                                
                }

            public void onNothingSelected(AdapterView<?> arg0) {
              
            }
        });

	}

	protected boolean isRouteDisplayed() {
		return false;
	}
 private void storeSliderPosition(){
	 SharedPreferences datePreferences = getSharedPreferences("NearMeDate", 0);	 
          Editor edit=datePreferences.edit();
          edit.putInt("slidingPos",selectedPosition-1);
          //edit.putInt("diningStyle",diningStyle);
          edit.commit();
          
 }
	/**
	 * Back button functionality
	 */
	@Override
	public void onBackPressed()
	{
		//super.onBackPressed();
		AppConstant.flagFormMaping = false;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		

	}
	/**
	 * For Zooming purpose
	 */
	public void zoomOption() 
	{
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;
				
		if(itemAddress != null)
			for (GeoPoint item : itemAddress) 
			{
				int lat = item.getLatitudeE6();
				int lon = item.getLongitudeE6();	 
				maxLat = Math.max(lat, maxLat);
				minLat = Math.min(lat, minLat);
				maxLon = Math.max(lon, maxLon);
				minLon = Math.min(lon, minLon);
			}
//		GeoPoint myLocation = new GeoPoint((int) (AppConstant.dev_lat * 1E6),(int) (AppConstant.dev_lang * 1E6));
//		maxLat = Math.max(myLocation.getLatitudeE6(), maxLat);
//		minLat = Math.min(myLocation.getLatitudeE6(), minLat);
//		maxLon = Math.max(myLocation.getLongitudeE6(), maxLon);
//		minLon = Math.min(myLocation.getLongitudeE6(), minLon);	
		mapController = MView.getController();
		if(mapController != null)
		{
			double borderFactor = 1.5;
			mapController.zoomToSpan((int) (Math.abs(maxLat - minLat) * borderFactor), (int)(Math.abs(maxLon - minLon) * borderFactor));		
			//mapController.animateTo(myLocation);
			mapController.animateTo(new GeoPoint(
					(maxLat + minLat)/2,
					(maxLon + minLon)/2 ));
		}
	}

	@Override
	public void onResume() 
	{
		super.onResume();
		
		t=LocationManagerToggle.getInstance().setTimer(null, 0,5000);
		
		SharedPreferences spreferences = getSharedPreferences(AppConstant.SELECTED_PREFS, 0);
		selectedposition = spreferences.getInt(AppConstant.KEY_SELECTED_ITEM, 0);
		ga.setAdapter(new NearMeGalleryAdapter(MappingActivity.this, pics));
		ga.setSelection(selectedposition);
		
		LocationManagerToggle.getInstance().initalizeLocationManagerService(this,this);
		/**
		 * do auto checkin
		 */
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,getApplicationContext(),offerList);
		activity.doCheckIn();
	
	}

	@Override
	public void onStart() 
	{
		super.onStart();
		
	}
	public void myMapView()	{
		
		
		List<DealsDetailVo> dealsList = application.getDealsList();
		ArrayList<GeoPoint> geoPointsList2=new ArrayList<GeoPoint>();
		
		if(!ValidationUtil.isNullOrEmpty(dealsList))
		{
		itemizedOverlay =  new SimpleItemizedOverlay( getResources().getDrawable(R.drawable.green_map_pin),MView,dealsList);
		}
		else{
			itemizedOverlay =  new SimpleItemizedOverlay( getResources().getDrawable(R.drawable.green_map_pin),MView);
		}
	    MView.getOverlays().clear();
		mapController = MView.getController();
		
		//MView.setBuiltInZoomControls(true);
		MView.displayZoomControls(true);
    
		//Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
	
		
		if(!ValidationUtil.isNullOrEmpty(dealsList))
		{
			for (int i = 0; i < dealsList.size(); i++) 
			{
				
					try 
					{/*
						List<Address> addresses = geoCoder.getFromLocationName(dealsList.get(i).address, 1);
		
						if (addresses != null && addresses.size() > 0)
						{
							GeoPoint point = new GeoPoint((int) (addresses.get(0).getLatitude() * 1E6),(int) (addresses.get(0).getLongitude() * 1E6));
							itemAddress.add(point);
							StringBuilder businessName = new StringBuilder();
							//businessName.append(offerCount).append(")").append(" ").append(dealsList.get(i).getBusinessName());
							
							OverlayItem overlayItem = new OverlayItem(point, dealsList.get(i).getBusinessName(), dealsList.get(i).getDealName());
							itemizedOverlay.addOverlay(overlayItem);
							MView.getOverlays().add(itemizedOverlay);
							offerCount++;
						} else{
							
							GeoCoderUtil.getLatLong(URLEncoder.encode(dealsList.get(i).address, "UTF-8"));
							GeoPoint point = new GeoPoint((int) (AppConstant.locationLat * 1E6),(int) (AppConstant.locationLong * 1E6));
							itemAddress.add(point);
							StringBuilder businessName = new StringBuilder();
							businessName.append(offerCount).append(")").append(" ").append(dealsList.get(i).getBusinessName());
							OverlayItem overlayItem = new OverlayItem(point, businessName.toString(), dealsList.get(i).getDealName());
							itemizedOverlay.addOverlay(overlayItem);
							MView.getOverlays().add(itemizedOverlay);
					
							offerCount++;
						}
					*/
						
						if(!geoPointsList2.contains(geoPointsList.get(i)))
						{
						point=geoPointsList.get(i);
						geoPointsList2.add(point);
						itemAddress.add(point);
						StringBuilder businessName = new StringBuilder();
						businessName.append(dealsList.get(i).getBusinessName());
						OverlayItem overlayItem = new OverlayItem(point, businessName.toString(), dealsList.get(i).getDealDescription());
						itemizedOverlay.addOverlay(overlayItem);
						MView.getOverlays().add(itemizedOverlay);
						zoomOption();
						}
						
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				
			}
			geoPointsList2.clear();
		}
		if(ValidationUtil.isNullOrEmpty(dealsList))
		{
			Double currentLat=0.0 ,currentLong=0.0;
			List<Address> addressList = null;
			SharedPreferences currentLocation = getSharedPreferences("LocationDetails", 0);
			String lat = currentLocation.getString("locLat", "");
			String lang = currentLocation.getString("locLong", "");
			if (!ValidationUtil.isNullOrEmpty(lat)&& !ValidationUtil.isNullOrEmpty(lang)) {
				try 
				{
					currentLat = Double.valueOf(lat);
					currentLong = Double.valueOf(lang);
				} catch (NumberFormatException e) {

					Log.e("Exception","Exception occuerd when converting String int doubble",e);
				}
				Geocoder geocoder = new Geocoder(getBaseContext(),Locale.getDefault());
				try {
					addressList = geocoder.getFromLocation(currentLat,currentLong,1);
				} catch (IOException e)
				{
					Log.e("Exception:","Exception occuerd at the time getting address list from Geo Coder.");
					e.printStackTrace();
				}
			}
		itemizedOverlay1 =  new MapItemizedOverlay( getResources().getDrawable(R.drawable.location__icon), MappingActivity.this);	

		MView.getOverlays().add(itemizedOverlay1);
		point1 = new GeoPoint((int) (currentLat * 1E6),(int) (currentLong * 1E6));
		if(!ValidationUtil.isNullOrEmpty(addressList))
		{
			OverlayItem overlayItem = new OverlayItem(point1, addressList.get(0).getAddressLine(0), "");
			itemizedOverlay1.addOverlay(overlayItem);
			itemizedOverlay.addOverlay(overlayItem);
			//MView.getOverlays().add(itemizedOverlay);
			MView.getController().animateTo(point1);
		}
		}
		//MView.getController().setZoom(18);
		
		if(mDialog!=null)
			mDialog.dismiss();
	}
	/*
	 * Radius Adapter for distance dropdown in search page.
	 * 
	 * @author Lakshmipathi.P
	 */
	private class RadiusAdapter extends AbstractWheelTextAdapter
	{

		/**
		 * Constructor
		 */
		protected RadiusAdapter(Context context) {
			super(context, R.layout.distancetext, NO_RESOURCE);

			setItemTextResource(R.id.radiusText);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);

			return view;
		}

		@Override
		public int getItemsCount() {
			return dinningStyles.length;
		}

		@Override
		protected CharSequence getItemText(int index) {
			return dinningStyles[index];
		}
	}

	/**
	 * Select menu button
	 * @param item
	 */
	public void onMenuSelected(int item) 
	{
	switch (item) 
	{
		case 0:
			Intent homeIntent=new Intent(this, MyOffersActivity.class);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
			break;
		
		case 1:
			Intent businessearchIntent=new Intent(this, NearMeActivity.class);
			businessearchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(businessearchIntent);
			break;
	
		case 2:
			Intent contactmanagerIntent=new Intent(this, SearchActivity.class);
			contactmanagerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(contactmanagerIntent);
			break;				
		case 3:
			Intent followupIntent=new Intent(this, SettingsActivity.class);
			followupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(followupIntent);
			break;		
	}		
}
	/**
	 * Retrieve all the deals from number of page count
	 * 
	 * @param pageCount
	 */
	private void getDealList(int pageCount, String diningId, int diningStyle)
	{
		SharedPreferences spc1 = getSharedPreferences("Distance", 0);
		String distance_set = spc1.getString("distancevalue", "");
		if (ValidationUtil.isNullOrEmpty(distance_set)) {
			distance_set = "20";
		}

		SharedPreferences spc3 = getSharedPreferences("UserDetails", 0);
		userId = spc3.getString("UserId", "");		

		dev_lat = AppConstant.selectedLat == 0.0 ? AppConstant.dev_lat
				: AppConstant.selectedLat;
		dev_lang = AppConstant.selectedLong == 0.0 ? AppConstant.dev_lang
				: AppConstant.selectedLong;

		/**
		 * Set all the data in near me vo object
		 */
		dinningType.setText(dinningStyles[diningStyle]);
		SearchVo searchVo = new SearchVo();		
		searchVo.setLocLat(String.valueOf(dev_lat));
		searchVo.setLocLaong(String.valueOf(dev_lang));
		searchVo.setDistance(distance_set);
		searchVo.setUserId(userId);
		searchVo.setPageIndex(String.valueOf(pageCount));
		searchVo.setDiningId(diningId);
		searchVo.setDate(selectedDate);
		application.setNearMePageCount(pageCount);
		setTimeRange(searchVo, diningStyle);
		Log.v("searchVo is ", searchVo.toString());
		
		new NearListAsyncTask().execute(searchVo);
		
		
	}
	/**
	 * Set time range according to dining Id
	 * 
	 * @param nearMeVo
	 * @param position
	 */
	private void setTimeRange(SearchVo searchVo, int position)
	{
		//String startDate= searchVo.getDate().split(" ")[0]+searchVo.;
		
				/*SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				String currentDateandTime = sdf.format(new Date());*/
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");//dd/MM/yyyy
				String formattedTime = sdfDate.format(cal.getTime());
				/*2013-11-15*/
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date currentDate=new Date();
				Date convertedDate=new Date();
				//String newFormat = formatter.format(searchVo.getDate());
				try {
				     convertedDate = formatter.parse(searchVo.getDate());
				     //formattedDate = targetFormat.format(convertedDate);
				} catch (Exception e) {
				// 
				e.printStackTrace();
				}
				if(convertedDate.after(currentDate))
				{
					switch (position) {
					case 0:
						searchVo.setMinTime(0);
						searchVo.setMaxTime(24);
						AppConstant.SEARCH_MAX_TIME_RANGE = "0";
						break;
					case 1:
						searchVo.setMinTime(04);
						searchVo.setMaxTime(11);
						AppConstant.SEARCH_MAX_TIME_RANGE = "0";
						break;
					case 2:
						searchVo.setMinTime(11);
						searchVo.setMaxTime(13);
						AppConstant.SEARCH_MAX_TIME_RANGE = "0";
						break;
					case 3:
						searchVo.setMinTime(13);
						searchVo.setMaxTime(17);
						AppConstant.SEARCH_MAX_TIME_RANGE = "0";
						break;
					case 4:
						searchVo.setMinTime(17);
						searchVo.setMaxTime(21);
						AppConstant.SEARCH_MAX_TIME_RANGE = "0";
						break;
					case 5:
						searchVo.setMinTime(21);
						searchVo.setMaxTime(04);
						AppConstant.SEARCH_MAX_TIME_RANGE = "0";
						break;
					default:
						break;
					}
					
				}
				else 
				{
					switch (position) {
					case 0:
						searchVo.setMinTime(0);
						searchVo.setMaxTime(24);
						AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
						break;
					case 1:
						searchVo.setMinTime(04);
						searchVo.setMaxTime(11);
						AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
						break;
					case 2:
						searchVo.setMinTime(11);
						searchVo.setMaxTime(13);
						AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
						break;
					case 3:
						searchVo.setMinTime(13);
						searchVo.setMaxTime(17);
						AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
						break;
					case 4:
						searchVo.setMinTime(17);
						searchVo.setMaxTime(21);
						AppConstant.SEARCH_MAX_TIME_RANGE =formattedTime;
						break;
					case 5:
						searchVo.setMinTime(21);
						searchVo.setMaxTime(04);
						AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
						break;
					default:
						break;
					}
				}
		
	/*	switch (position)
		{
		case 0:
			searchVo.setMinTime(0);
			searchVo.setMaxTime(24);
			AppConstant.SEARCH_MAX_TIME_RANGE="24";
			break;
		case 1:
			searchVo.setMinTime(04);
			searchVo.setMaxTime(11);
			AppConstant.SEARCH_MAX_TIME_RANGE="11";
			break;
		case 2:
			searchVo.setMinTime(11);
			searchVo.setMaxTime(13);
			AppConstant.SEARCH_MAX_TIME_RANGE="13";
			break;
		case 3:
			searchVo.setMinTime(13);
			searchVo.setMaxTime(17);
			AppConstant.SEARCH_MAX_TIME_RANGE="17";
			break;
		case 4:
			searchVo.setMinTime(17);
			searchVo.setMaxTime(21);
			AppConstant.SEARCH_MAX_TIME_RANGE="21";
			break;
		case 5:
			searchVo.setMinTime(21);
			searchVo.setMaxTime(04);
			AppConstant.SEARCH_MAX_TIME_RANGE="04";
			break;
		default:
			break;
		}*/
	}
	/**
	 * This class will be used to display the Map
	 * 
	 * @author Dillip.Lenka
	 *
	 */
	public class MapListAsyncTask extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog mDialog=null;
		@Override
		protected void onPreExecute()
		{	
			mDialog=new ProgressDialog(MappingActivity.this);
			mDialog.setCancelable(false);
		    mDialog.setMessage("Please Wait...");
		    mDialog.show();
		  
		}
		@Override
		protected Void doInBackground(Void... params)
		{
			List<DealsDetailVo> dealList = application.getDealsList();
			try{
				if(!ValidationUtil.isNullOrEmpty(dealList))
					getMapPosition(dealList);
			}
			catch(Exception e)
			{
				Log.e("Exception occured", "Exception occured at the time of login",e);
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result)
		{
			myMapView();
			if(mDialog!=null&&mDialog.isShowing()){
				mDialog.dismiss();
			}
				
				
			
		}		
	}
	/**
	 * Get list of Deals from the Search service.
	 * 
	 * @author Dillip.Lenka
	 *
	 */
public class NearListAsyncTask extends AsyncTask<SearchVo, Void, List<DealsDetailVo>>
{
	private ProgressDialog mDialog=null;
	@Override
	protected void onPreExecute()
	{	
		
		mDialog=new ProgressDialog(MappingActivity.this);
		mDialog.setCancelable(false);
	    mDialog.setMessage("Please Wait...");
	    if(!MappingActivity.this.isFinishing())
	    mDialog.show();
	  
	}
	@Override
	protected List<DealsDetailVo> doInBackground(SearchVo... searchVo)
	{
		List<DealsDetailVo> dealsList =null;
		try{
			SearchService searchService = new SearchService();
			dealsList = searchService.getSearchList(searchVo[0]);
			getMapPosition(dealsList);
		}
		catch(ConnectTimeoutException e)
		{
			Log.e("Exception occured", "Exception occured at the time of login",e);
			/**
			 * Contact to TangoTab contact support team
			 */
			Intent contactIntent=new Intent(getApplicationContext(), ContactSupportActvity.class);
			startActivity(contactIntent);
		}
		catch(Exception e)
		{
			Log.e("Exception occured", "Exception occured at the time of login",e);
		}
		return dealsList;
	}
	@Override
	protected void onPostExecute(List<DealsDetailVo> dealsList)
	{
		
		if(!ValidationUtil.isNullOrEmpty(dealsList))
		{
			application.setDealsList(dealsList);
		}
		myMapView();
		
		if((mDialog!=null)&& (mDialog.isShowing())){
			mDialog.dismiss();
		}
			
			
		
	}		
}
/*public class MyGestureDetector extends SimpleOnGestureListener{ 

   @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
    		float velocityY) {
    	//Log.e("on fling","called");
    	if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) 
            return false; 
        if(e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && 
            Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
            onRTLFling(); 
        }  else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE && 
            Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
            onLTRFling(); 
        } 
        return false; 
    }
    @Override
    public boolean onDown(MotionEvent e) {
    //	Log.e("on down","called");
    	return true;
    }
} */
 private void getMapPosition(List<DealsDetailVo> dealsList)
 {
	 	geoPointsList.clear();
	 	
		Geocoder geoCoder = new Geocoder(MappingActivity.this, Locale.getDefault());
		if(!ValidationUtil.isNullOrEmpty(dealsList))
		{
			int offerCount=1;
			for (DealsDetailVo dealsVo:dealsList) 
			{
				if(!ValidationUtil.isNullOrEmpty(dealsVo.address))
				{
					try 
					{
						if (android.os.Build.VERSION.SDK_INT > 9) 
						{
					        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					        StrictMode.setThreadPolicy(policy); 
						}
						List<Address> addresses = geoCoder.getFromLocationName(dealsVo.address, 1);
		
						if (addresses != null && addresses.size() > 0)
						{
							if (android.os.Build.VERSION.SDK_INT > 9) 
							{
						        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
						        StrictMode.setThreadPolicy(policy); 
							}
							GeoPoint point = new GeoPoint((int) (addresses.get(0).getLatitude() * 1E6),(int) (addresses.get(0).getLongitude() * 1E6));
							geoPointsList.add(point);
							offerCount++;
						} else{
							if (android.os.Build.VERSION.SDK_INT > 9) 
							{
						        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
						        StrictMode.setThreadPolicy(policy); 
							}
							GeoCoderUtil.getLatLong(URLEncoder.encode(dealsVo.address, "UTF-8"));
							GeoPoint point = new GeoPoint((int) (AppConstant.locationLat * 1E6),(int) (AppConstant.locationLong * 1E6));
							geoPointsList.add(point);
							offerCount++;
						}
					}
					catch (Exception e)
					{
						try{
							if (android.os.Build.VERSION.SDK_INT > 9) 
							{
						        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
						        StrictMode.setThreadPolicy(policy); 
							}
							GeoCoderUtil.getLatLong(URLEncoder.encode(dealsVo.address, "UTF-8"));
							GeoPoint point = new GeoPoint((int) (AppConstant.locationLat * 1E6),(int) (AppConstant.locationLong * 1E6));
							geoPointsList.add(point);
						}catch(Exception exe)
						{
							exe.printStackTrace();
						}
					}
				}
			}
		}	
 }
/* private void onLTRFling()
 {
	 if(diningStyle!=1){
		 diningStyle--;
		 application.getDealsList().clear();
        getDealList(1,diningId, diningStyle);
	 }
	 Log.e("swipe","ltf");
   }

    private void onRTLFling()
    {
    	if(diningStyle < 5){
			 diningStyle++;
			 application.getDealsList().clear();
               getDealList(1,diningId, diningStyle);
		 }
   	 Log.e("swipe","rtl");
       
    }*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode!=RESULT_CANCELED){
    		if(requestCode==AppConstant.GET_DATE_REQUEST_CODE)
    		{
    			StringBuilder date = new StringBuilder();	    			
    			int year = Integer.parseInt(data.getStringExtra("year"));
    			int month = Integer.parseInt(data.getStringExtra("month"));
    			int day = Integer.parseInt(data.getStringExtra("day"));
    			date.append(year).append("-").append(month).append("-").append(day);
    			selectedDate = date.toString();
    			   			
    			String selectText = getTextFromDate(year, month, day);
    			
    			
    			SharedPreferences datePreferences = getSharedPreferences("NearMeDate", 0);
    			SharedPreferences.Editor dateEdit = datePreferences.edit();
    			dateEdit.putString("selectText", selectText);
    			dateEdit.putInt("year", year);
    			dateEdit.putInt("month", month-1);
    			dateEdit.putInt("day", day);	    			
    			dateEdit.putString("selectedDate", selectedDate);
    			dateEdit.commit();   			
    			
    			currentDate.setText(selectText);   
    			
    			SharedPreferences dateSelect = getSharedPreferences("SearchDate", 0);
				SharedPreferences.Editor selectDate = dateSelect.edit();
				selectDate.putString("dateSelected",selectedDate);
				selectDate.commit();
    			
    			getDealList(1, diningId,diningStyle);
    		}
    	}
    }
    
    private String getTextFromDate(int year,int month,int day)
    {
    	String result =null;
    	Log.v("selectedDate is", selectedDate);
		
		Calendar selectCal = Calendar.getInstance();
		selectCal.set(Calendar.YEAR, year);
		selectCal.set(Calendar.MONTH, month-1);
		selectCal.set(Calendar.DAY_OF_MONTH, day);
		selectCal.set(Calendar.HOUR_OF_DAY, 0);
		selectCal.set(Calendar.MINUTE, 0);
		selectCal.set(Calendar.SECOND, 0);
		selectCal.set(Calendar.MILLISECOND, 0);
		Date selectDate = selectCal.getTime();
		
		Calendar today = Calendar.getInstance();
		today.setTime(today.getTime());
		Date current = today.getTime();
		
		today.add(Calendar.DAY_OF_MONTH,1);
		Date tomorrowDate = today.getTime();
		
		String dateSelect = new SimpleDateFormat("MMM d yyyy ").format(selectDate);
		String todaysDate = new SimpleDateFormat("MMM d yyyy ").format(current);
		String tommorwsDate = new SimpleDateFormat("MMM d yyyy ").format(tomorrowDate);	 
		
		if(dateSelect.equalsIgnoreCase(todaysDate))
			result ="Today";
		else if(dateSelect.equalsIgnoreCase(tommorwsDate))
			result= "Tomorrow";
		else{	    				
			result =dateSelect;	   
		}
		return result;
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	storeSliderPosition();
    }
    
    @Override
	public boolean onKeyDown(int keycode, KeyEvent e)
    {
	    switch(keycode)
	    {
	        case KeyEvent.KEYCODE_MENU:
	        	Intent mainMenuIntent = new Intent(MappingActivity.this,MainMenuActivity.class);
				mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mainMenuIntent);
	            return true;
	        case KeyEvent.KEYCODE_SEARCH:
	        	Intent searchIntent=new Intent(MappingActivity.this, SearchActivity.class);
				searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(searchIntent);
	            return true;
	    }

	    return super.onKeyDown(keycode, e);
	}
    /**
	 * Populate the radius wheel
	 */
	private void populateRadius(WheelView radiusWheel, String[] disvalues,int index)
	{
		CustomWheelAdapter adapter = new CustomWheelAdapter(this, disvalues);
		adapter.setTextSize(17);
		radiusWheel.setViewAdapter(adapter);
		radiusWheel.setCurrentItem(index);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		LocationManagerToggle.getInstance().cancelTimer(t);
		LocationManagerToggle.getInstance().removeCurentLocationUpdate();
		SharedPreferences spreferences = getSharedPreferences(AppConstant.SELECTED_PREFS, 0);
		SharedPreferences.Editor editor = spreferences.edit();
		editor.putInt(AppConstant.KEY_SELECTED_ITEM,ga.getSelectedItemPosition());
		editor.commit();
	}
	
	
}