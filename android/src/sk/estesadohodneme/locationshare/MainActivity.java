package sk.estesadohodneme.locationshare;

import java.io.File;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements TrackListener,
		OnSharedPreferenceChangeListener {
	private static final String SESSION_PREFS = "SessionsFile";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String ZOOM = "zoom";

	private Handler mHandler = new Handler();
	private MapController mMapController;
	private MapView mMapView;
	private MyLocationOverlay mLocationOverlay;
	private PathOverlay mPathOverlay;
	private GpxTrack mGpxTrack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMapView = (MapView) findViewById(R.id.mapview);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mMapView.setUseDataConnection(sharedPreferences.getBoolean(
				SettingsActivity.PREF_ONLINE_MAPS, false));

		final ITileSource tileSource = new XYTileSource(
				"FreemapSlovakiaHiking", ResourceProxy.string.offline_mode, 9,
				15, 256, ".png", "http://tiles.freemap.sk/T/");
		mMapView.setTileSource(tileSource);
		
		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);
		mMapController = mMapView.getController();

		SharedPreferences lastSession = getSharedPreferences(SESSION_PREFS,
				MODE_PRIVATE);
		GeoPoint point = new GeoPoint(lastSession.getInt(LATITUDE, 48158210),
				lastSession.getInt(LONGITUDE, 17083310));
		mMapController.setCenter(point);
		mMapController.setZoom(lastSession.getInt(ZOOM, 14));

		// ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
		// OverlayItem oItem = new OverlayItem("laciKE", "my location", point);
		// Drawable marker = getResources().getDrawable(R.drawable.marker);
		// oItem.setMarker(marker);
		// mItems.add(oItem);
		// ItemizedIconOverlay<OverlayItem> mFriendsLocationOverlay = new
		// ItemizedIconOverlay<OverlayItem>(
		// this, mItems, null);
		// mMapView.getOverlays().add(mFriendsLocationOverlay);

		mGpxTrack = new GpxTrack(this, mHandler);

		mPathOverlay = new PathOverlay(Color.CYAN, this);
		mMapView.getOverlays().add(mPathOverlay);

		mLocationOverlay = new CustomMyLocationOverlay(this, mMapView, this);
		mLocationOverlay.setLocationUpdateMinDistance(5);
		mLocationOverlay.setLocationUpdateMinTime(500);
		mLocationOverlay.setDrawAccuracyEnabled(true);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.disableFollowLocation();
		mLocationOverlay.enableCompass();
		mLocationOverlay.setEnabled(true);
		mMapView.getOverlays().add(mLocationOverlay);

		// ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
		// mMapView.getOverlays().add(scaleBarOverlay);

		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onDestroy() {
		SharedPreferences lastSession = getSharedPreferences(SESSION_PREFS,
				MODE_PRIVATE);
		Editor editor = lastSession.edit();
		editor.putInt(ZOOM, mMapView.getZoomLevel());
		IGeoPoint mapCenter = mMapView.getMapCenter();
		editor.putInt(LATITUDE, mapCenter.getLatitudeE6());
		editor.putInt(LONGITUDE, mapCenter.getLongitudeE6());
		editor.commit();

		mLocationOverlay.disableCompass();
		mLocationOverlay.disableFollowLocation();
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.setEnabled(false);
		mPathOverlay.clearPath();
		mGpxTrack.clear();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onNewTrackPoint(Location location) {
		MyGeoPoint trackPoint = new MyGeoPoint(location);
		mGpxTrack.add(trackPoint);
		mPathOverlay.addPoint(trackPoint);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_my_location:
			// mMapController.zoomInFixing(mLocationOverlay.getMyLocation());
			// mMapController.animateTo(mLocationOverlay.getMyLocation());
			mLocationOverlay.enableFollowLocation();
			return true;
		case R.id.action_save_track:
			mGpxTrack.saveGpxTrack(getStorageDirectory());
			return true;
		case R.id.action_layers:
			// mMapView.setTileSource(TileSourceFactory.MAPNIK);
			return true;
		case R.id.action_my_friends:
			// mMapView.setTileSource(TileSourceFactory.CYCLEMAP);
			return true;
		case R.id.action_settings:
			showPreferences();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void showPreferences() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsActivity.PREF_ONLINE_MAPS)) {
			mMapView.setUseDataConnection(sharedPreferences.getBoolean(
					SettingsActivity.PREF_ONLINE_MAPS, false));
		}
	}

	public File getStorageDirectory() {
		String externalStorage = Environment.getExternalStorageDirectory()
				.getPath();
		File appDirectory = new File(externalStorage, getResources().getString(
				R.string.app_name));
		if (!appDirectory.exists()) {
			appDirectory.mkdir();
		}

		return appDirectory;
	}

}
