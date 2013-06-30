package sk.estesadohodneme.locationshare;

import android.location.Location;

public interface MyLocationListener {
	public void onLocationChanged(Location location);

	public void onStatusChanged(String provider, int status);
}
