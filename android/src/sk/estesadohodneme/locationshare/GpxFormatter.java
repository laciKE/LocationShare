package sk.estesadohodneme.locationshare;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.location.Location;

/**
 * Formats {@link List} of {@link Location} objects as a GPX 1.1 {@link String}.
 */
public class GpxFormatter {
	protected static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" "
			+ "standalone=\"no\"?>\n";
	protected static final String GPX_OPEN = "<gpx version=\"1.1\" "
			+ "creator=\"Location Share\""
			+ " xmlns=\"http://www.topografix.com/GPX/1/1\""
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1"
			+ " http://www.topografix.com/GPX/1/1/gpx.xsd\">\n";
	protected static final String GPX_CLOSE = "</gpx>\n";
	protected static final String TRK_OPEN = "<trk>\n";
	protected static final String TRK_CLOSE = "</trk>\n";
	protected static final String NAME = "\t<name>%s</name>\n";
	protected static final String TRKSEG_OPEN = "\t<trkseg>\n";
	protected static final String TRKSEG_CLOSE = "\t</trkseg>\n";
	protected static final String TRKPT_OPEN = "\t\t<trkpt lat=\"%f\" lon=\"%f\">\n";
	protected static final String TRKPT_CLOSE = "\t\t</trkpt>\n";
	protected static final String TIME = "\t\t\t<time>%s</time>\n";
	protected static final String ELE = "\t\t\t<ele>%s</ele>\n";

	protected static SimpleDateFormat UTCSimpleDateFormat = null;

	/**
	 * Creates {@link String} with GPX log from given {@link Location} objects.
	 * 
	 * @param trackPoints
	 *            {@link List} with trackpoints as a {@link Location} objects
	 * @return GPX log as a {@link String}
	 */
	public static String createGpxLog(List<Location> trackPoints) {
		StringBuilder gpxLog = new StringBuilder();
		gpxLog.append(XML);
		gpxLog.append(GPX_OPEN);
		gpxLog.append(TRK_OPEN);
		String startTime = timestampToUTCString(trackPoints.get(0).getTime());
		String stopTime = timestampToUTCString(trackPoints.get(
				trackPoints.size() - 1).getTime());
		gpxLog.append(String.format(NAME, startTime + " -- " + stopTime));
		gpxLog.append(TRKSEG_OPEN);

		for (int i = 0; i < trackPoints.size(); i++) {
			Location location = trackPoints.get(i);
			gpxLog.append(String.format(Locale.US, TRKPT_OPEN,
					location.getLatitude(), location.getLongitude()));
			gpxLog.append(String.format(Locale.US, TIME,
					timestampToUTCString(location.getTime())));
			if (location.hasAltitude()) {
				gpxLog.append(String.format(Locale.US, ELE,
						location.getAltitude()));
			}

			gpxLog.append(TRKPT_CLOSE);
		}

		gpxLog.append(TRKSEG_CLOSE);
		gpxLog.append(TRK_CLOSE);
		gpxLog.append(GPX_CLOSE);
		return gpxLog.toString();
	}

	/**
	 * Converts given UTC time in milliseconds since January 1, 1970, to UTC
	 * time {@link String} with format yyyy-MM-dd'T'HH:mm:ss'Z'
	 * 
	 * @param milliseconds
	 * @return UTC time {@link String}
	 */
	public static String timestampToUTCString(long milliseconds) {
		if (UTCSimpleDateFormat == null) {
			UTCSimpleDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
			UTCSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		return UTCSimpleDateFormat.format(new Date(milliseconds));
	}
}
