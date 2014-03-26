package com.tangotab.search.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.c3global.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.tangotab.LocationManagerToggle;
import com.tangotab.calendar.activity.CalendarActivity;
import com.tangotab.calendar.utils.CalendarView;
import com.tangotab.claimOffer.activity.ClaimOfferActivity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.core.view.RangeSeekBar;
import com.tangotab.core.view.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.tangotab.core.view.RangeSeekBar.RangeBarValues;
import com.tangotab.mainmenu.activity.MainMenuActivity;
import com.tangotab.map.activity.MappingActivity;
import com.tangotab.me.activity.MeActivity;
import com.tangotab.myOfferDetails.activity.AutoCheckinActivity;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.nearMe.activity.NearMeActivity;
import com.tangotab.nearMe.vo.DealsDetailVo;
import com.tangotab.search.adapter.CustomWheelAdapter;
import com.tangotab.search.vo.SearchVo;

/**
 * 
 * Search activity class will be used for search the restaurant.
 * 
 * <br> Class :SearchActivity <br>
 * Layout :search.xml
 * 
 * @author Dillip.Lenka
 * 
 */
@SuppressLint("SimpleDateFormat")
public class SearchActivity extends Activity {
	/**
	 * Meta definitions
	 */
	private EditText editAddress;
	ListView itemsList = null;
	Boolean status = false;

	private String disvalues[] = { "1 Mile", "3 Miles", "5 Miles", "10 Miles",
			"20 Miles", "50 Miles", "50+ Miles" };
	private String disvaluesKm[] = { "1 Km", "3 Km", "5 Km", "10 Km", "20 Km",
			"50 Km", "50+ Km" };

	public TangoTab application;
	private int mNewIndex;
	// Scrolling flag
	private boolean scrolling = false;
	private String sel_distance = null;
	private Button distance, location, calendar;
	private int valueToSet;
	public String userName, postalZip;
	public static final int LOCATION_REQUEST_CODE = 19;
	public static final int CALENDAR_REQUEST_CODE = 1;
	private int maxTime = 24;
	private int minTime = 0;
	private String country;
	private GoogleAnalyticsTracker tracker;
	private String homeZipCode;
	private String workZipCode;
	private String alternateZipCode;
	private String locationText;
	String address;
	private String selectedDate1;
	private Timer t;

	/**
	 * Execution will start here.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search);

		/**
		 * Google analytics implementation added here.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(AppConstant.GA_REG_KEY, 10, this);
		tracker.setDebug(true);
		tracker.trackPageView(AppConstant.SEARCH_PAGE);
		tracker.trackEvent("Search", "TrackEvent", "search", 1);

		application = (TangoTab) getApplication();
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this, getApplicationContext(), offerList);
		activity.doCheckIn();

		itemsList = (ListView) findViewById(android.R.id.list);
		editAddress = (EditText) findViewById(R.id.searchaddress);
		calendar = (Button) findViewById(R.id.date);
		editAddress.setOnEditorActionListener(mEditorActionListener);

		application = (TangoTab) getApplication();

		Button topMenu = (Button) findViewById(R.id.topMenuButton);
		Button meButton = (Button) findViewById(R.id.topMeMenuButton);
		Button nearMe = (Button) findViewById(R.id.topNearmeMenuMenuButton);
		final Button searchOffers = (Button) findViewById(R.id.searchUpdateButton);
		searchOffers.setEnabled(true);
		distance = (Button) findViewById(R.id.distance);
		location = (Button) findViewById(R.id.Downtown);

		Button mapButton = (Button) findViewById(R.id.map);
		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);

		SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", 0);
		homeZipCode = sharedPreferences.getString("postal", "");
		workZipCode = sharedPreferences.getString("workZip", "");
		alternateZipCode = sharedPreferences.getString("alternateZip", "");

		SharedPreferences restSearch = getSharedPreferences("restSearch", 0);
		if (!ValidationUtil.isNullOrEmpty(restSearch.getString("restSearch", "")))
			editAddress.setText(restSearch.getString("restSearch", ""));

		country = sharedPreferences.getString("country", "");
		System.out.println("Country  in search Activity" + country);
		if (!ValidationUtil.isNullOrEmpty(country) && country.equalsIgnoreCase("canada")) {
			disvalues = disvaluesKm;
		}

		SharedPreferences date = getSharedPreferences("SearchDate", 0);
		String selectedDate = date.getString("dateSelected", "");
		if (!ValidationUtil.isNullOrEmpty(selectedDate) && !selectedDate.equals("Select Date")) {

			Date searchDate;
			try {
				SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy");
				searchDate = format1.parse(selectedDate);
				selectedDate = format2.format(searchDate);
			} catch (ParseException e) {

				Log.e("Exception occured in date parsing ",
						e.getLocalizedMessage());
			}
			calendar.setText(selectedDate);
		} else {
			Calendar today = Calendar.getInstance();
			SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
			selectedDate = sdfDate.format(today.getTime());
			calendar.setText(selectedDate);
		}

		SharedPreferences spc1 = getSharedPreferences("locationDetail", 0);
		locationText = spc1.getString("currentLocation", "Current Location");
		setLocationText();
		/**
		 * Get customURlHandler information.
		 * 
		 */
		Bundle bundel = getIntent().getExtras();
		if (bundel != null) {
			String fromPage = (String) bundel.getString("fromPage");
			if (!ValidationUtil.isNullOrEmpty(fromPage)
					&& fromPage.equals("customURL")) {
				String address = (String) bundel.getString("address");
				if (!ValidationUtil.isNullOrEmpty(address)) {

					editAddress.setText(address);

					SearchVo searchVo = new SearchVo();
					getSearch(searchVo);
					SharedPreferences range = getSharedPreferences("RangeBar",
							0);
					int max = range.getInt("maxTime", 0);
					int min = range.getInt("minTime", 0);
					searchVo.setMinTime(min);
					if (max != 0)
						searchVo.setMaxTime(max);
					else
						searchVo.setMaxTime(24);
					/*
					 * Starting main menu activity
					 */

					Intent nearmeIntent = new Intent(SearchActivity.this,
							NearMeActivity.class);
					nearmeIntent.putExtra("searchVo", searchVo);
					nearmeIntent.putExtra("fromPage", "searchPage");
					nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(nearmeIntent);
				} else {
					DealsDetailVo dealsDetailVo = (DealsDetailVo) bundel.getSerializable("selectDeal");
					String dealId = (String) bundel.getString("dealId");
					if (!ValidationUtil.isNull(dealsDetailVo)
							&& !ValidationUtil.isNullOrEmpty(dealId)) {
						/**
						 * Start the claim offer activity with deal id and deal
						 * date.
						 */
						Intent claimIntent = new Intent(
								getApplicationContext(),
								ClaimOfferActivity.class);
						claimIntent.putExtra("from", "search");
						claimIntent.putExtra("fromPage", "customURL");
						claimIntent.putExtra("dealId", dealId);
						claimIntent.putExtra("selectDeal", dealsDetailVo);

						claimIntent.putExtra("fromsearch", "fromsearch");
						startActivity(claimIntent);
					}
				}

			}
		}

