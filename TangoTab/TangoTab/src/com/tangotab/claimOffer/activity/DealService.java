package com.tangotab.claimOffer.activity;

import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import com.tangotab.nearMe.vo.DealsDetailVo;

import android.util.Log;
public class DealService
{

	public List<DealsDetailVo> getOffers(DeepLinkInputsVo deepLinkInputsVo ) throws ConnectTimeoutException
	{
		List<DealsDetailVo> dealList =null;
		try
		{
			DealDao dao = new DealDao();
			dealList = dao.getOfferList(deepLinkInputsVo);
		}
		catch(Exception e)
		{
			Log.e("Error ", "Exception occures when invoking service to get all Offers ");	
		}
		return dealList;
	}
	
	
}
