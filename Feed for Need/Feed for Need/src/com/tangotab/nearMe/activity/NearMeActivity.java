package com.tangotab.nearMe.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.tangotab.LocationManagerToggle;
import com.c3global.R;
import com.tangotab.appNotification.activity.AppNotificationActivity;
import com.tangotab.calendar.activity.CalendarActivity;
import com.tangotab.calendar.utils.CalendarView;
import com.tangotab.claimOffer.activity.ClaimOfferActivity;
import com.tangotab.contactSupport.activity.ContactSupportActvity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.ex.TangoTabException;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.DateFormatUtil;
import com.tangotab.core.utils.GeoCoderUtil;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.core.vo.LocationVo;
import com.tangotab.login.vo.LoginVo;
import com.tangotab.mainmenu.activity.MainMenuActivity;
import com.tangotab.map.activity.MappingActivity;
import com.tangotab.me.activity.MeActivity;
import com.tangotab.me.service.MyPhilanthropyService;
import com.tangotab.myOfferDetails.activity.AutoCheckinActivity;
import com.tangotab.myOfferDetails.activity.MyoffersDetailActivity;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.myOffers.activity.MyOffersActivity;
import com.tangotab.myOffers.service.MyOffersService;
import com.tangotab.nearMe.adapter.NearMeGalleryAdapter;
import com.tangotab.nearMe.adapter.NearMeListAdapter;
import com.tangotab.nearMe.vo.DealsDetailVo;
import com.tangotab.search.activity.SearchActivity;
import com.tangotab.search.adapter.CustomWheelAdapter;
import com.tangotab.search.adapter.SearchListAdapter;
import com.tangotab.search.service.SearchService;
import com.tangotab.search.vo.SearchVo;

/**
 * This class will be display list of deals from near to your selected distance
 * location.
 * 
 * <bR>
 * Class : NearMeActivity <br>
 * Layout : nearme.xml
 * 
 * @author Dillip.Lenka
 * 
 */
public class NearMeActivity extends ListActivity {

	/*
	 * Meta Definitions
	 */
	private String userId;
	private String provider;
	private Timer t;
	private double dev_lat;
	private double dev_lang;
	private ListView itemsList;
	private List<DealsDetailVo> finalDealList = new ArrayList<DealsDetailVo>();
	private int pageCount = 1;
	private LoginVo loginvo;
	private TextView emptyList, currentDate, locationInfo;
	private TextView dinningType;
	public TangoTab application;
	private List<OffersDetailsVo> offersList, uncheckedOffers;
	private List<OffersDetailsVo> displayoffers = new ArrayList<OffersDetailsVo>();
	private Vibrator vibrator;
	private static int count;
	private RelativeLayout statusFed;
	private RelativeLayout LocationInfo;
	private TextView statusFedLabel;
	private RelativeLayout statusPendingOffers;
	private TextView statusPendingOffersLabel;

	private String country;
	private String homeZipCode;
	private String workZipCode;
	private String alternateZipCode;
	private RelativeLayout relativeLayoutTopBar;

	private String[] dinningStyles = new String[] { "All", "Breakfast",
			"Lunch", "Happy Hour", "Dinner", "Late" };
	int[] pics = { R.drawable.allselector, R.drawable.casual_selector,
			R.drawable.upscale_selector };

	private int[] topBarImgs = new int[] { R.drawable.white_one,
			R.drawable.white_two, R.drawable.white_three };
	GestureDetector gestureDetector;

	private int diningStyle = 0;
	private String dinningStyleType;

	private String diningId;
	private String selectedDate;
	private GoogleAnalyticsTracker tracker;
	private boolean scrolling = false;
	private String fromPage;
	private int mNewIndex;
	private int valueToSet = 0;
	private int pendingOffersStVisible;
	private NearMeGallery ga;
	private int selectedposition;
	boolean isZipCode;
	String Address;
	private String alternateZipCode1;
	private boolean status;
	private SearchVo searchVo;
	private int frmSearchCount = 1;
	private String zipCode;
	private int prevPos;
	private boolean isAutoScrolled = false;
	private LocationManager locationManager = null;

	private SharedPreferences gpsPreferences;
	private SharedPreferences.Editor gpsEditor;

	/**
	 * Execution will start here
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.nearme);

		/*
		 * boolean isGPS =
		 * locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		 * if(!isGPS) { showDialog(100); }
		 */

		gpsPreferences = getSharedPreferences(AppConstant.ENABLE_GPS_DIALOG,
				Context.MODE_PRIVATE);
		gpsEditor = gpsPreferences.edit();

