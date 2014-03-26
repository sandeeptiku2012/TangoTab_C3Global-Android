package com.tangotab.localNotification.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.tangotab.R;
import com.tangotab.appNotification.activity.AppNotificationActivity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.utils.DateFormatUtil;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.nearMe.activity.NearMeActivity;
/**
 * This class will receive the alarm intent from broad cast and send it to Local notification.
 * 
 * @author Dillip.Lenka
 *
 */
public class LocalNotificationReceiver extends BroadcastReceiver
{
	private OffersDetailsVo  offersDetailsVo;
	private String offerDate;
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		
		String action = intent.getAction();
		 Bundle receiverBundle = intent.getExtras();
		 offersDetailsVo= (OffersDetailsVo)receiverBundle.getParcelable("selectOffers");
		 
		// Handle the alarm broadcast
		if (AppConstant.ALARM_ACTION_NAME.equals(action))
		{
			/**
			 * Put the consumer deals object into alarm intent.
			 */
			/*Bundle b = new Bundle();
	        b.putParcelable("selectOffers", offersDetailsVo);			
			Intent alarmIntent = new Intent("android.intent.action.MAIN");
			alarmIntent.putExtras(b);
			alarmIntent.setClass(context, LocalNotificationActivity.class);
			alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);			
			context.startActivity(alarmIntent);*/
			
			String dealId = offersDetailsVo.getDealId();
			SharedPreferences spc2 = context.getSharedPreferences("LocalNotification", 0);
			String localDealId = spc2.getString("dealId", "");
			if(ValidationUtil.isNullOrEmpty(localDealId) || !(localDealId.equals(dealId)))
			{
				offerDate = offersDetailsVo.getReserveTimeStamp();
				String formattedOfferDate = DateFormatUtil.getconvertdate(offerDate);
				StringBuilder notifyMessage = new StringBuilder();
				
				//generating message for the notification
				if(!ValidationUtil.isNull(offersDetailsVo))
				{			
					notifyMessage.append("We hope you enjoyed your visit to ").append(offersDetailsVo.getBusinessName()).append(" on ").append(formattedOfferDate).append(". Please let us know if you were able to attend");
				}
				
				SharedPreferences spc = context.getSharedPreferences("LocalNotification", 0);
				SharedPreferences.Editor edit = spc.edit();
				edit.putString("dealId", offersDetailsVo.getDealId());
				edit.commit();
				
				//Intent toIntent = new Intent(context, NearMeActivity.class);
				
				Bundle bundle = new Bundle();
				bundle.putParcelable("selectOffers", offersDetailsVo);	
		        Intent appNotificationIntent = new Intent(context,AppNotificationActivity.class);
				appNotificationIntent.putExtras(bundle);
								
			    PendingIntent pIntent = PendingIntent.getActivity(context, 0, appNotificationIntent, 0);
				NotificationCompat.Builder mBuilder =
				            new NotificationCompat.Builder(context)
				            .setSmallIcon(R.drawable.tangotab_icon)
				            .setContentTitle("Tango Tab")
				            .setContentText(notifyMessage);
			    mBuilder.setContentIntent(pIntent);
			    mBuilder.setAutoCancel(true);
			    Notification toNotify= mBuilder.build();
			    toNotify.flags |= Notification.FLAG_AUTO_CANCEL;
			    NotificationManager mNotificationManager =
			    	    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			    	// mId allows you to update the notification later on.
		    	mNotificationManager.notify(5, mBuilder.build());
			}
		}
	}
}