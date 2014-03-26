package com.tangotab.core.utils;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

public class EnableGpsCommand extends CancelCommand {
	
	public EnableGpsCommand( Activity activity) {
	    super(activity);
	}

	public void execute(){
	     m_activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	     super.execute();
	}
}