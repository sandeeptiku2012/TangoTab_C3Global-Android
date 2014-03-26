package com.tangotab.myOfferDetails.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.DateFormatUtil;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.myOfferDetails.service.MyOffersDetailService;
import com.tangotab.myOffers.Vo.OffersDetailsVo;

public class AutoCheckinActivity extends Activity implements LocationListener
{
	
	private Activity act;
	private Context con;
	private OffersDetailsVo offersDetailsVo;
	private List<OffersDetailsVo> offerList;
	
	
	public AutoCheckinActivity(Activity act ,Context con,List<OffersDetailsVo> offerList ) {
		this.act =act;
		this.con=con;
		this.offerList = offerList;
		
	}
	
	private ProgressDialog mDialog;
	String message;
	
	LocationManager loc;
	
	
	private boolean checkInternetConnection() 
	{
		ConnectivityManager conMgr = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		return (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())? true:false;
		
	}

	public void doCheckIn()
	{
		//loc=(LocationManager) con.getSystemService(Context.LOCATION_SERVICE);
		
		/*boolean isGPS = loc.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(!isGPS)
		{
			showDialog(0);
		}*/
		
		if (checkInternetConnection())
		{
				//Get all user details from sharedPreferences.				
				SharedPreferences spc = con.getSharedPreferences("UserDetails", 0);
				String firstName = spc.getString("firstName", "");
				String lastName = spc.getString("lastName", "");
				
				/*if(offerList==null)
				{
					try{
					TangoTab application =(TangoTab) getApplication();
					offerList=application.getOffersList();
					}
					catch(Exception e)
					{
						Log.e("Error", "Error ocuuered on getting the offer list =",e);
					}
				}*/
				
				if(offerList!=null)
				{
				for(OffersDetailsVo indOffer:offerList)
				{
				
					indOffer.setFirstName(firstName);
					indOffer.setLastName(lastName);
					indOffer.setAutoCheckIn("Y");
					new InsertDealAsyncTask().execute(indOffer);
				}
				//loc.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 5, this);
				}			
		}
		else{
			//	act.showDialog(10);
			}
	}
	
	
	
	public class InsertDealAsyncTask extends AsyncTask<OffersDetailsVo , Void, String>
	{		
		

		@Override
		protected void onPreExecute()
		{
			/*mDialog = ProgressDialog.show(con, "Searching ", "Please wait...");
			mDialog.setCancelable(true);*/
		}
		
		@Override
		protected String doInBackground(OffersDetailsVo... offersDetailsVo)
		{
			message =null;
			try {
				MyOffersDetailService service = new MyOffersDetailService();
				message = service.checkIn(offersDetailsVo[0]);
				if(!ValidationUtil.isNullOrEmpty(message))
				{
					Toast.makeText(con, "Auto Checkin Occured for dealID : "+ offersDetailsVo[0].getDealId(),Toast.LENGTH_SHORT).show();
				}
			}
			catch (Exception e)
			{
				Log.e("Error", "Error ocuuered in invoking check in service url and detailurl =",e);
				message =null;
			}
			return message;
		}
		
		@Override
		protected void onPostExecute(String message) 
		{
			SharedPreferences spc = con.getSharedPreferences("AppNotification", 0);
			SharedPreferences.Editor edit = spc.edit();
			edit.putBoolean("isCheckInOccured",true);
			edit.commit();
			// mDialog.dismiss();
			if (message != null && message.equals("Successfully CheckIn.")) 
			{
				offersDetailsVo.setIsConsumerShownUp("1");
				Toast.makeText(con, message, Toast.LENGTH_LONG).show();	
	           
			}			
		
		}	

	}



