package sk.estesadohodneme.locationshare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

/**
 * Writes GPX logs to file.
 */
public class GpxLogWriter {

	/**
	 * Writes GPX log {@link String} with trackpoints from
	 * {@link SharedLocationStorage} to file at given directory. Also displays
	 * {@link ProgressDialog} and status {@link Toast} when finishes.
	 * 
	 * @param context
	 *            application {@link Context}
	 * @param handler
	 *            {@link Handler} for controlling {@link ProgressDialog} and
	 *            showing {@link Toast} messages.
	 * @param directory
	 *            location for writing GPX log
	 */
	public static void saveGpxLog(final Context context, final Handler handler,
			final File directory) {
		SharedLocationStorage sharedLocationStorage = SharedLocationStorage
				.getInstance();
		final int pointsCount = sharedLocationStorage.size();

		if (pointsCount == 0) {
			Toast.makeText(context,
					"No GPX trackpoints have been recorded yet.",
					Toast.LENGTH_LONG).show();
			return;
		}

		final ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();

		Thread writerThread = new Thread(new Runnable() {

			@SuppressLint("ShowToast")
			@Override
			public void run() {
				List<Location> trackPoints = new GpxLogWriter.LocationList();
				String gpxLog = GpxFormatter.createGpxLog(trackPoints);

				Time time = new Time();
				time.setToNow();
				String fileName = "track_" + time.format2445() + ".gpx";
				File logFile = new File(directory, fileName);
				String filePath = logFile.getAbsolutePath();

				boolean success = true;
				String message = pointsCount + " GPX trackpoints saved to "
						+ filePath;
				try {
					PrintWriter out = new PrintWriter(new FileOutputStream(
							logFile), true);
					out.print(gpxLog);
					out.println();
					out.flush();
					out.close();
				} catch (FileNotFoundException e) {
					success = false;
					Log.e("GPX_WRITER", e.getMessage());
					message = "Error while saving GPX log: " + e.getMessage();
					e.printStackTrace();
				} finally {
					final boolean finalSuccess = success;
					final String toastMessage = message;
					handler.post(new Runnable() {
						@Override
						public void run() {
							progressDialog.dismiss();
							if (finalSuccess) {
								Toast.makeText(context, toastMessage,
										Toast.LENGTH_LONG).show();
							} else {

							}
						}
					});
				}
			}
		});

		writerThread.start();
	}

	/**
	 * Delegates some {@link List} methods to {@link SharedLocationStorage} and
	 * enables using of {@link SharedLocationStorage} as a input to
	 * {@link GpxFormatter}.
	 */
	public static class LocationList implements List<Location> {
		protected SharedLocationStorage mSharedLocationStorage;

		/**
		 * Creates delegate for {@link SharedLocationStorage}.
		 */
		public LocationList() {
			mSharedLocationStorage = SharedLocationStorage.getInstance();
		}

		/**
		 * Adds the specified object at the end of {@link SharedLocationStorage}
		 * .
		 * 
		 * @param location
		 *            the {@link Location} object to add
		 * @return always true
		 */
		@Override
		public boolean add(Location location) {
			mSharedLocationStorage.add(location);
			return true;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public void add(int location, Location object) {
		}

		/**
		 * Not implemented.
		 */
		@Override
		public boolean addAll(Collection<? extends Location> arg0) {
			return false;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public boolean addAll(int arg0, Collection<? extends Location> arg1) {
			return false;
		}

		/**
		 * Removes all elements from {@link SharedLocationStorage}, leaving its
		 * empty.
		 */
		@Override
		public void clear() {
			mSharedLocationStorage.clear();
		}

		/**
		 * Not implemented.
		 */
		@Override
		public boolean contains(Object object) {
			return false;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public boolean containsAll(Collection<?> arg0) {
			return false;
		}

		/**
		 * Returns the element at the specified location in
		 * {@link SharedLocationStorage}.
		 * 
		 * @param index
		 *            the index of the element to return
		 * @return the element at the specified index
		 */
		@Override
		public Location get(int index) {
			return mSharedLocationStorage.get(index);
		}

		/**
		 * Not implemented.
		 */
		@Override
		public int indexOf(Object object) {
			return 0;
		}

		/**
		 * Returns whether {@link SharedLocationStorage} contains no elements.
		 * 
		 * @return true if {@link SharedLocationStorage} has no elements, false
		 *         otherwise
		 */
		@Override
		public boolean isEmpty() {
			return mSharedLocationStorage.size() == 0;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public Iterator<Location> iterator() {
			return null;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public int lastIndexOf(Object object) {
			return 0;
		}

		@Override
		public ListIterator<Location> listIterator() {
			return null;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public ListIterator<Location> listIterator(int location) {
			return null;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public Location remove(int location) {
			return null;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public boolean remove(Object object) {
			return false;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public boolean removeAll(Collection<?> arg0) {
			return false;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public boolean retainAll(Collection<?> arg0) {
			return false;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public Location set(int location, Location object) {
			return null;
		}

		/**
		 * Returns the number of elements in {@link SharedLocationStorage}.
		 * 
		 * @return the number of elements in {@link SharedLocationStorage}
		 */
		@Override
		public int size() {
			return mSharedLocationStorage.size();
		}

		/**
		 * Not implemented.
		 */
		@Override
		public List<Location> subList(int start, int end) {
			return null;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public Object[] toArray() {
			return null;
		}

		/**
		 * Not implemented.
		 */
		@Override
		public <T> T[] toArray(T[] array) {
			return null;
		}
	}
}
