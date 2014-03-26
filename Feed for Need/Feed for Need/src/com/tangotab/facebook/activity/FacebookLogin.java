package com.tangotab.facebook.activity;

import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.xmlpull.v1.XmlSerializer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.c3global.R;
import com.facebook.FacebookActivity;
import com.facebook.GraphUser;
import com.facebook.LoginButton;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.tangotab.LocationManagerToggle;
import com.tangotab.claimOffer.activity.DeepLinkInputsVo;
import com.tangotab.claimOffer.activity.DeepLinkOfferActivity;
import com.tangotab.contactSupport.activity.ContactSupportActvity;
import com.tangotab.core.constant.AppConstant;
import com.tangotab.core.session.TangoTab;
import com.tangotab.core.utils.ValidationUtil;
import com.tangotab.customUrl.service.CustonUrlService;
import com.tangotab.customUrl.vo.CustomDealVo;
import com.tangotab.facebook.service.FacebookLoginService;
import com.tangotab.login.activity.LogInActivity;
import com.tangotab.login.activity.SplashScreenActivity;
import com.tangotab.login.vo.LoginVo;
import com.tangotab.nearMe.activity.NearMeActivity;
import com.tangotab.nearMe.vo.DealsDetailVo;
import com.tangotab.search.activity.SearchActivity;
import com.tangotab.signUp.activity.SignUpActivity;

/**
 * Class will be used for Face book login authentication.
 * 
 * Class Name :FacebookLogin XML File :facebook_login
 * 
 * @author lakshmipathi.p
 * 
 */
public class FacebookLogin extends FacebookActivity {
	private Timer t;

	@Override
	protected void onPause() {
		LocationManagerToggle.getInstance().cancelTimer(t);
		LocationManagerToggle.getInstance().removeCurentLocationUpdate();
		super.onPause();
	}

	@Override
	protected void onResume() {
		t = LocationManagerToggle.getInstance().setTimer(null, 0, 5000);
		LocationManagerToggle.getInstance().initalizeLocationManagerService(
				this, this);
		super.onResume();
	}

	LoginButton facebook;
	private Button LoginEmail;
	private Button SignupEmail;

	private String userName = null;
	private String password = null;
	private String postalZip = null;
	private String workZip = null;
	private String mPromoCode = null;
	private String encriptPwd = null;
	private String encriptUserName = null;
	private String facebookId = null;
	private String message = null;
	private String firstName = null;
	private String lastName = null;
	private String userId = null;
	private String dealId = null;
	private GraphUser user;

	Map<String, String> response = new HashMap<String, String>();

	public TangoTab application;

	static final int PICK_ZIPCODE = 1;
	static final int PICK_ZIPCODE_UPDATING = 0;

	private Vibrator myVib;
	private GoogleAnalyticsTracker tracker;
	
