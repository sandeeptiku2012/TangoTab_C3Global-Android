package com.tangotab.search.activity;

import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tangotab.LocationManagerToggle;
import com.c3global.R;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.mainmenu.activity.MainMenuActivity;
import com.tangotab.myOfferDetails.activity.AutoCheckinActivity;
import com.tangotab.myOffers.Vo.OffersDetailsVo;
import com.tangotab.search.adapter.CustomWheelAdapter;

/**
 * 
 * @author Lakshmipathi.P
 * 
 */
public class LocationActivity extends Activity {

	// Scrolling flag
	private boolean scrolling = false;
	private int mNewIndex;
	final String locations[] = new String[] { "Current Location", "HomeZip","WorkZip", "alternateZipCode" };
	private String locations1[]= new String[] { "Current Location", "Home",	"Work", "Other" };
	private String alternateZipfromSearch;
	private EditText alternateZip;
	public TangoTab application;
	private Timer t;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);

		application = (TangoTab) getApplication();

		SharedPreferences spc1 = getSharedPreferences("UserDetails", 0);
		String homeZipCode = spc1.getString("postal", "");
		String workZipCode = spc1.getString("workZip", "");
		String alternateZipCode = spc1.getString("alternateZip", "");
		Button doneButton = (Button) findViewById(R.id.doneButton);

		if(!homeZipCode.equalsIgnoreCase("") && homeZipCode != null){
			locations1[1] = "Home ("+ValidationUtil.capitalizeFully(homeZipCode,null)+")";
		}
		if(!workZipCode.equalsIgnoreCase("") && workZipCode != null){
			locations1[2] = "Work ("+ValidationUtil.capitalizeFully(workZipCode,null)+")";
		}
		if(!alternateZipCode.equalsIgnoreCase("") && alternateZipCode != null){
			locations1[3] = "Other ("+ValidationUtil.capitalizeFully(alternateZipCode,null)+")";
		}

		final WheelView locWheelView = (WheelView) findViewById(R.id.locWheelView);
		final TextView homeZipText = (TextView) findViewById(R.id.homeZipTextView);
		final EditText homeZip = (EditText) findViewById(R.id.homeZip);
		final EditText workZip = (EditText) findViewById(R.id.workZip);
		alternateZip = (EditText) findViewById(R.id.alternateZip);
		// String alternateZipSearch=alternateZip.getText().toString();

		SharedPreferences spf = getSharedPreferences("alternateZipCode", 0);
		alternateZipCode = spf.getString("alternateZipCode", "");

		SharedPreferences aspf = getSharedPreferences("Alternate", 0);
		alternateZipfromSearch = aspf.getString("alternateZipfromSearch", "");

		homeZipText.setVisibility(View.GONE);
		homeZip.setVisibility(View.GONE);


		CustomWheelAdapter adapter = new CustomWheelAdapter(this, locations);
		adapter.setTextSize(17);
		locWheelView.setViewAdapter(adapter);

		/*locWheelView.setViewAdapter(new LocationAdapter(this));
		locWheelView.setVisibleItems(3);*/
		homeZip.setText(ValidationUtil.capitalizeFully(spc1.getString("postal", ""),null));
		workZip.setText(ValidationUtil.capitalizeFully(spc1.getString("workZip", ""),null));
		/*
		 * Visibility of location Field.
		 */
		int RollValue =0;
		SharedPreferences spc2 = getSharedPreferences("locationDetail", 0);
		String selectedLocation=spc2.getString("currentLocation", "Current Location");
		if(selectedLocation.equalsIgnoreCase("Current Location"))
		{
			RollValue=0;
		}
		else if(selectedLocation.equalsIgnoreCase("HomeZip"))
		{
			RollValue=1;
		}
		else if(selectedLocation.equalsIgnoreCase("WorkZip"))
		{
			RollValue=2;
		}
		else if(selectedLocation.equalsIgnoreCase("alternateZipCode"))
		{
			RollValue=3;
		}

		if(!ValidationUtil.isNullOrEmpty(spc2.getString("wheelIndex", "")))
			RollValue = Integer.parseInt(spc2.getString("wheelIndex", ""));

		if (RollValue == 3) {
			homeZipText.setVisibility(View.VISIBLE);
			homeZipText.setText("Other Zip / Postal");
			alternateZip.setVisibility(View.VISIBLE);
			workZip.setVisibility(View.GONE);
			homeZip.setVisibility(View.GONE);
		} else if (RollValue == 0) {
			homeZipText.setVisibility(View.GONE);
			homeZipText.setText("Alterate Location");
			alternateZip.setVisibility(View.GONE);
			workZip.setVisibility(View.GONE);
			homeZip.setVisibility(View.GONE);
		} else if (RollValue == 1) {
			homeZipText.setVisibility(View.GONE);
			homeZipText.setText("Alterate Location");
			alternateZip.setVisibility(View.GONE);
			workZip.setVisibility(View.GONE);
			homeZip.setVisibility(View.GONE);
		} else if (RollValue == 2) {
			homeZipText.setVisibility(View.GONE);
			homeZipText.setText("Alterate Location");
			alternateZip.setVisibility(View.GONE);
			workZip.setVisibility(View.GONE);
			homeZip.setVisibility(View.GONE);
		}

		if (!ValidationUtil.isNullOrEmpty(alternateZipCode)
				&& ValidationUtil.isNullOrEmpty(alternateZipfromSearch)) {
			alternateZip.setText(ValidationUtil.capitalizeFully(alternateZipCode,null));
		} else {
			alternateZip.setText(ValidationUtil.capitalizeFully(alternateZipfromSearch,null));
		}
		String wheelIndex = spc1.getString("wheelIndex", "");
		if (!ValidationUtil.isNullOrEmpty(wheelIndex))
			locWheelView.setCurrentItem(Integer.parseInt(wheelIndex));
		else
			locWheelView.setCurrentItem(0);

		String selLoc = getIntent().getStringExtra("selectedLoc");
		for (int count = 0; count < locations.length; count++) {
			if (selLoc.equals(locations[count])) {
				mNewIndex = count;
				break;
			}
		}

		/**
		 * wheel view onchange listener.
		 */
		locWheelView.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					populateRadius(wheel, locations1, newValue);
				}
			}
		});

		/**
		 * wheel view scroll listener
		 */
		locWheelView.addScrollingListener(new OnWheelScrollListener() {
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
			}

			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;
				populateRadius(wheel, locations1, wheel.getCurrentItem());
				mNewIndex = wheel.getCurrentItem();
				if (mNewIndex == 3) {

					homeZipText.setVisibility(View.VISIBLE);
					homeZipText.setText("Other Zip/Postal");
					alternateZip.setVisibility(View.VISIBLE);
					workZip.setVisibility(View.GONE);
					homeZip.setVisibility(View.GONE);
				} else if (mNewIndex == 2) {
					homeZipText.setVisibility(View.GONE);
					homeZipText.setText("Work Zip/Postal");
					workZip.setVisibility(View.GONE);
					alternateZip.setVisibility(View.GONE);
					homeZip.setVisibility(View.GONE);
				} else if (mNewIndex == 1) {
					homeZipText.setVisibility(View.GONE);
					homeZipText.setText("Home Zip/Postal");
					homeZip.setVisibility(View.GONE);
					alternateZip.setVisibility(View.GONE);
					workZip.setVisibility(View.GONE);
				} else if (mNewIndex == 0) {
					homeZipText.setVisibility(View.GONE);
					alternateZip.setVisibility(View.GONE);
					workZip.setVisibility(View.GONE);
					homeZip.setVisibility(View.GONE);
				}
			}
		});

		locWheelView.setCurrentItem(mNewIndex);

		doneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String homeZipCode = homeZip.getText().toString();
				String workZipCode = workZip.getText().toString();
				alternateZipfromSearch = alternateZip.getText().toString();
				SharedPreferences spc1 = getSharedPreferences("locationDetail",	0);
				SharedPreferences.Editor edits = spc1.edit();
				edits.putString("workZip", workZipCode);
				edits.putString("homeZip", homeZipCode);
				Toast.makeText(LocationActivity.this, locations[mNewIndex] + " --&&--" + workZipCode, Toast.LENGTH_SHORT).show();
				edits.putString("currentLocation", locations[mNewIndex]);
				edits.putString("wheelIndex", String.valueOf(mNewIndex));
				edits.putString("alternateZip", alternateZipfromSearch);
				edits.commit();

				SharedPreferences spf=getSharedPreferences("alternateZipCode", 0);
				SharedPreferences.Editor edits1 = spf.edit();
				edits1.putString("alternateZipCode", alternateZipfromSearch);
				edits1.commit();



				/**
				 * put all the user information into the shared preferences
				 */
				SharedPreferences spc2 = getSharedPreferences("UserDetails", 0);
				SharedPreferences.Editor edits2 = spc2.edit();
				edits2.putString("postal", homeZipCode);
				edits2.putString("workZip", workZipCode);
				edits2.putString("alternateZip", alternateZipfromSearch);
				edits2.commit();

				if (mNewIndex == 3) {
					Pattern pattern = Pattern
							.compile("(^\\d{5}(-\\d{4})?$)|(^[ABCEGHJKLMNPRSTVXYabceghjklmnprstvxy]{1}\\d{1}[A-Za-z]{1} *\\d{1}[A-Za-z]{1}\\d{1}$)");

					Matcher alternateMatcher = pattern
							.matcher(alternateZipfromSearch);
					if (ValidationUtil.isNullOrEmpty(alternateZipfromSearch)) {
						showDialog(1);
					} else if (!ValidationUtil
							.isNullOrEmpty(alternateZipfromSearch)) {
						if (!alternateMatcher.matches()) {
							showDialog(1);

						} else {
							SharedPreferences aspf = getSharedPreferences(
									"Alternate", 0);
							SharedPreferences.Editor aspfeditor = aspf.edit();
							aspfeditor.putString("alternateZipfromSearch", 
									alternateZipfromSearch);
							aspfeditor.commit();
							Intent searchIntent = new Intent(
									LocationActivity.this, SearchActivity.class);
							startActivity(searchIntent);
						}


					} 
				} else {
					Intent searchIntent = new Intent(LocationActivity.this,
							SearchActivity.class);
					startActivity(searchIntent);
				}
			}
		});

	}

	/**
	 * Populate the radius wheel
	 */
	private void populateRadius(WheelView radiusWheel, String[] locations, int index) {
		CustomWheelAdapter adapter = new CustomWheelAdapter(this, locations);
		adapter.setTextSize(17);
		radiusWheel.setViewAdapter(adapter);
		radiusWheel.setCurrentItem(index);
	}

	/*
	 * location Adapter for distance location in search page.
	 * 
	 * @author Lakshmipathi.P
	 */
	private class LocationAdapter extends AbstractWheelTextAdapter {
		/**
		 * Constructor
		 */
		protected LocationAdapter(Context context) {
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
			return locations.length;
		}

		@Override
		protected CharSequence getItemText(int index) {
			return locations1[index];
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			AlertDialog.Builder homeZipErrDialog = new AlertDialog.Builder(
					LocationActivity.this);
			homeZipErrDialog.setTitle("TangoTab");
			homeZipErrDialog.setMessage("Invalid Zip code.");
			homeZipErrDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return homeZipErrDialog.create();

		case 2:
			AlertDialog.Builder workZipErrDialog = new AlertDialog.Builder(
					LocationActivity.this);
			workZipErrDialog.setTitle("TangoTab");
			workZipErrDialog.setMessage("Invalid Work Zip code.");
			workZipErrDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return workZipErrDialog.create();
		case 3:
			AlertDialog.Builder homeZipEmptyDialog = new AlertDialog.Builder(
					LocationActivity.this);
			homeZipEmptyDialog.setTitle("TangoTab");
			homeZipEmptyDialog.setMessage("Please Specify Home Zip code.");
			homeZipEmptyDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return homeZipEmptyDialog.create();
		case 5:
			AlertDialog.Builder alternateZipDialog = new AlertDialog.Builder(
					LocationActivity.this);
			alternateZipDialog.setTitle("TangoTab");
			alternateZipDialog.setMessage("Please Enter a Valid Zip");
			alternateZipDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return alternateZipDialog.create();

		case 4:
			AlertDialog.Builder workZipEmptyDialog = new AlertDialog.Builder(
					LocationActivity.this);
			workZipEmptyDialog.setTitle("TangoTab");
			workZipEmptyDialog.setMessage("Please Specify Work Zip code.");
			workZipEmptyDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

				}
			});
			return workZipEmptyDialog.create();
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		switch (keycode) {
		case KeyEvent.KEYCODE_MENU:
			Intent mainMenuIntent = new Intent(LocationActivity.this,
					MainMenuActivity.class);
			mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainMenuIntent);
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			Intent searchIntent = new Intent(LocationActivity.this,
					SearchActivity.class);
			searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(searchIntent);
			return true;
		}

		return super.onKeyDown(keycode, e);
	}

	@Override
	protected void onResume()
	{
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
	protected void onPause() {
		LocationManagerToggle.getInstance().cancelTimer(t);
		LocationManagerToggle.getInstance().removeCurentLocationUpdate();
		super.onPause();
	}
}
