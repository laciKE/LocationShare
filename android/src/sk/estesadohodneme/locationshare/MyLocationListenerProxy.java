package sk.estesadohodneme.locationshare;

import org.osmdroid.LocationListenerProxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyLocationListenerProxy extends LocationListenerProxy {
	protected LocationListener mLocationListener;
	protected Context mContext;
	protected BroadcastReceiver mBroadcastReceiver;

	public MyLocationListenerProxy(Context ctx) {
		super(null);
		mContext = ctx;
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (mLocationListener != null) {
					Log.i("LocProxy", intent.toString());
					if(LocationService.LOCATION_UPDATE.equals(intent.getAction())){
						Location location = SharedLocationStorage.getInstance().getLast();
						mLocationListener.onLocationChanged(location);
					}
				}
			}
		};
	}

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
		Log.i("LocProxy", intent.toString());

		return true;
	}

	@Override
	public void stopListening() {
		Intent intent = new Intent(mContext, LocationService.class);
		intent.putExtra(LocationService.FOREGROUND_LISTENER, false);
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mBroadcastReceiver);
		mContext.startService(intent);
		Log.i("LocProxy", intent.toString());
	}

}
