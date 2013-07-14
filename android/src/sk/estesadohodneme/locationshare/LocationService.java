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
 * Background service listening for location updates, managing parameters
 * changes of location updates and sending http update messages. Based on
 * examples at http://developer.android.com/reference/android/app/Service.html.
 * New locations are storing into the {@link SharedLocationStorage} and info
 * about them are sending via broadcast intents.
 */
public class LocationService extends Service implements
		OnSharedPreferenceChangeListener, MyLocationListener {
	// available commands
	// commands with string argument
	public final static String COMMAND = "command";
	// string arguments for COMMAND
	public final static String COMMAND_START = "start";
	public final static String COMMAND_STOP = "stop";
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
	private HttpConnectionManager mHttpConnectionManager;
	private Location mLastLiveLocation;
	private boolean mRequestStop = false;
	private float mMinDistanceUpdate = 10;
	private long mForegroundUpdate = 60 * 1000;
	private long mBackgroundUpdate = 60 * 1000;
	private long mLiveTrackingUpdate = 60 * 1000;
	private boolean mLiveTrackingEnabled = false;
	private boolean mForegroundUpdateEnabled = false;

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
		mHttpConnectionManager = new HttpConnectionManager();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		mHttpConnectionManager.setUrl(sharedPreferences.getString(
				SettingsActivity.PREF_LIVETRACKING_URL, null));

		updateLocationFrequency(false);
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
		Log.i("LocService", intent.toString() + " "+ startId);
		if (mRequestStop) {
			stopSelf(startId);
		}
		if (COMMAND_STOP.equals(intent.getStringExtra(COMMAND))) {
			mRequestStop = true;
			stopSelf(startId);
		}
		if (COMMAND_START.equals(intent.getStringExtra(COMMAND))) {
			mMyLocationManager.enable();
		}

		if (intent.hasExtra(MIN_TIME_UPDATE)) {
			mForegroundUpdate = intent.getLongExtra(MIN_TIME_UPDATE, 60 * 1000);
		}
		if (intent.hasExtra(MIN_DISTANCE_UPDATE)) {
			mMinDistanceUpdate = intent.getFloatExtra(MIN_DISTANCE_UPDATE, 5);
			mMyLocationManager.setMinDistance(mMinDistanceUpdate, false);
		}
		if (intent.hasExtra(FOREGROUND_LISTENER)) {
			mForegroundUpdateEnabled = intent.getBooleanExtra(
					FOREGROUND_LISTENER, false);
			updateLocationFrequency(true);
		}
	}

	/**
	 * Computes new interval between location updates.
	 * 
	 * @param updateListener
	 *            if true, changes are processed immediately
	 */
	protected void updateLocationFrequency(boolean updateListener) {
		long updateInterval = mBackgroundUpdate;
		if (mLiveTrackingEnabled) {
			updateInterval = Math.min(updateInterval, mLiveTrackingUpdate);
		}
		if (mForegroundUpdateEnabled) {
			updateInterval = Math.min(updateInterval, mForegroundUpdate);
		}
		mMyLocationManager.setMinTime(updateInterval, false);

		if (updateListener) {
			mMyLocationManager.updateLocationListener();
		}
	}

	/**
	 * Updates parameters of the location providers: state of the providers and
	 * frequencies of the location updates in foreground, background and
	 * livetracking mode.
	 * 
	 * @param sharedPreferences
	 *            {@link SharedPreferences} object for resolving new values
	 * @param updateListener
	 *            if true, changes are processed immediately
	 */
	protected void updateLocationProviders(SharedPreferences sharedPreferences,
			boolean updateListener) {
		String[][] providers = {
				{ SettingsActivity.PREF_NETWORK_PROVIDER,
						LocationManager.NETWORK_PROVIDER },
				{ SettingsActivity.PREF_GPS_PROVIDER,
						LocationManager.GPS_PROVIDER } };
		for (String[] provider : providers) {
			if (sharedPreferences.getBoolean(provider[0], false)) {
				mMyLocationManager.enableProvider(provider[1], false);
			} else {
				mMyLocationManager.disableProvider(provider[1], false);
			}
		}

		mForegroundUpdate = Long.parseLong(sharedPreferences.getString(
				SettingsActivity.PREF_UPDATE_FOREGROUND, "60000"));
		mBackgroundUpdate = Long.parseLong(sharedPreferences.getString(
				SettingsActivity.PREF_UPDATE_BACKGROUND, "60000"));
		mLiveTrackingUpdate = Long.parseLong(sharedPreferences.getString(
				SettingsActivity.PREF_UPDATE_LIVETRACKING, "60000"));
		mLiveTrackingEnabled = sharedPreferences.getBoolean(
				SettingsActivity.PREF_LIVETRACKING, false);

		updateLocationFrequency(updateListener);
	}

	/**
	 * Listens for location updates from {@link MyLocationManager}, stores new
	 * {@link Location} into the {@link SharedLocationStorage} and informs other
	 * application components about it via broadcast intents. Also if
	 * livetracking is enabled and it is time for new update, sends new
	 * {@link Location} to the {@link HttpConnectionManager}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		Log.i("LocationService", location.toString());
		SharedLocationStorage.getInstance().add(location);
		Intent intent = new Intent(LOCATION_UPDATE);
		LocalBroadcastManager.getInstance(getApplicationContext())
				.sendBroadcast(intent);
		if (mLiveTrackingEnabled
				&& ((mLastLiveLocation == null) || (location.getTime()
						- mLastLiveLocation.getTime() > 0.9 * mLiveTrackingUpdate))) {
			mLastLiveLocation = location;
			mHttpConnectionManager.send(location);
		}
	}

	/**
	 * Informs user about status changes of the location providers.
	 */
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

	/**
	 * Listens for changes in the {@link SharedPreferences} and if the change
	 * are interesting, processes this change. <br>
	 * Interesting changes are enabling or disabling location providers, changes
	 * in location updates and livetracking url.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.PREF_GPS_PROVIDER)
				|| key.equals(SettingsActivity.PREF_NETWORK_PROVIDER)) {
			updateLocationProviders(sharedPreferences, true);
		}
		if (key.equals(SettingsActivity.PREF_LIVETRACKING)
				|| key.equals(SettingsActivity.PREF_UPDATE_BACKGROUND)
				|| key.equals(SettingsActivity.PREF_UPDATE_FOREGROUND)
				|| key.equals(SettingsActivity.PREF_UPDATE_LIVETRACKING)) {
			updateLocationProviders(sharedPreferences, true);
		}
		if (key.equals(SettingsActivity.PREF_LIVETRACKING_URL)) {
			mHttpConnectionManager.setUrl(sharedPreferences.getString(
					SettingsActivity.PREF_LIVETRACKING_URL, null));
		}
	}
}
