package com.tangotab.claimOffer.activity;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.conn.ConnectTimeoutException;

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
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.FacebookActivity;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.Facebook;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.nostra13.socialsharing.common.AuthListener;
import com.nostra13.socialsharing.common.LogoutListener;
import com.nostra13.socialsharing.common.PostListener;
import com.nostra13.socialsharing.facebook.FacebookEvents;
import com.nostra13.socialsharing.facebook.FacebookFacade;
import com.tangotab.LocationManagerToggle;
import com.c3global.R;
import com.tangotab.VolleySingleton;
import com.tangotab.claimOffer.service.ClaimOfferService;
import com.tangotab.contactSupport.activity.ContactSupportActvity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.constant.Constants;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.DateFormatUtil;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.login.vo.LoginVo;
import com.tangotab.mainmenu.activity.MainMenuActivity;
import com.tangotab.map.activity.MapPointActivity;
import com.tangotab.me.activity.MeActivity;
import com.tangotab.myOfferDetails.activity.AutoCheckinActivity;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.myOffers.activity.MyOffersActivity;
import com.tangotab.myOffers.service.MyOffersService;
import com.tangotab.nearMe.activity.NearMeActivity;
import com.tangotab.nearMe.vo.DealsDetailVo;
import com.tangotab.search.activity.SearchActivity;
import com.tangotab.settings.activity.SettingsActivity;
import com.tangotab.twitter.util.TwitterUtils;

/**
 * Class will give offer detail information with a chance to claim the offer.
 * 
 * <br>
 * class :ClaimOfferActivity <br>
 * layout :offerclaim.xml
 * 
 * @author dillip.lenka
 * 
 */