		LocationInfo = (RelativeLayout) findViewById(R.id.locationInfo);
		/**
		 * Google analytics implementation added here.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(AppConstant.GA_REG_KEY, 10, this);
		tracker.setDebug(true);
		tracker.trackPageView(AppConstant.NEARME_PAGE);
		tracker.trackEvent("NearMe", "TrackEvent", "nearme", 1);
		// String title= getIntent().getExtras().getString("frmPagelogin");

		// DisplayMetrics dm = getResources().getDisplayMetrics();

		SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", 0);
		country = sharedPreferences.getString("Address", "");
		if (ValidationUtil.isNullOrEmpty(country))
			country = getIntent().getStringExtra("country");

		homeZipCode = sharedPreferences.getString("postal", "");
		workZipCode = sharedPreferences.getString("workZip", "");
		alternateZipCode = sharedPreferences.getString("alternateZip", "");
		locationInfo = (TextView) findViewById(R.id.locationLabel);

		application = (TangoTab) getApplication();
		String searchBy = getIntent().getStringExtra("SearchBy");
		SharedPreferences spf1 = getSharedPreferences("UserDetails", 0);

		if (ValidationUtil.isNullOrEmpty(searchBy)) {
			SharedPreferences spc1 = getSharedPreferences("locationDetail", 0);
			searchBy = spc1.getString("currentLocation", "Current Location");
		}
		// searchBy=AppConstant.SEARCH_BY;

		if (!ValidationUtil.isNullOrEmpty(searchBy)
				&& searchBy.equalsIgnoreCase("WorkZip")) {
			locationInfo.setText(ValidationUtil.capitalizeFully(workZipCode,
					null));
			zipCode = workZipCode;
		} else if (!ValidationUtil.isNullOrEmpty(searchBy)
				&& searchBy.equalsIgnoreCase("HomeZip")) {
			locationInfo.setText(ValidationUtil.capitalizeFully(homeZipCode,
					null));
			zipCode = homeZipCode;
		} else if (!ValidationUtil.isNullOrEmpty(searchBy)
				&& searchBy.equalsIgnoreCase("alternateZipCode")) {
			locationInfo.setText(ValidationUtil.capitalizeFully(
					alternateZipCode, null));
			zipCode = alternateZipCode;
		} else {
			locationInfo.setText("Current Location");
		}

		if (locationInfo.getText().toString()
				.equalsIgnoreCase("Current Location")) {
			AppConstant.selectedLat = AppConstant.dev_lat;
			AppConstant.selectedLong = AppConstant.dev_lang;
			LocationManagerToggle.getInstance().initalizeLocationManagerService(this, this);
			// initalizeLocationManagerService();
		} else {
			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			LocationVo location = GeoCoderUtil.getLatLongFromAddress(zipCode);
			if (location != null) {
				AppConstant.selectedLat = location.getLat();
				AppConstant.selectedLong = location.getLang();
			}

		}
		relativeLayoutTopBar = (RelativeLayout) findViewById(R.id.relativeLayoutBackground);
		emptyList = (TextView) findViewById(R.id.emptylist);
		final SharedPreferences datePreferences = getSharedPreferences(
				"NearMeDate", 0);
		itemsList = (ListView) findViewById(android.R.id.list);
		final GestureDetector gestureDetector = new GestureDetector(
				new MyGestureDetector());
		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Log.e("on touch", "called");
				gestureDetector.onTouchEvent(event);
				return false;
			}
		};
		itemsList.setOnTouchListener(gestureListener);

		View.OnTouchListener textListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Log.e("on touch", "called");

				return gestureDetector.onTouchEvent(event);
			}
		};
		emptyList.setOnTouchListener(textListener);
		relativeLayoutTopBar.setOnTouchListener(textListener);

		Button map = (Button) findViewById(R.id.map);
		Button topMenu = (Button) findViewById(R.id.topMenuButton);
		Button meButton = (Button) findViewById(R.id.topMeMenuButton);
		Button search = (Button) findViewById(R.id.topSearchMenuButton);
		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		statusFed = (RelativeLayout) findViewById(R.id.statusbarFed);
		statusFedLabel = (TextView) findViewById(R.id.statusbarFedLabel);
		statusPendingOffers = (RelativeLayout) findViewById(R.id.statusPendingOffers);
		statusPendingOffersLabel = (TextView) findViewById(R.id.statusbarPendingOffersLabel);
		// statusFed.setVisibility(View.GONE);
		statusPendingOffers.setVisibility(View.GONE);
		dinningType = (TextView) findViewById(R.id.tvDinningType);
		currentDate = (TextView) findViewById(R.id.tvCurrentDate);

		String calenderText = datePreferences.getString("selectText", "");
		selectedDate = datePreferences.getString("selectedDate", "");

		SharedPreferences dateSelect = getSharedPreferences("SearchDate", 0);
		SharedPreferences.Editor selectDate = dateSelect.edit();
		selectDate.putString("dateSelected", selectedDate);
		selectDate.commit();

		if (!ValidationUtil.isNullOrEmpty(calenderText)
				&& !ValidationUtil.isNullOrEmpty(selectedDate)) {
			currentDate.setText(calenderText);
		} else {
			Calendar today = Calendar.getInstance();
			// StringBuilder currentDate = new StringBuilder();
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
			selectedDate = sdfDate.format(today.getTime());
			currentDate.setText("Today");
		}

		currentDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent getDate = new Intent(NearMeActivity.this,
						CalendarActivity.class);
				int year = datePreferences.getInt("year", 0);
				int month = datePreferences.getInt("month", 0);
				int day = datePreferences.getInt("day", 0);
				CalendarView.prevDay = day;
				CalendarView.prevMonth = month;
				CalendarView.prevYear = year;
				startActivityForResult(getDate,
						AppConstant.GET_DATE_REQUEST_CODE);
				application.getDealsList().clear();

			}
		});

		/**
		 * Slider onItem selected implementation added.
		 */
		ga = (NearMeGallery) findViewById(R.id.nearMeGalleryView);
		ga.setAdapter(new NearMeGalleryAdapter(getApplicationContext(), pics));
		ga.setSelection(0, true);

		diningStyle = datePreferences.getInt("diningStyle", 0);

		prevPos = datePreferences.getInt("slidingPos", 0);
		Bundle bun = getIntent().getExtras();
		if (bun != null) {
			fromPage = bun.getString("fromPage");
			if (fromPage != null && fromPage.equalsIgnoreCase("searchPage")) {
				ga.setSelection(0, true);
				dinningType.setText(dinningStyles[0]);
			}
		} else {
			ga.setSelection(prevPos, true);
			dinningType.setText(dinningStyles[diningStyle]);
		}

		ga.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				Log.v("Changed----->", "" + pos);
				int change = pos;
				relativeLayoutTopBar.setBackgroundResource(topBarImgs[pos]);
				StringBuilder dinning = new StringBuilder();
				isAutoScrolled = false;
				selectedposition = pos + 1;
				if (selectedposition == 1) {
					dinning.append("1").append(",").append("2").append(",")
							.append("3").append(",").append("4");
					diningId = dinning.toString();

				} else if (selectedposition == 2) {
					dinning.append("2").append(",").append("3");
					diningId = dinning.toString();
				} else {
					dinning.append("4").append(",").append("5");
					diningId = dinning.toString();
				}

