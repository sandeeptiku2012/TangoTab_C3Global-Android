package com.tangotab.me.activity;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookActivity;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.nostra13.socialsharing.common.AuthListener;
import com.nostra13.socialsharing.common.LogoutListener;
import com.nostra13.socialsharing.common.PostListener;
import com.nostra13.socialsharing.facebook.FacebookEvents;
import com.nostra13.socialsharing.facebook.FacebookFacade;
import com.nostra13.socialsharing.facebook.extpack.com.facebook.android.Facebook;
import com.tangotab.LocationManagerToggle;
import com.c3global.R;
import com.tangotab.claimOffer.activity.ClaimOfferActivity;
import com.tangotab.contactSupport.activity.ContactSupportActvity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.constant.Constants;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.mainmenu.activity.MainMenuActivity;
import com.tangotab.map.activity.MappingActivity;
import com.tangotab.me.activity.MeActivity.GetCharityDetails.Philantraphy;
import com.tangotab.myOfferDetails.activity.AutoCheckinActivity;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.myOffers.activity.MyOffersActivity;
import com.tangotab.nearMe.activity.NearMeActivity;
import com.tangotab.nearMe.vo.DealsDetailVo;
import com.tangotab.search.activity.SearchActivity;
import com.tangotab.settings.activity.SettingsActivity;
import com.tangotab.twitter.util.TwitterUtils;
/**
 * 
 * @author Dillip.Lenka
 *
 */
public class MeActivity extends FacebookActivity implements OnClickListener,Handler.Callback
{
	private Button mBtnTwitterShare,facebookShare;

	private SharedPreferences prefs;
	private LinearLayout mePhil;
	private LinearLayout tangoTabPhil;
	//private LinearLayout friendPhil;
	//private LinearLayout NetwrkPhil;
	private static final int TWITTER_ID=40;
	private static final int TWEET_SENT=35;
	private static final int TWITTER_FAILED=77;
	private static final int TWITTER_POST_FAILED=78;
	//private Map<String, String> response1 = new HashMap<String, String>();
	private StringBuilder description = new StringBuilder();
	private StringBuilder facebookDescription = new StringBuilder();
	Handler myhand,twitterHandler;
	final String TAG = getClass().getName();
	private OAuthConsumer consumer; 
	private OAuthProvider provider;
	boolean pageFinish=false;
	private Dialog dialog;
	private ProgressDialog progressDialog;
	private TangoTab application;
	private GoogleAnalyticsTracker tracker;
	//private boolean twitter=false;
	FacebookFacade facebookFacade;
	public Handler postHandler;
	
	private String mMeFeed;
	private String mCompanyFeed;

	private SharedPreferences twitterPreferences;
	private SharedPreferences facebookPreferences;

	private SharedPreferences.Editor twitterEditor;
	private SharedPreferences.Editor facebookEditor;

