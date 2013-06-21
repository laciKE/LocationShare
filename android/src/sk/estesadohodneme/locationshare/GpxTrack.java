package sk.estesadohodneme.locationshare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import org.osmdroid.contributor.util.RecordedGeoPoint;
import org.osmdroid.contributor.util.RecordedRouteGPXFormatter;

import android.content.Context;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class GpxTrack {
	private Context mContext;
	private Handler mHandler;
	private ArrayList<RecordedGeoPoint> mTrackPoints;

	public GpxTrack(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mTrackPoints = new ArrayList<RecordedGeoPoint>();
	}

	public void add(MyGeoPoint wp) {
		mTrackPoints.add(wp);
	}

	public RecordedGeoPoint getLastPoint() {
		return mTrackPoints.get(mTrackPoints.size() - 1);
	}

	public void clear() {
		mTrackPoints.clear();
	}

	public void saveGpxTrack(final File directory) {
		final int pointsCount = mTrackPoints.size();

		if (pointsCount == 0) {
			Toast.makeText(mContext,
					"No GPX trackpoints have been recorded yet.",
					Toast.LENGTH_LONG).show();
			return;
		}
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
		TimeZone defaultTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		final String gpxLog = RecordedRouteGPXFormatter.create(mTrackPoints);
		TimeZone.setDefault(defaultTimeZone);
		Locale.setDefault(defaultLocale);

		Thread writerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Time time = new Time();
				time.setToNow();
				String fileName = "track_" + time.format2445() + ".gpx";
				File logFile = new File(directory, fileName);
				final String filePath = logFile.getAbsolutePath();

				try {
					PrintWriter out = new PrintWriter(new FileOutputStream(
							logFile), true);
					out.print(gpxLog);
					out.println();
					out.flush();
					out.close();
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(
									mContext,
									pointsCount + " GPX trackpoints saved to "
											+ filePath, Toast.LENGTH_LONG)
									.show();
						}
					});
				} catch (FileNotFoundException e) {
					Log.e("GPX_WRITER", e.getMessage());
					e.printStackTrace();

					final String errorMessage = e.getMessage();
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(
									mContext,
									"Error while saving GPX log: "
											+ errorMessage, Toast.LENGTH_LONG)
									.show();
						}
					});
				}
			}
		});

		writerThread.start();
	}
}
