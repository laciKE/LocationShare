package sk.estesadohodneme.locationshare;

import java.util.ArrayList;

import android.location.Location;

public class SharedLocationStorage {
	protected static volatile SharedLocationStorage mInstance = null;
	protected ArrayList<Location> mLocations = null;

	protected SharedLocationStorage() {
		if (mLocations == null) {
			mLocations = new ArrayList<Location>();
		}
	}

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

	public void add(Location location) {
		synchronized (SharedLocationStorage.class) {
			mLocations.add(location);
		}
	}

	public Location get(int index) {
		Location location = null;
		synchronized (SharedLocationStorage.class) {
			location = new Location(mLocations.get(index));
		}
		return location;
	}

	public Location getLast() {
		Location location = null;
		synchronized (SharedLocationStorage.class) {
			location = new Location(mLocations.get(mLocations.size() - 1));
		}
		return location;
	}

	public int size() {
		synchronized (SharedLocationStorage.class) {
			return mLocations.size();
		}
	}

	public void clear() {
		synchronized (SharedLocationStorage.class) {
			mLocations.clear();
		}
	}

}
