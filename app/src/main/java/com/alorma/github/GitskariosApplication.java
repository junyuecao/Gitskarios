package com.alorma.github;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Trace;

import com.alorma.github.sdk.security.ApiConstants;
import com.alorma.github.sdk.security.StoreCredentials;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Bernat on 08/07/2014.
 */
public class GitskariosApplication extends Application {

	private GoogleAnalytics analytics;
	private Tracker tracker;

	@Override
	public void onCreate() {
		super.onCreate();

		JodaTimeAndroid.init(this);
		
		if (BuildConfig.DEBUG) {
			int enabled = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

			ComponentName component = new ComponentName(this,
					Interceptor.class);

			getPackageManager().setComponentEnabledSetting(component, enabled,
					PackageManager.DONT_KILL_APP);
		}

		ApiConstants.CLIENT_ID = BuildConfig.CLIENT_ID;
		ApiConstants.CLIENT_SECRET = BuildConfig.CLIENT_SECRET;
		ApiConstants.CLIENT_CALLBACK = BuildConfig.CLIENT_CALLBACK;

		analytics = GoogleAnalytics.getInstance(this);

		int flag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

		ComponentName componentName = new ComponentName(this, Interceptor.class);
		getPackageManager().setComponentEnabledSetting(componentName, flag, PackageManager.DONT_KILL_APP);

	}

	public Tracker getTracker() {
		if (tracker == null) {
			tracker = analytics.newTracker(R.xml.global_tracker);
		}

		return tracker;
	}
}
