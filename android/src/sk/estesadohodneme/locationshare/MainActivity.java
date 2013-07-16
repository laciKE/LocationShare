package sk.estesadohodneme.locationshare;

import java.io.File;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity implements
		OnSharedPreferenceChangeListener {
	private static final String SESSION_PREFS = "SessionsFile";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String ZOOM = "zoom";

	protected Handler mHandler = new Handler();
	private MapController mMapController;
	private MapView mMapView;
	private Overlays mOverlays;
	private IGeoPoint mMapCenter;
	private int mMapZoom;

	private boolean mRequestExit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent intent = new Intent(this, LocationService.class);
		intent.putExtra(LocationService.COMMAND, LocationService.COMMAND_START);
		startService(intent);

		mMapView = (MapView) findViewById(R.id.mapview);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mMapView.setUseDataConnection(sharedPreferences.getBoolean(
				SettingsActivity.PREF_ONLINE_MAPS, false));

		String tileSourceName = sharedPreferences.getString(
				SettingsActivity.PREF_TILE_SOURCE, this.getResources()
						.getString(R.string.osm_mapnik));
		mMapView.setTileSource(MyTileSourceFactory.createTileSource(
				tileSourceName, this.getResources()));

		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);
		mMapController = mMapView.getController();

		mOverlays = new Overlays(this, mMapView, sharedPreferences);

		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		SharedPreferences lastSession = getSharedPreferences(SESSION_PREFS,
				MODE_PRIVATE);
		mMapCenter = new GeoPoint(lastSession.getInt(LATITUDE, 48158210),
				lastSession.getInt(LONGITUDE, 17083310));
		mMapZoom = lastSession.getInt(ZOOM, 14);
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapController.setZoom(mMapZoom);
		mMapController.setCenter(mMapCenter);

		mOverlays.onResume();
	}

	@Override
	public void onPause() {
		mMapCenter = mMapView.getMapCenter();
		mMapZoom = mMapView.getZoomLevel();
		mOverlays.onPause();
		super.onPause();
	}

	@Override
	public void onStop() {
		SharedPreferences lastSession = getSharedPreferences(SESSION_PREFS,
				MODE_PRIVATE);
		Editor editor = lastSession.edit();
		editor.putInt(ZOOM, mMapZoom);
		editor.putInt(LATITUDE, mMapCenter.getLatitudeE6());
		editor.putInt(LONGITUDE, mMapCenter.getLongitudeE6());
		editor.commit();

		if (mRequestExit) {
			// stop LocationService
			Intent intent = new Intent(this, LocationService.class);
			intent.putExtra(LocationService.COMMAND,
					LocationService.COMMAND_STOP);
			startService(intent);
		}
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_my_location:
			// mMapController.zoomInFixing(mLocationOverlay.getMyLocation());
			// mMapController.animateTo(mLocationOverlay.getMyLocation());
			mOverlays.enableFollowLocation();
			return true;
		case R.id.action_save_track:
			GpxLogWriter.saveGpxLog(this, mHandler, getStorageDirectory());
			return true;
		case R.id.action_clear_track:
			mOverlays.clearPath();
			Toast.makeText(this, R.string.track_cleared, Toast.LENGTH_SHORT)
					.show();
			return true;
		case R.id.action_layers:
			mOverlays.showSelectionDialog();
			return true;
			// case R.id.action_my_friends:
			// return true;
		case R.id.action_settings:
			showPreferences();
			return true;
		case R.id.action_exit:
			mRequestExit = true;
			finish();
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
		if (key.equals(SettingsActivity.PREF_TILE_SOURCE)) {
			String newValue = sharedPreferences.getString(
					SettingsActivity.PREF_TILE_SOURCE, this.getResources()
							.getString(R.string.osm_mapnik));
			mMapView.setTileSource(MyTileSourceFactory.createTileSource(
					newValue, this.getResources()));
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
