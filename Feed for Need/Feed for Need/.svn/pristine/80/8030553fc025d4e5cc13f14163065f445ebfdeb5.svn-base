package com.tangotab.search.dao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

import com.tangotab.core.connectionManager.ConnectionManager;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.dao.TangoTabBaseDao;
import com.tangotab.core.ex.TangoTabException;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.nearMe.vo.DealsDetailVo;
import com.tangotab.nearMe.xmlHandler.DealDetailHandler;
import com.tangotab.search.vo.SearchVo;
/**
 * Get list of Deals from the search criteria.
 * 
 * @author dillip.lenka
 *
 */
public class SearchDao extends TangoTabBaseDao
{
	/**
	 * Get list of deals form search criteria.
	 * 
	 * @param searchVo
	 * @return
	 */
	public List<DealsDetailVo> getSearchList(SearchVo searchVo)throws ConnectTimeoutException,TangoTabException
	{
		List<DealsDetailVo> dealsList =null;		
		TangoTabBaseDao instance = TangoTabBaseDao.getInstance();
		ConnectionManager cManager = instance.getConManger();
		DealDetailHandler searchHandler = new DealDetailHandler();
		instance.getXmlReader().setContentHandler(searchHandler);
		String searchUrl = getSearchUrl(searchVo);
		if(ValidationUtil.isNullOrEmpty(searchUrl))
			return null;
		Log.v("Search URL: ", searchUrl);		
		cManager.setupHttpGet(searchUrl);
		
		try {
			if(AppConstant.dev_lat != 0.0 && AppConstant.dev_lang != 0.0)
			{	
				InputSource m_is = cManager.makeGetRequestGetResponse();
					if(m_is!=null)
					{
						instance.getXmlReader().parse(m_is);
						dealsList = searchHandler.getDealsList();
					}
				}
			}
			catch (ConnectTimeoutException e)
			{
				Log.e("ConnectTimeoutException occured ", "ConnectTimeoutException occured in getSearchList() method ", e);
				throw new ConnectTimeoutException(e.getLocalizedMessage());
			}
			catch (IOException e)
			{
				Log.e("Exception occured ", "IOException occured in getSearchList() method ", e);
				throw new TangoTabException("SearchDao", "getSearchList", e);
			} catch (SAXException e)
			{
				Log.e("Exception occured ", "SAXException occured in getDealsList() method ", e);
				throw new TangoTabException("SearchDao", "getSearchList", e);
			}
			return dealsList;
	}
	
	
	/**
	 * generate search URL from searchVo Object.
	 * @param searchVo
	 * @return
	 */
	private String getSearchUrl(SearchVo searchVo)
	{
		String searchUrl =null;
		/*String formattedTime = null;
		Date searchDate =null;*/
		if(ValidationUtil.isNullOrEmpty(searchVo.getType()))
		{
			String addressText =null;
			/*String selectDate = searchVo.getDate();
			try {
				String split[] = selectDate.split("-");
				int year = Integer.parseInt(split[0]);
				int month = Integer.parseInt(split[1]);
				int day = Integer.parseInt(split[2]);
				
				Calendar selectCal = Calendar.getInstance();
				selectCal.set(Calendar.YEAR, year);
				selectCal.set(Calendar.MONTH, month - 1);
				selectCal.set(Calendar.DAY_OF_MONTH, day);
				selectCal.set(Calendar.HOUR_OF_DAY, 0);
				selectCal.set(Calendar.MINUTE, 0);
				selectCal.set(Calendar.SECOND, 0);
				selectCal.set(Calendar.MILLISECOND, 0);
				searchDate = selectCal.getTime();
				
				Calendar today = Calendar.getInstance();
				today.setTime(today.getTime());
				Date current = today.getTime();
				String todaysDate = new SimpleDateFormat("MMM d yyyy ").format(current);
				String dateSelect = new SimpleDateFormat("MMM d yyyy ")	.format(searchDate.getTime());
				if(todaysDate.equalsIgnoreCase(dateSelect))
				{
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");//dd/MM/yyyy
				    formattedTime = sdfDate.format(cal.getTime());
				}
				else{
					formattedTime = AppConstant.SEARCH_MAX_TIME_RANGE;
					searchVo.setMaxTime(24);				
				}
			} catch (Exception exe) {
				Log.e("Exception :","Exception occures search date parsing", exe);
			}		*/	
			
			String address = searchVo.getAddress();
			String restName = searchVo.getRestName();			   
			
			if(!ValidationUtil.isNullOrEmpty(address))
				addressText = address.replace(" ", "%20");
			searchUrl =  AppConstant.baseUrl+"/deals/search?restName="+ TangoTabBaseDao.encodeURI(restName)
					+ "&address="+null+ "&pageIndex="+searchVo.getPageIndex()+ "&noOfdeals=0&searchingradius="
					+ searchVo.getDistance()+ "&coordinate="+ searchVo.getLocLat()+ ","+ searchVo.getLocLaong()
					+ "&userId=" + searchVo.getUserId()+"&diningId="+searchVo.getDiningId()+"&timeRange="+AppConstant.SEARCH_MAX_TIME_RANGE+","+searchVo.getMaxTime()+"&date="+TangoTabBaseDao.encodeURI(searchVo.getDate());		
			
			Log.v("searchUrl is", searchUrl);
		}
		else
		{
			/**
			 * Search url custom url handler.
			 */
			String city = searchVo.getAddress();
			String cityText = null;
			if(!ValidationUtil.isNullOrEmpty(city))
			{				
				cityText = city.replace(" ", "%20");
				searchUrl =  AppConstant.baseUrl+"/deals/search?type="+ searchVo.getType()
						+ "&city="+ TangoTabBaseDao.encodeURI(cityText)+ "&zipcode="+searchVo.getZipCode()+ "&coordinate="+ AppConstant.dev_lat+ ","+ AppConstant.dev_lang+ "&searchingradius="
						+ searchVo.getDistance()+"&pageIndex="+searchVo.getPageIndex()+"&noOfdeals=0"+"&restName="+searchVo.getRestName()+"&address="+searchVo.getAddress()+"&version="+searchVo.getVersionName()+"&userId="+searchVo.getUserId();
				Log.v("searchUrl is", searchUrl);		
			}		
			
		}
		
		return searchUrl;
	}
		
	
	
}
