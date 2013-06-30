package sk.estesadohodneme.locationshare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Based on examples at
 * http://developer.android.com/reference/android/app/Service.html.
 */
public class LocationService extends Service implements
		OnSharedPreferenceChangeListener, MyLocationListener {
	// available commands
	// commands with string argument
	public final static String COMMAND = "command";
	// string arguments for COMMAND
	public final static String COMMAND_START = "start";
	public final static String COMMAND_STOP = "stop";
	public final static String COMMAND_GET_LOCATION = "get_location";
	// commands with boolean argument
	public final static String FOREGROUND_LISTENER = "foreground_listener";
	public final static String PROVIDER_GPS = "gps_provider";
	public final static String PROVIDER_NETWORK = "network_provider";
	public final static String LIVETRACKING = "livetracking";
	// commands with long argument
	public final static String MIN_TIME_UPDATE = "min_time";
	// commands with float argument
	public final static String MIN_DISTANCE_UPDATE = "min_distance";

	// available broadcast intent commands
	public final static String LOCATION_UPDATE = "location_update";
	
	private NotificationManager mNM;
	private MyLocationManager mMyLocationManager;
	private boolean mRequestStop = false;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.service_started;

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification(R.string.service_started);

		mMyLocationManager = new MyLocationManager(getApplicationContext(),
				this);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		updateLocationProviders(sharedPreferences, false);

		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	// This is the old onStart method that will be called on the pre-2.0
	// platform. On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent, startId);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i("LocationService", "Received stop");
		mMyLocationManager.disable();
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);

		// Tell the user we stopped.
		// showNotification(R.string.service_stopped);
	}

	/**
	 * Shows a notification while this service is running.
	 */
	@SuppressWarnings("deprecation")
	protected void showNotification(int resId) {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(resId);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(
				R.drawable.ic_stat_location_service, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this,
				getText(R.string.location_service_label), text, contentIntent);

		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}

	/**
	 * Handles command from Activity.
	 */
	protected void handleCommand(Intent intent, int startId) {
		// TODO
		Log.i("LocationService", "Received start id " + startId + ": " + intent);
		if(mRequestStop){
			stopSelf(startId);
		}
		if (COMMAND_STOP.equals(intent.getStringExtra(COMMAND))) {
			mRequestStop = true;
		}
		if (COMMAND_START.equals(intent.getStringExtra(COMMAND))) {
			mMyLocationManager.enable();
		}
	}

	protected void updateLocationProviders(SharedPreferences sharedPreferences,
			boolean updateListener) {
		String[][] providers = {
				{ SettingsActivity.PREF_NETWORK_PROVIDER,
						LocationManager.NETWORK_PROVIDER },
				{ SettingsActivity.PREF_GPS_PROVIDER,
						LocationManager.GPS_PROVIDER } };
		for (String[] provider : providers) {
			if (sharedPreferences.getBoolean(provider[0], true)) {
				mMyLocationManager.enableProvider(provider[1], false);
			} else {
				mMyLocationManager.disableProvider(provider[1], false);
			}
		}
		if (updateListener) {
			mMyLocationManager.updateLocationListener();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO
		Log.i("LocationService", location.toString());
		SharedLocationStorage.getInstance().add(location);
		Intent intent = new Intent(LOCATION_UPDATE);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}

	@Override
	public void onStatusChanged(String provider, int status) {
		CharSequence statusDescription = null;
		switch (status) {
		case LocationProvider.OUT_OF_SERVICE:
			statusDescription = getText(R.string.out_of_service);
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			statusDescription = getText(R.string.temporarily_unavailable);
			break;
		}
		Toast.makeText(getApplicationContext(),
				provider + " is " + statusDescription + ".", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.PREF_GPS_PROVIDER)
				|| key.equals(SettingsActivity.PREF_NETWORK_PROVIDER)) {
			Log.i("LocationService", "sharedpref changed: " + key);
			updateLocationProviders(sharedPreferences, true);
		}
	}

}
