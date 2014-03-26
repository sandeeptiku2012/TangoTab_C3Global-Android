package com.tangotab.core.google.analytics;

import android.content.Context;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Utility for using Google Analytics
 * @author dillip.lenka
 *
 */

public class GoogleAnalyticsConnector 
{
  private final static String GA_REG_KEY="UA-4500699-13";

   public static GoogleAnalyticsTracker getConnector(Context ctx)
   {
		GoogleAnalyticsTracker tracker;
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(GoogleAnalyticsConnector.GA_REG_KEY,ctx); 
		return tracker;
	}
}
