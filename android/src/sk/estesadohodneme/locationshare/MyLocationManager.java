package sk.estesadohodneme.locationshare;

import java.util.HashSet;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class MyLocationManager {
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	private MyLocationListener mMyLocationListener;
	private long mMinTime = 60 * 1000;
	private float mMinDistance = 5;
	private boolean mIsEnabled = false;
	private HashSet<String> mEnabledProviders;
	private Location mLastLocation = null;

	public MyLocationManager(Context context,
			MyLocationListener myLocationListener) {
		mMyLocationListener = myLocationListener;
		mEnabledProviders = new HashSet<String>();

		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				if ((status == LocationProvider.TEMPORARILY_UNAVAILABLE)
						|| (status == LocationProvider.OUT_OF_SERVICE)) {
					mMyLocationListener.onStatusChanged(provider, status);
				}
			}

			@Override
			public void onProviderEnabled(String provider) {
				mEnabledProviders.add(provider);
				updateLocationListener();
			}

			@Override
			public void onProviderDisabled(String provider) {
				mEnabledProviders.remove(provider);
				updateLocationListener();
			}

			@Override
			public void onLocationChanged(Location location) {
				if (mLastLocation == null) {
					mLastLocation = location;
					mMyLocationListener.onLocationChanged(location);
					return;
				}

				long timeDelta = location.getTime() - mLastLocation.getTime();
				float accuracyDelta = location.getAccuracy()
						- mLastLocation.getAccuracy();
				boolean isSignificantlyNewer = timeDelta > Math.max(mMinTime,
						30 * 1000);
				boolean isNewerAndMoreAccurate = (timeDelta > (mMinTime / 2))
						&& (accuracyDelta < -50);
				if (isSignificantlyNewer || isNewerAndMoreAccurate) {
					mLastLocation = location;
					mMyLocationListener.onLocationChanged(location);
				}
			}
		};
	}

	public void setMinTime(long minTime, boolean updateListener) {
		mMinTime = minTime;
		if (updateListener) {
			updateLocationListener();
		}
	}

	public void setMinDistance(float minDistance, boolean updateListener) {
		mMinDistance = minDistance;
		if (updateListener) {
			updateLocationListener();
		}
	}

	public void enableProvider(String provider, boolean updateListener) {
		mEnabledProviders.add(provider);
		if (updateListener) {
			updateLocationListener();
		}
	}

	public void disableProvider(String provider, boolean updateListener) {
		mEnabledProviders.remove(provider);
		if (updateListener) {
			updateLocationListener();
		}
	}

	public void enable() {
		mIsEnabled = true;
		updateLocationListener();
	};

	public void disable() {
		mIsEnabled = false;
		mLocationManager.removeUpdates(mLocationListener);
	};

	public void updateLocationListener() {
		if (mIsEnabled) {
			Log.i("MyLocationManager", "updateListener " + mMinTime);
			mLocationManager.removeUpdates(mLocationListener);
			for (String provider : mEnabledProviders) {
				Log.i("MyLocationManager", "request updates from " + provider);
				mLocationManager.requestLocationUpdates(provider, mMinTime,
						mMinDistance, mLocationListener);
			}
		}
	}

}
