package com.tangotab;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tangotab.appNotification.activity.AppNotificationActivity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.DateFormatUtil;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.myOfferDetails.activity.AutoCheckinActivity;
import com.tangotab.myOffers.Vo.OffersDetailsVo;

public class LocationManagerToggle implements LocationListener{

	private LocationManager locationManager;
	private static LocationManagerToggle locationManagerToggle=null;
	private String provider;
	private Context con=null;
	Activity act=null;
	private Timer t;
	private boolean canGetLocation;
	
	public static LocationManagerToggle getInstance()
	{
		if(locationManagerToggle==null)
			locationManagerToggle=new LocationManagerToggle();
						
		return locationManagerToggle;
	}

	public void initalizeLocationManagerService(Activity activity,Context context) {
		/*
		 * GPS Configuration
		 */
		removeCurentLocationUpdate();
		act=activity;
		con=context;
		/*locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		 List<String> providers = locationManager.getProviders(true);
		 Location bestLocation = null;
		 String bestProvider=null;
		 for (String provider : providers) {
		        Location l = locationManager.getLastKnownLocation(provider);
		        
		        if (l == null || provider.equalsIgnoreCase("passive")) {
		            continue;
		        }
		        if (bestLocation == null
		                || l.getAccuracy() < bestLocation.getAccuracy()) {
		              bestLocation = l;
		              bestProvider=provider;
		        }
		    }		    
		
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		//Location location = locationManager.getLastKnownLocation(provider);
		 if(bestProvider!=null)
			 	locationManager.requestLocationUpdates(bestProvider,60000, 0, this);
		 else 
		 {
			 Toast.makeText(act, "Unable to get any provider for getting location details!!", Toast.LENGTH_SHORT).show();
		 }*/

		Location loc=getLocation();
		// Initialize the location fields
		if (!ValidationUtil.isNull(loc)) {
			Log.i("GEO LOCATION", "Provider " + provider
					+ " has been selected.");
			double lat = loc.getLatitude();
			double lng = loc.getLongitude();
			Log.i("GEO LOCATION DETAILS", "LAT and Long " + lat + "  " + lng);
			// Push the Lat Lng values into Global Execution Context
			AppConstant.dev_lat = lat;
			AppConstant.dev_lang = lng;
			SharedPreferences currentLocation = context.getSharedPreferences("LocationDetails", 0);
			SharedPreferences.Editor edit = currentLocation.edit();
			edit.putString("locLat", String.valueOf(lat));
			edit.putString("locLong", String.valueOf(lng));
			edit.commit();
		} else {
			Log.i("GEO LOCATION", "Provider is not available");
			SharedPreferences currentLocation = context.getSharedPreferences(
					"LocationDetails", 0);
			SharedPreferences.Editor edit = currentLocation.edit();
			edit.putString("locLat", "0.0");
			edit.putString("locLong", "0.0");
			edit.commit();
			AppConstant.dev_lat = 0.0;
			AppConstant.dev_lang = 0.0;
		}
	}
	
	public void removeCurentLocationUpdate()
	{
		if(locationManager!=null)
		{
			locationManager.removeUpdates(this);
			
		}
		
	}

	@Override
	public void onLocationChanged(Location location) {
		if(AppConstant.isNetworkAvailable(con))
		{
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		Log.i("LocationChanged", "Lat = " + lat + "   : Lng = " + lng);
		//Toast.makeText(con, "Lat : "+lat+", Long : "+lng, Toast.LENGTH_SHORT).show();
		// Push the Lat Lng values into Global Execution Context
		AppConstant.dev_lat = lat;
		AppConstant.dev_lang = lng;	
		TangoTab application = (TangoTab) act.getApplication();
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(act,con.getApplicationContext(),offerList);
		activity.doCheckIn();
		
		
		//sendingAppNotification(offerList);
		}
	}
	
	public Timer setTimer(final List<OffersDetailsVo> offersList,long initDelay,long interval)
	{
		t = new Timer();
		//Set the schedule function and rate
		t.scheduleAtFixedRate(new TimerTask() {

		    @Override
		    public void run() {
		    	
		    	sendingAppNotification(offersList);
		    }
		         
		},
		//Set how long before to start calling the TimerTask (in milliseconds)
		Long.parseLong(ValidationUtil.isNullOrEmpty(""+initDelay)?""+0:initDelay+"")
		,
		//Set the amount of time between each execution (in milliseconds)
		Long.parseLong(ValidationUtil.isNullOrEmpty(""+interval)?""+5000:interval+""));
		return t;
		
	}	
	
	public void cancelTimer(Timer t){
		if(t!=null)
			t.cancel();
	}
	
