package sk.estesadohodneme.locationshare;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	public static final String PREF_ONLINE_MAPS = "pref_online_maps";
	public static final String PREF_TILE_SOURCE = "pref_tile_source";

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
