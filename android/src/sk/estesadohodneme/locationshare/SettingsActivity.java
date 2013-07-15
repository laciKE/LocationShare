package sk.estesadohodneme.locationshare;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Activity for manage application settings.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Keys for accessing settings items.
	 */
	public static final String PREF_ONLINE_MAPS = "pref_online_maps";
	public static final String PREF_TILE_SOURCE = "pref_tile_source";
	public static final String PREF_NETWORK_PROVIDER = "pref_network_provider";
	public static final String PREF_GPS_PROVIDER = "pref_gps_provider";
	public static final String PREF_UPDATE_FOREGROUND = "pref_update_foreground";
	public static final String PREF_UPDATE_BACKGROUND = "pref_update_background";
	public static final String PREF_LIVETRACKING = "pref_livetracking";
	public static final String PREF_LIVETRACKING_URL = "pref_livetracking_url";
	public static final String PREF_UPDATE_LIVETRACKING = "pref_update_livetracking";

	/**
	 * Creates preferences from xml resource at /res/xml/settings.xml.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
