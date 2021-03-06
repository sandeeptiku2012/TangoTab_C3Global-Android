package com.tangotab.appNotification.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.tangotab.contactSupport.activity.ContactSupportActvity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.login.vo.LoginVo;
import com.tangotab.myOfferDetails.dao.MyOffersDetailDao;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.myOffers.service.MyOffersService;

/**
 * class will be used for get App notifications for expired offers and for all
 * local notifications.
 * 
 * @author Dillip.Lenka
 * 
 */

public class AppNotificationActivity extends Activity {
	private static final int DIALOG_ALARM = 0;
	private AlertDialog dlg = null;
	private boolean showingDialog=false;
	private OffersDetailsVo offersDetailsVo=null;
	private Activity act;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		act=this;
		Bundle bundle = getIntent().getExtras();
		
		if (bundle != null) {
			/**
			 * Retrieve the Offers Details Vo object from the intent.
			 */
			offersDetailsVo = (OffersDetailsVo) bundle.getParcelable("selectOffers");
		}
				
		
		//check if internet is available or not 
		if(AppConstant.isNetworkAvailable(this)){
			
		if(offersDetailsVo!=null){
				if(offersDetailsVo.getDealId().equalsIgnoreCase(offersDetailsVo.getDealId()) && Integer.parseInt(offersDetailsVo.getIsConsumerShownUp())==0)
				{	
					String dealId = offersDetailsVo.getDealId();
					SharedPreferences spc2 = getSharedPreferences("AppNotification", 0);
					String localDealId = spc2.getString("dealId", "");
					// super.onCreateDialog(id);
						showDialog(DIALOG_ALARM);
						showingDialog=true;
				
				}
		if(!showingDialog)
			AppNotificationActivity.this.finish();
		}
		
		}
		else 
		{
			AppNotificationActivity.this.finish();
		}
		
	//	AppNotificationActivity.this.finish();
		// Get the alarm ID from the intent extra data
			//showDialog(DIALOG_ALARM);
		
	}

	/**
	 * Method will be used for display dialog boxes.
	 */
	protected Dialog onCreateDialog(int id) {
		// super.onCreateDialog(id);
		// Build the dialog
		AlertDialog.Builder alert = null;
		alert = new AlertDialog.Builder(this);
		alert.setTitle("TangoTab");
		String offerDate = offersDetailsVo.getReserveTimeStamp();
	     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a:ss");
	        Date testDate = null;
	        try {
	            testDate = sdf.parse(offerDate);
	        }catch(Exception ex){
	            ex.printStackTrace();
	        }
	        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd,yyyy");
	        String newFormat = formatter.format(testDate);
		/*String formattedOfferDate = DateFormatUtil
				.getconvertdate(offerDate);*/
		if (!ValidationUtil.isNull(offersDetailsVo)) {
			StringBuilder notifyMessage = new StringBuilder();
			notifyMessage.append("We hope you enjoyed your visit to ")
					.append(offersDetailsVo.getBusinessName())
					.append(" on ").append(newFormat)
					.append(".\r\n").append("Were you able to attend?");
			alert.setMessage(notifyMessage.toString());
		}
		alert.setCancelable(false);

		// Remove title bar
		/*this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Remove notification bar
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
		/**
		 * put the dela id into SharedPreferences
		 */
		SharedPreferences spc = getSharedPreferences("AppNotification", 0);
		SharedPreferences.Editor edit = spc.edit();
		edit.putString("dealId", offersDetailsVo.getDealId());
		edit.commit();

		// Dialog Yes Button confirmation
		alert.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						/**
						 * Check in the offer after the app notification
						 */
						if (!ValidationUtil.isNull(offersDetailsVo)) {

							doCheckIn("A");
						}
						
					//	AppNotificationActivity.this.finish();
					}
				});

		/**
		 * Dialog No Button confirmation
		 */
		alert.setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						/**
						 * Check in the offer after the appNotification
						 */
						if (!ValidationUtil.isNull(offersDetailsVo)) {
							doCheckIn("NA");
						}
						//AppNotificationActivity.this.finish();
					}
				});

		// Create and return the dialog
		dlg = alert.create();
		
		return dlg;
	}

	/**
	 * This method will take care all the check in functionality.
	 * 
	 * @param autoCheckIn
	 */
	private void doCheckIn(String status) {

		if (AppConstant.isNetworkAvailable(this)) {
			// Get all user details from sharedPreferences.
			SharedPreferences spc = getSharedPreferences("UserDetails", 0);
			String firstName = spc.getString("firstName", "");
			String lastName = spc.getString("lastName", "");
			offersDetailsVo.setFirstName(firstName);
			offersDetailsVo.setLastName(lastName);
			offersDetailsVo.setAutoCheckIn(status);
			/**
			 * Call the asyntask to execute the service
			 */
			new InsertDealAsyncTask().execute(offersDetailsVo);
		}

	}

	/**
	 * This call will be used to run the check in service in different thread.
	 * 
	 * @author dillip.lenka
	 * 
	 */
	class InsertDealAsyncTask extends AsyncTask<OffersDetailsVo, Void, Void> {

		@Override
		protected Void doInBackground(OffersDetailsVo... offersDetailsVo) {
			try {
				/*MyOffersDetailService service = new MyOffersDetailService();
				service.checkIn(offersDetailsVo[0]);*/
				MyOffersDetailDao dao = new MyOffersDetailDao();
				Object checkinMessage = dao.doAppNotificationCheckIn(offersDetailsVo[0]);
			} catch (Exception e) {
				Log.e("Error",
						"Error ocuuered in invoking check in service url and detailurl =",
						e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			SharedPreferences spc = getSharedPreferences("AppNotification", 0);
			SharedPreferences.Editor edit = spc.edit();
			edit.putBoolean("isCheckInOccured", true);
			edit.commit();
			SharedPreferences userDetails = act.getSharedPreferences("UserName", 0);
			String userId = userDetails.getString("username", "");
			String password = userDetails.getString("password", "");
			LoginVo userloginvo = new LoginVo();
			userloginvo.setUserId(userId);
			userloginvo.setPassword(password);
			new MyOffersListAsyncTask().execute(userloginvo);
		}
	}

	private boolean checkInternetConnection() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable() && conMgr
				.getActiveNetworkInfo().isConnected()) ? true : false;

	}
	
	public class MyOffersListAsyncTask extends
	AsyncTask<LoginVo, Void, List<OffersDetailsVo>> {

		@Override
		protected List<OffersDetailsVo> doInBackground(LoginVo... loginVo) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		
			List<OffersDetailsVo> offersList = null;
			try {
				MyOffersService service = new MyOffersService();
				offersList = service.getOffers(1, loginVo[0]);
		
			} catch (ConnectTimeoutException e) {
				Log.e("ConnectTimeoutException occured get list of offers", "",
						e);
				Intent contactIntent = new Intent(getApplicationContext(),
						ContactSupportActvity.class);
				startActivity(contactIntent);
			} catch (Exception e) {
				Log.e("Exception occured get list of offers", "", e);
			}
			return offersList;
		}
		
		@Override
		protected void onPostExecute(List<OffersDetailsVo> offersList) {
			TangoTab application = (TangoTab) act.getApplication();
			application.setOffersList(offersList);
			AppNotificationActivity.this.finish();

		}
}
	
}