public class ClaimOfferActivity extends FacebookActivity implements
OnClickListener, Handler.Callback {
	/**
	 * Meta definitions
	 */
	private static final int TWITTER_FAILED = 77;
	private static final int TWITTER_POST_FAILED = 78;
	private Timer t;
	// private static final int FACEBOOK_ID=50;
	private static final int Message_Id = 0;
	private static final int Reser_comp = 1;
	private static final int TWEET_SENT = 35;
	private Button mBtnNext, mBtnBack, mTwitterShare;
	private Button facebookShare;
	private TextView dealDate, dealTime;;
	private LinearLayout addToCalender;
	private TextView businessName;
	private LinearLayout getDirection;
	private TextView restPhone;
	private LinearLayout restPhoneLayout;
	private TextView dealName;
	private TextView dealNameBold;
	private TextView dealRestriction;
	private com.android.volley.toolbox.NetworkImageView offerImage;
	private TextView distance;
	private Vibrator myVib;
	private DealsDetailVo dealsDetailVo;
	final String TAG = getClass().getName();
	public TangoTab application;
	private int mSelectedIndex;
	List<DealsDetailVo> searchList;
	private String message;
	private SharedPreferences prefs;
	Dialog dialog;
	private String availableStartTime;
	private String availableEndTime;
	private String country;
	private static final int TWITTER_ID = 40;
	private int position;
	private RotateAnimation rotate;
	Handler myhand, twitterHandler;
	private OAuthConsumer consumer;
	private OAuthProvider provider;
	boolean pageFinish = false;
	private LinearLayout imageLayout;

	private TextView Name;
	private LinearLayout OfferDetailRl;

	private int selectedPosition;

	ProgressDialog progressDialog;
	boolean twitter = false;
	private GoogleAnalyticsTracker tracker;
	public boolean isFaceBookShare;
	public boolean isTwitterShare;
	boolean isClaimClicked = false;
	Facebook mFacebook;
	List<String> permissions;
	FacebookFacade facebookFacade;
	Session session;
	public Handler postHandler;
	Runnable dialogrunner;
	SharedPreferences.Editor shareeditor;
	ArrayList<Integer> shareflow;
	LinearLayout dealImage;
	FrameLayout dealImageHolder;

	private SharedPreferences twitterPreferences;
	private SharedPreferences facebookPreferences;	

	private boolean istwitterclicked;
	private boolean isfacebookclicked;

	private int showeddialog = 0;

	private SharedPreferences.Editor twitterEditor;
	private SharedPreferences.Editor facebookEditor;
	private String mDeal_ID;

	public void onCreate(Bundle savedInstances){
		super.onCreate(savedInstances);
		setContentView(R.layout.offerdetails);

		application = (TangoTab) getApplication();
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,getApplicationContext(),offerList);
		activity.doCheckIn();

		Uri data = getIntent().getData();
		if(data != null){
			String scheme = data.getScheme(); // "http"
			String host = data.getHost(); // "twitter.com"
			List<String> params = data.getPathSegments();
			//mDeal_ID = params.get(0); // "status"
			//Toast.makeText(ClaimOfferActivity.this, mDeal_ID, Toast.LENGTH_LONG).show();

			//DealsDetailVo dealsDetailVo = application.getSelectDeal();
			//String country = application.getCountry();
		}

		twitterPreferences = getSharedPreferences(AppConstant.TWITTER_PREFERENCES, Context.MODE_PRIVATE);
		facebookPreferences = getSharedPreferences(AppConstant.FACEBOOK_PREFERENCES, Context.MODE_PRIVATE);

		twitterEditor = twitterPreferences.edit();
		facebookEditor = facebookPreferences.edit();

		shareflow = new ArrayList<Integer>();

		facebookFacade = new FacebookFacade(this, Constants.FACEBOOK_APP_ID);
		overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
		twitterHandler = new Handler(this);
		myVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		/**
		 * Google analytics implementation added here.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(AppConstant.GA_REG_KEY, 10, this);
		tracker.setDebug(true);
		tracker.trackPageView(AppConstant.CLAIM_OFFER_PAGE);
		tracker.trackEvent("ClaimNow", "TrackEvent", "claimnow", 1);

		if (mDeal_ID != null && mDeal_ID.length() > 0){
			//selectedPosition = AppConstant.SELECTED_POSITION;
		}
		else if(getIntent().getExtras() != null ){
			selectedPosition = getIntent().getExtras().getInt("selectedPosition");
		}

		OfferDetailRl = (LinearLayout) findViewById(R.id.OfferDetailRl);
		Name = (TextView) findViewById(R.id.name);
		imageLayout = (LinearLayout) findViewById(R.id.imageLayout);



		/**
		 * Field details from UI
		 */
		Button map = (Button) findViewById(R.id.topMapMenuButton);
		Button claimNow = (Button) findViewById(R.id.clmwantit);
		mBtnNext = (Button) findViewById(R.id.btnNext);
		mBtnNext.setOnClickListener(this);
		mTwitterShare = (Button) findViewById(R.id.btnTweeterShare);
		mTwitterShare.setOnClickListener(this);

		facebookShare = (Button) findViewById(R.id.btnFacebookShare);

		permissions = new ArrayList<String>();
		permissions.add("publish_stream");
		permissions.add("publish_checkins");

		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(this);
		dealDate = (TextView) findViewById(R.id.dealdate);
		dealTime = (TextView) findViewById(R.id.dealtime);
		addToCalender = (LinearLayout) findViewById(R.id.addToCalendarLayout);
		businessName = (TextView) findViewById(R.id.businessName);
		getDirection = (LinearLayout) findViewById(R.id.getDirectionsLayout);
		restPhone = (TextView) findViewById(R.id.restPhone);
		restPhoneLayout = (LinearLayout) findViewById(R.id.restPhoneLayout);
		dealName = (TextView) findViewById(R.id.dealname);
		dealNameBold = (TextView) findViewById(R.id.dealnamebold);
		dealRestriction = (TextView) findViewById(R.id.dealrestrictions);
		//offerImage = (ImageView) findViewById(R.id.image);
		offerImage = (NetworkImageView)findViewById(R.id.image);
		offerImage.setDefaultImageResId(R.drawable.image_bg);
		distance = (TextView) findViewById(R.id.distance);
		LinearLayout share = (LinearLayout) findViewById(R.id.shareDetail);
		dealImage = (LinearLayout) findViewById(R.id.offerDetails);
		dealImageHolder = (FrameLayout) findViewById(R.id.dealImage);	

		SharedPreferences automatedSharing = getSharedPreferences("automatedSharing", 0);
		isFaceBookShare = automatedSharing.getBoolean("facebookSharing", false);
		isTwitterShare = automatedSharing.getBoolean("twitterSharing", false);

		if (twitterPreferences.getString("showicon", "true").equalsIgnoreCase("true")
				&& facebookPreferences.getString("showicon", "true").equalsIgnoreCase("true")){
			share.setVisibility(View.VISIBLE);
			facebookShare.setVisibility(View.VISIBLE);
			mTwitterShare.setVisibility(View.VISIBLE);
		} 
		else if (twitterPreferences.getString("showicon", "true").equalsIgnoreCase("true")
				&& facebookPreferences.getString("showicon", "true").equalsIgnoreCase("false")) {
			share.setVisibility(View.VISIBLE);
			facebookShare.setVisibility(View.GONE);
			mTwitterShare.setVisibility(View.VISIBLE);
		} 
		else if (twitterPreferences.getString("showicon", "true").equalsIgnoreCase("false")
				&& facebookPreferences.getString("showicon", "true").equalsIgnoreCase("true")) {
			share.setVisibility(View.VISIBLE);
			facebookShare.setVisibility(View.VISIBLE);
			mTwitterShare.setVisibility(View.GONE);
		}
		else if (twitterPreferences.getString("showicon", "true").equalsIgnoreCase("false")
				&& facebookPreferences.getString("showicon", "true").equalsIgnoreCase("false")) {
			share.setVisibility(View.GONE);
		}

		if (TangoTab.getInstance().getShareflow() != null)
			shareflow = TangoTab.getInstance().getShareflow();
		else {
			shareflow.add(AppConstant.FACEBOOK_SHARE);
			shareflow.add(AppConstant.TWITTER_SHARE);
			shareflow.add(AppConstant.CLAIM_OFFER);
			facebookEditor.putString("showicon", "true");
			twitterEditor.putString("showicon", "true");
			facebookEditor.commit();
			twitterEditor.commit();
		}
		//		final String from;
		//		final String fromPage = null;
		//		if (mDeal_ID != null && mDeal_ID.length() > 0){
		//			from = getIntent().getStringExtra("from");
		//			fromPage = getIntent().getStringExtra("fromPage");
		//			country = getIntent().getStringExtra("country");
		//		}
		//else{
		final String	from = getIntent().getStringExtra("from");
		final String	fromPage = getIntent().getStringExtra("fromPage");
		country = getIntent().getStringExtra("country");
		//}

		rotate = (RotateAnimation) AnimationUtils.loadAnimation(getBaseContext(), R.anim.rot);

		if(mDeal_ID != null && mDeal_ID.length() > 0){
			//			ArrayList<DealsDetailVo> businessList = new ArrayList<DealsDetailVo>();
			//			for (DealsDetailVo dealsVo : application.getDealsList()) {
			//				if (dealsDetailVo.businessName.equals(dealsVo.businessName)) {
			//					businessList.add(dealsVo);
			//				}
			//			}
			//			searchList = businessList;
		}
		else{
			searchList = (List<DealsDetailVo>) getIntent().getSerializableExtra("businessList");
		}

		// position = getIntent().getIntExtra("index",0);
		/**
		 * Retrieve the selected deals from near me list
		 */

		if(mDeal_ID != null && mDeal_ID.length() > 0){
			//dealsDetailVo = application.getSelectDeal();
		}
		else{
			dealsDetailVo = (DealsDetailVo) getIntent().getSerializableExtra("selectDeal");
		}

		if (!ValidationUtil.isNull(dealsDetailVo)) {
			if (!ValidationUtil.isNullOrEmpty(searchList)) {

				if (searchList.size() == 1) {
					mBtnNext.setVisibility(View.GONE);
				} 
				else {
					for (int count = 0; count < searchList.size(); count++) {
						if (dealsDetailVo.getId().equals(searchList.get(count).getId())) 
						{
							position = count;
							mSelectedIndex = position;

						}
					}
				}
				/**
				 * Selected index on the list
				 */
				if (mSelectedIndex == searchList.size() - 1) {
					mBtnNext.setVisibility(View.GONE);
				}
			}
			/**
			 * Set all the field informations
			 */

			setData(dealsDetailVo);
		}
		postHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == AppConstant.SUCCESFULL_POST) {
					if (!isFinishing())
						showDialog(AppConstant.FACEBOOK_POST_SUCCESSFUL_DIALOG);
				}
			}
		};

		/**
		 * On click listener on claim Now.
		 */
		claimNow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				myVib.vibrate(50);
				if (checkInternetConnection())
				{
					AppConstant.IS_CLAIM_OFFER_PAGE = false;
					SharedPreferences user = getSharedPreferences("UserName", 0);
					String userName = user.getString("username", "");
					dealsDetailVo.setUserId(userName);
					/**
					 * If offer came from customUrl handler
					 */
					shareflow.clear();
					shareflow.add(AppConstant.CLAIM_OFFER);

					if (next < shareflow.size()) {
						shareClaimHandler.sendEmptyMessage(shareflow.get(next));
						next++;
					}

					if (!ValidationUtil.isNullOrEmpty(fromPage)&& fromPage.equalsIgnoreCase("customURL")){
						String spMailingId = application.getSpMailingID();
						String spUserId = application.getSpUserId();
						String spJobId = application.getSpJobId();
						String dealId = getIntent().getStringExtra("dealId");

						if(mDeal_ID != null && mDeal_ID.length() > 0){
							//dealId = mDeal_ID;
						}

						/*
						 * Download the image from url and set into the image
						 * view
						 */
						if (!ValidationUtil.isNullOrEmpty(spMailingId)&& !ValidationUtil.isNullOrEmpty(spUserId)
								&& !ValidationUtil.isNullOrEmpty(spJobId)&& !ValidationUtil.isNullOrEmpty(dealId)) 
						{
							StringBuilder imageUrl = new StringBuilder();
							imageUrl.append("http://recp.mkt51.net/cot?m=").append(spMailingId).append("&r=")
							.append(spUserId).append("&j").append(spJobId).append("&a=Android")
							.append("&d=").append(dealId).append("&amt=4");
							//ImageLoader imageLoader = new ImageLoader(getApplicationContext());
							offerImage.setImageUrl(dealsDetailVo.getImageUrl(),VolleySingleton.getInstance(ClaimOfferActivity.this).getImageLoader());
							//	imageLoader.DisplayImage(dealsDetailVo.getImageUrl(), offerImage,ClaimOfferActivity.this);
						}
					}
					//						else if (mDeal_ID != null && mDeal_ID.length() > 0)
					//						{
					//							String spMailingId = application.getSpMailingID();
					//							String spUserId = application.getSpUserId();
					//							String spJobId = application.getSpJobId();
					//							String dealId = mDeal_ID;
					//							/*
					//							 * Download the image from url and set into the imageview
					//							 */
					//							if (!ValidationUtil.isNullOrEmpty(spMailingId)&& !ValidationUtil.isNullOrEmpty(spUserId)
					//									&& !ValidationUtil.isNullOrEmpty(spJobId)&& !ValidationUtil.isNullOrEmpty(dealId)) 
					//							{
					//								StringBuilder imageUrl = new StringBuilder();
					//								imageUrl.append("http://recp.mkt51.net/cot?m=").append(spMailingId).append("&r=")
					//								.append(spUserId).append("&j").append(spJobId).append("&a=Android")
					//								.append("&d=").append(dealId).append("&amt=4");
					//								//ImageLoader imageLoader = new ImageLoader(getApplicationContext());
					//								offerImage.setImageUrl(dealsDetailVo.getImageUrl(),VolleySingleton.getInstance(ClaimOfferActivity.this).getImageLoader());
					//								//imageLoader.DisplayImage(dealsDetailVo.getImageUrl(), offerImage,ClaimOfferActivity.this);
					//							}
					//						}

				} else {
					showDialog(10);
				}
				isClaimClicked = true;
			}
		});

		/**
		 * On click listener on map button.
		 */

		map.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				myVib.vibrate(50);
				if (checkInternetConnection()) {
					/**
					 * Pass to Map point activity in order to display
					 */
					Intent mapIntent = new Intent(getApplicationContext(),MapPointActivity.class);
					if (!ValidationUtil.isNullOrEmpty(from))
					{
						mapIntent.putExtra("from", from);
					}
					mapIntent.putExtra("businessname",dealsDetailVo.getBusinessName());
					mapIntent.putExtra("dealname", dealsDetailVo.getDealName());
					mapIntent.putExtra("address", dealsDetailVo.getAddress());
					//mapIntent.putExtra("fromPage", "claimOffer");

					startActivity(mapIntent);
				} else
					showDialog(10);
			}
		});

		/**
		 * Phone dialing functionality
		 */

		restPhoneLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent call = new Intent(Intent.ACTION_DIAL);
				call.setData(Uri.parse("tel:" + restPhone.getText()));
				startActivity(call);
			}
		});

		/**
		 * Add event to Calendar functionality
		 */

		addToCalender.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent calIntent = new Intent(Intent.ACTION_EDIT);
				calIntent.setType("vnd.android.cursor.item/event");

				calIntent.putExtra("title","TangoTab at " + dealsDetailVo.getBusinessName());
				calIntent.putExtra("description",dealsDetailVo.getDealDescription());
				if (!ValidationUtil.isNullOrEmpty(dealsDetailVo.getAddress())) {
					try {
						String[] splitAdd = dealsDetailVo.getAddress().split(",");
						if (splitAdd != null) {
							StringBuilder address = new StringBuilder();
							for (int count = 0; count < splitAdd.length - 1; count++) 
							{
								address.append(splitAdd[count]).append(", ");
							}
							address.append(splitAdd[splitAdd.length - 1]);
							calIntent.putExtra("eventLocation",address.toString());
						} else {
							calIntent.putExtra("eventLocation",	dealsDetailVo.getAddress());
						}
					} catch (StringIndexOutOfBoundsException e) {
						Log.e("Exception:","StringIndexOutOfBoundsException occured at the time of splitting the address",e);
					}
				}

				Date beginTime = DateFormatUtil.parseIntoDifferentFormat(availableStartTime, "yyyy-MM-dd hh:mm aa");
				Date endTime = DateFormatUtil.parseIntoDifferentFormat(availableEndTime, "yyyy-MM-dd hh:mm aa");
				Calendar cal = Calendar.getInstance();
				cal.setTime(beginTime);
				long beginTimeInMillis = cal.getTimeInMillis();
				calIntent.putExtra("beginTime", beginTimeInMillis);
				cal.setTime(endTime);
				long endTimeInMillis = cal.getTimeInMillis();
				calIntent.putExtra("endTime", endTimeInMillis);
				calIntent.putExtra("eventStatus", 1);
				calIntent.putExtra("visibility", 0);
				calIntent.putExtra("transparency", 0);
				calIntent.putExtra("hasAlarm", 1);

				try {
					startActivity(calIntent);
				} catch (Exception e)
				{
					Toast.makeText(getApplicationContext(),	"Sorry, no compatible calendar is found!",Toast.LENGTH_LONG).show();
				}
			}
		});

		// Get Direction onclick listener
		getDirection.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myVib.vibrate(50);
				if (checkInternetConnection()) {
					/**
					 * Pass to Map point activity in order to display
					 */
					Intent mapIntent = new Intent(getApplicationContext(), MapPointActivity.class);
					if (!ValidationUtil.isNullOrEmpty(from))
					{
						mapIntent.putExtra("from", from);
					}
					mapIntent.putExtra("selectDeal",(DealsDetailVo) dealsDetailVo);
					mapIntent.putExtra("fromPage", "claimOffer");
					mapIntent.putExtra("address", dealsDetailVo.getAddress());
					mapIntent.putExtra("businessname",dealsDetailVo.getBusinessName());
					mapIntent.putExtra("dealname", dealsDetailVo.getDealName());
					startActivity(mapIntent);
				} else
					showDialog(10);
			}
		});

		/**
		 * Top Bar functionality
		 */
		Button meButton = (Button) findViewById(R.id.topMeMenuButton);
		Button nearMe = (Button) findViewById(R.id.topNearmeMenuMenuButton);
		Button topSearchBtn = (Button) findViewById(R.id.topSearchMenuButton);
		Button topMenuBtn = (Button) findViewById(R.id.topMenuButton);
		topMenuBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent meIntent = new Intent(ClaimOfferActivity.this,MainMenuActivity.class);
				meIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(meIntent);
			}
		});

		// me button onclick listener.
		meButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent meIntent = new Intent(ClaimOfferActivity.this,MeActivity.class);
				meIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(meIntent);
			}
		});

		// near me button onclick listener
		nearMe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent nearmeIntent = new Intent(ClaimOfferActivity.this,NearMeActivity.class);
				nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(nearmeIntent);
			}
		});

		// Top Search button onclick listener.
		topSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent searchIntent = new Intent(ClaimOfferActivity.this,SearchActivity.class);				
				searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(searchIntent);
				finish();
			}
		});

		/*
		 * Code for facebook sharing
		 */

		final Session session = Session.getActiveSession();
		if (!ValidationUtil.isNull(session) && !session.isClosed()) {
			session.closeAndClearTokenInformation();
		}

		/**
		 * Face book user info changed call back added
		 */
		facebookShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isfacebookclicked = true;

				if (facebookPreferences.getString("iconpopup", "true").equalsIgnoreCase("true")) {
					showDialog(AppConstant.ICON_FACEBOOK_SHARE);
				} else if (facebookPreferences.getString("iconfacebookon","true").equalsIgnoreCase("true")) {
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

	/**
	 * Inner class for executing service in back ground thread in order to
	 * insert deal into the data base.
	 * 
	 * @author dillip.lenka
	 * 
	 */
	public class InsertDealAsyncTask extends AsyncTask<DealsDetailVo, Void, Void> {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			if (!isFinishing()) {
				mDialog = ProgressDialog.show(ClaimOfferActivity.this,"Claiming ", "Please wait...");
				mDialog.setCancelable(false);
			}

		}

		@Override
		protected Void doInBackground(DealsDetailVo... dealsDetailVo) {
			try {
				ClaimOfferService claimService = new ClaimOfferService();
				message = claimService.claimTheOffer(dealsDetailVo[0]);
				Log.v("Calim service mesage ", message);
			} catch (ConnectTimeoutException exe) {
				message = null;
				Log.e("Exception occured","ConnectTimeoutException occured at the time of Calim the offer",exe);
				Intent contactIntent = new Intent(getApplicationContext(),ContactSupportActvity.class);
				startActivity(contactIntent);
			} catch (Exception e) {
				message = null;
				Log.e("Exception occured","Exception occured at the time of login", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) 
		{
			if (mDialog != null)
				mDialog.dismiss();
			if (!ValidationUtil.isNullOrEmpty(message)&& message.equals("You have successfully claimed this offer."))
			{
				List<OffersDetailsVo> offersList = application.getOffersList();
				if(!ValidationUtil.isNullOrEmpty(application.getDealsList()))
					application.getDealsList().clear();
				if (!ValidationUtil.isNullOrEmpty(offersList))
				{offersList.clear();
				SharedPreferences userDetails = getSharedPreferences("UserName", 0);
				String userId = userDetails.getString("username", "");
				String password = userDetails.getString("password", "");
				LoginVo userloginvo = new LoginVo();
				userloginvo.setUserId(userId);
				userloginvo.setPassword(password);
				try {
					application.setOffersList(new MyOffersListAsyncTask().execute(userloginvo).get());
				} catch (Exception e) {
					e.printStackTrace();
				} 
				}

				if (!isFinishing())
					showDialog(Reser_comp);

			} else {
				if (!isFinishing())
					showDialog(Message_Id);
			}
		}
	}

	//get fresh offerList
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
			TangoTab application = (TangoTab) getApplication();
			application.setOffersList(offersList);
			application = (TangoTab) getApplication();
			AutoCheckinActivity activity = new AutoCheckinActivity(ClaimOfferActivity.this,getApplicationContext(), offersList);
			activity.doCheckIn();

		}
	}


	/**
	 * Menu information added here.
	 */
	public void onMenuSelected(int item) {
		switch (item) {
		case 0:
			Intent homeIntent = new Intent(this, MyOffersActivity.class);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(homeIntent);
			break;

		case 1:
			Intent businessearchIntent = new Intent(this, NearMeActivity.class);
			businessearchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(businessearchIntent);
			break;

		case 2:
			Intent contactmanagerIntent = new Intent(this, SearchActivity.class);
			contactmanagerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(contactmanagerIntent);
			break;
		case 3:
			Intent followupIntent = new Intent(this, SettingsActivity.class);
			followupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(followupIntent);
			break;
		}
	}

	/**
	 * Set the data into the UI
	 * 
	 * @param dealsDetailVo
	 */
	private void setData(DealsDetailVo dealsDetailVo) {
		/*if (selectedPosition != 3)
		{*/
		ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
		sv.scrollTo(0, 0);
		Name.setVisibility(View.GONE);

		businessName.setText(dealsDetailVo.getBusinessName());
		/*	offerImage.setScaleType(ImageView.ScaleType.FIT_XY);
			Display display = getWindowManager().getDefaultDisplay(); 
	          int width = display.getWidth();
	          int height = display.getHeight();
			offerImage.setLayoutParams(new FrameLayout.LayoutParams((int) (width/2),(int) (height/4)));*/
		//ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		offerImage.setImageUrl(dealsDetailVo.getImageUrl(),VolleySingleton.getInstance(this).getImageLoader());
		//imageLoader.DisplayImage(dealsDetailVo.getImageUrl(), offerImage,ClaimOfferActivity.this);
		dealName.setText(dealsDetailVo.getDealName());
		dealNameBold.setText(dealsDetailVo.getDealDescription());
		String phone = dealsDetailVo.getPhone();

		try {
			if (!ValidationUtil.isNullOrEmpty(phone)) {
				String output = phone.substring(0, 3) + "-"+ phone.substring(3, 6) + "-"+ phone.substring(6, 10);
				restPhone.setText(output);
			}
		} catch (StringIndexOutOfBoundsException e) {
			Log.e("Excepton:", "StringIndexOutOfBoundsException ", e);
		}
		TextView newDeals = (TextView) findViewById(R.id.myImageViewText);
		String availableDeals = dealsDetailVo.getNoDealsAvailable();
		if (availableDeals.length() > 1) {
			newDeals.setText(" " + dealsDetailVo.getNoDealsAvailable()+ "  " + "\n" + "LEFT");
		} else {
			newDeals.setText("  " + dealsDetailVo.getNoDealsAvailable()+ "\n" + "LEFT");
		}
		newDeals.startAnimation(rotate);
		String milesOrKm = " miles";
		StringBuilder dealsAvailable = new StringBuilder();
		String drivingDistance = dealsDetailVo.getDrivingDistance();
		if (!ValidationUtil.isNullOrEmpty(country)&& country.equalsIgnoreCase("canada")) {
			drivingDistance = String.valueOf(new DecimalFormat("##.##").format(Float.parseFloat(dealsDetailVo.getDrivingDistance()) * 1.61));
			milesOrKm = " Km";
		}
		distance.setText(dealsAvailable.append(drivingDistance).append(milesOrKm).toString());

		dealRestriction.setText(dealsDetailVo.getDealRestriction());
		String restDealStartDate = dealsDetailVo
				.getDealAvailableStartDate();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String mTimeStamp = null;
		try {
			Date dt = formatter.parse(restDealStartDate);
			SimpleDateFormat formatter2 = new SimpleDateFormat("EEEE, MMMM dd");
			mTimeStamp = formatter2.format(dt);
		} catch (Exception e) {
			Log.e("Exception occured :","Exception ocuured at the time parsing the date");
			e.printStackTrace();
		}
		dealDate.setText(mTimeStamp);
		dealTime.setText(dealsDetailVo.getStartTime() + " - "+ dealsDetailVo.getEndTime());
		String availableStartDate = dealsDetailVo.getDealAvailableStartDate();
		StringBuilder stDate = new StringBuilder();
		StringBuilder endDate = new StringBuilder();
		if (!ValidationUtil.isNullOrEmpty(availableStartDate)) {
			String endTime = dealsDetailVo.getEndTime();
			if (endTime.equals("12:00 AM")) {
				endTime = "11:59 PM";
			}
			String startTime = dealsDetailVo.getStartTime();
			availableStartTime = stDate.append(availableStartDate).append(" ").append(startTime).toString();
			availableEndTime = endDate.append(availableStartDate).append(" ").append(endTime).toString();
		}

		/*} else {
			{
				 Display display = getWindowManager().getDefaultDisplay(); 
		          int width = display.getWidth();
		          int height = display.getHeight();
				offerImage.setScaleType(ImageView.ScaleType.FIT_XY);
				offerImage.setLayoutParams(new FrameLayout.LayoutParams(width,(int) (height/2.66)));
				imageLayout.setVisibility(View.GONE);
				OfferDetailRl.setVisibility(View.GONE);
				Name.setText(dealsDetailVo.getBusinessName());
				businessName.setText(dealsDetailVo.getBusinessName());
				ImageLoader imageLoader = new ImageLoader(getApplicationContext());



				offerImage.setImageBitmap(imageLoader.getResizedBitmap(dealsDetailVo.getImageUrl(), width, 180, offerImage));
				dealName.setText(dealsDetailVo.getDealName());
				dealNameBold.setText(dealsDetailVo.getDealDescription());
				String phone = dealsDetailVo.getPhone();

				try {
					if (!ValidationUtil.isNullOrEmpty(phone)) {
						String output = phone.substring(0, 3) + "-"+ phone.substring(3, 6) + "-"+ phone.substring(6, 10);
						restPhone.setText(output);
					}
				} catch (StringIndexOutOfBoundsException e) {
					Log.e("Excepton:", "StringIndexOutOfBoundsException ", e);
				}
				TextView newDeals = (TextView) findViewById(R.id.myImageViewText);
				String availableDeals = dealsDetailVo.getNoDealsAvailable();
				if (availableDeals.length() > 1) {
					newDeals.setText(" " + dealsDetailVo.getNoDealsAvailable()+ "  " + "\n" + "LEFT");
				} else {
					newDeals.setText("  " + dealsDetailVo.getNoDealsAvailable()+ "\n" + "LEFT");
				}
				newDeals.startAnimation(rotate);
				String milesOrKm = " miles";
				StringBuilder dealsAvailable = new StringBuilder();
				String drivingDistance = dealsDetailVo.getDrivingDistance();
				if (!ValidationUtil.isNullOrEmpty(country)&& country.equalsIgnoreCase("canada")) {
					drivingDistance = String.valueOf(new DecimalFormat("##.##").format(Float.parseFloat(dealsDetailVo.getDrivingDistance()) * 1.61));
					milesOrKm = " Km";
				}
				distance.setText(dealsAvailable.append(drivingDistance)
						.append(milesOrKm).toString());
				dealRestriction.setText(dealsDetailVo.getDealRestriction());
				String restDealStartDate = dealsDetailVo.getDealAvailableStartDate();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String mTimeStamp = null;
				try {
					Date dt = formatter.parse(restDealStartDate);
					SimpleDateFormat formatter2 = new SimpleDateFormat("EEEE, MMMM dd");
					mTimeStamp = formatter2.format(dt);
				} catch (Exception e) {
					Log.e("Exception occured :","Exception ocuured at the time parsing the date");
					e.printStackTrace();
				}
				dealDate.setText(mTimeStamp);
				dealTime.setText(dealsDetailVo.getStartTime() + " - "+ dealsDetailVo.getEndTime());
				String availableStartDate = dealsDetailVo.getDealAvailableStartDate();
				StringBuilder stDate = new StringBuilder();
				StringBuilder endDate = new StringBuilder();
				if (!ValidationUtil.isNullOrEmpty(availableStartDate)) {
					String endTime = dealsDetailVo.getEndTime();
					if (endTime.equals("12:00 AM")) {
						endTime = "11:59 PM";
					}
					String startTime = dealsDetailVo.getStartTime();
					availableStartTime = stDate.append(availableStartDate).append(" ").append(startTime).toString();
					availableEndTime = endDate.append(availableStartDate).append(" ").append(endTime).toString();
				}
			}*/
		//}
	}

	/**
	 * This method will check the Internet connection for the application.
	 * 
	 * @return
	 */
	private boolean checkInternetConnection()
	{
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected()) ? true : false;

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Message_Id:
			AlertDialog.Builder ab = new AlertDialog.Builder(ClaimOfferActivity.this);
			ab.setTitle("TangoTab");
			ab.setMessage(message);
			ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return ab.create();
		case Reser_comp:
			AlertDialog.Builder ab1 = new AlertDialog.Builder(ClaimOfferActivity.this);
			ab1.setTitle("TangoTab");
			ab1.setMessage(message);
			ab1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					shareflow.clear();
					next = 0;
					/**
					 * Back to my offers page.
					 */

					Intent calimedIntent = new
							Intent(getApplicationContext(), MyOffersActivity.class);
					startActivity(calimedIntent);

				}
			});
			return ab1.create();
		case 10:
			AlertDialog.Builder ab2 = new AlertDialog.Builder(ClaimOfferActivity.this);
			ab2.setTitle("TangoTab");
			ab2.setMessage("We are unable to make an internet connection at this time.Some functionalities will be limited until a connection is made.");
			ab2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return ab2.create();
		case AppConstant.FACEBOOK_POST_SUCCESSFUL_DIALOG:
			AlertDialog.Builder facebookShareDialog = new AlertDialog.Builder(ClaimOfferActivity.this);
			facebookShareDialog.setTitle("TangoTab");
			facebookShareDialog.setMessage("Successfully posted on your facebook account wall");
			facebookShareDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Session session = Session.getActiveSession();
					if (!ValidationUtil.isNull(session)&& !session.isClosed()) {
						session.closeAndClearTokenInformation();
					}
					if (!isfacebookclicked) {
						next++;
						if (next < shareflow.size()) {
							shareHandler.sendEmptyMessage(shareflow.get(next));
						} else
							isfacebookclicked = false;

					}
				}
			});

			return facebookShareDialog.create();

		case TWITTER_ID:
			AlertDialog.Builder twitterDialog = new AlertDialog.Builder(ClaimOfferActivity.this);
			twitterDialog.setTitle("TangoTab");
			twitterDialog.setMessage(AppConstant.TWITTER_OFF_MESSGAGE);
			twitterDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return twitterDialog.create();

		case TWEET_SENT:
			AlertDialog.Builder tweetSent = new AlertDialog.Builder(ClaimOfferActivity.this);
			tweetSent.setTitle("TangoTab");
			tweetSent.setMessage("Successfully posted on your Twitter account wall");
			tweetSent.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (!istwitterclicked) {
						next++;
						if (next < shareflow.size()) {

							shareHandler.sendEmptyMessage(shareflow.get(next));

						} else {
							istwitterclicked = false;
						}
					}
					if (isClaimClicked) {
						Intent calimedIntent = new Intent(getApplicationContext(),MyOffersActivity.class);
						startActivity(calimedIntent);
						isClaimClicked = false;
					}
				}
			});
			return tweetSent.create();
		case TWITTER_FAILED:
			AlertDialog.Builder tweetFailed = new AlertDialog.Builder(
					ClaimOfferActivity.this);
			tweetFailed.setTitle("TangoTab");
			tweetFailed.setMessage("Tweet failed ");
			tweetFailed.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (isClaimClicked) {
						Intent calimedIntent = new Intent(
								getApplicationContext(),
								MyOffersActivity.class);
						startActivity(calimedIntent);
						isClaimClicked = false;
					}
				}
			});
			return tweetFailed.create();
		case TWITTER_POST_FAILED:
			AlertDialog.Builder postFailed = new AlertDialog.Builder(
					ClaimOfferActivity.this);
			postFailed.setTitle("TangoTab");
			postFailed.setMessage("Sorry this offer was already Tweeted");
			postFailed.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (isClaimClicked) {
						Intent calimedIntent = new Intent(
								getApplicationContext(),
								MyOffersActivity.class);
						startActivity(calimedIntent);
						isClaimClicked = false;
					}
				}
			});
			return postFailed.create();
		case AppConstant.SHARE_DISMISS_DIALOG:
			showeddialog = 1;
			AlertDialog.Builder sharedetails = new AlertDialog.Builder(
					ClaimOfferActivity.this);
			sharedetails.setTitle("TangoTab");
			sharedetails
			.setMessage("Would you like to share this claim with your friends on FaceBook ?");
			sharedetails.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					posttoFacebookClaim();
				}
			});
			sharedetails.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					dialog.dismiss();
					if (isClaimClicked) {
						if (isClaimClicked) {
							Intent calimedIntent = new Intent(
									getApplicationContext(),
									MyOffersActivity.class);
							startActivity(calimedIntent);
							isClaimClicked = false;
						}
					}

				}
			});
			return sharedetails.create();

		case AppConstant.SHARE_TWITTER_DISMISS_DIALOG:
			AlertDialog.Builder twitterdetails = new AlertDialog.Builder(
					ClaimOfferActivity.this);
			twitterdetails.setTitle("TangoTab");
			twitterdetails
			.setMessage("Would you like to share this claim with your friends on Twitter?");
			twitterdetails.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					postoTwitterClaim();
				}
			});
			twitterdetails.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (isClaimClicked) {
						if (isClaimClicked) {
							Intent calimedIntent = new Intent(
									getApplicationContext(),
									MyOffersActivity.class);
							startActivity(calimedIntent);
							isClaimClicked = false;
						}
					}

				}
			});
			return twitterdetails.create();
		case AppConstant.ICON_FACEBOOK_SHARE:
			AlertDialog.Builder sharedetails1 = new AlertDialog.Builder(
					ClaimOfferActivity.this);
			sharedetails1.setTitle("TangoTab");
			sharedetails1
			.setMessage("Thank you for your contribution to fight against hunger.");
			sharedetails1.setPositiveButton("Share",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
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

		case AppConstant.ICON_TWITTER_SHARE:
			AlertDialog.Builder twitterdetailsicon = new AlertDialog.Builder(
					ClaimOfferActivity.this);
			twitterdetailsicon.setTitle("TangoTab");
			twitterdetailsicon
			.setMessage("Thank you for your contribution to fight against hunger. ");
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

		case AppConstant.SHARE_COMMON_DIALOG:
			AlertDialog.Builder common = new AlertDialog.Builder(
					ClaimOfferActivity.this);
			common.setTitle("TangoTab");
			common.setMessage("Would you like to share this claim with your friends on Facebook and Twitter? ");
			common.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					shareflow.clear();
					shareflow.add(AppConstant.TWITTER_SHARE);
					posttoFacebookClaim();
				}
			});
			common.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (isClaimClicked) {
						if (isClaimClicked) {
							Intent calimedIntent = new Intent(
									getApplicationContext(),
									MyOffersActivity.class);
							startActivity(calimedIntent);
							isClaimClicked = false;
						}
					}

				}
			});
			return common.create();

		}
		return null;

	}

	/**
	 * Handle onClick events on the Views
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {

		if (v == mBtnNext) {
			Log.e("search list size", "" + searchList.size());
			try {

				/**
				 * Check the current index
				 */
				mSelectedIndex = mSelectedIndex + 1;
				if ((mSelectedIndex < searchList.size())) {
					setData(searchList.get(mSelectedIndex));
					dealsDetailVo = searchList.get(mSelectedIndex);
					if (mSelectedIndex + 1 == searchList.size()) {
						mBtnNext.setVisibility(View.GONE);
						// backLayout.width = LayoutParams.FILL_PARENT;
					}
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e("Exception:", "ArrayIndexOutOfExceptions occure", e);
			}

		}
		else if (v == mBtnBack)
		{
			AppConstant.IS_CLAIM_OFFER_PAGE =false;
			finish();
		} else if (v == mTwitterShare) {
			istwitterclicked = true;
			if (twitterPreferences.getString("iconpopup", "true")
					.equalsIgnoreCase("true")) {
				showDialog(AppConstant.ICON_TWITTER_SHARE);
			} else if (twitterPreferences.getString("icontwitteron", "true")
					.equalsIgnoreCase("true")) {
				posttoTwitter();
			}
		}
	}

	/**
	 * Displays a toast notification with No more offers
	 */
	private void showToastMessage(String message) {
		Toast.makeText(application, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Start a asynctask and send a tweet
	 */
	private void sendTweet() {
		new SendTweetAsyncTask().execute();
	}

	private void sendTweetClaim() {
		new SendTweetAsyncTaskClaim().execute();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AppConstant.TWITTER_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				sendTweet();
			} else {
				showToastMessage("There was an error while connecting with Twitter");
			}
		}
	}

	/**
	 * AsyncTask for sending a tweet
	 * 
	 * @author Ram.Donda
	 * 
	 */
	public class SendTweetAsyncTask extends AsyncTask<Void, Void, Integer> {
		private final int SUCCESS = 1, FAILED = 0;
		ProgressDialog mTweetProgressDialog;

		@Override
		protected Integer doInBackground(Void... params) {
			try {

				// twitter decription changes

				/*TwitterUtils.sendTweet(prefs, dealsDetailVo.getBusinessName()
						+ " " + dealsDetailVo.getDealName() + " ",
						dealsDetailVo.getImageUrl());*/

				TwitterUtils.sendTweet(prefs, dealsDetailVo.getBusinessName()+" offer on TangoTab" + "\n"
						+ dealsDetailVo.getDealName() + "\n"
						+" \n http://www.tangotab.com/jsp/dealSummary.do?dealId="+dealsDetailVo.getId()
						+"&date="+dealsDetailVo.getDealEndDate());

				return SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
				return FAILED;
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mTweetProgressDialog == null) {
				mTweetProgressDialog = new ProgressDialog(
						ClaimOfferActivity.this);
				mTweetProgressDialog.setMessage("Sending tweet..");
			}
			mTweetProgressDialog.show();

		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			mTweetProgressDialog.dismiss();
			if (result == SUCCESS) {
				showDialog(TWEET_SENT);
			} else {
				showDialog(TWITTER_POST_FAILED);
			}
		}
	}

	public class SendTweetAsyncTaskClaim extends AsyncTask<Void, Void, Integer> {
		private final int SUCCESS = 1, FAILED = 0;
		ProgressDialog mTweetProgressDialog;

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				SharedPreferences sharedUserDetails = getSharedPreferences(
						"UserDetails", 0);
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append(sharedUserDetails.getString("firstName", "")
						.substring(0, 1).toUpperCase());

				TwitterUtils.sendTweet(prefs,
						strBuilder.toString() + " is planning to claim "
								+ dealsDetailVo.getBusinessName() + " "
								+ dealsDetailVo.getDealDescription() + " "
								+ dealsDetailVo.getDealName());
				return SUCCESS;
			} catch (Exception e) {
				if (isClaimClicked) {
					Intent calimedIntent = new Intent(getApplicationContext(),
							MyOffersActivity.class);
					startActivity(calimedIntent);
					isClaimClicked = false;
				}
				return FAILED;
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mTweetProgressDialog == null) {
				mTweetProgressDialog = new ProgressDialog(
						ClaimOfferActivity.this);
				mTweetProgressDialog.setMessage("Sending tweet..");
			}
			mTweetProgressDialog.show();

		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			mTweetProgressDialog.dismiss();
			if (result == SUCCESS) {
				showDialog(TWEET_SENT);
			} else {
				// showDialog(TWITTER_POST_FAILED);
				if (isClaimClicked) {
					Intent calimedIntent = new Intent(getApplicationContext(),
							MyOffersActivity.class);
					startActivity(calimedIntent);
					isClaimClicked = false;
				}
			}
		}
	}

	/*
	 * Facebook sharing implementation
	 */

	class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

		private Context context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private SharedPreferences prefs;

		public RetrieveAccessTokenTask(Context context, OAuthConsumer consumer,
				OAuthProvider provider, SharedPreferences prefs) {
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.prefs = prefs;
		}

		@Override
		protected Void doInBackground(Uri... params) {
			final Uri uri = params[0];

			final String oauth_verifier = uri
					.getQueryParameter(OAuth.OAUTH_VERIFIER);
			// Log.i("path is..",oauth_verifier);
			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);

				final Editor edit = prefs.edit();
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET,
						consumer.getTokenSecret());
				edit.commit();

				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

				consumer.setTokenWithSecret(token, secret);
				myhand.post(run);

				Log.v("Authenticatiuon:", "OAuth - Access Token Retrieved");

			} catch (Exception e) {
				Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
				// twitterHandler.sendEmptyMessage(TWITTER_FAILED);
				if (!isFinishing()) {
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
				}
				if (isClaimClicked) {
					Intent calimedIntent = new Intent(getApplicationContext(),
							MyOffersActivity.class);
					startActivity(calimedIntent);
					isClaimClicked = false;
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

		}

		Runnable run = new Runnable() {
			public void run() {
				Log.e("twitter", "checked");
				twitter = true;
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				/*
				 * setResult(RESULT_OK); finish();
				 */
				sendTweet();
				// twittercheck.setImageResource(R.drawable.checked);
			}
		};
	}

	class OAuthRequestTokenTask extends AsyncTask<Void, Void, Void> {

		final String TAG = getClass().getName();
		private Context context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private Handler myhand;
		String url;

		/**
		 * 
		 * We pass the OAuth consumer and provider.
		 * 
		 * @param context
		 *            Required to be able to start the intent to launch the
		 *            browser.
		 * @param provider
		 *            The OAuthProvider object
		 * @param consumer
		 *            The OAuthConsumer object
		 */
		public OAuthRequestTokenTask(Context context, OAuthConsumer consumer,
				OAuthProvider provider, Handler myhand) {
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.myhand = myhand;
		}

		/**
		 * 
		 * Retrieve the OAuth Request Token and present a browser to the user to
		 * authorize the token.
		 * 
		 */
		@Override
		protected Void doInBackground(Void... params) {

			try {
				Log.i(TAG, "Retrieving request token from Google servers");
				url = provider.retrieveRequestToken(consumer,
						AppConstant.OAUTH_CALLBACK_URL);
				Log.i(TAG, "Popping a browser with the authorize URL : " + url);

				myhand.post(run);
			} catch (Exception e) {
				Log.e(TAG, "Error during OAUth retrieve request token", e);
				/*
				 * setResult(RESULT_CANCELED); finish();
				 */
				// mydialog.dismiss();
				// twitterHandler.sendEmptyMessage(TWITTER_FAILED);
				if (!isFinishing()) {
					ClaimOfferActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (progressDialog != null
									&& progressDialog.isShowing()) {
								progressDialog.dismiss();
								if (isClaimClicked) {
									if (isClaimClicked) {
										Intent calimedIntent = new Intent(
												getApplicationContext(),
												MyOffersActivity.class);
										startActivity(calimedIntent);
										isClaimClicked = false;
									}
								}
							}

						}
					});

				}
				if (isClaimClicked) {
					Intent calimedIntent = new Intent(getApplicationContext(),
							MyOffersActivity.class);
					startActivity(calimedIntent);
					isClaimClicked = false;
				}

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

		}

		Runnable run = new Runnable() {
			public void run() {

				Log.e("url is", url);
				dialog = new Dialog(context);
				// dialog.setOnDismissListener(MeActivity.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setCancelable(false);
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View vi = inflater.inflate(R.layout.twitterpopup, null);
				dialog.setContentView(vi);

				ImageView close = (ImageView) vi.findViewById(R.id.close);
				close.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						/*
						 * setResult(RESULT_CANCELED); finish();
						 */
						if (progressDialog != null
								&& progressDialog.isShowing()) {
							progressDialog.dismiss();
						}

						// mydialog.dismiss();
					}
				});
				WebView wb = (WebView) vi.findViewById(R.id.twitterweb);
				wb.setWebViewClient(new myWebClient());
				wb.getSettings().setJavaScriptEnabled(true);
				wb.loadUrl(url);

			}
		};

		class myWebClient extends WebViewClient { // HERE IS THE MAIN CHANGE.
			String url;

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				this.url = url;
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub

				this.url = url;
				if (url.startsWith("htt")) {

					view.loadUrl(url);
				} else {

					dialog.dismiss();
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(ClaimOfferActivity.this);
					final Uri uri = Uri.parse(url);
					if (uri != null
							&& uri.getScheme().equals(
									AppConstant.OAUTH_CALLBACK_SCHEME)) {
						Log.e(TAG, "Callback received : " + uri);
						Log.e(TAG, "Retrieving Access Token");
						new RetrieveAccessTokenTask(ClaimOfferActivity.this,
								consumer, provider, prefs).execute(uri);

					}
				}

				return true;

			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);

				if (url.startsWith("http://")) {
					Log.e("http", "yes");
					// mydialog.dismiss();
					try {
						dialog.show();
					} catch (Exception e) {

					}
				} else if (pageFinish) {
					Log.e("https", "finished");
					// mydialog.dismiss();
					dialog.show();
				} else {
					Log.e("https", "true");
					pageFinish = true;
				}

			}

		}
	}

	private void startTwitterLogin() {

		try {

			this.consumer = new CommonsHttpOAuthConsumer(
					AppConstant.CONSUMER_KEY, AppConstant.CONSUMER_SECRET);
			this.provider = new CommonsHttpOAuthProvider(
					AppConstant.REQUEST_URL, AppConstant.ACCESS_URL,
					AppConstant.AUTHORIZE_URL);
		} catch (Exception e) {
			Log.e(TAG, "Error creating consumer / provider", e);

		}

		myhand = new Handler();
		new OAuthRequestTokenTask(this, consumer, provider, myhand).execute();
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == TWITTER_FAILED) {

			showDialog(TWITTER_FAILED);
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			Intent mainMenuIntent = new Intent(ClaimOfferActivity.this,
					MainMenuActivity.class);
			mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainMenuIntent);
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			Intent searchIntent = new Intent(ClaimOfferActivity.this,
					SearchActivity.class);
			searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(searchIntent);
			return true;
		}

		return super.onKeyDown(keycode, e);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.push_left_out, R.anim.push_left_out);
	}

	class RequestAsyncTask extends AsyncTask<Void, Void, List<Response>> {
		private final String TAG = RequestAsyncTask.class.getCanonicalName();

		private final HttpURLConnection connection;
		private final RequestBatch requests;

		private Exception exception;

		/**
		 * Constructor. Serialization of the requests will be done in the
		 * background, so any serialization- related errors will be returned via
		 * the Response.getError() method.
		 * 
		 * @param requests
		 *            the requests to execute
		 */
		public RequestAsyncTask(Request... requests) {
			this(null, new RequestBatch(requests));
		}

		/**
		 * Constructor. Serialization of the requests will be done in the
		 * background, so any serialization- related errors will be returned via
		 * the Response.getError() method.
		 * 
		 * @param requests
		 *            the requests to execute
		 */
		public RequestAsyncTask(Collection<Request> requests) {
			this(null, new RequestBatch(requests));
		}

		/**
		 * Constructor. Serialization of the requests will be done in the
		 * background, so any serialization- related errors will be returned via
		 * the Response.getError() method.
		 * 
		 * @param requests
		 *            the requests to execute
		 */
		public RequestAsyncTask(RequestBatch requests) {
			this(null, requests);
		}

		/**
		 * Constructor that allows specification of an HTTP connection to use
		 * for executing the requests. No validation is done that the contents
		 * of the connection actually reflect the serialized requests, so it is
		 * the caller's responsibility to ensure that it will correctly generate
		 * the desired responses.
		 * 
		 * @param connection
		 *            the HTTP connection to use to execute the requests
		 * @param requests
		 *            the requests to execute
		 */
		public RequestAsyncTask(HttpURLConnection connection,
				Request... requests) {
			this(connection, new RequestBatch(requests));
		}

		/**
		 * Constructor that allows specification of an HTTP connection to use
		 * for executing the requests. No validation is done that the contents
		 * of the connection actually reflect the serialized requests, so it is
		 * the caller's responsibility to ensure that it will correctly generate
		 * the desired responses.
		 * 
		 * @param connection
		 *            the HTTP connection to use to execute the requests
		 * @param requests
		 *            the requests to execute
		 */
		public RequestAsyncTask(HttpURLConnection connection,
				Collection<Request> requests) {
			this(connection, new RequestBatch(requests));
		}

		/**
		 * Constructor that allows specification of an HTTP connection to use
		 * for executing the requests. No validation is done that the contents
		 * of the connection actually reflect the serialized requests, so it is
		 * the caller's responsibility to ensure that it will correctly generate
		 * the desired responses.
		 * 
		 * @param connection
		 *            the HTTP connection to use to execute the requests
		 * @param requests
		 *            the requests to execute
		 */
		public RequestAsyncTask(HttpURLConnection connection,
				RequestBatch requests) {
			this.requests = requests;
			this.connection = connection;
		}

		protected final Exception getException() {
			return exception;
		}

		@Override
		public String toString() {
			return new StringBuilder().append("{RequestAsyncTask: ")
					.append(" connection: ").append(connection)
					.append(", requests: ").append(requests).append("}")
					.toString();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			/*
			 * if (requests.getCallbackHandler() == null) { // We want any
			 * callbacks to go to a handler on this thread unless a handler has
			 * already been specified. requests.setCallbackHandler(new
			 * Handler()); }
			 */
		}

		@Override
		protected void onPostExecute(List<Response> result) {
			super.onPostExecute(result);

			if (exception != null) {
				Log.d(TAG,
						String.format(
								"onPostExecute: exception encountered during request: ",
								exception.getMessage()));
			}
			if (isTwitterShare) {
				// Check if the user is already logged in twitter
				if (TwitterUtils.isAuthenticated(prefs)) {
					sendTweet();

				}
				// Start twitter activity if user is not logged in
				else {
					if (progressDialog == null) {
						progressDialog = new ProgressDialog(
								ClaimOfferActivity.this);
						progressDialog.setMessage("Please wait..");
						progressDialog.setCancelable(false);
					}
					progressDialog.show();
					startTwitterLogin();
				}
			}
		}

		@Override
		protected List<Response> doInBackground(Void... params) {
			try {
				if (connection == null) {
					return Request.executeBatchAndWait(requests);
				} else {
					return Request.executeConnectionAndWait(connection,
							requests);
				}
			} catch (Exception e) {
				exception = e;
				return null;
			}
		}
	}

	private void publishMessageCalim() {

		SharedPreferences sharedUserDetails = getSharedPreferences(
				"UserDetails", 0);
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(sharedUserDetails.getString("firstName", "")
				.substring(0, 1).toUpperCase());
		facebookFacade.publishMessage(strBuilder.toString()
				+ " is planning to claim  " + dealsDetailVo.getBusinessName()
				+ "\n " + dealsDetailVo.getDealDescription(),
				"images.tangotab.com", dealsDetailVo.getBusinessName(), null,
				dealsDetailVo.getImageUrl(), null);

	}

	private void publishMessage() {
		SharedPreferences sharedUserDetails = getSharedPreferences(
				"UserDetails", 0);
		StringBuilder strBuilder = new StringBuilder();

		/*facebookFacade.publishMessage(
				 dealsDetailVo.getBusinessName(), "images.tangotab.com",
				dealsDetailVo.getDealDescription(), null,
				dealsDetailVo.getImageUrl(), null);*/
		facebookFacade.publishMessage(dealsDetailVo.getBusinessName()+" offers on TangoTab.",
				"http://www.tangotab.com/jsp/dealSummary.do?dealId="+dealsDetailVo.getId()+"&date="+dealsDetailVo.getDealEndDate(),
				dealsDetailVo.getDealDescription(),
				dealsDetailVo.getAddress(), dealsDetailVo.getImageUrl());
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

	PostListener claimpostListener = new PostListener() {

		public void onPostPublishingFailed() {
			postHandler.sendEmptyMessage(AppConstant.UNSUCCESFULL_POST);
		}

		public void onPostPublished() {

			if ((facebookPreferences.getString("showpopup", "false")
					.equalsIgnoreCase("false"))
					&& (twitterPreferences.getString("showpopup", "false")
							.equalsIgnoreCase("true"))) {
				if (!isFinishing()) {
					ClaimOfferActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							next = 0;
							if (next < shareflow.size()) {
								shareClaimHandler.sendEmptyMessage(shareflow
										.get(next));
							}

						}
					});
				}

			} else if ((facebookPreferences.getString("showpopup", "false")
					.equalsIgnoreCase("true"))
					&& (twitterPreferences.getString("showpopup", "false")
							.equalsIgnoreCase("false"))) {
				if (!isFinishing())
					ClaimOfferActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							next++;
							if (next < shareflow.size()) {
								shareClaimHandler.sendEmptyMessage(shareflow
										.get(next));
							}

						}
					});

			}

			else {
				next = 0;
				if (next < shareflow.size()) {
					shareClaimHandler.sendEmptyMessage(shareflow.get(next));
				}

			}// unregisterListeners();
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

	public void posttoFacebookClaim() {

		FacebookEvents.addAuthListener(authListener);
		FacebookEvents.addPostListener(claimpostListener);
		FacebookEvents.addLogoutListener(logoutListener);
		if (facebookFacade.isAuthorized()) {
			publishMessageCalim();

		} else {

			facebookFacade.authorize(new AuthListener() {

				public void onAuthSucceed() {

					publishMessageCalim();

				}

				public void onAuthFail(String error) {

				}
			});
		}

	}

	public void posttoFacebook() {

		registerListeners();
		if (facebookFacade.isAuthorized()) {
			publishMessage();

		} else {

			facebookFacade.authorize(new AuthListener() {

				public void onAuthSucceed() {

					publishMessage();

				}

				public void onAuthFail(String error) {
				}




			});
		}

	}

	int next = 0;
	boolean isSharecontinues = false;

	Handler shareHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case AppConstant.FACEBOOK_SHARE:
				if (showeddialog != 1
				&& facebookPreferences.getString("showpopup", "false")
				.equalsIgnoreCase("true")) {
					showDialog(AppConstant.SHARE_DISMISS_DIALOG);
				}
				if (facebookPreferences.getString("showpopup", "false")
						.equalsIgnoreCase("true")
						|| facebookPreferences
						.getString("isfacebookon", "true")
						.equalsIgnoreCase("true"))

					posttoFacebookClaim();
				facebookPreferences.getString("iconpopup", "false");

				break;
			case AppConstant.TWITTER_SHARE:
				if (showeddialog != 1
				&& twitterPreferences.getString("showpopup", "false")
				.equalsIgnoreCase("false")) {

					showDialog(AppConstant.SHARE_TWITTER_DISMISS_DIALOG);
				} else if (twitterPreferences.getString("showpopup", "false")
						.equalsIgnoreCase("true")
						|| twitterPreferences.getString("istwiiteron", "true")
						.equalsIgnoreCase("true"))
					postoTwitterClaim();
				twitterPreferences.getString("iconpopup", "false");

				break;
			case AppConstant.CLAIM_OFFER:
				new InsertDealAsyncTask().execute(dealsDetailVo);

				break;
			default:
				break;
			}

		};
	};

	Handler shareClaimHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case AppConstant.FACEBOOK_SHARE:

				showDialog(AppConstant.SHARE_DISMISS_DIALOG);

				break;
			case AppConstant.TWITTER_SHARE:

				if ((facebookPreferences.getString("showpopup", "false")
						.equalsIgnoreCase("true"))
						&& (twitterPreferences.getString("showpopup", "false")
								.equalsIgnoreCase("true"))) {
					postoTwitterClaim();

				} else if ((facebookPreferences.getString("showpopup", "false")
						.equalsIgnoreCase("false"))
						&& (twitterPreferences.getString("showpopup", "false")
								.equalsIgnoreCase("true"))) {
					showDialog(AppConstant.SHARE_TWITTER_DISMISS_DIALOG);
				} else if ((facebookPreferences.getString("showpopup", "false")
						.equalsIgnoreCase("true"))
						&& (twitterPreferences.getString("showpopup", "false")
								.equalsIgnoreCase("false"))) {
					postoTwitterClaim();
				} else {
					postoTwitterClaim();
				}

				break;

			case AppConstant.CLAIM_OFFER:
				new InsertDealAsyncTask().execute(dealsDetailVo);

				break;
			default:
				break;
			}

		};
	};


	public void postoTwitterClaim() {
		SharedPreferences userPreferences = getSharedPreferences("UserDetails",
				0);
		boolean isTwitterShareOn = userPreferences.getBoolean("isTwitterOn",
				false);
		// Check whether the twitter share option is on in settings

		// startTwitterDialog();

		if (TwitterUtils.isAuthenticated(prefs)) {
			sendTweetClaim();

		}
		// Start twitter activity if user is not logged in
		else {
			/*
			 * Intent twitterActivity=new
			 * Intent(ClaimOfferActivity.this,TwitterActivity.class);
			 * //startActivity(twitterActivity);
			 * startActivityForResult(twitterActivity
			 * ,AppConstant.TWITTER_REQUEST_CODE);
			 */if (!isFinishing()) {
				 if (progressDialog == null) {
					 progressDialog = new ProgressDialog(ClaimOfferActivity.this);
					 progressDialog.setMessage("Please wait..");
					 progressDialog.setCancelable(false);
				 }
				 progressDialog.show();
			 }

			 startTwitterLogin();
		}

	}

	public void posttoTwitter() {

		SharedPreferences userPreferences = getSharedPreferences("UserDetails",
				0);
		boolean isTwitterShareOn = userPreferences.getBoolean("isTwitterOn",
				false);
		// Check whether the twitter share option is on in settings

		// startTwitterDialog();

		if (TwitterUtils.isAuthenticated(prefs)) {
			sendTweet();

		}
		// Start twitter activity if user is not logged in
		else {
			/*
			 * Intent twitterActivity=new
			 * Intent(ClaimOfferActivity.this,TwitterActivity.class);
			 * //startActivity(twitterActivity);
			 * startActivityForResult(twitterActivity
			 * ,AppConstant.TWITTER_REQUEST_CODE);
			 */
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(ClaimOfferActivity.this);
				progressDialog.setMessage("Please wait..");
				progressDialog.setCancelable(false);
			}
			progressDialog.show();
			startTwitterLogin();
		}

	}


	@Override
	protected void onPause() {
		LocationManagerToggle.getInstance().cancelTimer(t);
		LocationManagerToggle.getInstance().removeCurentLocationUpdate();
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//timer for app notification
		t=LocationManagerToggle.getInstance().setTimer(null, 0,5000);
		LocationManagerToggle.getInstance().initalizeLocationManagerService(this,this);
		/**
		 * do auto checkin
		 */
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,getApplicationContext(),offerList);
		activity.doCheckIn();

	}

}