package sk.estesadohodneme.locationshare;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;

public class Overlays implements TrackListener,
		OnSharedPreferenceChangeListener {

	private Handler mHandler = new Handler();
	private Context mContext;
	private MapView mMapView;
	private List<Overlay> mOverlays;
	private MyLocationOverlay mLocationOverlay;
	private PathOverlay mPathOverlay;
	private ScaleBarOverlay mScaleBarOverlay;
	private MyLocationOverlay mCompassOverlay;
	private ItemizedIconOverlay<OverlayItem> mFriendsLocationOverlay;
	public GpxTrack mGpxTrack;

	private boolean[] mVisibleOverlays = { false, // My location
			false, // Friend's locations
			false, // Scale bar
			false, // Compass
			false // Path
	};

	public Overlays(Context context, MapView mapView,
			SharedPreferences sharedPreferences) {
		mContext = context;
		mMapView = mapView;
		mGpxTrack = new GpxTrack(mContext, mHandler);
		mOverlays = mMapView.getOverlays();
		mScaleBarOverlay = new ScaleBarOverlay(mContext);

		mCompassOverlay = new MyLocationOverlay(mContext, mapView);

		mLocationOverlay = new CustomMyLocationOverlay(mContext, mMapView, this);
		mLocationOverlay.setLocationUpdateMinDistance(5);
		mLocationOverlay.setLocationUpdateMinTime(Long
				.parseLong(sharedPreferences.getString(
						SettingsActivity.PREF_UPDATE_FOREGROUND, "60000")));

		mPathOverlay = new PathOverlay(Color.CYAN, mContext);

		ArrayList<OverlayItem> mOverlayItems = new ArrayList<OverlayItem>();
		Drawable marker = mContext.getResources()
				.getDrawable(R.drawable.marker);
		OverlayItem oItem = new OverlayItem("BA", "Bratislava", new GeoPoint(
				48.1579, 17.06902));
		mOverlayItems.add(oItem);
		oItem = new OverlayItem("KE", "Kosice", new GeoPoint(48.7166, 21.2601));
		oItem.setMarker(marker);
		mOverlayItems.add(oItem);
		oItem = new OverlayItem("ZA", "Zilina",
				new GeoPoint(49.22303, 18.73757));
		mOverlayItems.add(oItem);
		mFriendsLocationOverlay = new ItemizedIconOverlay<OverlayItem>(
				mContext, mOverlayItems, null);
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public void onResume() {
		showVisibleOverlays();
	}

	public void onPause() {
		if (mVisibleOverlays[3]) {
			mCompassOverlay.disableCompass();
			mCompassOverlay.setEnabled(false);
		}
		if (mVisibleOverlays[0]) {
			mLocationOverlay.disableFollowLocation();
			mLocationOverlay.disableMyLocation();
			mLocationOverlay.setEnabled(false);
		}
	}

	public void onStart() {
		SharedLocationStorage sharedLocationStorage = SharedLocationStorage
				.getInstance();
		for (int i = 0; i < sharedLocationStorage.size(); i++) {
			MyGeoPoint trackPoint = new MyGeoPoint(sharedLocationStorage.get(i));
			mGpxTrack.add(trackPoint);
			mPathOverlay.addPoint(trackPoint);
		}
	}

	public void onStop() {
		mPathOverlay.clearPath();
		mGpxTrack.clear();
	}

	public void enableFollowLocation() {
		mVisibleOverlays[0] = true;
		showVisibleOverlays();
		mLocationOverlay.enableFollowLocation();
	}

	public void showVisibleOverlays() {
		mOverlays.clear();
		if (mVisibleOverlays[0]) {
			mLocationOverlay.setDrawAccuracyEnabled(true);
			mLocationOverlay.enableMyLocation();
			mLocationOverlay.setEnabled(true);
			mOverlays.add(mLocationOverlay);
		} else {
			mLocationOverlay.disableFollowLocation();
			mLocationOverlay.setDrawAccuracyEnabled(false);
			mLocationOverlay.disableMyLocation();
			mLocationOverlay.setEnabled(false);
		}
		if (mVisibleOverlays[3]) {
			mCompassOverlay.enableCompass();
			mCompassOverlay.setEnabled(true);
			mOverlays.add(mCompassOverlay);
		} else {
			mCompassOverlay.disableCompass();
			mCompassOverlay.setEnabled(false);
		}
		if (mVisibleOverlays[1]) {
			mOverlays.add(mFriendsLocationOverlay);
		}
		if (mVisibleOverlays[2]) {
			mOverlays.add(mScaleBarOverlay);
		}
		if (mVisibleOverlays[4]) {
			mOverlays.add(mPathOverlay);
		}
		mMapView.invalidate();
	}

	public void showSelectionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(mContext, R.style.AlertDialog));
		builder.setTitle(R.string.choose_visible_layers);
		builder.setMultiChoiceItems(R.array.overlays, mVisibleOverlays,
				new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
					}
				});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SparseBooleanArray checked = ((AlertDialog) dialog)
						.getListView().getCheckedItemPositions();
				for (int i = 0; i < mVisibleOverlays.length; i++) {
					mVisibleOverlays[i] = checked.get(i);
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						showVisibleOverlays();
					}
				});
			}
		});
		builder.create().show();
	}

	@Override
	public void onNewTrackPoint(Location location) {
		MyGeoPoint trackPoint = new MyGeoPoint(location);
		mGpxTrack.add(trackPoint);
		mPathOverlay.addPoint(trackPoint);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.PREF_UPDATE_FOREGROUND)) {
			long minTime = Long.parseLong(sharedPreferences.getString(
					SettingsActivity.PREF_UPDATE_FOREGROUND, "60000"));
			mLocationOverlay.setLocationUpdateMinTime(minTime);
		}
	}

}
