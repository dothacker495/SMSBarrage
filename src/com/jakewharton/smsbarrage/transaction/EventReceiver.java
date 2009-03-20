package com.jakewharton.smsbarrage.transaction;

import com.jakewharton.smsbarrage.ui.Preferences;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class EventReceiver extends BroadcastReceiver {
	private static final String TAG="EventReceiver";
	
	static final Object mStartingServiceSync = new Object();
	static PowerManager.WakeLock mStartingService;

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		if (shared.getBoolean(Preferences.PREF_AUTO_START, true)) {
			intent.setClass(context, BarrageService.class);
			intent.putExtra("result", getResultCode());
			beginStartingService(context, intent);
		}
	}
	
	public static void beginStartingService(Context context, Intent intent) {
		synchronized (mStartingServiceSync) {
			if (mStartingService == null) {
				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StartingEventReceiver");
				mStartingService.setReferenceCounted(false);
			}
			mStartingService.acquire();
			context.startService(intent);
		}
	}
	
	public static void finishStartingService(Service service, int startId) {
		synchronized (mStartingServiceSync) {
			if (mStartingService != null) {
				if (service.stopSelfResult(startId)) {
					mStartingService.release();
				}
			}
		}
	}
}
