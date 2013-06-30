package sk.estesadohodneme.locationshare;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.location.Location;

public class CustomMyLocationOverlay extends MyLocationOverlay {
	protected Context mContext;

	private TrackListener mTrackListener = null;

	public CustomMyLocationOverlay(Context ctx, MapView mapView,
			ResourceProxy pResourceProxy) {
		super(ctx, mapView, pResourceProxy);
		mContext = ctx;
	}

	public CustomMyLocationOverlay(Context ctx, MapView mapView,
			TrackListener trackListener) {
		super(ctx, mapView);
		mTrackListener = trackListener;
		mContext = ctx;
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
	
	@Override
    public boolean enableMyLocation() {
            boolean result = true;

            if (mLocationListener == null) {
                    mLocationListener = new MyLocationListenerProxy(mContext);
                    
                    result = mLocationListener.startListening(this, getLocationUpdateMinTime(),
                                    getLocationUpdateMinDistance());
            }

            // Update the screen to see changes take effect
            if (mMapView != null) {
                    mMapView.postInvalidate();
            }

            return result;
    }
	
}
