package sk.estesadohodneme.locationshare;

import android.location.Location;

public interface TrackListener {
	public void onNewTrackPoint(Location location);
}
