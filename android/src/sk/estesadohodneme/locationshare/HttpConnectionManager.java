package sk.estesadohodneme.locationshare;

/**
* HttpConnectionManager for sending http requests to web server with livetracker. 
* Location parameters (latitude, longitude, altitude, timestamp, speed, number of satellites) 
* will be GET parameters. URL can be set in application settings (in the similar way as in the Osmand). 
* Also http requests could have optional description as the POST parameter.
* HttpConnectionManager will provide interface for set URL of the livetracker 
* (Background service obtains URL from settings and sets it in HttpConnectionManager) 
* and for sending location updates (with optional description). 
* In case of network unavailability, HttpConnectionManager will sends update as soon as possible 
* (broadcast intents?). Also, all the http requests could not block the current thread, 
* so they could be managed in another thread.
*/

import java.net.*;
import java.io.*;

public class HttpConnectionManager {
	private static final String mServer = "http://fks.sk/~filip/livetracker/";
	// "http://nodla.wz.sk/livetracker/";
	private static final String mServerGet  = mServer + "getPosition.php";
	private static final String mServerPost = mServer + "updatePosition.php";
	private static final String mSplit = "#";
	
    public static MyGeoPoint getFriendPosition(int friendID) throws Exception {   
    	int lat=0,lon=0,alt=0;
    	long time=0;
    	
        URL server = new URL(mServerGet);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(server.openStream()));

        //read
        in.close();
        
    	return new MyGeoPoint(lat,lon,alt,time,0);
    } 
    
    public static void submitMyPosition(MyGeoPoint gp) throws Exception {
    	
    } 
}