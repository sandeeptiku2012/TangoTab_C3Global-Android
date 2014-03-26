package com.tangotab.myOfferDetails.dao;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.conn.ConnectTimeoutException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.location.Location;
import android.net.ParseException;
import android.util.Log;
import android.widget.Toast;

import com.tangotab.core.connectionManager.ConnectionManager;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.dao.TangoTabBaseDao;
import com.tangotab.core.ex.TangoTabException;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.signUp.xmlHandler.MessageHandler;

/**
 * Dao class for doing auto and manual check in.
 * 
 * @author Dillip.Lenka
 *
 */

public class MyOffersDetailDao extends TangoTabBaseDao
{
	/**
	 * do auto check in for the select offers .
	 * @param offersDetailsVo
	 * @return
	 * @throws TangoTabException
	 */
	public String doCheckIn(OffersDetailsVo offersDetailsVo)throws ConnectTimeoutException,TangoTabException
	{
		Log.v("Invoking doCheckIn method with parameter offersDetailsVo", offersDetailsVo.toString());
		String message=null;
		TangoTabBaseDao instance = TangoTabBaseDao.getInstance();
		ConnectionManager cManager = instance.getConManger();
		String checkinUrl = getCheckinUrl(offersDetailsVo);		
		MessageHandler messageHandler = new MessageHandler();
		instance.getXmlReader().setContentHandler(messageHandler);
		Log.v("checkinUrl is ", checkinUrl);
		cManager.setupHttpGet(checkinUrl);

		/*2013-11-12 09:01:06.518*/
		String dateString = offersDetailsVo.getCurrentDate();
		Date startTimeDate=new Date();
		Date endTimeDate=new Date();
		String startTime =dateString.split(" ")[0]+" "+ offersDetailsVo.getStartTime();
		String endTime =dateString.split(" ")[0]+" "+offersDetailsVo.getEndTime();
		Date currentDate= new Date();		
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
		Date offerDate=new Date();
		try{
			startTimeDate = simpledateformat.parse(startTime);
			endTimeDate = simpledateformat.parse(endTime);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		if(currentDate.getTime()>=startTimeDate.getTime()-900000 && currentDate.getTime()<=endTimeDate.getTime()+7200000)	     
		{
			if(Integer.parseInt(offersDetailsVo.getIsConsumerShownUp())==0)
			{
				double currentLat = AppConstant.dev_lat;
				double currentLang = AppConstant.dev_lang;
				Location offerLocation = new Location("offerLocation");
				Location devLocation = new Location("devLocation");
				offerLocation.setLatitude(Double.parseDouble(offersDetailsVo.getLatitude()));
				offerLocation.setLongitude(Double.parseDouble(offersDetailsVo.getLongitude()));
				devLocation.setLatitude(AppConstant.dev_lat);
				devLocation.setLongitude(AppConstant.dev_lang);
				Float distance = offerLocation.distanceTo(devLocation);
				distance = Math.abs(distance);
				if(distance<=300)
				{
					try {

						InputSource m_is = cManager.makeGetRequestGetResponse();
						if (m_is != null) {
							instance.getXmlReader().parse(m_is);
							message = messageHandler.getMessage();
							Log.v("Check in Message is", message);
						}
					}
					catch (ConnectTimeoutException e)
					{
						Log.e("ConnectTimeoutException", "ConnectTimeoutException occured in invoking check in service url and checkinUrl ="+checkinUrl);
						throw new ConnectTimeoutException(e.getLocalizedMessage());
					}
					catch (IOException e)
					{
						Log.e("Error", "IOException occured in invoking check in service url and checkinUrl ="+checkinUrl);
						throw new TangoTabException("MyOffersDetailDao", "doCheckIn", e);
					} catch (SAXException e)
					{
						Log.e("Error", "SAXException ocuuered in invoking check in service url and checkinUrl ="+checkinUrl);
						throw new TangoTabException("MyOffersDetailDao", "doCheckIn", e);
					}

				}
			}
		}
		return message;
	}

	//autocheckIn through app Notification
	public String doAppNotificationCheckIn(OffersDetailsVo offersDetailsVo)throws ConnectTimeoutException,TangoTabException
	{
		Log.v("Invoking doCheckIn method with parameter offersDetailsVo", offersDetailsVo.toString());
		String message=null;
		TangoTabBaseDao instance = TangoTabBaseDao.getInstance();
		ConnectionManager cManager = instance.getConManger();
		String checkinUrl =getCheckinUrl(offersDetailsVo);		
		MessageHandler messageHandler = new MessageHandler();
		instance.getXmlReader().setContentHandler(messageHandler);
		Log.v("checkinUrl is ", checkinUrl);
		cManager.setupHttpGet(checkinUrl);
		try {
			InputSource m_is = cManager.makeGetRequestGetResponse();
			if (m_is != null) {
				instance.getXmlReader().parse(m_is);
				message = messageHandler.getMessage();
				Log.v("Check in Message is", message);
			}
		}catch (ConnectTimeoutException e)
		{
			Log.e("ConnectTimeoutException", "ConnectTimeoutException occured in invoking check in service url and checkinUrl ="+checkinUrl);
			throw new ConnectTimeoutException(e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			Log.e("Error", "IOException occured in invoking check in service url and checkinUrl ="+checkinUrl);
			throw new TangoTabException("MyOffersDetailDao", "doCheckIn", e);
		} catch (SAXException e)
		{
			Log.e("Error", "SAXException ocuuered in invoking check in service url and checkinUrl ="+checkinUrl);
			throw new TangoTabException("MyOffersDetailDao", "doCheckIn", e);
		}
		return message;
	}
	/**
	 * Cancel the offer from the user request.
	 * 
	 * @param conResId
	 * @return
	 * @throws TangoTabException
	 */
	public String cancelOffer(String conResId)throws ConnectTimeoutException,TangoTabException
	{
		Log.v("Invoking cancelOffer method with parameter conResId", conResId);
		String message=null;
		TangoTabBaseDao instance = TangoTabBaseDao.getInstance();
		ConnectionManager cManager = instance.getConManger();
		String cancelUrl = getCancelUrl(conResId);		
		MessageHandler messageHandler = new MessageHandler();
		instance.getXmlReader().setContentHandler(messageHandler);
		Log.v("cancelUrl is ", cancelUrl);
		cManager.setupHttpGet(cancelUrl);	
		try {
			InputSource m_is = cManager.makeGetRequestGetResponse();
			if (m_is != null) {
				instance.getXmlReader().parse(m_is);
				message = messageHandler.getMessage();
				Log.v("Cancel offer Message is", message);
			}
		}catch (ConnectTimeoutException e)
		{
			Log.e("ConnectTimeoutException", "ConnectTimeoutException occured in invoking cancelOffer method ="+cancelUrl);
			throw new ConnectTimeoutException(e.getLocalizedMessage());
		}
		catch (IOException e)
		{
			Log.e("Error", "IOException occured in invoking cancelOffer method ="+cancelUrl);
			throw new TangoTabException("MyOffersDetailDao", "cancelOffer", e);
		} catch (SAXException e)
		{
			Log.e("Error", "SAXException ocuuered in invoking cancel the offer ="+cancelUrl);
			throw new TangoTabException("MyOffersDetailDao", "cancelOffer", e);
		}
		return message;
	}


	/**
	 * Get the check in url for auto check in and mannual check in.
	 * @param offersDetailsVo
	 * @return
	 */
	private String getCheckinUrl(OffersDetailsVo offersDetailsVo)
	{
		Log.v("Invoking getCheckinUrl() method with parameter offersDetailsVo =", offersDetailsVo.toString());
		String checkinUrl =null;
		String mCurrentDate = offersDetailsVo.getCurrentDate();
		Log.v("presentdate", "presentdate" + mCurrentDate);
		if(!ValidationUtil.isNullOrEmpty(mCurrentDate))
		{

			String consumerresId = offersDetailsVo.getConResId();
			String restname = offersDetailsVo.getBusinessName();
			String dealname = offersDetailsVo.getDealName();
			String dealdetails = offersDetailsVo.getDealDescription();
			String deal_manager_emailid = offersDetailsVo.getDealManagerEmailId();
			if (dealname.contains(" ")) {
				dealname = dealname.replace(" ", "%20");
			}
			if (dealdetails.contains(" ")) {
				dealdetails = dealdetails.replace(" ", "%20");
			}
			if (restname.contains(" ")) {
				restname = restname.replace(" ", "%20");
			}
			if (deal_manager_emailid.contains(" ")) {
				deal_manager_emailid = deal_manager_emailid.replace(" ","%20");
			}
			if (dealdetails.contains("%"))
			{
				dealdetails = dealdetails.replace("%", "%25");
				if (dealdetails.contains(" "))
				{
					dealdetails = dealdetails.replace(" ", "%20");
				}
			}
			/**
			 * Generate the Checkin service url
			 */
			checkinUrl = AppConstant.baseUrl+ "/mydeals/checkin?" +
					"consumerresId="+ TangoTabBaseDao.encodeURI(consumerresId) + 
					"&name=" + TangoTabBaseDao.encodeURI(offersDetailsVo.getFirstName()) + TangoTabBaseDao.encodeURI(offersDetailsVo.getLastName()) + 
					"&restname=" + TangoTabBaseDao.encodeURI(offersDetailsVo.getBusinessName()) + 
					"&dealname=" + TangoTabBaseDao.encodeURI(offersDetailsVo.getDealName()) + 
					"&dealdetails="	+ TangoTabBaseDao.encodeURI(dealdetails) + 
					"&coordinate="+ AppConstant.locationLat + ","+ AppConstant.locationLong + 
					"&restEmailId="+ TangoTabBaseDao.encodeURI(deal_manager_emailid) + 
					"&autocheckin="+ TangoTabBaseDao.encodeURI(offersDetailsVo.getAutoCheckIn());	
			
			Log.v("final checkinUrl is", checkinUrl);					


		}
		return checkinUrl;
	}
	/**
	 * Generate cancel URL from conResId.
	 * @param conResId
	 * @return
	 */
	public String getCancelUrl(String conResId)
	{
		Log.v("Invoking getCancelUrl() method with parameter conResId =", conResId);
		String cancelUrl = null;
		if(ValidationUtil.isNullOrEmpty(conResId))
			return cancelUrl;
		cancelUrl =AppConstant.baseUrl+ "/deals/removedeal?conResId="+ conResId;
		Log.v("cancelUrl is", cancelUrl);
		return cancelUrl;
	}
}