				if (fromPage != null && fromPage.equalsIgnoreCase("searchPage")) {
					if (pageCount == 1) {
						count = 10;
					}
					prevPos = datePreferences.getInt("slidingPos", 0);
					if (!ValidationUtil.isNullOrEmpty(application
							.getDealsList()) && prevPos == pos) {
						itemsList = getListView();
						itemsList.setCacheColorHint(Color.TRANSPARENT);
						emptyList.setVisibility(View.GONE);
						country = application.getCountry();
						locationInfo.setText(application.getLocation());
						// if (selectedposition != 3) {
						SearchListAdapter searchListAdapter = new SearchListAdapter(
								NearMeActivity.this,
								application.getDealsList(), true,
								selectedposition, country, NearMeActivity.this);
						setListAdapter(searchListAdapter);
						/*
						 * } else
						 * setUpScaleOrFineAdapter(application.getDealsList());
						 */
					} else {
						prevPos = change;
						Bundle bun = getIntent().getExtras();
						searchVo = (SearchVo) bun.getSerializable("searchVo");

						if (ValidationUtil.isNullOrEmpty(searchVo.getRestName())) {
							StringBuilder stringBuilder = new StringBuilder();
							if (!ValidationUtil
									.isNullOrEmpty(alternateZipCode1))
								locationInfo.setText(ValidationUtil
										.capitalizeFully(alternateZipCode1,
												null));
						}
						String searchDate = searchVo.getDate();
						try {
							String split[] = searchDate.split("-");
							int year = Integer.parseInt(split[0]);
							int month = Integer.parseInt(split[1]);
							int day = Integer.parseInt(split[2]);
							String searchDateText = getTextFromDate(year,
									month, day);
							if (frmSearchCount == 1) {
								currentDate.setText(searchDateText);
							}
							frmSearchCount++;

						} catch (Exception exe) {
							Log.e("Exception :", "Exception occures search date parsing", exe);
						}
						finalDealList.clear();
						Log.v("Dinning style is ", String.valueOf(diningStyle));
						searchVo.setDiningId(diningId);
						searchVo.setDate(selectedDate);
						setTimeRange(searchVo, diningStyle);
						String dateText = currentDate.getText().toString();
						if (dateText.equalsIgnoreCase("Today")) {
							Calendar cal = Calendar.getInstance();
							int hour = cal.getTime().getHours();
							searchVo.setMinTime(hour);
						}
						/**
						 * Call the asynctask to get the deals list.
						 */

						new DealsListAsyncTask().execute(searchVo);
					}

				} else {
					prevPos = datePreferences.getInt("slidingPos", 0);
					if (!ValidationUtil.isNullOrEmpty(application
							.getDealsList()) && prevPos == pos) {
						itemsList = getListView();
						itemsList.setCacheColorHint(Color.TRANSPARENT);
						emptyList.setVisibility(View.GONE);
						country = application.getCountry();
						locationInfo.setText(application.getLocation());
						/*
						 * if (selectedposition != 3) {
						 */
						NearMeListAdapter nearMeListAdapter = new NearMeListAdapter(
								NearMeActivity.this,
								application.getDealsList(), country, status,
								NearMeActivity.this);
						setListAdapter(nearMeListAdapter);
						/*
						 * } else {
						 * setUpScaleOrFineAdapter(application.getDealsList());
						 * 
						 * }
						 */
					} else {

						/**
						 * Retrieve all the deals
						 */
						SharedPreferences.Editor edits11 = datePreferences
								.edit();
						edits11.putInt("slidingPos", pos);
						edits11.commit();
						edits11 = null;
						application.getDealsList().clear();
						prevPos = pos;
						getDealList(1, diningId, diningStyle);
						fromPage = null;
						pageCount = 1;
					}
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

		final LinearLayout wheelLayout = (LinearLayout) findViewById(R.id.wheelLayout);
		Button doneBtn = (Button) findViewById(R.id.doneBtn);

		final WheelView wheelView = (WheelView) findViewById(R.id.radiusWheelView);

		CustomWheelAdapter adapter = new CustomWheelAdapter(this, dinningStyles);
		adapter.setTextSize(17);
		wheelView.setViewAdapter(adapter);

		wheelView.setVisibleItems(3);

		/**
		 * scroll change listener
		 */

		wheelView.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					populateRadius(wheel, dinningStyles, newValue);
				}
			}
		});

		/**
		 * scroll listener
		 */
		wheelView.addScrollingListener(new OnWheelScrollListener() {
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
			}

			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;

			}
		});

		wheelView.setCurrentItem(mNewIndex);
		/**
		 * distance onclick listener
		 */
		dinningType.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				diningStyle = datePreferences.getInt("diningStyle", 0);

				if (diningStyle == 0)
					dinningStyleType = dinningStyles[0];
				if (diningStyle == 1)
					dinningStyleType = dinningStyles[1];
				if (diningStyle == 2)
					dinningStyleType = dinningStyles[2];
				if (diningStyle == 3)
					dinningStyleType = dinningStyles[3];
				if (diningStyle == 4)
					dinningStyleType = dinningStyles[4];
				if (diningStyle == 5)
					dinningStyleType = dinningStyles[5];

				for (int i = 0; i < dinningStyles.length; i++) {
					if (dinningStyles[i].equalsIgnoreCase(dinningStyleType)) {
						valueToSet = i;
						break;
					}
				}

				wheelView.setCurrentItem(valueToSet);
				wheelLayout.setVisibility(View.VISIBLE);
				pendingOffersStVisible = statusPendingOffers.getVisibility();
				statusPendingOffers.setVisibility(View.GONE);
				statusFed.setVisibility(View.GONE);

			}

		});
		/*
		 * near me button onClick listener
		 */
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				wheelLayout.setVisibility(View.GONE);
				itemsList.setEnabled(true);

				if (!ValidationUtil.isNullOrEmpty(displayoffers)
						&& uncheckedOffers.size() > 0
						&& pendingOffersStVisible == View.VISIBLE) {
					statusFed.setVisibility(View.GONE);
					statusPendingOffers.setVisibility(View.VISIBLE);
				} else {
					statusFed.setVisibility(View.VISIBLE);
					statusPendingOffers.setVisibility(View.GONE);
				}
			}
		});

		/**
		 * scroll done button listener
		 */
		doneBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mNewIndex = wheelView.getCurrentItem();
				dinningStyleType = dinningStyles[mNewIndex];

				application.getDealsList().clear();

				Log.v("Select dining index is  ", String.valueOf(mNewIndex));

				diningStyle = mNewIndex;
				SharedPreferences datePreferences = getSharedPreferences(
						"NearMeDate", 0);
				SharedPreferences.Editor dateEdit = datePreferences.edit();
				dateEdit.putInt("diningStyle", mNewIndex);
				dateEdit.commit();

				itemsList.setEnabled(true);

				if (fromPage != null && fromPage.equalsIgnoreCase("searchPage")) {

					Bundle bun = getIntent().getExtras();
					searchVo = (SearchVo) bun.getSerializable("searchVo");
					searchVo.setDiningId(diningId);
					dinningType.setText(dinningStyles[mNewIndex]);
					String dateText = currentDate.getText().toString();
					setTimeRange(searchVo, mNewIndex);
					if (dateText.equalsIgnoreCase("Today")) {
						Calendar cal = Calendar.getInstance();
						int hour = cal.getTime().getHours();
						searchVo.setMinTime(hour);
					}
					/**
					 * Call the asynctask to get the deals list.
					 */
					new DealsListAsyncTask().execute(searchVo);

				} else {

					getDealList(1, diningId, mNewIndex);
					pageCount = 1;
				}
				wheelLayout.setVisibility(View.GONE);
				if (!ValidationUtil.isNullOrEmpty(displayoffers)
						&& displayoffers.size() > 0
						&& pendingOffersStVisible == View.VISIBLE) {
					statusFed.setVisibility(View.GONE);
					statusPendingOffers.setVisibility(View.VISIBLE);
				} else {
					statusFed.setVisibility(View.VISIBLE);
					statusPendingOffers.setVisibility(View.GONE);
				}

			}

		});

		/*
		 * Status bar implementation for pending offers
		 */
		statusPendingOffers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				statusFed.setVisibility(View.VISIBLE);
				statusPendingOffers.setVisibility(View.GONE);
				if (displayoffers.size() > 1) {

					Intent myoffersIntent = new Intent(NearMeActivity.this,
							MyOffersActivity.class);
					myoffersIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					storeSliderPosition();
					startActivity(myoffersIntent);
				} else if (displayoffers.size() == 1) {
					OffersDetailsVo offersDetailsVo = displayoffers.get(0);
					ArrayList<OffersDetailsVo> newOffersList = new ArrayList<OffersDetailsVo>();
					for (OffersDetailsVo OffersVo : displayoffers) {
						if (!offersDetailsVo.getDealId().equals(
								OffersVo.getDealId())
								&& OffersVo.getBusinessName().equalsIgnoreCase(
										offersDetailsVo.getBusinessName()))
							newOffersList.add(OffersVo);
					}
					Bundle bundle = new Bundle();
					Intent calimedIntent = new Intent(NearMeActivity.this,
							MyoffersDetailActivity.class);
					calimedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					bundle.putParcelable("selectOffers", offersDetailsVo);
					bundle.putParcelableArrayList("OffersList", newOffersList);
					calimedIntent.putExtras(bundle);
					storeSliderPosition();
					startActivity(calimedIntent);
				}
			}
		});

		topMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * Starting main menu activity
				 */
				Intent mainMenuIntent = new Intent(NearMeActivity.this,
						MainMenuActivity.class);
				mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				storeSliderPosition();
				startActivity(mainMenuIntent);
			}
		});

		/*
		 * me button onclick listener.
		 */
		meButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent meIntent = new Intent(NearMeActivity.this,
						MeActivity.class);
				meIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				storeSliderPosition();
				startActivity(meIntent);
			}
		});

		/**
		 * Search button onclick listener
		 */
		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent searchIntent = new Intent(NearMeActivity.this, SearchActivity.class);
				searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				storeSliderPosition();
				startActivity(searchIntent);
			}
		});

		application = (TangoTab) getApplication();

		/**
		 * Get all offers form web service and set the app notification
		 */
		if (ValidationUtil.isNullOrEmpty(fromPage)) {
			offersList = application.getOffersList();
			if (!ValidationUtil.isNullOrEmpty(offersList)) {
				/**
				 * Send app notifications for Expired offers
				 */
				sendAppNotification(offersList);

			} else {
				getOfferList();
			}
		}

		if (AppConstant.dev_lat == 0.0 && AppConstant.dev_lang == 0.0) {
			emptyList.setVisibility(View.GONE);
		}

		/**
		 * Auto scroll down offers
		 */
		itemsList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				int lastInScreen = firstVisibleItem + visibleItemCount;

				// is the bottom item visible? Load more !
				if ((lastInScreen == totalItemCount) && totalItemCount != 1
						&& totalItemCount != totalItemCount + 10
						&& totalItemCount == count) {
					diningStyle = datePreferences.getInt("diningStyle", 0);
					pageCount++;
					isAutoScrolled = true;
					if (fromPage != null
							&& fromPage.equalsIgnoreCase("searchPage")) {
						Bundle bun = getIntent().getExtras();
						searchVo = (SearchVo) bun.getSerializable("searchVo");

						Log.v("Dinning style is ", String.valueOf(diningStyle));
						searchVo.setDiningId(diningId);
						searchVo.setDate(selectedDate);
						searchVo.setPageIndex(String.valueOf(pageCount));
						setTimeRange(searchVo, diningStyle);
						String dateText = currentDate.getText().toString();
						if (dateText.equalsIgnoreCase("Today")) {
							Calendar cal = Calendar.getInstance();
							int hour = cal.getTime().getHours();
							searchVo.setMinTime(hour);
						}
						count += 10;
						new DealsListAsyncTask().execute(searchVo);
					} else {

						getDealList(pageCount, diningId, diningStyle);
						count += 10;

					}
				}

			}
		});

		/**
		 * set onClickListeners for Buttons(Nearby, Expiring, Recent and Map).
		 */

		map.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {

				Vibrator myVib = (Vibrator) getApplicationContext()
						.getSystemService(Context.VIBRATOR_SERVICE);
				myVib.vibrate(50);
				if (checkInternetConnection()) {
					/*
					 * new Thread(new Runnable() {
					 * 
					 * @Override public void run() { runOnUiThread(new
					 * Runnable() {
					 * 
					 * @Override public void run() {
					 *//**
					 * Start map activity
					 */
					storeSliderPosition();
					Intent mapIntent = new Intent(getApplicationContext(),
							MappingActivity.class);
					startActivity(mapIntent);
					/*
					 * } }); } }).start();
					 */
				} else
					showDialog(10);
			}

		});

		/*
		 * application = (TangoTab) getApplication(); List<OffersDetailsVo>
		 * offerList = application.getDisplayoffers(); AutoCheckinActivity
		 * activity = new AutoCheckinActivity(this, getApplicationContext(),
		 * offerList); activity.doCheckIn();
		 */
		new MyPhilanthropyInformation().execute();

	}

	/**
	 * Get list of offers for notifications.
	 * 
	 */
	private void getOfferList() {
		SharedPreferences spc = getSharedPreferences("UserName", 0);
		String userId = spc.getString("username", "");
		String password = spc.getString("password", "");
		loginvo = new LoginVo();
		loginvo.setUserId(userId);
		loginvo.setPassword(password);
		/**
		 * Get all the offers from asynctask call
		 */
		new MyOffersListAsyncTask().execute(loginvo);

	}

	/**
	 * Retrieve all the deals from number of page count
	 * 
	 * @param pageCount
	 */
	private void getDealList(int pageCount, String diningId, int diningStyle) {
		if (pageCount == 1) {
			count = 10;
		}
		if (!isAutoScrolled) {
			application.getDealsList().clear();
			finalDealList.clear();
		}
		SharedPreferences spc1 = getSharedPreferences("Distance", 0);
		String distance_set = spc1.getString("distancevalue", "");
		if (ValidationUtil.isNullOrEmpty(distance_set)) {
			distance_set = "20";
		}

		SharedPreferences spc3 = getSharedPreferences("UserDetails", 0);
		userId = spc3.getString("UserId", "");

		if (locationInfo.getText().toString()
				.equalsIgnoreCase("Current Location")) {
			dev_lat = AppConstant.dev_lat;
			dev_lang = AppConstant.dev_lang;
		} else {
			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			LocationVo location = GeoCoderUtil.getLatLongFromAddress(zipCode);
			if (location != null) {
				AppConstant.dev_lat = location.getLat();
				AppConstant.dev_lang = location.getLang();
			}

		}

		dev_lat = AppConstant.selectedLat == 0.0 ? AppConstant.dev_lat
				: AppConstant.selectedLat;
		dev_lang = AppConstant.selectedLong == 0.0 ? AppConstant.dev_lang
				: AppConstant.selectedLong;

		/**
		 * Set all the data in near me vo object
		 */
		dinningType.setText(dinningStyles[diningStyle]);
		SearchVo searchVo = new SearchVo();
		searchVo.setLocLat(String.valueOf(dev_lat));
		searchVo.setLocLaong(String.valueOf(dev_lang));
		searchVo.setDistance(distance_set);
		searchVo.setUserId(userId);
		searchVo.setPageIndex(String.valueOf(pageCount));
		searchVo.setDiningId(diningId);
		searchVo.setDate(selectedDate);
		application.setNearMePageCount(pageCount);
		setTimeRange(searchVo, diningStyle);
		Log.v("searchVo is ", searchVo.toString());
		new DealsListAsyncTask().execute(searchVo);

	}

	/**
	 * AsyncTask call to retrieve all the offers from service using different
	 * thread
	 * 
	 * @author Dillip.Lenka
	 * 
	 */
	public class MyOffersListAsyncTask extends AsyncTask<LoginVo, Void, List<OffersDetailsVo>> {
		@Override
		protected void onPreExecute() {
			dinningType.setEnabled(false);

		}

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
				offersList = service.getOffers(pageCount, loginVo[0]);
				for (OffersDetailsVo offersDetailsVo : offersList) {
					String reserveTime = offersDetailsVo.getReserveTimeStamp();
					int index = reserveTime.indexOf(" ");
					String reserve = reserveTime.substring(0, index).trim();
					String current = offersDetailsVo.getCurrentDate();
					index = current.indexOf(" ");
					String currentTime = current.substring(0, index).trim();
					if (reserve.compareToIgnoreCase(currentTime) == 0)
						displayoffers.add(offersDetailsVo);
				}
				application.setOffersList(offersList);
				application.setDisplayoffers(displayoffers);

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
			dinningType.setEnabled(true);
			if (!ValidationUtil.isNullOrEmpty(offersList)) {
				/**
				 * Send app notifications for Expired offers
				 */
				sendAppNotification(offersList);

			}

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		/**
		 * Save scroll position in shared preferences.
		 */

		SharedPreferences spreferences = getSharedPreferences(
				AppConstant.SELECTED_PREFS, 0);
		SharedPreferences.Editor editor = spreferences.edit();
		int selectedPosition = ga.getSelectedItemPosition();
		editor.putInt(AppConstant.KEY_SELECTED_ITEM,
				ga.getSelectedItemPosition());
		editor.commit();
		// cancels the timer for app notification for this activity
		LocationManagerToggle.getInstance().cancelTimer(t);
		SharedPreferences preferences = getSharedPreferences("NEARSCROLL", 0);
		SharedPreferences.Editor edit = preferences.edit();
		if (itemsList != null) {
			int scroll = itemsList.getFirstVisiblePosition();
			edit.putInt("scrollValue", scroll);
			View v = itemsList.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			edit.putInt("Top", top);
			edit.commit();
		}
		LocationManagerToggle.getInstance().removeCurentLocationUpdate();

	}

	@Override
	protected void onResume() {

		super.onResume();

		SharedPreferences preferences = getSharedPreferences("NEARSCROLL", 0);

		SharedPreferences spreferences = getSharedPreferences(
				AppConstant.SELECTED_PREFS, 0);
		final int scroll = preferences.getInt("scrollValue", 0);
		selectedposition = spreferences
				.getInt(AppConstant.KEY_SELECTED_ITEM, 0);

		ga.setAdapter(new NearMeGalleryAdapter(NearMeActivity.this, pics));
		ga.setSelection(selectedposition, true);

		View v = (View) ga.getItemAtPosition(ga.getSelectedItemPosition());
		ga.setSelected(true);
		ga.invalidate();
		int top = preferences.getInt("Top", 0);
		if (itemsList != null) {
			getListView().postDelayed(new Runnable() {

				@Override
				public void run() {
					SharedPreferences preferences = getSharedPreferences(
							"NEARSCROLL", 0);
					int top = preferences.getInt("Top", 0);
					itemsList.setSelection(preferences.getInt("scrollValue",
							top));

				}
			}, 100L);

		}
		// timer for app notification
		t = LocationManagerToggle.getInstance().setTimer(null, 0, 5000);

		// starting GPS Updates
		LocationManagerToggle.getInstance().initalizeLocationManagerService(
				this, this);
		/*
		 * if(locationManager==null) initalizeLocationManagerService();
		 */
		/**
		 * do auto checkin
		 */
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,
				getApplicationContext(), offerList);
		activity.doCheckIn();

	};

	/**
	 * Convert point to geo point and get the address details.
	 * 
	 * @param point
	 */
	public void ConvertToPoint(GeoPoint point) {
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			List<Address> addresseses = geoCoder.getFromLocation(
					point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6,
					1);
			dev_lat = addresseses.get(0).getLatitude();
			dev_lang = addresseses.get(0).getLongitude();
			AppConstant.dev_lat = dev_lat;
			AppConstant.dev_lang = dev_lang;

			Log.v("dev_lat and dev_lang ", "dev_lang = " + dev_lang
					+ "dev_lang = " + dev_lang);

		} catch (IOException e) {
			Log.e("Exception:",
					"Exception occuerd at the time of getting address from point",
					e);
			e.printStackTrace();
		}

	}

	/**
	 * Send app notifications for list of Expired offers.
	 * 
	 * @param offersList
	 */
	private void sendAppNotification(List<OffersDetailsVo> offersList) {
		if (ValidationUtil.isNullOrEmpty(offersList))
			return;
		uncheckedOffers = new ArrayList<OffersDetailsVo>();

		Log.v("Invoking is ", "consumerdealsList = " + offersList.size());
		for (OffersDetailsVo offersDetailsVo : offersList) {
			int isCheckin = 0;
			if (!ValidationUtil.isNullOrEmpty(offersDetailsVo
					.getIsConsumerShownUp())) {
				isCheckin = Integer.parseInt(offersDetailsVo
						.getIsConsumerShownUp());
			}
			Log.v("isCheckin = ", String.valueOf(isCheckin));
			/**
			 * If the offer neither manual or auto check in
			 */
			if (isCheckin == 0) {
				String reserveDate = offersDetailsVo.getReserveTimeStamp();
				StringBuilder finalClaimDate = new StringBuilder();
				String claimDate = null;
				if (!ValidationUtil.isNullOrEmpty(reserveDate)) {
					int index = reserveDate.indexOf(" ");
					claimDate = offersDetailsVo.getEndTime();
					Log.v("ClaimDate is ", claimDate);
					finalClaimDate
							.append(reserveDate.substring(0, index).trim())
							.append(" ").append(claimDate);
					Log.v("finalClaimDate is ", finalClaimDate.toString());
				}

				Date finalEndTime = DateFormatUtil.parseIntoDifferentFormat(
						finalClaimDate.toString(), "yyyy-MM-dd hh:mm aa");

				if (claimDate.equalsIgnoreCase("12:00 AM")) {
					Calendar c = Calendar.getInstance();
					c.setTime(finalEndTime);
					c.add(Calendar.DATE, 1);
					finalEndTime = c.getTime();
				}

				if (finalEndTime != null) {
					// Check whether the offer expired or not.
					String currentDate = offersDetailsVo.getCurrentDate();
					Date current = DateFormatUtil.parseIntoDifferentFormat(
							currentDate, "yyyy-MM-dd HH:mm:ss.SSSSSS");
					boolean isExpiredOffer = (current.after(finalEndTime)) ? true : false;

					Log.v("isExpiredOffer = ", String.valueOf(isExpiredOffer));
					finalEndTime.setTime(finalEndTime.getTime() + 3600000);
					current = new Date();
					if (isExpiredOffer || current.after(finalEndTime)) {
						Bundle bundle = new Bundle();
						bundle.putParcelable("selectOffers", offersDetailsVo);
						Intent appNotification = new Intent(getApplicationContext(), AppNotificationActivity.class);
						appNotification.putExtras(bundle);
						startActivity(appNotification);
						/**
						 * Remove the local notification
						 */
						int dealId = 0;
						if (!ValidationUtil.isNullOrEmpty(offersDetailsVo
								.getDealId()))
							dealId = Integer.parseInt(offersDetailsVo
									.getDealId());

						Log.v("dealId for remove local Notification ",
								String.valueOf(dealId));

						Intent alarmIntent = new Intent(
								AppConstant.ALARM_ACTION_NAME);
						PendingIntent pendingIntent = PendingIntent
								.getBroadcast(getApplicationContext(), dealId,
										alarmIntent,
										PendingIntent.FLAG_UPDATE_CURRENT);
						AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
						alarmManager.cancel(pendingIntent);
						pendingIntent.cancel();

					}
				}
			} else {
				uncheckedOffers.add(offersDetailsVo);
				Log.v("Don't send local notification already checked in",
						String.valueOf(isCheckin));
			}
		}

		if (!ValidationUtil
				.isNullOrEmpty(getIntent().getStringExtra("frmPage"))
				&& getIntent().getStringExtra("frmPage").equals("splashScreen")) {
			if (!ValidationUtil.isNullOrEmpty(displayoffers)
					&& displayoffers.size() > 0) {
				statusFed.setVisibility(View.GONE);
				statusPendingOffers.setVisibility(View.VISIBLE);
				SharedPreferences sharedUserDetails = getSharedPreferences(
						"UserDetails", 0);

				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append(sharedUserDetails.getString("firstName", "")
						.substring(0, 1).toUpperCase());
				strBuilder.append(
						sharedUserDetails.getString("firstName", "").substring(
								1)).append(", ");
				strBuilder.append("open your pending offer here");

				Animation pushleft = AnimationUtils.loadAnimation(this,
						R.anim.push_left_in);
				pushleft.setDuration(1000);
				statusPendingOffersLabel.startAnimation(pushleft);
				statusPendingOffersLabel.setText(strBuilder.toString());
			}
		} else {
			statusFed.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * This method will check the Internet connection for the application.
	 * 
	 * @return
	 */
	private boolean checkInternetConnection() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable() && conMgr
				.getActiveNetworkInfo().isConnected()) ? true : false;

	}

	/**
	 * Dialog message display.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			AlertDialog.Builder ab = new AlertDialog.Builder(
					NearMeActivity.this);
			ab.setTitle("TangoTab");
			ab.setMessage("No offers are found to display on map");
			ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return ab.create();
		case 16:
			AlertDialog.Builder ab2 = new AlertDialog.Builder(
					NearMeActivity.this);
			ab2.setTitle("Location Services Denied");
			ab2.setMessage("Your device is configured to deny location services to TangoTab. Some features of Tangotab require these services to be enabled . Please enable location services for TangoTab app in the device settings  to enable all app features.");
			ab2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return ab2.create();

		case 17:
			AlertDialog.Builder ab3 = new AlertDialog.Builder(
					NearMeActivity.this);
			ab3.setTitle("Location Services Denied");
			ab3.setMessage("Your account is inactive, Please contact TangoTab Administrator.");
			ab3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			return ab3.create();
		case 10:
			AlertDialog.Builder ab10 = new AlertDialog.Builder(
					NearMeActivity.this);
			ab10.setTitle("TangoTab");
			ab10.setMessage("We are unable to make an internet connection at this time.Some functionalities will be limited until a connection is made.");
			ab10.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return ab10.create();
		case 11:
			AlertDialog.Builder ab11 = new AlertDialog.Builder(
					NearMeActivity.this);
			ab11.setTitle("TangoTab");
			ab11.setMessage("Could not find any offers. Please adjust search criteria.");
			ab11.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent searchIntent = new Intent(NearMeActivity.this,
							SearchActivity.class);
					searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					SharedPreferences spc1 = getSharedPreferences(
							"locationDetail", 0);
					SharedPreferences.Editor edits11 = spc1.edit();
					edits11.putString("currentLocation", "Current Location");
					edits11.commit();
					AppConstant.SEARCH_BY = "Current Location";
					AppConstant.selectedLat = dev_lat;
					AppConstant.selectedLong = dev_lang;

					startActivity(searchIntent);
					dialog.dismiss();
				}
			});
			return ab11.create();
			/*
			 * case 100: AlertDialog.Builder a = new
			 * AlertDialog.Builder(NearMeActivity.this); a.setTitle("TangoTab");
			 * a.setMessage(
			 * "Your device is configured to deny location services to TangoTab. Some features of Tangotab require these services to be enabled . Please enable location services for TangoTab app in the device settings  to enable all app features."
			 * ); a.setPositiveButton("OK", new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
			 * }); return a.create();
			 */
		}
		return null;
	}

	/*
	 * ==========================================================================
	 * ======================================== Geo Informaation of the Device
	 * ==
	 * ========================================================================
	 * ========================================
	 */
	/**
	 * Initalizes the Location Manager and attaches the LocationUpdates Listener
	 * for the device current Lat Lng values
	 */
	/*
	 * private void initalizeLocationManagerService() {
	 * 
	 * GPS Configuration
	 * 
	 * 
	 * locationManager = (LocationManager)
	 * getSystemService(Context.LOCATION_SERVICE);
	 * 
	 * Criteria criteria = new Criteria(); provider =
	 * locationManager.getBestProvider(criteria, false); Location location =
	 * locationManager.getLastKnownLocation(provider);
	 * locationManager.requestLocationUpdates(provider,5000L, 0.0f, this);
	 * 
	 * // Initialize the location fields if (!ValidationUtil.isNull(location)) {
	 * Log.i("GEO LOCATION", "Provider " + provider + " has been selected.");
	 * double lat = location.getLatitude(); double lng =
	 * location.getLongitude(); Log.i("GEO LOCATION DETAILS", "LAT and Long " +
	 * lat + "  " + lng); // Push the Lat Lng values into Global Execution
	 * Context AppConstant.dev_lat = lat; AppConstant.dev_lang = lng;
	 * SharedPreferences currentLocation = getSharedPreferences(
	 * "LocationDetails", 0); SharedPreferences.Editor edit =
	 * currentLocation.edit(); edit.putString("locLat", String.valueOf(lat));
	 * edit.putString("locLong", String.valueOf(lng)); edit.commit(); } else {
	 * Log.i("GEO LOCATION", "Provider is not available"); SharedPreferences
	 * currentLocation = getSharedPreferences( "LocationDetails", 0);
	 * SharedPreferences.Editor edit = currentLocation.edit();
	 * edit.putString("locLat", "0.0"); edit.putString("locLong", "0.0");
	 * edit.commit(); AppConstant.dev_lat = 0.0; AppConstant.dev_lang = 0.0; } }
	 */

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	class MyPhilanthropyInformation extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			MyPhilanthropyService myPhilanthropyService = new MyPhilanthropyService();
			Map<String, String> response = new HashMap<String, String>();
			try {
				SharedPreferences spc1 = getSharedPreferences("UserDetails", 0);
				String userId = spc1.getString("UserId", "");
				response = myPhilanthropyService.getMyPhilanthropy(userId);
				SharedPreferences.Editor edits = spc1.edit();
				edits.putString("mePhil", response.get("me"));
				edits.putString("friendsPhil", response.get("friends"));
				edits.putString("tangotabPhil", response.get("tangotab"));
				edits.putString("networkPhil", response.get("potential"));
				edits.commit();

			} catch (TangoTabException e) {
				Log.e("Exception ",
						"Exception occured at SahreMyPhilanthropy async task",
						e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			SharedPreferences sharedUserDetails = getSharedPreferences(
					"UserDetails", 0);

			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(sharedUserDetails.getString("firstName", "")
					.substring(0, 1).toUpperCase());
			strBuilder.append(
					sharedUserDetails.getString("firstName", "").substring(1))
					.append(", ");
			strBuilder.append("you've fed ");
			strBuilder.append(sharedUserDetails.getString("mePhil", "0"));
			strBuilder.append(" people in need");
			statusFedLabel.setText(strBuilder.toString());
		}

	}

	/**
	 * Get list of Deals from the search criteria.
	 * 
	 * @author dillip.lenka
	 * 
	 */
	public class DealsListAsyncTask extends
			AsyncTask<SearchVo, Void, List<DealsDetailVo>> {
		private ProgressDialog mDialog = null;

		@Override
		protected void onPreExecute() {
			mDialog = ProgressDialog.show(NearMeActivity.this, "Searching ",
					"Please wait...");
			mDialog.setCancelable(true);
		}

		@Override
		protected List<DealsDetailVo> doInBackground(SearchVo... searchVo) {
			List<DealsDetailVo> dealsList = null;
			try {
				SearchService service = new SearchService();
				dealsList = service.getSearchList(searchVo[0]);
			} catch (ConnectTimeoutException e) {
				dealsList = null;
				Log.e("", "", e);
				Intent contactIntent = new Intent(getApplicationContext(),
						ContactSupportActvity.class);
				startActivity(contactIntent);
			} catch (Exception e) {
				dealsList = null;
				Log.e("", "", e);
			}

			return dealsList;
		}

		@Override
		protected void onPostExecute(List<DealsDetailVo> dealsList) {
			try {
				mDialog.dismiss();
				EnableGPSIfPossible();
			} catch (Exception e) {
				Log.e("EXception:", "Exception occured before dismiss dilog.");
			}

			if (!ValidationUtil.isNullOrEmpty(dealsList)) {

				// finalDealList.clear();
				application.setCountry(country);
				application.setLocation(locationInfo.getText().toString());
				List<DealsDetailVo> nearMeList = application.getDealsList();
				if (!ValidationUtil.isNullOrEmpty(nearMeList)
						&& finalDealList.size() == 0) {
					if (!ValidationUtil.isNullOrEmpty(dealsList)
							&& isAutoScrolled)
						nearMeList.addAll(dealsList);
					finalDealList.addAll(nearMeList);
				} else {
					finalDealList.addAll(dealsList);
				}
				emptyList.setVisibility(View.GONE);
				itemsList.setVisibility(View.VISIBLE);
				application.setOffersList(offersList);
				Log.v("NearListAsyncTask size of the finalDealList is",
						String.valueOf(finalDealList.size()));
				application.setDealsList(finalDealList);
				AppConstant.dealsList = finalDealList;

				/**
				 * Display list of deals by using adapter.
				 */
				/*
				 * if (selectedposition != 3) {
				 */
				status = NearMeActivity.this.getIntent().getBooleanExtra(
						"status", false);
				NearMeListAdapter nearMeListAdapter = new NearMeListAdapter(
						NearMeActivity.this, finalDealList, country, status,
						NearMeActivity.this);
				setListAdapter(nearMeListAdapter);
				/*
				 * } else { setUpScaleOrFineAdapter(finalDealList); }
				 */
				/**
				 * Cursor to be point in next 10 records.
				 */
				if (!ValidationUtil.isNullOrEmpty(finalDealList)
						&& finalDealList.size() > 10) {
					getListView().postDelayed(new Runnable() {

						@Override
						public void run() {
							itemsList.setSelection((pageCount - 1) * 10);
						}
					}, 100L);

				}
				itemsList = (ListView) getListView();
				itemsList.setCacheColorHint(Color.TRANSPARENT);

			} else {
				if (fromPage != null && fromPage.equalsIgnoreCase("searchPage")
						&& selectedposition == 1) {
					showDialog(11);

				} else {
					List<DealsDetailVo> olddealsList = application
							.getDealsList();
					if (ValidationUtil.isNullOrEmpty(olddealsList)) {
						emptyList.setVisibility(View.VISIBLE);
						itemsList.setVisibility(View.GONE);
					} else {
						/*
						 * if (selectedposition != 3) {
						 */status = NearMeActivity.this.getIntent()
								.getBooleanExtra("status", false);
						NearMeListAdapter nearMeListAdapter = new NearMeListAdapter(
								NearMeActivity.this, olddealsList, null,
								status, NearMeActivity.this);
						setListAdapter(nearMeListAdapter);
						/*
						 * } else { setUpScaleOrFineAdapter(olddealsList); }
						 */
					}
					if (AppConstant.dev_lat == 0.0
							&& AppConstant.dev_lang == 0.0) {
						emptyList.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	/**
	 * Set time range according to dining Id
	 * 
	 * @param nearMeVo
	 * @param position
	 */
	private void setTimeRange(SearchVo searchVo, int position) {

		// String startDate= searchVo.getDate().split(" ")[0]+searchVo.;

		/*
		 * SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); String
		 * currentDateandTime = sdf.format(new Date());
		 */
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");// dd/MM/yyyy
		String formattedTime = sdfDate.format(cal.getTime());
		/* 2013-11-15 */
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		Date convertedDate = new Date();
		// String newFormat = formatter.format(searchVo.getDate());
		try {
			convertedDate = formatter.parse(searchVo.getDate());
			// formattedDate = targetFormat.format(convertedDate);
		} catch (Exception e) {
			//
			e.printStackTrace();
		}
		if (convertedDate.after(currentDate)) {
			switch (position) {
			case 0:
				searchVo.setMinTime(0);
				searchVo.setMaxTime(24);
				AppConstant.SEARCH_MAX_TIME_RANGE = "0";
				break;
			case 1:
				searchVo.setMinTime(04);
				searchVo.setMaxTime(11);
				AppConstant.SEARCH_MAX_TIME_RANGE = "0";
				break;
			case 2:
				searchVo.setMinTime(11);
				searchVo.setMaxTime(13);
				AppConstant.SEARCH_MAX_TIME_RANGE = "0";
				break;
			case 3:
				searchVo.setMinTime(13);
				searchVo.setMaxTime(17);
				AppConstant.SEARCH_MAX_TIME_RANGE = "0";
				break;
			case 4:
				searchVo.setMinTime(17);
				searchVo.setMaxTime(21);
				AppConstant.SEARCH_MAX_TIME_RANGE = "0";
				break;
			case 5:
				searchVo.setMinTime(21);
				searchVo.setMaxTime(04);
				AppConstant.SEARCH_MAX_TIME_RANGE = "0";
				break;
			default:
				break;
			}

		} else {
			switch (position) {
			case 0:
				searchVo.setMinTime(0);
				searchVo.setMaxTime(24);
				AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
				break;
			case 1:
				searchVo.setMinTime(04);
				searchVo.setMaxTime(11);
				AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
				break;
			case 2:
				searchVo.setMinTime(11);
				searchVo.setMaxTime(13);
				AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
				break;
			case 3:
				searchVo.setMinTime(13);
				searchVo.setMaxTime(17);
				AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
				break;
			case 4:
				searchVo.setMinTime(17);
				searchVo.setMaxTime(21);
				AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
				break;
			case 5:
				searchVo.setMinTime(21);
				searchVo.setMaxTime(04);
				AppConstant.SEARCH_MAX_TIME_RANGE = formattedTime;
				break;
			default:
				break;
			}
		}

	}

	public class MyGestureDetector extends SimpleOnGestureListener {

		// Detect a single-click and call my own handler.
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			ListView lv = getListView();
			int pos = lv.pointToPosition((int) e.getX(), (int) e.getY());
			myOnItemClick(pos);
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return true;
		}
	}

	private void myOnItemClick(int position) {
		try {
			ArrayList<DealsDetailVo> businessList = new ArrayList<DealsDetailVo>();
			DealsDetailVo dealsDetailVo = application.getDealsList().get(
					position);
			// businessList.addAll(application.getDealsList());
			for (DealsDetailVo dealsVo : application.getDealsList()) {
				if (dealsDetailVo.businessName.equals(dealsVo.businessName)) {
					businessList.add(dealsVo);
				}
			}
			
			AppConstant.IS_CLAIM_OFFER_PAGE = true;
			AppConstant.SELECTED_POSITION = selectedposition;
			application.setSelectDeal(dealsDetailVo);
			application.setCountry(country);
			Intent calimedIntent = new Intent(NearMeActivity.this, ClaimOfferActivity.class);
			calimedIntent.putExtra("from", "nearme");
			calimedIntent.putExtra("selectDeal", dealsDetailVo);
			calimedIntent.putExtra("businessList", businessList);
			calimedIntent.putExtra("country", country);
			calimedIntent.putExtra("selectedPosition", selectedposition);
			startActivity(calimedIntent);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_CANCELED) {
			if (requestCode == AppConstant.GET_DATE_REQUEST_CODE) {
				StringBuilder date = new StringBuilder();
				int year = Integer.parseInt(data.getStringExtra("year"));
				int month = Integer.parseInt(data.getStringExtra("month"));
				int day = Integer.parseInt(data.getStringExtra("day"));
				date.append(year).append("-").append(month).append("-")
						.append(day);
				selectedDate = date.toString();

				String selectText = getTextFromDate(year, month, day);

				SharedPreferences datePreferences = getSharedPreferences(
						"NearMeDate", 0);
				SharedPreferences.Editor dateEdit = datePreferences.edit();
				dateEdit.putString("selectText", selectText);
				dateEdit.putInt("year", year);
				dateEdit.putInt("month", month - 1);
				dateEdit.putInt("day", day);
				dateEdit.putString("selectedDate", selectedDate);
				dateEdit.commit();

				SharedPreferences dateSelect = getSharedPreferences(
						"SearchDate", 0);
				SharedPreferences.Editor selectDate = dateSelect.edit();
				selectDate.putString("dateSelected", selectedDate);
				selectDate.commit();

				currentDate.setText(selectText);

				getDealList(1, diningId, diningStyle);
				pageCount = 1;
			}
		}
	}

	private String getTextFromDate(int year, int month, int day) {
		String result = null;
		Log.v("selectedDate is", selectedDate);

		Calendar selectCal = Calendar.getInstance();
		selectCal.set(Calendar.YEAR, year);
		selectCal.set(Calendar.MONTH, month - 1);
		selectCal.set(Calendar.DAY_OF_MONTH, day);
		selectCal.set(Calendar.HOUR_OF_DAY, 0);
		selectCal.set(Calendar.MINUTE, 0);
		selectCal.set(Calendar.SECOND, 0);
		selectCal.set(Calendar.MILLISECOND, 0);
		Date selectDate = selectCal.getTime();

		Calendar today = Calendar.getInstance();
		today.setTime(today.getTime());
		Date current = today.getTime();

		today.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrowDate = today.getTime();

		String dateSelect = new SimpleDateFormat("MMM d yyyy ")
				.format(selectDate);
		String todaysDate = new SimpleDateFormat("MMM d yyyy ").format(current);
		String tommorwsDate = new SimpleDateFormat("MMM d yyyy ")
				.format(tomorrowDate);

		if (dateSelect.equalsIgnoreCase(todaysDate))
			result = "Today";
		else if (dateSelect.equalsIgnoreCase(tommorwsDate))
			result = "Tomorrow";
		else {
			result = dateSelect;
		}
		return result;
	}

	private void storeSliderPosition() {
		SharedPreferences datePreferences = getSharedPreferences("NearMeDate",
				0);
		Editor edit = datePreferences.edit();
		edit.putInt("slidingPos", selectedposition - 1);
		int value = selectedposition - 1;

		diningStyle = datePreferences.getInt("diningStyle", 0);

		edit.commit();
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			Intent mainMenuIntent = new Intent(NearMeActivity.this,
					MainMenuActivity.class);
			mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainMenuIntent);
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			Intent searchIntent = new Intent(NearMeActivity.this,
					SearchActivity.class);
			searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(searchIntent);
			return true;
		}

		return super.onKeyDown(keycode, e);
	}

	/**
	 * Populate the radius wheel
	 */
	private void populateRadius(WheelView radiusWheel, String[] disvalues,
			int index) {
		CustomWheelAdapter adapter = new CustomWheelAdapter(this, disvalues);
		adapter.setTextSize(17);
		radiusWheel.setViewAdapter(adapter);
		radiusWheel.setCurrentItem(index);
	}

	private boolean EnableGPSIfPossible() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
			return true;
		}
		return false;
	}

	private void buildAlertMessageNoGps() {
		if (!gpsPreferences.getBoolean(AppConstant.ENABLE_GPS_KEY, false)) {
			gpsEditor.putBoolean(AppConstant.ENABLE_GPS_KEY, true);
			gpsEditor.commit();
			showDialog(16);
		}
	}

}
