package sk.estesadohodneme.locationshare;

import org.osmdroid.contributor.util.RecordedGeoPoint;

import android.location.Location;

public class MyGeoPoint extends RecordedGeoPoint {

	private static final long serialVersionUID = 1L;

	public MyGeoPoint(int latitudeE6, int longitudeE6) {
		super(latitudeE6, longitudeE6);
	}

	public MyGeoPoint(int latitudeE6, int longitudeE6, long aTimeStamp,
			int aNumSatellites) {
		super(latitudeE6, longitudeE6, aTimeStamp, aNumSatellites);
	}

	public MyGeoPoint(int latitudeE6, int longitudeE6, int aAltitude,
			long aTimeStamp, int aNumSatellites) {
		super(latitudeE6, longitudeE6, aTimeStamp, aNumSatellites);
		this.setAltitude(aAltitude);
	}

	public MyGeoPoint(int latitudeE6, int longitudeE6, int aAltitude,
			long aTimeStamp) {
		super(latitudeE6, longitudeE6, aTimeStamp, 0);
		this.setAltitude(aAltitude);
	}

	public MyGeoPoint(Location aLocation) {
		super((int) (aLocation.getLatitude() * 1E6), (int) (aLocation
				.getLongitude() * 1E6), aLocation.getTime(), (int) aLocation
				.getAltitude());
	}
	
}