	private boolean isFromDeepLink = false;
	private DeepLinkInputsVo mDeepLinkInputsVo;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebooklogin);
		
		boolean finish = getIntent().getBooleanExtra("finish", false);
		if (finish) {
			startActivity(new Intent(getApplicationContext(), SplashScreenActivity.class));
			finish();
			return;
		}
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.getString(AppConstant.DEEPLINK_KEY).equalsIgnoreCase(AppConstant.DEEPLINK_KEY)){
				isFromDeepLink = true;
				mDeepLinkInputsVo = (DeepLinkInputsVo) bundle.getSerializable(AppConstant.OBJECT_KEY);
			}
		}
		
		/**
		 * Google analytics implementation added here.
		 */
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession(AppConstant.GA_REG_KEY, 10, this);
		tracker.setDebug(true);
		tracker.trackPageView(AppConstant.FACEBOOK_LOGIN);
		tracker.trackEvent("FaceBook", "TrackEvent", "facebook", 1);

		/**
		 * UI widgets
		 */
		facebook = (LoginButton) findViewById(R.id.facebook);
		facebook.setTag(true);
		LoginEmail = (Button) findViewById(R.id.login_email);
		SignupEmail = (Button) findViewById(R.id.signup_email);

		application = (TangoTab) getApplication();

		myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		facebook.setBackgroundResource(R.drawable.facebookloginselector);
		facebook.setApplicationId(AppConstant.APP_ID);
		List<String> permissions = new ArrayList<String>();
		permissions.add("email");
		permissions.add("user_birthday");
		permissions.add("user_checkins");
		permissions.add("user_likes");
		permissions.add("user_location");
		permissions.add("offline_access");
		facebook.setReadPermissions(permissions);

		/**
		 * Face book user info changed call back added
		 */
		facebook.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				FacebookLogin.this.user = user;
				if (user != null) {
					new CheckIfUserPresentElseRegisterAsynTask().execute();
				}
			}
		});
		/**
		 * Login using email on click listner added.
		 */
		LoginEmail.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				myVib.vibrate(50);
				Intent login = new Intent(getBaseContext(), LogInActivity.class);
				if(isFromDeepLink){
					login.putExtra(AppConstant.DEEPLINK_KEY, AppConstant.DEEPLINK_KEY);
					login.putExtra(AppConstant.OBJECT_KEY, mDeepLinkInputsVo);
				}
				startActivity(login);
				finish();
			}
		});
		/**
		 * Sign up with email on click listner.
		 */
		SignupEmail.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				myVib.vibrate(50);
				Intent signup = new Intent(getBaseContext(), SignUpActivity.class);
				if(isFromDeepLink){
					signup.putExtra(AppConstant.DEEPLINK_KEY, AppConstant.DEEPLINK_KEY);
					signup.putExtra(AppConstant.OBJECT_KEY, mDeepLinkInputsVo);
				}
				startActivity(signup);
				finish();
			}
		});
	}

	/**
	 * Check whether the user already present else register the new user.
	 * 
	 * @author lakshmipathi.p
	 * 
	 */
	public class CheckIfUserPresentElseRegisterAsynTask extends
	AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		private LoginVo loginVo;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(FacebookLogin.this,
					"Connecting", "Please Wait...");
			progressDialog.setCancelable(true);
			if (user != null) {
				userName = null;
				firstName = null;
				lastName = null;
				if (!ValidationUtil.isNull(user.getProperty("email")))
					userName = user.getProperty("email").toString().trim();
				if (!ValidationUtil.isNull(user.getProperty("first_name")))
					firstName = user.getProperty("first_name").toString()
					.trim();
				if (!ValidationUtil.isNull(user.getProperty("last_name")))
					lastName = user.getProperty("last_name").toString().trim();
				if (!ValidationUtil.isNull(user.getProperty("id")))
					facebookId = user.getProperty("id").toString().trim();
				password = user.getProperty("id").toString().trim();
				// postalZip = user.getProperty("zip").toString();

				if (!ValidationUtil.isNullOrEmpty(password)) {
					encriptPwd = Base64.encodeToString(password.getBytes(),
							Base64.DEFAULT);
					encriptPwd.trim();
					encriptPwd = encriptPwd.substring(0,
							encriptPwd.length() - 1);
				}
				if (!ValidationUtil.isNullOrEmpty(userName)) {
					encriptUserName = generateShaCode(userName);
					encriptUserName.trim();
					encriptUserName = encriptUserName.substring(0,
							encriptUserName.length() - 1);
				}
			}
			// InputMethodManager im = (InputMethodManager)
			// getSystemService(Context.INPUT_METHOD_SERVICE);
			// im.hideSoftInputFromWindow(signIn.getWindowToken(), 0);
			String newUser = userName;
			String newPass = facebookId;
			final TelephonyManager tm = (TelephonyManager) getBaseContext()
					.getSystemService(Context.TELEPHONY_SERVICE);
			System.getProperty("os.version");
			String OSId = android.os.Build.VERSION.RELEASE;
			String networkName = tm.getNetworkOperatorName();
			/*
			 * String PhoneId=tm.getDeviceId(); int phoneName =
			 * tm.getPhoneType(); String operatoName = tm.getSimOperatorName();
			 * String networkId = tm.getNetworkOperator(); String version =
			 * tm.getDeviceSoftwareVersion(); String deviceName2 =
			 * android.os.Build.PRODUCT; String deviceName4 =
			 * android.os.Build.DEVICE;
			 */
			String brand = android.os.Build.BRAND;
			String deviceName = android.os.Build.MODEL;
			StringBuilder deviceDetails = new StringBuilder();
			deviceDetails.append(brand).append(" ").append(deviceName);
			// Toast.makeText(this,
			// "Device details    "+deviceDetails.toString(),
			// Toast.LENGTH_LONG).show();

			String AppId = null;
			try {
				AppId = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}
			// String
			// AndroidId=android.provider.Settings.Secure.getString(getContentResolver(),
			// android.provider.Settings.Secure.ANDROID_ID);

			// password encoding
			String encodedpass = Base64.encodeToString(newPass.getBytes(),
					Base64.DEFAULT);
			encodedpass.trim();
			String enpass = encodedpass.substring(0, encodedpass.length() - 1);

			loginVo = new LoginVo();
			loginVo.setUserId(newUser);
			loginVo.setPassword(newPass);
			loginVo.setPhoneId(deviceDetails.toString());
			loginVo.setOsId(OSId);
			loginVo.setTtAppId(AppId);
			loginVo.setNetworkId(networkName);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (user != null) {
				try {
					FacebookLoginService facebookLoginService = new FacebookLoginService();
					// response =
					// facebookLoginService.checkForUser(userName,facebookId);
					response = facebookLoginService.checkForUser(loginVo);
					message = response.get("message");
					postalZip = response.get("zipCode");
					workZip = response.get("workCode");
					userId = response.get("userId");
					SharedPreferences settingsPage = getSharedPreferences(
							"SettingsPage", 0);
					Editor edits11 = settingsPage.edit();
					edits11.putBoolean("facebookStatus",
							response.get("facebook_share").equalsIgnoreCase(
									"true") ? true : false);
					edits11.putBoolean("twitterStatus",
							response.get("twitter_share").equalsIgnoreCase(
									"true") ? true : false);
					edits11.commit();

				} catch (Exception e) {
					//message = null;
					Log.e("Exception occured",
							"Exception occured at the time of facebook login",
							e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (!ValidationUtil.isNull(message)
					&& message.equalsIgnoreCase("Invalid Email ID or Password.")) {
				Intent zipIntent = new Intent(FacebookLogin.this, ZipActivity.class);
				startActivityForResult(zipIntent, PICK_ZIPCODE);
			} 
			else if (!ValidationUtil.isNull(message)
					&& message.equalsIgnoreCase("")
					&& ValidationUtil.isNullOrEmpty(response.get("zipCode"))) {
				Intent zipIntent = new Intent(FacebookLogin.this, ZipActivity.class);
				startActivityForResult(zipIntent, PICK_ZIPCODE_UPDATING);
			} 
			else {
				SharedPreferences spc = getSharedPreferences("UserName", 0);
				SharedPreferences.Editor edit = spc.edit();
				edit.putString("username", userName);
				edit.putString("password", encriptPwd);
				edit.putString("enuser", encriptUserName);
				edit.commit();

				/**
				 * put all the user information into the shared preferences
				 */
				String country = null;
				boolean isCanadaZip = ValidationUtil.validateCanadZip(postalZip);
				if (isCanadaZip) {
					country = "canada";
					Log.v("country IN LGIN PAGE", country);
				}
				SharedPreferences spc1 = getSharedPreferences("UserDetails", 0);
				SharedPreferences.Editor edits = spc1.edit();
				edits.putString("firstName", firstName);
				edits.putString("lastName", lastName);
				edits.putString("fbID", facebookId);
				edits.putString("postal", postalZip);
				edits.putString("workZip", workZip);
				edits.putString("UserId", userId);
				edits.putString("country", country);
				/*
				 * edits.putBoolean("isTwitterOn", true);
				 * edits.putBoolean("isFacebookOn", true);
				 */
				edits.commit();

				SharedPreferences customURL = getSharedPreferences("CustomURL",
						0);
				String fromPage = customURL.getString("fromPage", "");

				if (!ValidationUtil.isNullOrEmpty(fromPage)
						&& fromPage.equals("customURL")) {
					String uriPath = customURL.getString("uriPath", "");
					String parameter = customURL.getString("parameter", "");
					// CustomUrlActivity activity = new CustomUrlActivity();
					getIntoActivity(uriPath, parameter);
				} 
				else {
					Intent mainIntent;
					if(isFromDeepLink){
						mainIntent = new Intent(getApplicationContext(), DeepLinkOfferActivity.class);
						mainIntent.putExtra(AppConstant.DEEPLINK_KEY, AppConstant.DEEPLINK_KEY);
						mainIntent.putExtra(AppConstant.OBJECT_KEY, mDeepLinkInputsVo);
					}
					else{
						mainIntent = new Intent(getApplicationContext(), NearMeActivity.class);
						mainIntent.putExtra("selectedId", 0);
						mainIntent.putExtra("frmPage", "faceBook");
						mainIntent.putExtra("isGetStarted", "true");
					}
					startActivity(mainIntent);
					finish();
				}

			}
			progressDialog.dismiss();
		}

	}

	public void getIntoActivity(String uriPath, String parameter) {
		Log.v(" uriPath  and  parameter", "uriPath = " + uriPath
				+ " parameter = " + parameter);
		/**
		 * Start near me activity with no parameter.
		 */
		/* getting location of device */

		if (uriPath.equalsIgnoreCase("nearMe")) {
			Intent nearMeIntent = new Intent(FacebookLogin.this,
					NearMeActivity.class);
			startActivity(nearMeIntent);
		}
		if (uriPath.equalsIgnoreCase("search")) {
			String city = null;
			// application.getSearchList().clear();
			int index = parameter.indexOf("/");
			if (index != -1) {
				String[] param = parameter.split("/");
				city = param[0];
				/**
				 * Set the spMailingid,spUserId and spJobId into session.
				 */
				try {
					application.setSpMailingID(param[2]);
					application.setSpUserId(param[3]);
					application.setSpJobId(param[4]);
					Log.v("spMailingId ,spUserdId ,spJobId from dealSilver pop link",
							"spMailingId =" + param[2] + " spUserdId="
									+ param[3] + " spJobId=" + param[4]);
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.v("Exception:",
							"Exception occured without passing parameter spMailingId.");
				}
			} else {
				city = parameter;
			}
			Log.v("parameter for search from custom url ", "City name  ="
					+ city);
			/**
			 * Start the search activity with city as parameter.
			 */
			Intent searchIntent = new Intent(FacebookLogin.this,
					SearchActivity.class);
			searchIntent.putExtra("fromPage", "customURL");
			searchIntent.putExtra("address", city);
			startActivity(searchIntent);
		}
		/**
		 * Start claim offer activity from here.
		 */
		if (uriPath.equalsIgnoreCase("deal")) {

			String date = null;

			if (!ValidationUtil.isNullOrEmpty(parameter)) {
				String param[] = parameter.split("/");
				dealId = param[0];
				date = param[1];
				Log.v("Deal id and Deal deal date from URL is ", "dealId ="
						+ dealId + " Deal Date " + date);
				/**
				 * Set the spMailingid,spUserId and spJobId into session.
				 */
				try {
					application.setSpMailingID(param[2]);
					application.setSpUserId(param[3]);
					application.setSpJobId(param[4]);
					Log.v("spMailingId ,spUserdId ,spJobId from dealSilver pop link",
							"spMailingId =" + param[2] + " spUserdId="
									+ param[3] + " spJobId=" + param[4]);
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.v("Exception:",
							"Exception occured without passing parameter spMailingId.");
				}

			}

			/**
			 * Get location lat and long from the shared preferences.
			 */
			SharedPreferences location = getSharedPreferences(
					"LocationDetails", 0);
			String locLat = location.getString("locLat", "");
			String locLong = location.getString("locLong", "");
			/**
			 * Create new CustomDealVo object
			 */
			CustomDealVo customDealVo = new CustomDealVo();
			customDealVo.setDealId(dealId);
			customDealVo.setDealDate(date);
			customDealVo.setLocLat(locLat);
			customDealVo.setLocLong(locLong);
			/**
			 * Get deal from deal id and Date.
			 */

			new GetDealAsyncTask().execute(customDealVo);
		}
	}

	/**
	 * Inner class for executing service in back ground thread in order to get
	 * deal from deal id and Deal date.
	 * 
	 * @author dillip.lenka
	 * 
	 */
	public class GetDealAsyncTask extends
	AsyncTask<CustomDealVo, Void, DealsDetailVo> {
		private ProgressDialog mDialog = null;

		@Override
		protected void onPreExecute() {
			mDialog = ProgressDialog.show(FacebookLogin.this, "Searching ",
					"Please wait...");
			mDialog.setCancelable(true);
		}

		@Override
		protected DealsDetailVo doInBackground(CustomDealVo... customDealVo) {
			DealsDetailVo dealsDetailVo = null;
			try {
				CustonUrlService customService = new CustonUrlService();
				dealsDetailVo = customService.getCustomDeal(customDealVo[0]);
			} catch (ConnectTimeoutException exe) {
				Log.e("ConnectTimeoutException occured",
						"ConnectTimeoutException occured at the time get mating deal with deal id",
						exe);
				Intent contactIntent = new Intent(getApplicationContext(),
						ContactSupportActvity.class);
				startActivity(contactIntent);
			} catch (Exception e) {
				Log.e("Exception occured",
						"Exception occured at the time of login", e);
			}
			return dealsDetailVo;
		}

		@Override
		protected void onPostExecute(DealsDetailVo dealsDetailVo) {
			try {
				mDialog.dismiss();
			} catch (Exception e) {
				Log.e("EXception:", "Exception occured before dismiss dilog.");
			}
			/**
			 * Start search activity
			 */
			Intent searchIntent = new Intent(FacebookLogin.this,
					SearchActivity.class);
			searchIntent.putExtra("fromPage", "customURL");
			searchIntent.putExtra("dealId", dealId);
			searchIntent.putExtra("selectDeal", dealsDetailVo);
			startActivity(searchIntent);

		}
	}

	/**
	 * This class will be register the new user
	 * 
	 * @author lakshmipathi.p
	 * 
	 */
	public class SignupAsyncTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;
		private LoginVo loginVo;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(FacebookLogin.this,
					"Connecting", "Please Wait...");
			progressDialog.setCancelable(true);

			String newUser = userName;
			String newPass = password;
			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
			System.getProperty("os.version");
			String OSId = android.os.Build.VERSION.RELEASE;
			String networkName = tm.getNetworkOperatorName();
			String brand = android.os.Build.BRAND;
			String deviceName = android.os.Build.MODEL;
			StringBuilder deviceDetails = new StringBuilder();
			deviceDetails.append(brand).append(" ").append(deviceName);

			String AppId = null;
			try {
				AppId = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}

			// password encoding
			String encodedpass = Base64.encodeToString(newPass.getBytes(),
					Base64.DEFAULT);
			encodedpass.trim();
			String enpass = encodedpass.substring(0, encodedpass.length() - 1);

			loginVo = new LoginVo();
			loginVo.setUserId(newUser);
			loginVo.setPassword(newPass);
			loginVo.setPhoneId(deviceDetails.toString());
			loginVo.setOsId(OSId);
			loginVo.setTtAppId(AppId);
			loginVo.setNetworkId(networkName);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				FacebookLoginService facebookLoginService = new FacebookLoginService();
				facebookLoginService.signUpToTangoTab(signupDetailsRequest());
			} catch (ConnectTimeoutException e) {
				message = null;
				Log.e("ConnectTimeoutException occured",
						"ConnectTimeoutException occured at the time of facebook signup process", e);
				Intent contactIntent = new Intent(getApplicationContext(),
						ContactSupportActvity.class);
				startActivity(contactIntent);
			} catch (Exception e) {
				message = null;
				Log.e("Exception occured", "Exception occured at the time of facebook signup process", e);

				try {
					FacebookLoginService facebookLoginService = new FacebookLoginService();
					response = facebookLoginService.checkForUser(loginVo);
					message = response.get("message");
					postalZip = response.get("zipCode");
					workZip = response.get("workCode");
					userId = response.get("userId");
				} catch (ConnectTimeoutException e2) {
					message = null;
					Log.e("ConnectTimeoutException occured", "ConnectTimeoutException occured at the time of facebook signup process",
							e2);
					Intent contactIntent = new Intent(getApplicationContext(),
							ContactSupportActvity.class);
					startActivity(contactIntent);
				} catch (Exception e1) {
					message = null;
					Log.e("Exception occured", "Exception occured at the time of facebook login", e1);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (!ValidationUtil.isNull(message)
					&& !message.equalsIgnoreCase("Invalid Email ID or Password.")) {
				SharedPreferences spc = getSharedPreferences("UserName", 0);
				SharedPreferences.Editor edit = spc.edit();
				edit.putString("username", userName);
				edit.putString("password", encriptPwd);
				edit.putString("enuser", encriptUserName);
				edit.commit();

				SharedPreferences spc1 = getSharedPreferences("UserDetails", 0);
				SharedPreferences.Editor edits = spc1.edit();
				edits.putString("firstName", firstName);
				edits.putString("lastName", lastName);
				edits.putString("fbID", facebookId);
				edits.putString("postal", postalZip);
				edits.putString("workZip", workZip);
				edits.putString("UserId", userId);
				edits.commit();

				SharedPreferences location = getSharedPreferences(
						"locationDetail", 0);
				SharedPreferences.Editor edits1 = location.edit();
				edits1.putString("workZip", workZip);
				edits1.putString("homeZip", postalZip);
				edits1.commit();

				Intent mainIntent;
				if(isFromDeepLink){
					mainIntent = new Intent(getApplicationContext(), DeepLinkOfferActivity.class);
					mainIntent.putExtra(AppConstant.DEEPLINK_KEY, AppConstant.DEEPLINK_KEY);
					mainIntent.putExtra(AppConstant.OBJECT_KEY, mDeepLinkInputsVo);
				}
				else{
					mainIntent = new Intent(getApplicationContext(), NearMeActivity.class);
					mainIntent.putExtra("selectedId", 0);
				}
				startActivity(mainIntent);
				finish();
			}
		}

	}

	/**
	 * Method will return a String entity in order to add all the parmeter
	 * 
	 * @return
	 */
	public StringEntity signupDetailsRequest() {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		StringEntity stringEntity = null;
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag(null, "signup");

			serializer.startTag(null, "email");
			serializer.text(userName);
			serializer.endTag(null, "email");

			serializer.startTag(null, "password");
			serializer.text(password);
			serializer.endTag(null, "password");

			serializer.startTag(null, "firstname");
			serializer.text(firstName);
			serializer.endTag(null, "firstname");

			serializer.startTag(null, "lastname");
			serializer.text(lastName);
			serializer.endTag(null, "lastname");

			serializer.startTag(null, "facebookid");
			serializer.text(facebookId);
			serializer.endTag(null, "facebookid");

			serializer.startTag(null, "zipcode");
			serializer.text(postalZip);
			serializer.endTag(null, "zipcode");

			serializer.startTag(null, "workzip");
			serializer.text(workZip);
			serializer.endTag(null, "workzip");

			serializer.startTag(null, "promocode");
			serializer.text(mPromoCode);
			serializer.endTag(null, "promocode");

			serializer.endTag(null, "signup");
			serializer.endDocument();
			stringEntity = new StringEntity(writer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringEntity;
	}

	private String generateShaCode(String userId) {
		String userName = userId;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(userName.getBytes());

		byte byteData[] = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		System.out.println("Hex format : " + sb.toString());
		// convert the byte to hex format method 2
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(0xff & byteData[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		System.out.println("Hex format : " + hexString.toString());
		return hexString.toString();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_ZIPCODE) {
			if (resultCode == RESULT_OK) {
				postalZip = data.getStringExtra("Zip_Code");
				workZip = data.getStringExtra("Zip_Code_work");
				mPromoCode = data.getStringExtra(AppConstant.PROMO_CODE);
			}
			new SignupAsyncTask().execute();
		} else if (requestCode == PICK_ZIPCODE_UPDATING) {
			if (resultCode == RESULT_OK) {
				postalZip = data.getStringExtra("Zip_Code");
				workZip = data.getStringExtra("Zip_Code_work");
				mPromoCode = data.getStringExtra(AppConstant.PROMO_CODE);
			}
			new UpdateZipCodeAsyncTask().execute();
		}
	}

	/**
	 * 
	 * @author lakshmipathi.p
	 * 
	 */
	public class UpdateZipCodeAsyncTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(FacebookLogin.this,
					"Searching ", "Please wait...");
			progressDialog.setCancelable(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				FacebookLoginService facebookLoginService = new FacebookLoginService();
				facebookLoginService.updateZipForUser(userName, postalZip, workZip);
			} catch (ConnectTimeoutException e) {
				message = null;
				Log.e("ConnectTimeoutException occured",
						"ConnectTimeoutException occured at the time of updating Zip",
						e);
				Intent contactIntent = new Intent(getApplicationContext(),
						ContactSupportActvity.class);
				startActivity(contactIntent);
			} catch (Exception e) {
				message = null;
				Log.e("Exception occured",
						"Exception occured at the time of updating Zip", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			SharedPreferences spc = getSharedPreferences("UserName", 0);
			SharedPreferences.Editor edit = spc.edit();
			edit.putString("username", userName);
			edit.putString("password", encriptPwd);
			edit.putString("enuser", encriptUserName);
			edit.commit();

			SharedPreferences spc1 = getSharedPreferences("UserDetails", 0);
			SharedPreferences.Editor edits = spc1.edit();
			edits.putString("firstName", firstName);
			edits.putString("lastName", lastName);
			edits.putString("fbID", facebookId);
			edits.putString("postal", postalZip);
			edits.putString("UserId", userId);
			edits.commit();

			Intent mainIntent;
			if(isFromDeepLink){
				mainIntent = new Intent(getApplicationContext(), DeepLinkOfferActivity.class);
				mainIntent.putExtra(AppConstant.DEEPLINK_KEY, AppConstant.DEEPLINK_KEY);
				mainIntent.putExtra(AppConstant.OBJECT_KEY, mDeepLinkInputsVo);
			}
			else{
				mainIntent = new Intent(getApplicationContext(), NearMeActivity.class);
				mainIntent.putExtra("selectedId", 0);
			}
			startActivity(mainIntent);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		// moveTaskToBack(true);
	}
}
