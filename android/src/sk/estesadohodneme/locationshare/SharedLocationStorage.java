package sk.estesadohodneme.locationshare;

import java.util.ArrayList;

import android.app.Service;
import android.location.Location;

/**
 * Thread-safe storage for sharing {@link Location} objects between
 * {@link MainActivity}, {@link Service} and other components. Implementation by
 * Singleton design pattern with lazy initialization.
 */
public class SharedLocationStorage {
	protected static volatile SharedLocationStorage mInstance = null;
	protected ArrayList<Location> mLocations = null;

	/**
	 * Protected constructor of this singleton.
	 */
	protected SharedLocationStorage() {
		if (mLocations == null) {
			mLocations = new ArrayList<Location>();
		}
	}

	/**
	 * Method for accessing this singleton instance.
	 * 
	 * @return the instance of {@link SharedLocationStorage}
	 */
	public static SharedLocationStorage getInstance() {
		if (mInstance == null) {
			synchronized (SharedLocationStorage.class) {
				if (mInstance == null) {
					mInstance = new SharedLocationStorage();
				}
			}
		}
		return mInstance;
	}

	/**
	 * Adds the specified object at the end of this ArrayList.
	 * 
	 * @param location
	 *            the {@link Location} object to add
	 */
	public void add(Location location) {
		synchronized (SharedLocationStorage.class) {
			mLocations.add(location);
		}
	}

	/**
	 * Returns the element at the specified location in this storage.
	 * 
	 * @param index
	 *            the index of the element to return
	 * @return the element at the specified index
	 */
	public Location get(int index) {
		Location location = null;
		synchronized (SharedLocationStorage.class) {
			location = new Location(mLocations.get(index));
		}
		return location;
	}

	/**
	 * Returns the last element in this storage.
	 * 
	 * @return the last element
	 */
	public Location getLast() {
		Location location = null;
		synchronized (SharedLocationStorage.class) {
			location = new Location(mLocations.get(mLocations.size() - 1));
		}
		return location;
	}

	/**
	 * Returns the number of elements in this storage.
	 * 
	 * @return the number of elements in this storage
	 */
	public int size() {
		synchronized (SharedLocationStorage.class) {
			return mLocations.size();
		}
	}

	/**
	 * Removes all elements from this storage, leaving it empty.
	 */
	public void clear() {
		synchronized (SharedLocationStorage.class) {
			mLocations.clear();
		}
	}

}
