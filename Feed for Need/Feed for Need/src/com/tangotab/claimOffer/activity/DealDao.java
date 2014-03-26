package com.tangotab.claimOffer.activity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.conn.ConnectTimeoutException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

import com.tangotab.core.connectionManager.ConnectionManager;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.dao.TangoTabBaseDao;
import com.tangotab.core.ex.TangoTabException;
import com.tangotab.myOffers.xmlHandler.OfferDetailHandler;
import com.tangotab.nearMe.vo.DealsDetailVo;
/**
 * Dao class to retrieve all the offers claimed by the end user.
 * .
 * @author Dillip.Lenka
 *
 */
public class DealDao extends TangoTabBaseDao
{
	/**
	 * Get list of offers for my offers tab
	 * @param deepLinkInputsVo 
	 * @param count
	 * @param loginVo
	 * @return
	 * @throws TangoTabException
	 */
	public List<DealsDetailVo> getOfferList(DeepLinkInputsVo deepLinkInputsVo) throws ConnectTimeoutException
	{
		TangoTabBaseDao instance = TangoTabBaseDao.getInstance();	
		ConnectionManager cManager = instance.getConManger();
		List<DealsDetailVo> dealDetailsList= null;
		String myOffersUrl = getMyOffersUrl(deepLinkInputsVo);
		Log.v("myOffersUrl  is", myOffersUrl);

		OfferDetailHandler offerHandler = new OfferDetailHandler();			
		instance.getXmlReader().setContentHandler(offerHandler);
		cManager.setupHttpGet(myOffersUrl);		
		InputSource m_is = cManager.makeGetRequestGetResponse();
		if (m_is != null){
			dealDetailsList = DOMParser(m_is);
		}	

		return dealDetailsList;	
	}


