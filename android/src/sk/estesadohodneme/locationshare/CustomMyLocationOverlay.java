package sk.estesadohodneme.locationshare;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.location.Location;

public class CustomMyLocationOverlay extends MyLocationOverlay {

	private TrackListener mTrackListener = null;

	public CustomMyLocationOverlay(Context ctx, MapView mapView,
			ResourceProxy pResourceProxy) {
		super(ctx, mapView, pResourceProxy);
	}

	public CustomMyLocationOverlay(Context ctx, MapView mapView,
			TrackListener trackListener) {
		super(ctx, mapView);
		mTrackListener = trackListener;
	}

	public CustomMyLocationOverlay(Context ctx, MapView mapView) {
		super(ctx, mapView);
	}

	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		if (mTrackListener != null) {
			mTrackListener.onNewTrackPoint(location);
		}
	}
	
}