	public void sendingAppNotification(List<OffersDetailsVo> offersList)
	{
		if(ValidationUtil.isNullOrEmpty(offersList))
		{	TangoTab application = (TangoTab) act.getApplication();
			offersList = application.getOffersList();
			
		}
		
		if(!ValidationUtil.isNullOrEmpty(offersList) && AppConstant.isNetworkAvailable(con))
			{
			for (OffersDetailsVo offersDetailsVo : offersList) {
				int isCheckin = 0;
				if (!ValidationUtil.isNullOrEmpty(offersDetailsVo
						.getIsConsumerShownUp())) {
					isCheckin = Integer.parseInt(offersDetailsVo
							.getIsConsumerShownUp());
				}
				Log.v("isCheckin = ", String.valueOf(isCheckin));
				/**
				 * If the offer neither manual or auto check in
				 */
				if (isCheckin == 0) {
					String reserveDate = offersDetailsVo.getReserveTimeStamp();
					StringBuilder finalClaimDate = new StringBuilder();
					String claimDate = null;
					if (!ValidationUtil.isNullOrEmpty(reserveDate)) {
						int index = reserveDate.indexOf(" ");
						claimDate = offersDetailsVo.getEndTime();
						Log.v("ClaimDate is ", claimDate);
						finalClaimDate
								.append(reserveDate.substring(0, index).trim())
								.append(" ").append(claimDate);
						Log.v("finalClaimDate is ", finalClaimDate.toString());
					}
	
					Date finalEndTime = DateFormatUtil.parseIntoDifferentFormat(
							finalClaimDate.toString(), "yyyy-MM-dd hh:mm aa");
	
					if (claimDate.equalsIgnoreCase("12:00 AM")) {
						Calendar c = Calendar.getInstance();
						c.setTime(finalEndTime);
						c.add(Calendar.DATE, 1);
						finalEndTime = c.getTime();
					}
	
					if (finalEndTime != null) {
						// Check whether the offer expired or not.
						String currentDate = offersDetailsVo.getCurrentDate();
						Date current = DateFormatUtil.parseIntoDifferentFormat(
								currentDate, "yyyy-MM-dd HH:mm:ss.SSSSSS");
						boolean isExpiredOffer = (current.after(finalEndTime)) ? true
								: false;
	
						Log.v("isExpiredOffer = ", String.valueOf(isExpiredOffer));
						finalEndTime.setTime(finalEndTime.getTime()+3600000);
						current=new Date();
						if (isExpiredOffer || current.after(finalEndTime)) {
							Bundle bundle = new Bundle();
							bundle.putParcelable("selectOffers", offersDetailsVo);
							Intent appNotification = new Intent(
									act.getApplicationContext(),
									AppNotificationActivity.class);
							appNotification.putExtras(bundle);
							act.startActivity(appNotification);
							/**
							 * Remove the local notification
							 */
							int dealId = 0;
							if (!ValidationUtil.isNullOrEmpty(offersDetailsVo
									.getDealId()))
								dealId = Integer.parseInt(offersDetailsVo
										.getDealId());
	
							Log.v("dealId for remove local Notification ",
									String.valueOf(dealId));
	
							Intent alarmIntent = new Intent(
									AppConstant.ALARM_ACTION_NAME);
							PendingIntent pendingIntent = PendingIntent
									.getBroadcast(act.getApplicationContext(), dealId,
											alarmIntent,
											PendingIntent.FLAG_UPDATE_CURRENT);
							AlarmManager alarmManager = (AlarmManager) act.getSystemService(act.ALARM_SERVICE);
							alarmManager.cancel(pendingIntent);
							pendingIntent.cancel();
	
						}
					}
				} else {
					//uncheckedOffers.add(offersDetailsVo);
					Log.v("Don't send local notification already checked in",
							String.valueOf(isCheckin));
				}
			}
	}
		
}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	
	public Location getLocation() {
	    Location location=null;
		try {
	        locationManager = (LocationManager) con
	                .getSystemService(Context.LOCATION_SERVICE);

	        // getting GPS status
	        boolean isGPSEnabled = locationManager
	                .isProviderEnabled(LocationManager.GPS_PROVIDER);

	        // getting network status
	        boolean isNetworkEnabled = locationManager
	                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	        if (!isGPSEnabled && !isNetworkEnabled) {
	            // no network provider is enabled
	        } else {
	            canGetLocation = true;
	            double latitude;
				double longitude;
				if (isNetworkEnabled) {
	                locationManager.requestLocationUpdates(
	                        LocationManager.NETWORK_PROVIDER,
	                       10000,
	                        0, this);
	                Log.d("Network", "Network Enabled");
	                if (locationManager != null) {
	                    location = locationManager
	                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                    if (location != null) {
	                        latitude = location.getLatitude();
	                        longitude = location.getLongitude();
	                    }
	                }
	            }
	            // if GPS Enabled get lat/long using GPS Services
	            if (isGPSEnabled) {
	                if (location == null) {
	                    locationManager.requestLocationUpdates(
	                            LocationManager.GPS_PROVIDER,
	                           10000,
	                            0, this);
	                    Log.d("GPS", "GPS Enabled");
	                    if (locationManager != null) {
	                        location = locationManager
	                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                        if (location != null) {
	                            latitude = location.getLatitude();
	                            longitude = location.getLongitude();
	                        }
	                    }
	                }
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return location;
	}
	
}
