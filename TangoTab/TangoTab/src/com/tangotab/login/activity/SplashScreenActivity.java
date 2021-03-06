package com.tangotab.login.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;

import com.crashlytics.android.Crashlytics;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.gcm.GCMRegistrar;
import com.tangotab.R;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.core.vo.DeviceResponse;
import com.tangotab.facebook.activity.FacebookLogin;
import com.tangotab.login.service.LoginService;
import com.tangotab.nearMe.activity.NearMeActivity;
/**
 * This activity will be our start activity 
 * 
 * <br> Class :SplashScreenActivity
 * <br> Layout:splashscreen.xml
 * 
 * @author dillip.lenka
 *
 */

public class SplashScreenActivity extends Activity
{
	private LocationManager locationManager;
	private GoogleAnalyticsTracker tracker;
	private String regId;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(R.layout.splashscreen);
		System.out.println("----------onCreate---------------");

		/* getting location of device */
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		/*	boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(!isGPS)
		{
			showDialog(0);
		}*/
		/**
		 * Google analytics implementation added here.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(AppConstant.GA_REG_KEY,10, this);
		tracker.setDebug(true);
		tracker.trackPageView(AppConstant.SPLASH_SCREEN);
		tracker.trackEvent("SplashScreen", "TrackEvent", "splashscreen", 1);	

		//Checks whether the the device supports GCM
		GCMRegistrar.checkDevice(this);

		/**
		 * verifies that the application manifest contains, meets 
		 * all the requirements for gcm.You can remove this after
		 * testing gcm successfully
		 */
		GCMRegistrar.checkManifest(this);

		regId = GCMRegistrar.getRegistrationId(this);

		Log.w("regId",regId);
		if(checkInternetConnection()){	
			System.out.println("----------checkInternetConnection---------------");
			new DeviceAsyncTask().execute();
		}
		else{
			System.out.println("----------checkInternetConnection Fails---------------");
			showDialog(15);	
		}

	}


	@SuppressWarnings("unused")
	private final LocationListener locationListener = new LocationListener()
	{ 
		public void onLocationChanged(Location location){ 
			updateWithNewLocation(location);
			locationManager.removeUpdates(locationListener);
		}

		public void onProviderDisabled(String provider){
			updateWithNewLocation(null);
		}

		public void onProviderEnabled(String provider) 
		{

		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{

		}
	};

	/**
	 * This method will be update with the new location.
	 * 
	 * @param location
	 */
	private void updateWithNewLocation(Location location){
		if(location!=null){
			/**
			 * Put the current location latitude and longitude into shared preferences.
			 */
			SharedPreferences currentLocation = getSharedPreferences("LocationDetails", 0);
			SharedPreferences.Editor edit = currentLocation.edit();
			edit.putString("locLat", String.valueOf(location.getLatitude()));
			edit.putString("locLong", String.valueOf(location.getLongitude()));
			edit.commit();
		}
	}

	/**
	 * This class will be used to display the Map
	 * 
	 * @author Dillip.Lenka
	 *
	 */
	public class DeviceAsyncTask extends AsyncTask<Void, Void, DeviceResponse>
	{
		@Override
		protected DeviceResponse doInBackground(Void... params){
			DeviceResponse deviceResponse = null;
			String serviceName = Context.TELEPHONY_SERVICE;
			TelephonyManager m_telephonyManager = (TelephonyManager) getSystemService(serviceName);
			try{
				String deviceId = m_telephonyManager.getDeviceId();
				LoginService loginService = new LoginService();
				deviceResponse= loginService.getDeviceDetails(deviceId);
			}
			catch(Exception e)
			{
				Log.e("Exception occured", "Exception occured at the time of login",e);
			}
			return deviceResponse;
		}
		@Override
		protected void onPostExecute(DeviceResponse result){
			//timeExecutes = Integer.parseInt(result.gettTL())*3600000;
			boolean isRun =true;
			if(result!=null && result.getResponsCode().equals("200") && result.getAppStatus().equals("0")){		
				isRun = false;
				showDialog(100);
			}
			
			if(isRun){
				SharedPreferences date = getSharedPreferences("SearchDate", 0);
				SharedPreferences.Editor ScrollEdit = date.edit();
				ScrollEdit.putString("dateSelected", "");
				ScrollEdit.commit();

				SharedPreferences spf1 = getSharedPreferences("UserDetails", 0);
				SharedPreferences.Editor edt1=spf1.edit();
				edt1.putString("country", "USA");
				edt1.commit();

				SharedPreferences spreferences = getSharedPreferences(AppConstant.SELECTED_PREFS, 0);
				SharedPreferences.Editor editor = spreferences.edit();
				editor.putInt(AppConstant.KEY_SELECTED_ITEM,0);
				editor.commit();

				//Check whether the device was already registered
				if (regId.equals("")){		
					GCMRegistrar.register(getApplicationContext(),AppConstant.GCM_APP_ID);
				}

				new Handler().postDelayed(new Runnable()
				{
					public void run() 
					{
						SharedPreferences spc = getSharedPreferences("UserName", 0);
						String userName =	spc.getString("username", "");
						String password =spc.getString("password", "");

						if(!ValidationUtil.isNullOrEmpty(userName) && !ValidationUtil.isNullOrEmpty(password))
						{
							Intent nearIntent = new Intent(SplashScreenActivity.this, NearMeActivity.class);
							nearIntent.putExtra("frmPage", "splashScreen");
							nearIntent.putExtra("isGetStarted", "true");
							startActivity(nearIntent);	
						}
						else{
							Intent loginIntent = new Intent(SplashScreenActivity.this, FacebookLogin.class);
							startActivity(loginIntent);
						}

						finish();
					}
				}, 2000);

			}

		}		
	}
	/**
	 * This method will check the Internet connection for the application.
	 * 
	 * @return
	 */
	private boolean checkInternetConnection() 
	{
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())? true:false;

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			AlertDialog.Builder a = new AlertDialog.Builder(SplashScreenActivity.this);
			a.setTitle("TangoTab");
			a.setMessage("Your device is configured to deny location services to TangoTab. Some features of Tangotab require these services to be enabled . Please enable location services for TangoTab app in the device settings  to enable all app features.");
			a.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return a.create();

		case 100:
			AlertDialog.Builder ab = new AlertDialog.Builder(SplashScreenActivity.this);
			ab.setTitle("TangoTab");
			ab.setMessage("Application seems to have some issues.Please Contact TangoTab");
			return ab.create();

		case 15:
			AlertDialog.Builder ab15 = new AlertDialog.Builder(SplashScreenActivity.this);
			ab15.setTitle("TangoTab");
			ab15.setMessage("We are unable to make an internet connection at this time.Some functionalities will be limited until a connection is made.");
			ab15.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent loginIntent = new Intent(SplashScreenActivity.this,
							LogInActivity.class);
					startActivity(loginIntent);
				}
			});
			return ab15.create();
		}
		return null;
	}

}