	Facebook facebook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.me);


		/**
		 * Google analytics implementation added here.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(AppConstant.GA_REG_KEY,10, this);
		tracker.setDebug(true);
		tracker.trackPageView(AppConstant.ME_SCREEN);
		tracker.trackEvent("MeScreen", "TrackEvent", "mescreen", 1);

		application = (TangoTab) getApplication();
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,getApplicationContext(),offerList);
		activity.doCheckIn();

		application = (TangoTab) getApplication();
		twitterHandler=new Handler(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mBtnTwitterShare=(Button)findViewById(R.id.btnTweeterShare);
		mBtnTwitterShare.setBackgroundResource(R.drawable.tweeterselector);
		mBtnTwitterShare.setOnClickListener(this);
		//Button topMeBtn = (Button) findViewById(R.id.topMeMenuButton);
		Button nearMe = (Button) findViewById(R.id.topNearmeMenuMenuButton);
		Button topMenuBtn = (Button) findViewById(R.id.topMenuButton);
		Button topSearchBtn = (Button) findViewById(R.id.topSearchMenuButton);
		Button btnMyOffers = (Button) findViewById(R.id.btnMyOffers);
		Button btnMySettings = (Button) findViewById(R.id.btnMySettings);

		mePhil = (LinearLayout) findViewById(R.id.linearLayoutMe);
		tangoTabPhil = (LinearLayout) findViewById(R.id.LinearLayoutPhilNumb);
		//friendPhil = (LinearLayout) findViewById(R.id.LinearLayoutPhilNumbFriendText);
		//NetwrkPhil = (LinearLayout) findViewById(R.id.LinearLayoutMyPhilNetwrk);
		Button map =(Button)findViewById(R.id.topMapMenuButton);
		facebookFacade = new FacebookFacade(this, Constants.FACEBOOK_APP_ID);
		facebook = new Facebook(TAG);

		twitterPreferences = getSharedPreferences(
				AppConstant.TWITTER_PREFERENCES, Context.MODE_PRIVATE);
		facebookPreferences = getSharedPreferences(
				AppConstant.FACEBOOK_PREFERENCES, Context.MODE_PRIVATE);

		twitterEditor = twitterPreferences.edit();
		facebookEditor = facebookPreferences.edit();

		if (TangoTab.getInstance().getShareflow() == null)

		{
			facebookEditor.putString("showpopup", "true");
			facebookEditor.putString("iconpopup", "true");
			facebookEditor.putString("iconfacebookon","true");
			twitterEditor.putString("showpopup", "true");
			twitterEditor.putString("iconpopup", "true");
			twitterEditor.putString("icontwitteron", "true");
			facebookEditor.commit();
			twitterEditor.commit();	
		}
		/*
		 *Me button onclick listener 
		 */
		/*topMeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent nearmeIntent=new Intent(MeActivity.this, NearMeActivity.class);
				nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(nearmeIntent);
			}
		});*/
		/*
		 * near me button onclick listener
		 */
		nearMe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v)
			{  
				if(AppConstant.IS_CLAIM_OFFER_PAGE)
				{
					DealsDetailVo dealsDetailVo = application.getSelectDeal();
					String country = application.getCountry();
					ArrayList<DealsDetailVo> businessList = new ArrayList<DealsDetailVo>();
					for (DealsDetailVo dealsVo : application.getDealsList())
					{
						if (dealsDetailVo.businessName.equals(dealsVo.businessName)) {
							businessList.add(dealsVo);
						}

					}			
					Intent calimedIntent = new Intent(MeActivity.this,ClaimOfferActivity.class);
					calimedIntent.putExtra("from", "nearme");
					calimedIntent.putExtra("selectDeal", dealsDetailVo);
					calimedIntent.putExtra("businessList", businessList);
					calimedIntent.putExtra("country", country);
					calimedIntent.putExtra("selectedPosition",AppConstant.SELECTED_POSITION);
					startActivity(calimedIntent);
				}
				else{
					Intent nearmeIntent = new Intent(MeActivity.this,NearMeActivity.class);
					nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(nearmeIntent);
				}
			}

		});

		/*
		 *me button onclick listener.
		 */
		topMenuBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent meIntent=new Intent(MeActivity.this, MainMenuActivity.class);
				meIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(meIntent);
			}
		});

		/*
		 *Top Search button onclick listener.
		 */
		topSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent searchIntent=new Intent(MeActivity.this, SearchActivity.class);
				searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(searchIntent);
			}
		});

		/*
		 * My offers button onclick listener
		 */
		btnMyOffers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!ValidationUtil.isNullOrEmpty(application.getOffersList()))
					application.getOffersList().clear();
				Intent myOffersIntent=new Intent(MeActivity.this, MyOffersActivity.class);
				myOffersIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myOffersIntent);
			}
		});
		/**
		 * Map button on click listener
		 */
		map.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{

				Vibrator myVib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				myVib.vibrate(50);

				if (checkInternetConnection()) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									/**
									 * Start map activity
									 */
									Intent mapIntent = new Intent(getApplicationContext(),MappingActivity.class);										
									startActivity(mapIntent);
								}
							});
						}
					}).start();
				} else
					showDialog(10);
			}

		});

		/*
		 * My Settings button onclick listener
		 */
		btnMySettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mySettingsIntent=new Intent(MeActivity.this, SettingsActivity.class);
				mySettingsIntent.putExtra("frmPage", "MeActivity");
				mySettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mySettingsIntent);
			}
		});

		//updateMyPhilanthropy();
		getChatityDetails();

		/*
		 *  Code for facebook sharing
		 */


		final Session session = Session.getActiveSession();
		if (!ValidationUtil.isNull(session) && !session.isClosed()) {
			session.closeAndClearTokenInformation();
		}

		facebookShare =(Button)findViewById(R.id.btnFacebookShare);

		facebookShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (facebookPreferences.getString("iconpopup", "true")
						.equalsIgnoreCase("true")) {
					showDialog(AppConstant.ICON_FACEBOOK_SHARE);
				} else if (facebookPreferences.getString("iconfacebookon",
						"true").equalsIgnoreCase("true")) {
					//postPicToFaceBook();
					posttoFacebook();
				}

			}
		});
		postHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == AppConstant.SUCCESFULL_POST) {
					if (!isFinishing())
						showDialog(AppConstant.FACEBOOK_POST_SUCCESSFUL_DIALOG);
				}
			}
		};

	}

	@Override
	public void onClick(View v) {
		if(v==mBtnTwitterShare){

			if (twitterPreferences.getString("iconpopup", "true")
					.equalsIgnoreCase("true")) {
				showDialog(AppConstant.ICON_TWITTER_SHARE);
			} else if (twitterPreferences.getString("icontwitteron", "true")
					.equalsIgnoreCase("true")) {		
				posttoTwitter();
			}

		}

	}
	private void posttoTwitter() {

		SharedPreferences userPreferences = getSharedPreferences("UserDetails", 0);
		boolean isTwitterShareOn=userPreferences.getBoolean("isTwitterOn", false);
		//Check whether the twitter share option is on in settings

		//Check if the user is already logged in twitter
		if(AppConstant.isNetworkAvailable(this))
		{
			if (TwitterUtils.isAuthenticated(prefs)) 
			{
				sendTweet();
			}
			//Start twitter activity if user is not logged in
			else
			{
				/*	Intent twitterActivity=new Intent(MeActivity.this,TwitterActivity.class);
	    		startActivityForResult(twitterActivity,1);*/
				if(progressDialog==null){
					progressDialog=new ProgressDialog(MeActivity.this);
					progressDialog.setMessage("Please wait..");
					progressDialog.setCancelable(false);
				}
				progressDialog.show();
				startTwitterLogin();
			}
		}
		else
		{
			Toast.makeText(this,"No Internet Connection !!",Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Displays a toast notification with No more offers
	 */
	private void showToastMessage(String message){
		Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
	}
	/**
	 * Start a asynctask and send a tweet
	 */
	private void sendTweet(){
		new SendTweetAsyncTask().execute();
	}
	/**
	 * AsyncTask for sending a tweet
	 * @author Ram.Donda
	 *
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode!=RESULT_CANCELED){
			if(requestCode==1){
				if(resultCode==RESULT_OK){
					sendTweet();
				}
				else{
					//howToastMessage("There was an error while connecting with twitter");
					showDialog(TWITTER_FAILED);
				}
			}
		}
	}
	public class SendTweetAsyncTask extends AsyncTask<Void,Void,Integer>{
		private final int SUCCESS=1,FAILED=0;
		ProgressDialog mTweetProgressDialog;
		@Override
		protected Integer doInBackground(Void... params) {
			try {
				//TwitterUtils.sendTweet(prefs,dealsDetailVo.getBusinessName()+" "+dealsDetailVo.getDealName()+" ",dealsDetailVo.getImageUrl());

				//String fileName = "android.resource://" + MeActivity.this.getPackageName() + "/drawable/tangotab_icon";
				SharedPreferences sharPreferences = getSharedPreferences("UserDetails", 0);
				String me = sharPreferences.getString("mePhil", "0");
				me=createPhilontrophyFormatString(me);
				String tangotab = sharPreferences.getString("tangotabPhil", "0");
				String philonThrophy=createPhilontrophyFormatString(tangotab);
				String toPost= "I've raised < $+ mMeFeed +> just by dining out with C3 Global and Tangotab. " +
						"It's a free app to get free restaurant deals. ";
				if(toPost.length() > 139)
				{
					TwitterUtils.sendTweet(prefs,toPost.substring(0, 139));
				}else{
					TwitterUtils.sendTweet(prefs,toPost);
				}
				return SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
				return FAILED;
			}

		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(mTweetProgressDialog==null){
				mTweetProgressDialog=new ProgressDialog(MeActivity.this);
				mTweetProgressDialog.setMessage("Sending tweet..");
			}
			mTweetProgressDialog.show();
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			mTweetProgressDialog.dismiss();
			if(result==SUCCESS){
				showDialog(TWEET_SENT);
				//showToastMessage("Tweet sent");
			}
			else{
				showDialog(TWITTER_POST_FAILED);
				//	showToastMessage("Sending tweet failed");
			}
		}
	}




	private void updateMyPhilanthropy(Philantraphy philantraphy){

		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/digi.ttf");
		String me = addDecimalsToCount(philantraphy.getMeCount());
		mMeFeed = me;
		if(!me.equals("0")){
			//String meFormatted=createPhilontrophyFormatString(me);
			String meFormatted=(me);
			TextView tv1=new TextView(MeActivity.this);
			tv1.setText("$");
			tv1.setTypeface(font);
			tv1.setTextSize(32);
			tv1.setTextColor(Color.WHITE);
			tv1.setBackgroundColor(Color.BLACK);
			tv1.setPadding(2,0,2,0);
			LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp1.setMargins(1, 5, 0, 0);
			tv1.setLayoutParams(lp1);
			mePhil.addView(tv1);

			TextView tv2=new TextView(MeActivity.this);
			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp2.setMargins(0, 5, 10, 0);
			tv2.setLayoutParams(lp2);
			mePhil.addView(tv2);

			for(int meresult=0;meresult<meFormatted.length();meresult++){
				String number=meFormatted.substring(meresult, meresult+1);
				TextView tv=new TextView(MeActivity.this);
				tv.setText(number);
				tv.setTextSize(32);
				tv.setTypeface(font);

				if(!number.equalsIgnoreCase(".")){
					tv.setTextColor(Color.WHITE);
					tv.setBackgroundColor(Color.BLACK);
					tv.setPadding(2,0,2,0);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(1, 5, 0, 0);
					tv.setLayoutParams(lp);
				}else
				{
					tv.setTextColor(Color.BLACK);
					tv.setPadding(0,0,-2,0);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 8, 0, -8);
					tv.setLayoutParams(lp);		
				}
				mePhil.addView(tv);
			}
		}
		else{
			TextView tv=new TextView(MeActivity.this);
			tv.setText("0");
			tv.setTextSize(32);
			tv.setTypeface(font);
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.BLACK);
			tv.setPadding(2,0,2,0);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(1, 5, 0, 0);
			tv.setLayoutParams(lp);
			mePhil.addView(tv);
		}
		//mePhil.setText(Html.fromHtml("<html> <body><table border='0' align='center'><tr><td bgcolor=\"#000000\"><h3 face='Helvetica' color='white'>"+response.get("me")+"</h3></td></tr></body></html>"));
		//tangoTabPhil.setText(Html.fromHtml("<html> <body><table border='0' align='center'><tr><td bgcolor=\"#000000\"><h3 face='Helvetica' color='white'>"+response.get("tangotab")+"</h3></td></tr></body></html>"));

		String tangotab = addDecimalsToCount(philantraphy.getCompanyCount());
		mCompanyFeed = tangotab;
		
		if(!tangotab.equals("0")){
			String philonThrophy = (tangotab);
			TextView tv1=new TextView(MeActivity.this);
			tv1.setText("$");
			tv1.setTypeface(font);
			tv1.setTextSize(32);
			tv1.setTextColor(Color.WHITE);
			tv1.setBackgroundColor(Color.BLACK);
			tv1.setPadding(2,0,2,0);
			LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp1.setMargins(-2, 5, 0, 0);
			tv1.setLayoutParams(lp1);
			tangoTabPhil.addView(tv1);

			TextView tv2=new TextView(MeActivity.this);
			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp2.setMargins(0, 5, 10, 0);
			tv2.setLayoutParams(lp2);
			tangoTabPhil.addView(tv2);

			for(int tangoTabResult=0;tangoTabResult<philonThrophy.length();tangoTabResult++){
				String number= philonThrophy.substring(tangoTabResult, tangoTabResult+1);
				TextView tv=new TextView(MeActivity.this);
				tv.setText(number);
				tv.setTextSize(32);  
				tv.setTypeface(font);  
				if(!number.equalsIgnoreCase(".")){
					tv.setTextColor(Color.WHITE);
					tv.setBackgroundColor(Color.BLACK);
					tv.setPadding(2,0,2,0);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(1, 5, 0, 0);
					tv.setLayoutParams(lp);
				}
				else{
					tv.setTextColor(Color.BLACK);
					tv.setPadding(0,0,-2,0);	
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.setMargins(0, 8, 0, -8);
					tv.setLayoutParams(lp);
				}

				tangoTabPhil.addView(tv);
			}
		}else{
			TextView tv=new TextView(MeActivity.this);
			tv.setText("0");
			tv.setTextSize(32);
			tv.setTypeface(font);
			tv.setTextColor(Color.WHITE);
			tv.setBackgroundColor(Color.BLACK);
			tv.setPadding(2,0,2,0);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(1, 5, 0, 0);
			tv.setLayoutParams(lp);
			tangoTabPhil.addView(tv);
		}

		//		String friends = sharPreferences.getString("friendsPhil", "0");
		//		if(!friends.equals("0")){
		//			//String friends="345657";
		//			String firendsFormated=createPhilontrophyFormatString(friends);
		//			TextView tv1=new TextView(MeActivity.this);
		//			tv1.setText("$");
		//			tv1.setTypeface(font);
		//			tv1.setTextSize(32);
		//			tv1.setTextColor(Color.WHITE);
		//			tv1.setBackgroundColor(Color.BLACK);
		//			tv1.setPadding(2,0,2,0);
		//			LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//			lp1.setMargins(1, 5, 0, 0);
		//			tv1.setLayoutParams(lp1);
		//			friendPhil.addView(tv1);
		//
		//			TextView tv2=new TextView(MeActivity.this);
		//			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//			lp2.setMargins(0, 5, 10, 0);
		//			tv2.setLayoutParams(lp2);
		//			friendPhil.addView(tv2);
		//
		//			for(int friendsResult=0;friendsResult<firendsFormated.length();friendsResult++){
		//				String number=firendsFormated.substring(friendsResult, friendsResult+1);
		//				TextView tv=new TextView(MeActivity.this);
		//				tv.setText(number);
		//				tv.setTextSize(32);
		//				tv.setTypeface(font);
		//				if(!number.equalsIgnoreCase(",")){
		//					tv.setTextColor(Color.WHITE);
		//					tv.setBackgroundColor(Color.BLACK);
		//					tv.setPadding(2,0,2,0);
		//					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//					lp.setMargins(1, 5, 0, 0);
		//					tv.setLayoutParams(lp);
		//				}
		//				else{
		//					tv.setTextColor(Color.BLACK);
		//					tv.setPadding(0,0,-2,0);
		//					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//					lp.setMargins(0, 8, 0, -8);
		//					tv.setLayoutParams(lp);
		//				}
		//				friendPhil.addView(tv);
		//			}
		//			//friendPhil.setText(Html.fromHtml("<html> <body><table border='0' align='center'><tr><td bgcolor=\"#000000\"><h3 face='Helvetica' color='white'>"+response.get("friends")+"</h3></td></tr></body></html>"));
		//		}
		//		else{
		//			TextView tv=new TextView(MeActivity.this);
		//			tv.setText("0");
		//			tv.setTextSize(32);
		//			tv.setTypeface(font);
		//			tv.setTextColor(Color.WHITE);
		//			tv.setBackgroundColor(Color.BLACK);
		//			tv.setPadding(2,0,2,0);
		//			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//			lp.setMargins(1, 5, 0, 0);
		//			tv.setLayoutParams(lp);
		//			friendPhil.addView(tv);
		//		}

		//		String potential = sharPreferences.getString("networkPhil", "0");
		//		String potentialFormatted=createPhilontrophyFormatString(potential);
		//
		//		TextView tv1=new TextView(MeActivity.this);
		//		tv1.setText("$");
		//		tv1.setTypeface(font);
		//		tv1.setTextSize(32);
		//		tv1.setTextColor(Color.WHITE);
		//		tv1.setBackgroundColor(Color.BLACK);
		//		tv1.setPadding(2,0,2,0);
		//		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//		lp1.setMargins(1, 5, 0, 0);
		//		tv1.setLayoutParams(lp1);
		//		NetwrkPhil.addView(tv1);
		//
		//		TextView tv2=new TextView(MeActivity.this);
		//		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//		lp2.setMargins(0, 5, 10, 0);
		//		tv2.setLayoutParams(lp2);
		//		NetwrkPhil.addView(tv2);

		//		if(!potential.equals("0")){
		//			for(int potentialResult=0;potentialResult<potentialFormatted.length();potentialResult++){
		//				String number=potentialFormatted.substring(potentialResult, potentialResult+1);
		//				TextView tv=new TextView(MeActivity.this);
		//				tv.setText(number);
		//				tv.setTextSize(32);
		//				tv.setTypeface(font);
		//
		//				if(!number.equalsIgnoreCase(",")){
		//					tv.setTextColor(Color.WHITE);
		//					tv.setBackgroundColor(Color.BLACK);
		//					tv.setPadding(2,0,2,0);
		//					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//					lp.setMargins(1, 5, 0, 0);
		//					tv.setLayoutParams(lp);
		//				}
		//				else{
		//					tv.setTextColor(Color.BLACK);
		//					tv.setPadding(0,0,-2,0);
		//					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//					lp.setMargins(0, 8, 0, -8);
		//					tv.setLayoutParams(lp);
		//				}			
		//				NetwrkPhil.addView(tv);
		//
		//			}
		//
		//			//	NetwrkPhil.setText(Html.fromHtml("<html> <body><table border='0' align='center'><tr><td bgcolor=\"#000000\"><h3 face='Helvetica' color='white'>"+response.get("potential")+"</h3></td></tr></body></html>"));
		//		}
		//		else{
		//			TextView tv=new TextView(MeActivity.this);
		//			tv.setText("0");
		//			tv.setTextSize(32);
		//			tv.setTypeface(font);
		//			tv.setTextColor(Color.WHITE);
		//			tv.setBackgroundColor(Color.BLACK);
		//			tv.setPadding(2,0,2,0);
		//			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//			lp.setMargins(1, 5, 0, 0);
		//			tv.setLayoutParams(lp);
		//			NetwrkPhil.addView(tv);
		//		}

		description.append("\n");
		//description.append(sharPreferences.getString("firstName", "").substring(0, 1).toUpperCase()+(sharPreferences.getString("firstName", "").substring(1)));
		description.append("I've fed ");
		//description.append(me);
		description.append(createPhilontrophyFormatString(me));
		description.append(" people in need using TangoTab when i dined outwith TangoTab.");
		description.append(" It's a free app to get free restaurant deals. http://apps.tangotab.com");
		/*description.append(sharPreferences.getString("firstName", "").substring(0, 1).toUpperCase()+(sharPreferences.getString("firstName", "").substring(1))+"'s");

			description.append(" friends have fed ");
			//description.append(friends);
			description.append(createPhilontrophyFormatString(friends));
			description.append(" people in need,");
			description.append("\n");
			description.append("TangoTab has fed ");
			//description.append(tangotab);
			description.append(createPhilontrophyFormatString(tangotab));
			description.append(" people in need");*/

		facebookDescription.append("\n");
		//facebookDescription.append(sharPreferences.getString("firstName", "").substring(0, 1).toUpperCase()+(sharPreferences.getString("firstName", "").substring(1)));
		facebookDescription.append("I've fed ");
		//facebookDescription.append(me);
		facebookDescription.append(createPhilontrophyFormatString(me));
		facebookDescription.append(" people in need when I dined out.");
		facebookDescription.append(" So far TangoTab has donated over ");
		facebookDescription.append(createPhilontrophyFormatString(tangotab));
		facebookDescription.append(" meals to people in need. TangoTab offers you free restaurant deals.");
		facebookDescription.append(" This is definitely an app you want on your phone! http://apps.tangotab.com.");

		/*

			facebookDescription.append(sharPreferences.getString("firstName", "").substring(0, 1).toUpperCase()+(sharPreferences.getString("firstName", "").substring(1))+
			"'s");
			facebookDescription.append(" friends have fed ");
			//facebookDescription.append(friends);
			facebookDescription.append(createPhilontrophyFormatString(friends));
			facebookDescription.append(" people in need,");
			facebookDescription.append("\n");

			facebookDescription.append(sharPreferences.getString("firstName", "").substring(0, 1).toUpperCase()+(sharPreferences.getString("firstName", "").substring(1))+
					"'s");
			facebookDescription.append(" network could feed ");
			//facebookDescription.append(potential);
			facebookDescription.append(createPhilontrophyFormatString(potential));
			facebookDescription.append(" people in need,");


			facebookDescription.append("\n");
			facebookDescription.append("TangoTab has fed ");
			facebookDescription.append(createPhilontrophyFormatString(tangotab));
			facebookDescription.append(" people in need");*/

	}
	
	private String addDecimalsToCount(String count) {
		double temp;
		String returnValue;
		
		try{
			temp = Double.valueOf(count);
		}
		catch(Exception e){
			returnValue = "0.00";
			return returnValue;
		}
		
		if(count == "0.00")
			return count;
		
		double result = temp - (int) temp;
		if(result == 0.0)
			returnValue = temp +"0";
		else
			returnValue = temp +"";
		
		return returnValue;
	}

	private void getChatityDetails() {
		SharedPreferences sharPreferences = getSharedPreferences("UserDetails", 0);
		String user_ID = sharPreferences.getString("UserId", "o");
		System.out.println("--------------USER_ID------------" + user_ID);
		String URI = "http://api.tangotab.com/user/charity/?user_id=" + user_ID;
		new GetCharityDetails(MeActivity.this, URI).execute();
	}

	public class GetCharityDetails extends AsyncTask<Void, Void, String>{
		private ProgressDialog mDialog=null;
		private Context mContext = null;
		private String URI;
		private String message;

		private GetCharityDetails(Context context, String URI){
			mContext = context;
			this.URI = URI;
		}

		@Override
		protected void onPreExecute() {
			mDialog = ProgressDialog.show(mContext, "Connecting", "Please Wait...");
			mDialog.setCancelable(true);
		}

		@Override
		protected String doInBackground(Void... params){
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httppost = new HttpGet(URI);
				HttpResponse response = httpclient.execute(httppost);
				message = EntityUtils.toString(response.getEntity());

			}  catch (SocketTimeoutException ste) {
				Log.e("Timeout Exception: ", ste.toString());
				Intent contactIntent=new Intent(getApplicationContext(), ContactSupportActvity.class);
				startActivity(contactIntent);

			}
			catch(ConnectTimeoutException e){
				Log.e("ConnectTimeoutException occured", "ConnectTimeoutException occured at the time of login",e);
				message =null;
				Intent contactIntent=new Intent(getApplicationContext(), ContactSupportActvity.class);
				startActivity(contactIntent);
			}
			catch(Exception e){
				Log.e("Exception occured", "Exception occured at the time of login",e);
				message =null;
			}

			return message;
		}

		@Override
		protected void onPostExecute(String response){
			mDialog.dismiss();
			System.out.println("--------------Response------------" + response);
			Philantraphy philantraphy = new Philantraphy();
			JSONObject json_data;
			
			try {
				json_data = new JSONObject(response);
				philantraphy.setMeCount(json_data.getString("me"));
				philantraphy.setCompanyCount(json_data.getString("company_total"));
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(mContext, "Server Error..!", Toast.LENGTH_SHORT).show();
				philantraphy.setMeCount("0.00");
				philantraphy.setCompanyCount("0.00");
			}
			
			updateMyPhilanthropy(philantraphy);
		}

		public class Philantraphy{
			private String meCount;
			private String companyCount;

			public String getMeCount() {
				return meCount;
			}
			public void setMeCount(String meCount) {
				this.meCount = meCount;
			}
			public String getCompanyCount() {
				return companyCount;
			}
			public void setCompanyCount(String companyCount) {
				this.companyCount = companyCount;
			}
		}

	}

	/**
	 * Used to create a my philonthrophy format string
	 * @param data
	 *            String which is to be converted
	 * @return
	 *        Converted String
	 */
	private String createPhilontrophyFormatString(String data){
		/*StringBuilder builder=new StringBuilder(data);
	if(builder.length()>5){

 	   builder.insert(3,",");
 	   if(builder.length()>7){
 		   builder.insert(7,",");  
 	   }
 	   if(builder.length()>10){
 		 String format=builder.substring(0,10); 
 	     format=format+"..";
 	     return format;
 	   }
    }

	return builder.toString();*/
		/*DecimalFormat formatter = new DecimalFormat("#,###");
		String formattedString=formatter.format(Double.valueOf(data));
	     StringBuilder builder=new StringBuilder(formattedString);
	     if(builder.length()>10){
	    	 builder.insert(10,"..");
	    	 String result=builder.substring(0,11);
	    	 return result;
	     }
		 return formattedString;*/
		StringBuilder builder=new StringBuilder(data);
		if(builder.length()>3){
			builder.insert(3,",");	
		}
		if(builder.length()>9){
			String subString=builder.substring(0,9);
			return subString;
		}
		return builder.toString();
	}
	/*
	 * Facebook sharing implementation
	 */
	private void postOfferToFacebook() {

		Session session = Session.getActiveSession();

		if (session != null){
			Bundle postParams = new Bundle();

			postParams.putString("message", facebookDescription.toString());

			Request.Callback callback= new Request.Callback() {
				@Override
				public void onCompleted(Response response) {


					JSONObject graphResponse = null;
					if(!ValidationUtil.isNull(response) && !ValidationUtil.isNull(response.getGraphObject()) && !ValidationUtil.isNull(response.getGraphObject().getInnerJSONObject())){
						graphResponse = response.getGraphObject().getInnerJSONObject();
					}
					String postId = null;
					try {
						if(!ValidationUtil.isNull(graphResponse)){
							postId = graphResponse.getString("id");
							if(!ValidationUtil.isNull(postId)){
								showDialog(11);
							}
						}
					} catch (JSONException e) {
						Log.i("Error",
								"JSON error "+ e.getMessage());
					}
					if (response.getError() != null) {
						Toast.makeText(MeActivity.this,
								response.getError().getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
			};

			Request request = new Request(session, "me/feed", postParams, 
					HttpMethod.POST,callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();

		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case AppConstant.FACEBOOK_POST_SUCCESSFUL_DIALOG:
			AlertDialog.Builder facebookShareDialog = new AlertDialog.Builder(
					MeActivity.this);
			facebookShareDialog.setTitle("TangoTab");
			facebookShareDialog
			.setMessage("Successfully posted on your facebook account wall");
			facebookShareDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Session session = Session.getActiveSession();
					if (!ValidationUtil.isNull(session)
							&& !session.isClosed()) {
						session.closeAndClearTokenInformation();
					}
				}
			});

			return facebookShareDialog.create();
		case AppConstant.ICON_TWITTER_SHARE:
			AlertDialog.Builder twitterdetailsicon = new AlertDialog.Builder(
					MeActivity.this);
			twitterdetailsicon.setTitle("TangoTab");
			twitterdetailsicon
			.setMessage("Thank you for your contribution to fight against hunger.");
			twitterdetailsicon.setPositiveButton("Share",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					posttoTwitter();
				}
			});
			twitterdetailsicon.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return twitterdetailsicon.create();
		case AppConstant.ICON_FACEBOOK_SHARE:
			AlertDialog.Builder sharedetails1 = new AlertDialog.Builder(
					MeActivity.this);
			sharedetails1.setTitle("TangoTab");
			sharedetails1
			.setMessage("Thank you for your contribution to fight against hunger.");
			sharedetails1.setPositiveButton("Share",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//postPicToFaceBook();
					posttoFacebook();
				}
			});
			sharedetails1.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return sharedetails1.create();
		case TWITTER_ID:
			AlertDialog.Builder twitterDialog = new AlertDialog.Builder(MeActivity.this);
			twitterDialog.setTitle("TangoTab");
			twitterDialog.setMessage(AppConstant.TWITTER_OFF_MESSGAGE);
			twitterDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return twitterDialog.create();
			/*case FACEBOOK_ID:
			AlertDialog.Builder fbDialog = new AlertDialog.Builder(MyoffersDetailActivity.this);
			fbDialog.setTitle("TangoTab");
			fbDialog.setMessage(AppConstant.FACEBOOK_OFF_MESSAGE);
			fbDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});return fbDialog.create();*/
		case TWEET_SENT:
			AlertDialog.Builder tweetSent = new AlertDialog.Builder(MeActivity.this);
			tweetSent.setTitle("TangoTab");
			tweetSent.setMessage("Successfully posted on your Twitter account wall");
			tweetSent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});return tweetSent.create();

		case TWITTER_FAILED:
			AlertDialog.Builder tweetFailed = new AlertDialog.Builder(MeActivity.this);
			tweetFailed.setTitle("TangoTab");
			tweetFailed.setMessage("There was an error while connecting with Twitter");
			tweetFailed.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});return tweetFailed.create();	
		case 10:
			AlertDialog.Builder ab10 = new AlertDialog.Builder(MeActivity.this);
			ab10.setTitle("TangoTab");
			ab10.setMessage("We are unable to make an internet connection at this time.Some functionalities will be limited until a connection is made.");

			ab10.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return ab10.create();
		case TWITTER_POST_FAILED:
			AlertDialog.Builder postFailed = new AlertDialog.Builder(MeActivity.this);
			postFailed.setTitle("TangoTab");
			postFailed.setMessage("Sorry this message was already tweeted");
			postFailed.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});return postFailed.create();		
		}
		return null;
	}

	/**
	 * This method will check the Internet connection for the application.
	 * 
	 * @return
	 */
	private boolean checkInternetConnection() 
	{
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())? true:false;

	}
	class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void>
	{

		private Context	context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private SharedPreferences prefs;

		public RetrieveAccessTokenTask(Context context, OAuthConsumer consumer,OAuthProvider provider, SharedPreferences prefs) 
		{
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.prefs=prefs;
		}



		@Override
		protected Void doInBackground(Uri...params) {
			final Uri uri = params[0];

			final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			//Log.i("path is..",oauth_verifier);
			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);

				final Editor edit = prefs.edit();
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
				edit.commit();

				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

				consumer.setTokenWithSecret(token, secret);
				myhand.post(run);

				Log.v("Authenticatiuon:", "OAuth - Access Token Retrieved");

			} catch (Exception e) {
				Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
				twitterHandler.sendEmptyMessage(TWITTER_FAILED);	
			}

			return null;
		}


		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

		}

		Runnable run=new Runnable()
		{
			public void run()
			{
				Log.e("twitter","checked");
				// twitter=true;
				if(progressDialog!=null&&progressDialog.isShowing()){
					progressDialog.dismiss();
				}
				/* setResult(RESULT_OK);
	        finish();*/
				sendTweet();
				//twittercheck.setImageResource(R.drawable.checked);
			}
		};
	}	
	class OAuthRequestTokenTask extends AsyncTask<Void, Void, Void>
	{

		final String TAG = getClass().getName();
		private Context	context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private Handler myhand;
		String url;

		/**
		 * 
		 * We pass the OAuth consumer and provider.
		 * 
		 * @param 	context
		 * 			Required to be able to start the intent to launch the browser.
		 * @param 	provider
		 * 			The OAuthProvider object
		 * @param 	consumer
		 * 			The OAuthConsumer object
		 */
		public OAuthRequestTokenTask(Context context,OAuthConsumer consumer,OAuthProvider provider,Handler myhand)
		{
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.myhand=myhand;
		}

		/**
		 * 
		 * Retrieve the OAuth Request Token and present a browser to the user to authorize the token.
		 * 
		 */
		@Override
		protected Void doInBackground(Void... params) {

			try {
				Log.i(TAG, "Retrieving request token from Google servers");
				url = provider.retrieveRequestToken(consumer,AppConstant.OAUTH_CALLBACK_URL);
				Log.i(TAG, "Popping a browser with the authorize URL : " + url);

				myhand.post(run);
			} catch (Exception e) {
				Log.e(TAG, "Error during OAUth retrieve request token", e);
				/*setResult(RESULT_CANCELED);
				finish();*/
				//mydialog.dismiss();
				twitterHandler.sendEmptyMessage(TWITTER_FAILED);

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

		}

		Runnable run=new Runnable()
		{
			public void run()
			{
				Log.e("url is",url);
				dialog = new Dialog(context);
				//dialog.setOnDismissListener(MeActivity.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(false);
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View vi = inflater.inflate(R.layout.twitterpopup, null);
				dialog.setContentView(vi);

				ImageView close=(ImageView)vi.findViewById(R.id.close);
				close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						/*setResult(RESULT_CANCELED);
						finish();*/
						if(progressDialog!=null&&progressDialog.isShowing()){
							progressDialog.dismiss();
						}

						//mydialog.dismiss();
					}
				});
				WebView wb = (WebView)vi.findViewById(R.id.twitterweb);
				wb.setWebViewClient(new myWebClient());  
				wb.getSettings().setJavaScriptEnabled(true);  
				wb.loadUrl(url);  



			}
		};
		class myWebClient extends WebViewClient{  //HERE IS THE MAIN CHANGE. 
			String url;
			@Override  
			public void onPageStarted(WebView view, String url, Bitmap favicon) {  
				// TODO Auto-generated method stub  
				this.url=url;
				super.onPageStarted(view, url, favicon);  
			}  

			@Override  
			public boolean shouldOverrideUrlLoading(WebView view, String url) {  
				// TODO Auto-generated method stub  

				this.url=url;
				if(url.startsWith("htt"))
				{

					view.loadUrl(url); 
				}
				else
				{

					dialog.dismiss();
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MeActivity.this);
					final Uri uri = Uri.parse(url);
					if (uri != null && uri.getScheme().equals(AppConstant.OAUTH_CALLBACK_SCHEME)) {
						Log.e(TAG, "Callback received : " + uri);
						Log.e(TAG, "Retrieving Access Token");
						new RetrieveAccessTokenTask(MeActivity.this,consumer,provider,prefs).execute(uri);

					}
				}


				return true;  

			}


			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);

				if(url.startsWith("http://"))
				{
					Log.e("http","yes");
					//mydialog.dismiss();
					try{
						dialog.show();
					}catch(Exception e){

					}
				}
				else if(pageFinish)
				{
					Log.e("https","finished");
					//mydialog.dismiss();
					dialog.show();
				}
				else
				{
					Log.e("https","true");
					pageFinish=true;
				}

			}



		}
	}

	private void startTwitterLogin()
	{


		try {

			this.consumer = new CommonsHttpOAuthConsumer(AppConstant.CONSUMER_KEY, AppConstant.CONSUMER_SECRET);
			this.provider = new CommonsHttpOAuthProvider(AppConstant.REQUEST_URL,AppConstant.ACCESS_URL,AppConstant.AUTHORIZE_URL);
		} catch (Exception e) {
			Log.e(TAG, "Error creating consumer / provider",e);

		}

		myhand=new Handler();
		new OAuthRequestTokenTask(this,consumer,provider,myhand).execute();
	}

	@Override
	public boolean handleMessage(Message msg) {
		if(msg.what==TWITTER_FAILED){
			if(progressDialog!=null&&progressDialog.isShowing()){
				progressDialog.dismiss();
			}
			showDialog(TWITTER_FAILED);
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch(keycode) {
		case KeyEvent.KEYCODE_MENU:
			Intent mainMenuIntent = new Intent(MeActivity.this,MainMenuActivity.class);
			mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainMenuIntent);
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			Intent searchIntent=new Intent(MeActivity.this, SearchActivity.class);
			searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(searchIntent);
			return true;
		}

		return super.onKeyDown(keycode, e);
	}

	private void publishMessage() {

		//String url="https://fbcdn-photos-f-a.akamaihd.net/hphotos-ak-prn1/851561_358228107610729_1599010098_n.gif";
		//facebookFacade.publishMessage(facebookDescription.toString(),null,null,null);
		SharedPreferences sharPreferences = getSharedPreferences("UserDetails", 0);
		String me = sharPreferences.getString("mePhil", "0");
		me=createPhilontrophyFormatString(me);
		String tangotab = sharPreferences.getString("tangotabPhil", "0");
		String philonThrophy=createPhilontrophyFormatString(tangotab);
		
		facebookFacade.publishMessage("I've raised <$ + mMeFeed +> just by dining out with C3 Global and TangoTab. " +
				"So far C3 Global has donated over <$ + mCompanyFeed +> towards feeding people in need. " +
				"C3 Global offers you  free restaurant deals. " +
				"This is definitely an app you want on your phone! ",
				null,
				null,
				null,
				null);
	}

	PostListener postListener = new PostListener() {

		public void onPostPublishingFailed() {
			postHandler.sendEmptyMessage(AppConstant.UNSUCCESFULL_POST);
		}

		public void onPostPublished() {
			postHandler.sendEmptyMessage(AppConstant.SUCCESFULL_POST);

			// unregisterListeners();
		}
	};

	AuthListener authListener = new AuthListener() {

		public void onAuthSucceed() {
		}

		public void onAuthFail(String error) {
		}
	};
	LogoutListener logoutListener = new LogoutListener() {

		public void onLogoutComplete() {
		}
	};

	private Timer t;

	/** Should be call at {@link Activity#onStart()} */
	public void registerListeners() {

		FacebookEvents.addAuthListener(authListener);
		FacebookEvents.addPostListener(postListener);
		FacebookEvents.addLogoutListener(logoutListener);
	}

	/** Should be call at {@link Activity#onStop()} */
	public void unregisterListeners() {

		FacebookEvents.removeAuthListener(authListener);
		FacebookEvents.removePostListener(postListener);
		FacebookEvents.removeLogoutListener(logoutListener);
	}

	public void posttoFacebook() {

		registerListeners();
		if (facebookFacade.isAuthorized()) {
			//	publishImage();
			publishMessage();

		} else {

			facebookFacade.authorize(new AuthListener() {

				public void onAuthSucceed() {
					//publishImage();
					publishMessage();
				}

				public void onAuthFail(String error) {
				}
			});
		}


	}
	public void postPicToFaceBook()
	{
		if (facebookFacade.isAuthorized()) {
			publishImage();
			finish();
		} else {
			// Start authentication dialog and publish image after successful authentication
			facebookFacade.authorize(new AuthListener() {
				@Override
				public void onAuthSucceed() {
					publishImage();
					finish();
				}

				@Override
				public void onAuthFail(String error) { // Do noting
				}
			});
		}
	}

	private void publishImage() {

		facebookFacade.publishMessage(facebookDescription.toString(),null,null,null);

		/*Bitmap bmp = ((BitmapDrawable) getResources().getDrawable(R.drawable.tangotab_icon)).getBitmap();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] bitmapdata = stream.toByteArray();
			facebookFacade.publishImage(null, description.toString());*/



		/*Map actions = new HashMap<String, String>();
			actions.put("Android Simple Social Sharing", "https://github.com/nostra13");
			facebookFacade.publishMessage("Look at this great App!",
			                        "Use Android Simple Social Sharing in your project!",
			                        "https://github.com/nostra13/Android-Simple-Social-Sharing",
			                        "Also see other projects of nostra13 on GitHub!",
			                        "https://fbcdn-photos-f-a.akamaihd.net/hphotos-ak-prn1/851561_358228107610729_1599010098_n.gif",
			                        actions);

		 */







		//facebookFacade.publishMessage("sample", "sample","sample", "sample", "https://fbcdn-photos-f-a.akamaihd.net/hphotos-ak-prn1/851561_358228107610729_1599010098_n.gif", null);
	}

	@Override
	protected void onPause() {
		LocationManagerToggle.getInstance().cancelTimer(t);
		LocationManagerToggle.getInstance().removeCurentLocationUpdate();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		t=LocationManagerToggle.getInstance().setTimer(null, 0,5000);
		LocationManagerToggle.getInstance().initalizeLocationManagerService(this,this);
		/**
		 * do auto checkin
		 */
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,getApplicationContext(),offerList);
		activity.doCheckIn();
	}

	@Override
	public void onBackPressed() {

		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}



