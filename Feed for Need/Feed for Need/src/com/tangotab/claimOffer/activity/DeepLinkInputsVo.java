package com.tangotab.claimOffer.activity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DeepLinkInputsVo implements Serializable {
	
	private String mDeal_ID;
	private String mLongitude;
	private String mLatitude;
	private String mDate;
	
	public String getDeal_ID() {
		return mDeal_ID;
	}
	public void setDeal_ID(String mDeal_ID) {
		this.mDeal_ID = mDeal_ID;
	}
	public String getLongitude() {
		return mLongitude;
	}
	public void setLongitude(String mLongitude) {
		this.mLongitude = mLongitude;
	}
	public String getLatitude() {
		return mLatitude;
	}
	public void setLatitude(String mLatitude) {
		this.mLatitude = mLatitude;
	}
	public String getDate() {
		return mDate;
	}
	public void setDate(String mDate) {
		this.mDate = mDate;
	}
}