	private List<DealsDetailVo> DOMParser(InputSource inputSource) {

		List<DealsDetailVo> dealsDetailVos = new ArrayList<DealsDetailVo>();

		DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = dBuilderFactory.newDocumentBuilder();
			Document dom = documentBuilder.parse(inputSource);

			// get the root element.....
			Element docElement = dom.getDocumentElement();
			//Log.i("Root Element", docElement.getTagName());

			// now get the NodeList of root elements
			NodeList nodeList = docElement.getElementsByTagName("deal");
			//Log.i("NodeList Length", nodeList.getLength()+"");
			for (int i = 0; i < nodeList.getLength(); i++) {

				DealsDetailVo dealsDetailVo = new DealsDetailVo();
				Element eleDeal = (Element) nodeList.item(i);

				NodeList titleNode = eleDeal.getElementsByTagName("noOfdeals");
				Element noOfDeals = (Element) titleNode.item(0);
				//Log.i("Title", "Title - "+noOfDeals.getFirstChild().getNodeValue());
				dealsDetailVo.setNoOfdeals(noOfDeals.getFirstChild().getNodeValue());

				NodeList dealsfromNode = eleDeal.getElementsByTagName("dealsfrom");
				Element dealsfromEle = (Element) dealsfromNode.item(0);
				//Log.i("dealsfromEle","dealsfromEle - "+dealsfromEle.getFirstChild().getNodeValue());
				dealsDetailVo.setDealsFrom(dealsfromEle.getFirstChild().getNodeValue());

				NodeList business_nameNode = eleDeal.getElementsByTagName("business_name");
				Element business_nameEle = (Element) business_nameNode.item(0);
				//Log.i("business_nameEle","business_nameEle - "+business_nameEle.getFirstChild().getNodeValue());
				dealsDetailVo.setBusinessName(business_nameEle.getFirstChild().getNodeValue());

				NodeList deal_nameNode = eleDeal.getElementsByTagName("deal_name");
				Element deal_nameEle = (Element) deal_nameNode.item(0);
				//Log.i("deal_nameEle", "deal_nameEle - "+deal_nameEle.getFirstChild().getNodeValue());
				dealsDetailVo.setDealName(deal_nameEle.getFirstChild().getNodeValue());

				NodeList rest_deal_restrictionsNode = eleDeal.getElementsByTagName("rest_deal_restrictions");
				Element rest_deal_restrictionsEle = (Element) rest_deal_restrictionsNode.item(0);
				//Log.i("rest_deal_restrictionsEle", "rest_deal_restrictionsEle - "+rest_deal_restrictionsEle.getFirstChild().getNodeValue());
				dealsDetailVo.setDealRestriction(rest_deal_restrictionsEle.getFirstChild().getNodeValue());

				NodeList driving_distanceNode = eleDeal.getElementsByTagName("driving_distance");
				Element driving_distanceEle = (Element) driving_distanceNode.item(0);
				//Log.i("driving_distanceEle", "driving_distanceEle - "+ driving_distanceEle.getFirstChild().getNodeValue());
				dealsDetailVo.setDrivingDistance(driving_distanceEle.getFirstChild().getNodeValue());

				NodeList rest_deal_availablestart_dateNode = eleDeal.getElementsByTagName("rest_deal_availablestart_date");
				Element rest_deal_availablestart_dateEle = (Element) rest_deal_availablestart_dateNode.item(0);
				//Log.i("rest_deal_availablestart_dateEle", "rest_deal_availablestart_dateEle - "+ rest_deal_availablestart_dateEle.getFirstChild().getNodeValue());
				dealsDetailVo.setDealAvailableStartDate(rest_deal_availablestart_dateEle.getFirstChild().getNodeValue());

				NodeList rest_deal_start_dateNode = eleDeal.getElementsByTagName("rest_deal_start_date");
				Element rest_deal_start_dateEle = (Element) rest_deal_start_dateNode.item(0);
				//Log.i("rest_deal_start_dateEle", "rest_deal_start_dateEle - "+ rest_deal_start_dateEle.getFirstChild().getNodeValue());
				dealsDetailVo.setRestDealStartDate(rest_deal_start_dateEle.getFirstChild().getNodeValue());

				NodeList rest_deal_end_dateNode = eleDeal.getElementsByTagName("rest_deal_end_date");
				Element rest_deal_end_dateEle = (Element) rest_deal_end_dateNode.item(0);
				//Log.i("rest_deal_end_dateEle", "rest_deal_end_dateEle - "+ rest_deal_end_dateEle.getFirstChild().getNodeValue());
				dealsDetailVo.setDealEndDate(rest_deal_end_dateEle.getFirstChild().getNodeValue());

				NodeList available_start_timeNode = eleDeal.getElementsByTagName("available_start_time");
				Element available_start_timeEle = (Element) available_start_timeNode.item(0);
				//Log.i("available_start_timeEle", "available_start_timeEle - "+ available_start_timeEle.getFirstChild().getNodeValue());
				dealsDetailVo.setStartTime(available_start_timeEle.getFirstChild().getNodeValue());

				NodeList available_end_timeNode = eleDeal.getElementsByTagName("available_end_time");
				Element available_end_timeEle = (Element) available_end_timeNode.item(0);
				//Log.i("available_end_timeEle", "available_end_timeEle - "+ available_end_timeEle.getFirstChild().getNodeValue());
				dealsDetailVo.setEndTime(available_end_timeEle.getFirstChild().getNodeValue());

				NodeList available_week_days = eleDeal.getElementsByTagName("available_week_days");
				Element available_week_daysEle = (Element) available_week_days.item(0);
				//Log.i("available_week_daysEle", "available_week_daysEle - "+ available_week_daysEle.getFirstChild().getNodeValue());
				dealsDetailVo.setAvailableWeekDays(available_week_daysEle.getFirstChild().getNodeValue());

				NodeList deal_description = eleDeal.getElementsByTagName("deal_description");
				Element deal_descriptionEle = (Element) deal_description.item(0);
				//Log.i("deal_descriptionEle", "deal_descriptionEle - "+ deal_descriptionEle.getFirstChild().getNodeValue());
				dealsDetailVo.setDealDescription(deal_descriptionEle.getFirstChild().getNodeValue());

				NodeList deal_credit_value = eleDeal.getElementsByTagName("deal_description");
				Element deal_credit_valueElement = (Element) deal_credit_value.item(0);
				//Log.i("deal_credit_valueElement", "deal_credit_valueElement - "+ deal_credit_valueElement.getFirstChild().getNodeValue());
				dealsDetailVo.setDealCreditValue(deal_credit_valueElement.getFirstChild().getNodeValue());

				NodeList location_rest_address = eleDeal.getElementsByTagName("location_rest_address");
				Element location_rest_addressElement = (Element) location_rest_address.item(0);
				//Log.i("location_rest_addressElement", "location_rest_addressElement - "+ location_rest_addressElement.getFirstChild().getNodeValue());
				dealsDetailVo.setLocationRestAddress(location_rest_addressElement.getFirstChild().getNodeValue());

				NodeList address = eleDeal.getElementsByTagName("address");
				Element addressElement = (Element) address.item(0);
				// Log.i("addressElement", "addressElement - "+ addressElement.getFirstChild().getNodeValue());
				dealsDetailVo.setAddress(addressElement.getFirstChild().getNodeValue());

				NodeList phone = eleDeal.getElementsByTagName("phone");
				Element phoneElement = (Element) phone.item(0);
				//Log.i("phoneElement", "phoneElement - "+ phoneElement.getFirstChild().getNodeValue());
				dealsDetailVo.setPhone(phoneElement.getFirstChild().getNodeValue());

				NodeList no_deals_available = eleDeal.getElementsByTagName("no_deals_available");
				Element no_deals_availableElement = (Element) no_deals_available.item(0);
				//Log.i("no_deals_availableElement", "no_deals_availableElement - "+ no_deals_availableElement.getFirstChild().getNodeValue());
				dealsDetailVo.setNoDealsAvailable(no_deals_availableElement.getFirstChild().getNodeValue());
				
				NodeList image_url = eleDeal.getElementsByTagName("image_url");
				Element image_urlElement = (Element) image_url.item(0);
				//Log.i("image_urlElement", "image_urlElement - "+ image_urlElement.getFirstChild().getNodeValue());
				dealsDetailVo.setImageUrl(image_urlElement.getFirstChild().getNodeValue());
			
				dealsDetailVos.add(dealsDetailVo);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return dealsDetailVos;
	}

	private String getMyOffersUrl(DeepLinkInputsVo deepLinkInputsVo){
		String myOffersUrl = AppConstant.productionServer +"/deals/get?" +
				"dealId=" + deepLinkInputsVo.getDeal_ID() +
				"&coordinate=" + deepLinkInputsVo.getLatitude() + "," + deepLinkInputsVo.getLongitude();		
		return myOffersUrl;
	}
}
