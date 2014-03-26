package com.tangotab.twitter.util;
import oauth.signpost.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.utils.ValidationUtil;

public class TwitterUtils 
{
/**
 * Check whether the user is already logged in or not
 * @param prefs
 * @return
 */
	public static boolean isAuthenticated(SharedPreferences prefs) {

		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		
		AccessToken a = new AccessToken(token,secret);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(AppConstant.CONSUMER_KEY, AppConstant.CONSUMER_SECRET);
		twitter.setOAuthAccessToken(a);
		try {
			return new GetAccSettings().execute(new Twitter[]{twitter}).get();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	public static class GetAccSettings extends AsyncTask<Twitter, Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(Twitter... params) {
			try {
				params[0].getAccountSettings();
				return true;
			} catch (TwitterException e)
			{
				return false;
			}
		}
	}
	/**
	 * Used to send a tweet from the application
	 * @param meActivity 
	 * @param prefs,SharedPrefrence where the credentials are stored
	 * @param msg,message to be sent 
	 * @throws Exception
	 */
	public static void sendTweet(SharedPreferences prefs,String msg) throws Exception {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		//Log.e("secret key..",secret);
		AccessToken a = new AccessToken(token,secret);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(AppConstant.CONSUMER_KEY, AppConstant.CONSUMER_SECRET);
		twitter.setOAuthAccessToken(a);
		/*if(!ValidationUtil.isNull(imageUrl)){
        twitter.updateStatus(msg+imageUrl);
		}
		else{*/
			   twitter.updateStatus(msg);	
		//}
     }	
	
	
	
	    
}
