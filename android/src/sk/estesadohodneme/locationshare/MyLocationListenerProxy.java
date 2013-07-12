package sk.estesadohodneme.locationshare;

import org.osmdroid.LocationListenerProxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Modification of {@link LocationListenerProxy} for using own LocationService
 * instead of {@link LocationManager}.
 */
public class MyLocationListenerProxy extends LocationListenerProxy {
	protected LocationListener mLocationListener;
	protected Context mContext;
	protected BroadcastReceiver mBroadcastReceiver;

	/**
	 * Creates {@link BroadcastReceiver} for receiving intents from
	 * {@link LocationService}.
	 * 
	 * @param ctx
	 *            application context
	 */
	public MyLocationListenerProxy(Context ctx) {
		super(null);
		mContext = ctx;
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (mLocationListener != null) {
					if (LocationService.LOCATION_UPDATE.equals(intent
							.getAction())) {
						Location location = SharedLocationStorage.getInstance()
								.getLast();
						mLocationListener.onLocationChanged(location);
					}
				}
			}
		};
	}

	/**
	 * Registers {@link BroadcastReceiver}, sends intent with update parameters
	 * to {@link LocationService} and requests for foreground listening of
	 * location updates.
	 */
	@Override
	public boolean startListening(final LocationListener pListener,
			final long pUpdateTime, final float pUpdateDistance) {
		mLocationListener = pListener;
		LocalBroadcastManager.getInstance(mContext).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(LocationService.LOCATION_UPDATE));
		Intent intent = new Intent(mContext, LocationService.class);
		intent.putExtra(LocationService.FOREGROUND_LISTENER, true);
		intent.putExtra(LocationService.MIN_TIME_UPDATE, pUpdateTime);
		intent.putExtra(LocationService.MIN_DISTANCE_UPDATE, pUpdateDistance);
		mContext.startService(intent);

		return true;
	}

	/**
	 * Unregisters {@link BroadcastReceiver} and tells {@link LocationService}
	 * that foreground listening is no longer needed.
	 */
	@Override
	public void stopListening() {
		Intent intent = new Intent(mContext, LocationService.class);
		intent.putExtra(LocationService.FOREGROUND_LISTENER, false);
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mBroadcastReceiver);
		mContext.startService(intent);
	}

}
