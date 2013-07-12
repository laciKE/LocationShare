package sk.estesadohodneme.locationshare;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

import android.R.string;
import android.content.res.Resources;

/**
 * Factory for creating various tile sources.
 */
public class MyTileSourceFactory {
	/**
	 * IDs of the string resources displayed in settings.
	 */
	private static int TILE_SOURCE_IDs[] = { R.string.freemap_hiking,
			R.string.freemap_cyclemap, R.string.freemap_caratlas,
			R.string.map1eu, R.string.osm_mapnik, R.string.osm_mapquest,
			R.string.osm_cyclemap };

	/**
	 * Creates {@link ITileSource} for given string identifier of the source.
	 * 
	 * @param tileSourceName
	 *            {@link String} identifier of desired tile source. String is
	 *            one of the string values from settings.
	 * @param resources
	 *            application resources
	 * @return {@link ITileSource} for given {@link String} identifier
	 */
	public static ITileSource createTileSource(String tileSourceName,
			Resources resources) {
		int id = 0;
		while (!tileSourceName.equals(resources.getString(TILE_SOURCE_IDs[id]))
				&& (id < TILE_SOURCE_IDs.length)) {
			id++;
		}
		if (id >= TILE_SOURCE_IDs.length) {
			id = 4;
		}
		return createTileSource(TILE_SOURCE_IDs[id]);
	}

	/**
	 * Creates {@link ITileSource} for given {@link Integer} identifier of the
	 * source.
	 * 
	 * @param tileSourceId
	 *            ID of the {@link string} resource with tile source's name
	 * @return {@link ITileSource} for given identifier
	 */
	public static ITileSource createTileSource(int tileSourceId) {
		ITileSource tileSource = null;
		switch (tileSourceId) {
		case R.string.freemap_hiking:
			tileSource = new XYTileSource("FreemapSlovakiaHiking",
					ResourceProxy.string.offline_mode, 6, 16, 256, ".png",
					"http://tiles.freemap.sk/T/");

			break;
		case R.string.freemap_cyclemap:
			tileSource = new XYTileSource("FreemapSlovakiaCycleMap",
					ResourceProxy.string.offline_mode, 6, 16, 256, ".png",
					"http://tiles.freemap.sk/C/");
			break;
		case R.string.freemap_caratlas:
			tileSource = new XYTileSource("FreemapSlovakiaCarAtlas",
					ResourceProxy.string.offline_mode, 6, 16, 256, ".png",
					"http://tiles.freemap.sk/A/");
			break;
		case R.string.map1eu:
			tileSource = new XYTileSource("Map1.eu",
					ResourceProxy.string.offline_mode, 6, 16, 256, ".jpg",
					"http://alpha.map1.eu/tiles/");
			break;
		case R.string.osm_mapnik:
			tileSource = TileSourceFactory.MAPNIK;
			break;
		case R.string.osm_mapquest:
			tileSource = TileSourceFactory.MAPQUESTOSM;
			break;
		case R.string.osm_cyclemap:
			tileSource = TileSourceFactory.CYCLEMAP;
			break;
		default:
			tileSource = TileSourceFactory.MAPNIK;
			break;
		}

		return tileSource;
	}
}