		/*
		 * Search offers button onclick listner.
		 */
		searchOffers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchOffers.setEnabled(false);
				searchOffers();
			}
		});
		/*
		 * custom range seekbar
		 */

		RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(minTime,
				maxTime, this);
		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
					Integer minValue, Integer maxValue) {
				// getSearch(1);
			}
		});
		
		SharedPreferences range = getSharedPreferences("RangeBar", 0);
		int max = range.getInt("maxTime", 0);
		int min = range.getInt("minTime", 0);
		seekBar.setSelectedMinValue(min);
		if (max != 0)
			seekBar.setSelectedMaxValue(max);
		else
			seekBar.setSelectedMaxValue(24);
		/*
		 * custom range seekbar
		 */
		seekBar.setRangeBarValuesListener(new RangeBarValues() {

			@Override
			public void getMinVal(long minVal, long maxVal) {
				Log.i("HOUR RANGE", "User selected new range values: MIN="
						+ minVal + ", MAX=" + maxVal);
				maxTime = (int) maxVal;
				minTime = (int) minVal;
				if (maxTime == 0)
					maxTime = 24;

				SharedPreferences range = getSharedPreferences("RangeBar", 0);
				SharedPreferences.Editor scrollEdit = range.edit();
				scrollEdit.putInt("minTime", minTime);
				scrollEdit.putInt("maxTime", maxTime);
				scrollEdit.commit();

				/*
				 * if
				 * (!ValidationUtil.isNullOrEmpty(application.getDealsList()))
				 * application.getDealsList().clear();
				 */
			}
		});
		/**
		 * Map button on click listener
		 */

		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {

				Vibrator myVib = (Vibrator) getApplicationContext()
						.getSystemService(Context.VIBRATOR_SERVICE);
				myVib.vibrate(50);

				/*
				 * new Handler().postDelayed(new Runnable() { public void run()
				 * { v.setClickable(true); } }, 600);
				 */
				if (checkInternetConnection()) {
					/**
					 * get list of deals from application.
					 */
					/*
					 * List<DealsDetailVo> mapDealsList = application
					 * .getSearchList(); if
					 * (ValidationUtil.isNullOrEmpty(mapDealsList)) {
					 * SharedPreferences currentLocation = getSharedPreferences(
					 * "LocationDetails", 0); String lat =
					 * currentLocation.getString("locLat", ""); String lang =
					 * currentLocation.getString("locLong", ""); if
					 * (!ValidationUtil.isNullOrEmpty(lat) &&
					 * !ValidationUtil.isNullOrEmpty(lang)) { try {
					 * AppConstant.dev_lat = Double.valueOf(lat);
					 * AppConstant.dev_lang = Double.valueOf(lang);
					 * 
					 * } catch (NumberFormatException e) {
					 * 
					 * Log.e("Exception",
					 * "Exception occuerd when converting String int doubble",
					 * e); } }
					 * 
					 * List<Address> addressList = null;
					 * 
					 * Geocoder geocoder = new Geocoder(getBaseContext(),
					 * Locale.getDefault()); try { addressList =
					 * geocoder.getFromLocation( AppConstant.dev_lat,
					 * AppConstant.dev_lang, 1); } catch (IOException e) {
					 * Log.e("Exception:",
					 * "Exception occuerd at the time getting address list from Geo Coder."
					 * ); e.printStackTrace(); }
					 * 
					 * if (!ValidationUtil.isNullOrEmpty(addressList)) { Intent
					 * myOfferMapIntent = new
					 * Intent(getApplicationContext(),MappingActivity.class);
					 * myOfferMapIntent.putExtra("businessname",
					 * addressList.get(0).getAddressLine(0));
					 * myOfferMapIntent.putExtra("dealname",
					 * addressList.get(0).getAddressLine(0));
					 * myOfferMapIntent.putExtra("IsFromPlaceOrSearch",
					 * "mySettings"); myOfferMapIntent.putExtra("fromPage",
					 * "mySettings"); startActivity(myOfferMapIntent); }
					 * 
					 * } else {
					 */

					new Thread(new Runnable() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									final Context appContext = getApplicationContext();
									/**
									 * Start map activity
									 */
									Intent mapIntent = new Intent(appContext,
											MappingActivity.class);
									startActivity(mapIntent);
								}
							});
						}
					}).start();

					// }
				} else
					showDialog(10);
			}

		});

		// add RangeSeekBar to pre-defined layout
		ViewGroup layout = (ViewGroup) findViewById(R.id.rangeSeekBarLayout);
		layout.addView(seekBar);

		/*
		 * top menu onclick listener
		 */
		topMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * Starting main menu activity
				 */
				Intent mainMenuIntent = new Intent(SearchActivity.this,
						MainMenuActivity.class);
				mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mainMenuIntent);
			}
		});

		/*
		 * me button onClick listener.
		 */
		meButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent meIntent = new Intent(SearchActivity.this,
						MeActivity.class);
				meIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(meIntent);
			}
		});
		/*
		 * calendar button onClick listener
		 */
		calendar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent calendarIntent = new Intent(SearchActivity.this,
						CalendarActivity.class);
				String date = calendar.getText().toString();
				String split[] = date.split("/");
				int day = Integer.parseInt(split[1]);
				int month = Integer.parseInt(split[0]);
				int year = Integer.parseInt(split[2]);
				Log.e("year..", "" + year);
				CalendarView.prevDay = day;
				CalendarView.prevMonth = month - 1;
				CalendarView.prevYear = year;
				/*
				 * calendarIntent.putExtra("prevDay",day);
				 * calendarIntent.putExtra("prevMonth",month);
				 * calendarIntent.putExtra("prevYear",year);
				 */
				startActivityForResult(calendarIntent, CALENDAR_REQUEST_CODE);
				/*
				 * if
				 * (!ValidationUtil.isNullOrEmpty(application.getDealsList()))
				 * application.getDealsList().clear();
				 */

			}
		});

		/*
		 * near me button onClick listener
		 */
		nearMe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (AppConstant.IS_CLAIM_OFFER_PAGE) {
					DealsDetailVo dealsDetailVo = application.getSelectDeal();
					country = application.getCountry();
					ArrayList<DealsDetailVo> businessList = new ArrayList<DealsDetailVo>();
					for (DealsDetailVo dealsVo : application.getDealsList()) {
						if (dealsDetailVo.businessName .equals(dealsVo.businessName)) {
							businessList.add(dealsVo);
						}
					}
					Intent calimedIntent = new Intent(SearchActivity.this, ClaimOfferActivity.class);
					calimedIntent.putExtra("from", "nearme");
					calimedIntent.putExtra("selectDeal", dealsDetailVo);
					calimedIntent.putExtra("businessList", businessList);
					calimedIntent.putExtra("country", country);
					calimedIntent.putExtra("selectedPosition", AppConstant.SELECTED_POSITION);
					startActivity(calimedIntent);
				}
				else {
					Intent nearmeIntent = new Intent(SearchActivity.this, NearMeActivity.class);
					nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(nearmeIntent);
				}
			}

		});

		final LinearLayout wheelLayout = (LinearLayout) findViewById(R.id.wheelLayout);
		final Button doneBtn = (Button) findViewById(R.id.doneBtn);

		final WheelView wheelView = (WheelView) findViewById(R.id.radiusWheelView);
		wheelView.setViewAdapter(new RadiusAdapter(this));
		wheelView.setVisibleItems(3);

		/**
		 * scroll change listener
		 */
		wheelView.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					if (country.equalsIgnoreCase("canada")) {
						populateRadius(wheel, disvaluesKm, newValue);
					} else {
						populateRadius(wheel, disvalues, newValue);
					}
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

				if (country.equalsIgnoreCase("canada")) {
					populateRadius(wheel, disvaluesKm, wheel.getCurrentItem());
				} else {
					populateRadius(wheel, disvalues, wheel.getCurrentItem());
				}
				// populateRadius(wheel, disvalues, wheel.getCurrentItem());
				mNewIndex = wheel.getCurrentItem();
			}
		});
		wheelView.setCurrentItem(1);
		wheelView.setCurrentItem(0);
		wheelView.setCurrentItem(mNewIndex);
		/**
		 * distance onclick listener
		 */
		distance.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] location = null;
				if (country.equalsIgnoreCase("canada"))
					location = disvaluesKm;
				else
					location = disvalues;
				SharedPreferences spc1 = getSharedPreferences("Distance", 0);
				String dist = spc1.getString("distancevalue", "20");

				if (dist == null)
					dist = location[4];
				if (dist.equalsIgnoreCase("1000"))
					dist = location[6];
				if (dist.equalsIgnoreCase("5"))
					dist = location[2];
				if (dist.equalsIgnoreCase("10"))
					dist = location[3];
				if (dist.equalsIgnoreCase("20"))
					dist = location[4];
				if (dist.equalsIgnoreCase("50"))
					dist = location[5];
				if (dist.equalsIgnoreCase("3"))
					dist = location[1];
				if (dist.equalsIgnoreCase("1"))
					dist = location[0];
				if (country.equalsIgnoreCase("canada")) {
					for (int i = 0; i < disvaluesKm.length; i++) {
						if (disvaluesKm[i].equalsIgnoreCase(dist.trim())) {
							valueToSet = i;
							break;
						}
					}
				}

				else {
					for (int i = 0; i < disvalues.length; i++) {
						if (disvalues[i].equalsIgnoreCase(dist.trim())) {
							valueToSet = i;
							break;
						}
					}
				}

				wheelView.setCurrentItem(valueToSet);
				wheelLayout.setVisibility(View.VISIBLE);
				// popupWindow.showAsDropDown(v);

				/*
				 * if
				 * (!ValidationUtil.isNullOrEmpty(application.getDealsList()))
				 * application.getDealsList().clear();
				 */
			}

		});
		/*
		 * near me button onClick listener
		 */
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wheelLayout.setVisibility(View.GONE);
			}
		});

		editAddress.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {

				address = editAddress.getText().toString();

				if (!ValidationUtil.isNullOrEmpty(address)) {
					boolean isZipCode = ValidationUtil
							.validateCanadZip(address)
							|| ValidationUtil.validateUSAZip(address);
					if (isZipCode) {
						country = ValidationUtil.isValidUSZip(address) ? "USA"
								: "canada";

						if (country.equalsIgnoreCase("canada")) {
							distance.setText("View " + disvaluesKm[mNewIndex]);
						} else
							distance.setText("View " + disvalues[mNewIndex]);

						SharedPreferences spc = getSharedPreferences(
								"locationDetail", 0);
						SharedPreferences.Editor edits = spc.edit();
						edits.putString("country", country);
						edits.commit();

					}
				}
			}
		});

		/**
		 * Edit listener for edit Address name edit.
		 */

		editAddress
		.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							editAddress.getWindowToken(), 0);

					/*
					 * if (!ValidationUtil.isNullOrEmpty(application.
					 * getDealsList()))
					 * application.getDealsList().clear();
					 */
					searchOffers();
					return true;
				}

				return false;
			}
		});

		/**
		 * scroll done button listener
		 */
		doneBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				sel_distance = disvalues[mNewIndex];

				distance.setText("View " + sel_distance);

				if (sel_distance.equalsIgnoreCase("50+ Miles")
						|| sel_distance.equalsIgnoreCase("50+ Km")) {
					sel_distance = "1000";
				}
				if (sel_distance.equalsIgnoreCase("50 Miles")
						|| sel_distance.equalsIgnoreCase("50 Km")) {
					sel_distance = "50";
				}
				if (sel_distance.equalsIgnoreCase("20 Miles")
						|| sel_distance.equalsIgnoreCase("20 Km")) {
					sel_distance = "20";
				}
				if (sel_distance.equalsIgnoreCase("10 Miles")
						|| sel_distance.equalsIgnoreCase("10 Km")) {
					sel_distance = "10";
				}
				if (sel_distance.equalsIgnoreCase("5 Miles")
						|| sel_distance.equalsIgnoreCase("5 Km")) {
					sel_distance = "5";
				}
				if (sel_distance.equalsIgnoreCase("3 Miles")
						|| sel_distance.equalsIgnoreCase("3 Km")) {
					sel_distance = "3";
				}
				if (sel_distance.equalsIgnoreCase("1 Mile")
						|| sel_distance.equalsIgnoreCase("1 Km")) {
					sel_distance = "1";
				}

				if (country.equalsIgnoreCase("canada")) {
					distance.setText("View " + disvaluesKm[mNewIndex]);
				} else
					distance.setText("View " + disvalues[mNewIndex]);

				SharedPreferences spc = getSharedPreferences("Distance", 0);
				String distance = spc.getString("distancevalue", "20");

				/**
				 * put the distance information in shared preferences.
				 */
				if (!distance.equals(sel_distance)) {
					SharedPreferences.Editor edit = spc.edit();
					edit.putString("distancevalue", sel_distance);
					edit.commit();

				}
				AppConstant.IS_SETTINGSCHANGED = "99999";
				/**
				 * Clear the dfeals from the application.
				 */
				clearFromApplication();

				if (!ValidationUtil.isNullOrEmpty(application.getDealsList()))
					application.getDealsList().clear();

				wheelLayout.setVisibility(View.GONE);

			}
		});

		MySettings();

		/**
		 * location button onclick listener.
		 */
		location.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent locationIntent = new Intent(SearchActivity.this,
						LocationActivity.class);
				locationIntent.putExtra("selectedLoc", locationText);
				locationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(locationIntent, LOCATION_REQUEST_CODE);
				/*
				 * if
				 * (!ValidationUtil.isNullOrEmpty(application.getDealsList()))
				 * application.getDealsList().clear();
				 */
			}
		});

	}

	/*
	 * && country.equalsIgnoreCase("canada")) { disvalues = disvaluesKm;
	 */
	private void setLocationText() {
		SharedPreferences spc2 = getSharedPreferences("locationDetail", 0);
		SharedPreferences.Editor edits11 = spc2.edit();
		if (locationText.equalsIgnoreCase("Current Location")) {
			edits11.putString("wheelIndex", "0");
			location.setText("Current Location");
		} else if (locationText.equalsIgnoreCase("HomeZip")) {
			if (!homeZipCode.equalsIgnoreCase("")) {
				location.setText("Home ("
						+ ValidationUtil.capitalizeFully(homeZipCode, null)
						+ ")");
				if (ValidationUtil.validateCanadZip(homeZipCode)) {
					edits11.putString("wheelIndex", "1");
					disvalues = disvaluesKm;
					status = true;
				}
			} else {
				location.setText("Home");
			}
		} else if (locationText.equalsIgnoreCase("WorkZip")) {
			if (!workZipCode.equalsIgnoreCase("")) {
				location.setText("Work ("
						+ ValidationUtil.capitalizeFully(workZipCode, null)
						+ ")");
				if (ValidationUtil.validateCanadZip(workZipCode)) {
					edits11.putString("wheelIndex", "2");
					disvalues = disvaluesKm;
					status = true;
				}
			} else {
				location.setText("Work");
			}
		} else if (locationText.equalsIgnoreCase("alternateZipCode")) {
			if (!alternateZipCode.equalsIgnoreCase("")) {
				location.setText("Other ("
						+ ValidationUtil
						.capitalizeFully(alternateZipCode, null) + ")");
				if (ValidationUtil.validateCanadZip(alternateZipCode)) {
					edits11.putString("wheelIndex", "3");
					disvalues = disvaluesKm;
					status = true;
				}
			} else {
				location.setText("Other");
			}
		}
		edits11.commit();
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

	/**
	 * search the deals from page count
	 * 
	 * @param pageCount
	 */
	private void getSearch(SearchVo searchVo) {
		Map<String, String> locationMap = null;
		searchVo.setPageIndex(String.valueOf(0));
		String addressText = editAddress.getText().toString().trim();
		searchVo.setRestName(addressText);
		locationMap = ValidationUtil.getLatLonFromAddress(addressText);
		if (!ValidationUtil.isNullOrEmpty(locationMap)) {
			Log.v("lat and lng", "lat= " + locationMap.get("lat") + " lang "
					+ locationMap.get("lng"));
			searchVo.setLocLaong(locationMap.get("lng"));
			searchVo.setLocLat(locationMap.get("lat"));
			searchVo.setAddress(addressText);
		}

		SharedPreferences spc1 = getSharedPreferences("UserDetails", 0);
		homeZipCode = spc1.getString("postal", "");
		workZipCode = spc1.getString("workZip", "");
		alternateZipCode = spc1.getString("alternateZip", "");

		if (locationText.toString().equalsIgnoreCase("Current Location")) {
			SharedPreferences currentLocation = getSharedPreferences(
					"LocationDetails", 0);
			String lat = currentLocation.getString("locLat", "");
			String lang = currentLocation.getString("locLong", "");
			searchVo.setLocLat(lat);
			searchVo.setLocLaong(lang);

			Geocoder geocoder = new Geocoder(getBaseContext(),
					Locale.getDefault());
			{
				try {
					double currentLat = Double.valueOf(lat);
					double currentLang = Double.valueOf(lang);
					List<Address> addresses = geocoder.getFromLocation(
							currentLat, currentLang, 5);
					if (addresses != null) {
						Address returnedZip = addresses.get(0);
						String zipCode = returnedZip.getPostalCode();
						boolean isCanadaZip = ValidationUtil
								.validateCanadZip(zipCode);
						country = (isCanadaZip) ? "Canada" : "USA";
						String address = returnedZip.getAddressLine(0);
						searchVo.setAddress(address);
						// searchVo.setZipCode(zipCode);
					} else
						country = "USA";
				} catch (Exception e) {
					Log.e("Exception:",
							"Exception occured to find the currentLocation");
					country = "USA";
				}
			}
		} else if (locationText.toString().equalsIgnoreCase("HomeZip")) {
			boolean isCanadaZip = ValidationUtil.validateCanadZip(homeZipCode);
			country = (isCanadaZip) ? "Canada" : "USA";

			locationMap = ValidationUtil.getLatLonFromAddress(homeZipCode);
			if (!ValidationUtil.isNullOrEmpty(locationMap)) {
				Log.v("lat and lng", "lat= " + locationMap.get("lat")
						+ " lang " + locationMap.get("lng"));
				searchVo.setLocLaong(locationMap.get("lng"));
				searchVo.setLocLat(locationMap.get("lat"));
				searchVo.setAddress(homeZipCode);
			}

		} else if (locationText.toString().equalsIgnoreCase("WorkZip")) {
			boolean isCanadaZip = ValidationUtil.validateCanadZip(workZipCode);
			country = (isCanadaZip) ? "Canada" : "USA";
			locationMap = ValidationUtil.getLatLonFromAddress(workZipCode);
			if (!ValidationUtil.isNullOrEmpty(locationMap)) {
				Log.v("lat and lng", "lat= " + locationMap.get("lat")
						+ " lang " + locationMap.get("lng"));
				searchVo.setLocLaong(locationMap.get("lng"));
				searchVo.setLocLat(locationMap.get("lat"));
				searchVo.setAddress(workZipCode);
			}

		} else if (locationText.toString().equalsIgnoreCase("alternateZipCode")) {
			boolean isCanadaZip = ValidationUtil
					.validateCanadZip(alternateZipCode);
			country = (isCanadaZip) ? "Canada" : "USA";

			locationMap = ValidationUtil.getLatLonFromAddress(alternateZipCode);
			if (!ValidationUtil.isNullOrEmpty(locationMap)) {
				Log.v("lat and lng", "lat= " + locationMap.get("lat")
						+ " lang " + locationMap.get("lng"));
				searchVo.setLocLaong(locationMap.get("lng"));
				searchVo.setLocLat(locationMap.get("lat"));
				searchVo.setAddress(workZipCode);
			}

		}
		SharedPreferences spc = getSharedPreferences("Distance", 0);
		String distance_set = spc.getString("distancevalue", "20");
		if (ValidationUtil.isNullOrEmpty(distance_set)) {
			distance_set = "20";
		}

		searchVo.setDistance(distance_set);
		SharedPreferences spc3 = getSharedPreferences("UserDetails", 0);
		String userId = spc3.getString("UserId", "");
		Log.v("userId in search page", userId);
		searchVo.setUserId(userId);
		searchVo.setMaxTime(maxTime);
		searchVo.setMinTime(minTime);
		if (calendar.getText() != null) {
			String selectedDate = calendar.getText().toString();
			try {
				String[] spit = selectedDate.split("/");
				if (spit != null && spit[0] != null) {
					StringBuilder selectdate = new StringBuilder();
					selectdate.append(spit[2]).append("-").append(spit[0])
					.append("-").append(spit[1]);
					searchVo.setDate(selectdate.toString());
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e("Exception:",
						"Exceptions occured as seleceted date is null", e);
			}
		}
		application.setSearchVo(searchVo);

	}

	/**
	 * Radius Adapter for distance dropdown in search page.
	 * 
	 * @author dillip.lenka
	 * 
	 */
	private class RadiusAdapter extends AbstractWheelTextAdapter {

		/**
		 * Constructor
		 */
		protected RadiusAdapter(Context context) {
			super(context, R.layout.distancetext, NO_RESOURCE);

			setItemTextResource(R.id.radiusText);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);

			return view;
		}

		@Override
		public int getItemsCount() {
			return disvalues.length;
		}

		@Override
		protected CharSequence getItemText(int index) {
			return disvalues[index];
		}
	}

	/**
	 * On Pause implementation added.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		LocationManagerToggle.getInstance().cancelTimer(t);
		LocationManagerToggle.getInstance().removeCurentLocationUpdate();
		// Save scroll position
		SharedPreferences preferences = getSharedPreferences("SCROLL", 0);
		SharedPreferences.Editor edit = preferences.edit();
		if (itemsList != null) {
			int scroll = itemsList.getFirstVisiblePosition();
			edit.putInt("ScrollValue", scroll);
			View v = itemsList.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			edit.putInt("Top", top);
			edit.commit();
		}
	}

	/**
	 * On resume implementation added.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		t = LocationManagerToggle.getInstance().setTimer(null, 0, 5000);
		LocationManagerToggle.getInstance().initalizeLocationManagerService(
				this, this);
		SharedPreferences preferences = getSharedPreferences("SCROLL", 0);
		int scroll = preferences.getInt("ScrollValue", 0);
		int top = preferences.getInt("Top", 0);
		if (itemsList != null)
			itemsList.setSelectionFromTop(scroll, top);

		/**
		 * do auto checkin
		 */
		List<OffersDetailsVo> offerList = application.getOffersList();
		AutoCheckinActivity activity = new AutoCheckinActivity(this,
				getApplicationContext(), offerList);
		activity.doCheckIn();
	};

	/**
	 * Back button functionality added.
	 */

	@Override
	public void onBackPressed() {
		SearchActivity.this.finish();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * clear the information from the application object.
	 * 
	 */
	private void clearFromApplication() {
		if (!ValidationUtil.isNullOrEmpty(application.getSearchList()))
			application.getSearchList().clear();
		if (!ValidationUtil.isNullOrEmpty(application.getOffersList()))
			application.getOffersList().clear();
		/*
		 * if (!ValidationUtil.isNullOrEmpty(application.getDealsList()))
		 * application.getDealsList().clear();
		 */
		/**
		 * Put the scroll to top of the list.
		 */

		SharedPreferences preferences = getSharedPreferences("NEARSCROLL", 0);
		SharedPreferences.Editor ScrollEdit = preferences.edit();
		ScrollEdit.putInt("scrollValue", 0);
		ScrollEdit.putInt("Top", 0);
		ScrollEdit.commit();

		/**
		 * Put the scroll to top of the list.
		 */
		SharedPreferences searchPreferences = getSharedPreferences("SCROLL", 0);
		SharedPreferences.Editor searchEdit = searchPreferences.edit();
		searchEdit.putInt("ScrollValue", 0);
		searchEdit.putInt("Top", 0);
		searchEdit.commit();
	}

	/**
	 * Function to Fetch existing user settings
	 */

	public void MySettings() {

		SharedPreferences spc1 = getSharedPreferences("Distance", 0);
		String dist = spc1.getString("distancevalue", "20");
		if (dist == null)
			dist = disvalues[4];
		if (dist.equalsIgnoreCase("1000"))
			dist = disvalues[6];
		if (dist.equalsIgnoreCase("5"))
			dist = disvalues[2];
		if (dist.equalsIgnoreCase("10"))
			dist = disvalues[3];
		if (dist.equalsIgnoreCase("20"))
			dist = disvalues[4];
		if (dist.equalsIgnoreCase("50"))
			dist = disvalues[5];
		if (dist.equalsIgnoreCase("3"))
			dist = disvalues[1];
		if (dist.equalsIgnoreCase("1"))
			dist = disvalues[0];
		distance.setText("View " + dist);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOCATION_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				location.setText(data.getStringExtra("locationSel"));
				postalZip = data.getStringExtra("Zip_Code");

			}
		}
		if (requestCode == CALENDAR_REQUEST_CODE) {
			if (data != null) {
				String day = data.getStringExtra("day");
				String month = data.getStringExtra("month");
				String year = data.getStringExtra("year");
				StringBuilder dateSelected = new StringBuilder();
				dateSelected.append("").append(month).append("/").append(day)
				.append("/").append(year);
				Log.e("date selected...", dateSelected.toString());
				calendar.setText(dateSelected.toString());
				/**
				 * Put selected date into shared preferences.
				 */
				SharedPreferences date = getSharedPreferences("SearchDate", 0);
				SharedPreferences.Editor ScrollEdit = date.edit();
				ScrollEdit.putString("dateSelected", dateSelected.toString());
				ScrollEdit.commit();

				StringBuilder date1 = new StringBuilder();
				int year1 = Integer.parseInt(data.getStringExtra("year"));
				int month1 = Integer.parseInt(data.getStringExtra("month"));
				int day1 = Integer.parseInt(data.getStringExtra("day"));
				date1.append(year).append("-").append(month).append("-")
				.append(day);
				selectedDate1 = date1.toString();

				String selectText = getTextFromDate(year1, month1, day1);

				SharedPreferences datePreferences = getSharedPreferences(
						"NearMeDate", 0);
				SharedPreferences.Editor dateEdit = datePreferences.edit();
				dateEdit.putString("selectText", selectText);
				dateEdit.putInt("year", year1);
				dateEdit.putInt("month", month1 - 1);
				dateEdit.putInt("day", day1);
				dateEdit.putString("selectedDate", selectedDate1);
				dateEdit.commit();

			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 10:
			AlertDialog.Builder ab10 = new AlertDialog.Builder(
					SearchActivity.this);
			ab10.setTitle("TangoTab");
			ab10.setMessage("We are unable to make an internet connection at this time.Some functionalities will be limited until a connection is made.");

			ab10.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return ab10.create();
		}
		return null;
	}

	/**
	 * This method will check the Internet connection for the application.
	 * 
	 * @return
	 */
	private boolean checkInternetConnection() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected()) ? true : false;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			Intent mainMenuIntent = new Intent(SearchActivity.this,
					MainMenuActivity.class);
			mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainMenuIntent);
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			Intent searchIntent = new Intent(SearchActivity.this,
					SearchActivity.class);
			searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(searchIntent);
			return true;
		}
		return super.onKeyDown(keycode, e);
	}

	private OnEditorActionListener mEditorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editAddress.getWindowToken(), 0);

				SharedPreferences spc11 = getSharedPreferences(
						"locationDetail", 0);
				SharedPreferences.Editor edits11 = spc11.edit();
				edits11.putString("SelectedLocation", locationText);
				edits11.commit();

				address = editAddress.getText().toString();
				SearchVo searchVo = new SearchVo();
				getSearch(searchVo);
				SharedPreferences range = getSharedPreferences("RangeBar", 0);
				int max = range.getInt("maxTime", 0);
				int min = range.getInt("minTime", 0);
				searchVo.setMinTime(min);
				if (max != 0)
					searchVo.setMaxTime(max);
				else
					searchVo.setMaxTime(24);
				/*
				 * Starting main menu activity
				 */

				Intent nearmeIntent = new Intent(SearchActivity.this,
						NearMeActivity.class);
				nearmeIntent.putExtra("searchVo", searchVo);
				nearmeIntent.putExtra("fromPage", "searchPage");
				nearmeIntent.putExtra("status", status);
				nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle bundle = new Bundle();
				bundle.putString("Address", address);
				nearmeIntent.putExtras(bundle);
				startActivity(nearmeIntent);
				return true;
			}
			return false;
		}
	};

	private String getTextFromDate(int year, int month, int day) {
		String result = null;
		Log.v("selectedDate is", selectedDate1);

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

		String dateSelect = new SimpleDateFormat("MMM d yyyy ").format(selectDate);

		if (dateSelect.equalsIgnoreCase(new SimpleDateFormat("MMM d yyyy ").format(current)))
			result = "Today";
		else if (dateSelect.equalsIgnoreCase(new SimpleDateFormat("MMM d yyyy ").format(tomorrowDate)))
			result = "Tomorrow";
		else {
			result = dateSelect;
		}
		return result;
	}

	private String getSeachCriteria() {
		String page = null;
		if (locationText.toString().equalsIgnoreCase("Current Location"))
			page = "Current Location";
		else if (locationText.toString().equalsIgnoreCase("HomeZip"))
			page = "HomeZip";
		else if (locationText.toString().equalsIgnoreCase("WorkZip"))
			page = "WorkZip";
		else if (locationText.toString().equalsIgnoreCase("alternateZipCode"))
			page = "alternateZipCode";
		return page;
	}

	private void searchOffers() {

		AppConstant.IS_CLAIM_OFFER_PAGE = false;
		SharedPreferences spc11 = getSharedPreferences("locationDetail", 0);
		SharedPreferences.Editor edits11 = spc11.edit();
		edits11.putString("SelectedLocation", locationText);
		edits11.commit();

		address = editAddress.getText().toString();
		SearchVo searchVo = new SearchVo();
		getSearch(searchVo);
		SharedPreferences range = getSharedPreferences("RangeBar", 0);
		int max = range.getInt("maxTime", 0);
		int min = range.getInt("minTime", 0);
		searchVo.setMinTime(min);
		if (max != 0)
			searchVo.setMaxTime(max);
		else
			searchVo.setMaxTime(24);
		/*
		 * Starting main menu activity
		 */
		if (!ValidationUtil.isNullOrEmpty(application.getDealsList()))
			application.getDealsList().clear();

		SharedPreferences spreferences = getSharedPreferences(AppConstant.SELECTED_PREFS, 0);
		SharedPreferences.Editor editor = spreferences.edit();
		editor.putInt(AppConstant.KEY_SELECTED_ITEM, 0);
		editor.commit();

		SharedPreferences datePreferences = getSharedPreferences("NearMeDate", 0);
		SharedPreferences.Editor dateEdit = datePreferences.edit();
		dateEdit.putInt("diningStyle", 0);
		dateEdit.commit();

		SharedPreferences restSearch = getSharedPreferences("restSearch", 0);
		SharedPreferences.Editor edits1 = restSearch.edit();
		edits1.putString("restSearch", address);
		edits1.commit();

		/*	if (getSeachCriteria().equalsIgnoreCase("Current Location")) {
			AlertDialog alertDialog = new AlertDialog.Builder(
                    this).create();

		    // Setting Dialog Title
		    alertDialog.setTitle("Alert Dialog");

		    // Setting Dialog Message
		    alertDialog.setMessage("Welcome to AndroidHive.info");

		    // Setting OK Button
		    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            // Write your code here to execute after dialog closed
		            //Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
		            }
		    });

    // Showing Alert Message
    alertDialog.show();
		}*/

		Intent nearmeIntent = new Intent(SearchActivity.this, NearMeActivity.class);
		nearmeIntent.putExtra("searchVo", searchVo);
		nearmeIntent.putExtra("fromPage", "searchPage");
		nearmeIntent.putExtra("SearchBy", getSeachCriteria());
		nearmeIntent.putExtra("status", status);
		nearmeIntent.putExtra("country", country);
		nearmeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Bundle bundle = new Bundle();
		bundle.putString("Address", address);
		nearmeIntent.putExtras(bundle);
		startActivity(nearmeIntent);
	}
}
