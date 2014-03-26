package com.tangotab.ui.floatinglistview;

import android.os.*;
import android.util.*;

import java.util.*;

import com.tangotab.R;

public class Data {
	public static final String TAG = Data.class.getSimpleName();

	public static List<Pair<String, List<Composer>>> getAllData() {
		List<Pair<String, List<Composer>>> res = new ArrayList<Pair<String, List<Composer>>>();

		for (int i = 0; i < 4; i++) {
			res.add(getOneSection(i));
		}

		return res;
	}

	public static List<Composer> getFlattenedData() {
		List<Composer> res = new ArrayList<Composer>();

		for (int i = 0; i < 4; i++) {
			res.addAll(getOneSection(i).second);
		}

		return res;
	}

	public static Pair<Boolean, List<Composer>> getRows(int page) {
		List<Composer> flattenedData = getFlattenedData();
		if (page == 1) {
			return new Pair<Boolean, List<Composer>>(true,
					flattenedData.subList(0, 5));
		} else {
			SystemClock.sleep(2000); // simulate loading
			return new Pair<Boolean, List<Composer>>(
					page * 5 < flattenedData.size(), flattenedData.subList(
							(page - 1) * 5,
							Math.min(page * 5, flattenedData.size())));
		}
	}

	public static Pair<String, List<Composer>> getOneSection(int index) {
		String[] titles = { "MY TANGOTAB", "VIEW OFFERS NEAR", "MAP VIEW",
				"" };
		Composer[][] composerss = {
				{ new Composer("My Philanthropy", R.drawable.ic_launcher),
						new Composer("Share TangoTab", R.drawable.ic_launcher),
						new Composer("My Offers", R.drawable.ic_launcher),new Composer("My Settings", R.drawable.ic_launcher) },
				{ new Composer("Current Location", R.drawable.ic_launcher),
						new Composer("Work", R.drawable.ic_launcher),
						new Composer("Home", R.drawable.ic_launcher),
						new Composer("Alternate", R.drawable.ic_launcher),new Composer("Edit Search Filter", R.drawable.ic_launcher) },
				{ new Composer("Map View of Offers", R.drawable.ic_launcher),
						

				},
				{ new Composer("Privacy Policy",  R.drawable.ic_launcher),
						new Composer("Terms and Conditions",  R.drawable.ic_launcher),
						new Composer("Log Out",  R.drawable.ic_launcher), }, };
		return new Pair<String, List<Composer>>(titles[index],
				Arrays.asList(composerss[index]));
	}
}
