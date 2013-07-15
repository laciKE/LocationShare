package sk.estesadohodneme.locationshare;

import org.osmdroid.views.overlay.PathOverlay;

import android.location.Location;

/**
 * Callbacks for processing new track points, e. g. drawing track line via
 * {@link PathOverlay}, storing gpx track points, etc.
 */
public interface TrackListener {
	public void onNewTrackPoint(Location location);
}