	@Override
	public void onLocationChanged(Location location) {
		
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		List<OffersDetailsVo> afterFilterList=new ArrayList<OffersDetailsVo>();
		
		
		float results[]= new float[4];
		for(OffersDetailsVo offersDetailsVo: offerList)
		{
			Location.distanceBetween(lat, lng,Double.parseDouble(offersDetailsVo.getLatitude()), Double.parseDouble(offersDetailsVo.getLongitude()), results);
			if(results[0]<=50 && offersDetailsVo.getIsConsumerShownUp().equalsIgnoreCase("0"))//if(results[0]<=50)
			{
				afterFilterList.add(offersDetailsVo);
				//offersDetailsVo.setAutoCheckIn("Y");
				
				
				
			}
			
		}
		
		StringBuilder str=new StringBuilder();
		loc.removeUpdates(this);
		
		// autochecc
		if(afterFilterList.size()==1)
		{
			Boolean isTimeToAutoCheck =false;
			offersDetailsVo = afterFilterList.get(0);
			

			String reserveTime = offersDetailsVo.getReserveTimeStamp();
			StringBuilder stDate = new StringBuilder();
			StringBuilder endDate = new StringBuilder();
			if(!ValidationUtil.isNullOrEmpty(reserveTime))
			{
				int index = reserveTime.indexOf(" ");	
				String endTime = offersDetailsVo.getEndTime();
				if (endTime.equals("12:00 AM"))
				{
					endTime ="11:59 PM";
				}			
				String startTime = offersDetailsVo.getStartTime();
				if(index!=-1)
				{
					String reserveDate = reserveTime.substring(0,index).trim();
					String reserveStartTime = stDate.append(reserveDate).append(" ").append(startTime).toString();
					String  reserveEndTime  = endDate.append(reserveDate).append(" ").append(endTime).toString();
					Date finalReservStartTime = DateFormatUtil.dateAfterSomeTimePeriod(reserveStartTime,"mins",15,"yyyy-MM-dd hh:mm aa");
					Date finalReservEndTime = DateFormatUtil.dateAfterSomeTimePeriod(reserveEndTime,"hour",2,"yyyy-MM-dd hh:mm aa");
					Log.v(" finalReservStartTime is ", finalReservStartTime.toString());
					Log.v("finalReservEndTime is ", finalReservEndTime.toString());
					String currentDate = offersDetailsVo.getCurrentDate();
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
					String localTime = sdf.format(new Date());
					
	                if(!ValidationUtil.isNullOrEmpty(localTime))
	                {
	                	//Date current = DateFormatUtil.parseIntoDifferentFormat(currentDate,"yyyy-MM-dd HH:mm:ss.SSSSSS");
						Date currentLocalTime = DateFormatUtil.parseIntoDifferentFormat(localTime,"yyyy-MM-dd HH:mm:ss.SSSSSS");
	                	isTimeToAutoCheck = ((finalReservStartTime.before(currentLocalTime)) && (finalReservEndTime.after(currentLocalTime)))?true:false;
	                }
				}
			}
			//str.append(offersDetailsVo.getDealName()+"\n");
			String isConsumerShownUp = offersDetailsVo.getIsConsumerShownUp();
			int checkinStatus =0;
			if(!ValidationUtil.isNullOrEmpty(isConsumerShownUp))
				checkinStatus = Integer.parseInt(isConsumerShownUp);
			if( isTimeToAutoCheck && checkinStatus==0)
			{
				offersDetailsVo.setAutoCheckIn("Y");
				new InsertDealAsyncTask().execute(offersDetailsVo);
			}
			/*else
			{
				Toast.makeText(con,"Not correct time to auto check in", Toast.LENGTH_LONG).show();
			}*/
		
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
	
	/**
	 * Dialog message display.
	 */

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			AlertDialog.Builder ab = new AlertDialog.Builder(AutoCheckinActivity.this);
			ab.setTitle("TangoTab");
			ab.setMessage("Your device is configured to deny location services to TangoTab. Some features of Tangotab require these services to be enabled . Please enable location services for TangoTab app in the device settings  to enable all app features.");
			ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return ab.create();
		
		}
		return null;
	}
}
