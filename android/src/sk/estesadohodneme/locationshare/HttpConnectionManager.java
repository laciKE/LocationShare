package sk.estesadohodneme.locationshare;

/**
* HttpConnectionManager for sending http requests to web server with livetracker. 
* Location parameters (latitude, longitude, altitude, timestamp) will be POST parameters. 
* Also http requests could have optional description as the POST parameter.
* URL can be set in application settings (in the similar way as in the Osmand). 
* 
* HttpConnectionManager will provide interface for set URL of the livetracker 
* (Background service obtains URL from settings and sets it in HttpConnectionManager) 
* and for sending location updates (with optional description). 
* In case of network unavailability, HttpConnectionManager will sends update as soon as possible 
* (broadcast intents?). Also, all the http requests could not block the current thread, 
* so they could be managed in another thread.
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.location.Location;
import android.util.Log;

public class HttpConnectionManager {
	private Queue<Message> mQueue;
	private String mUrl;
	private static final String mServer = "http://fks.sk/~filip/livetracker/";
			//"http://nodla.wz.sk/livetracker/";
	private static final String mSubmitPage = "updateMyPosition.php";
	private static final String mUser = "org";

	public HttpConnectionManager() {
		mQueue = new LinkedList<HttpConnectionManager.Message>();
	}

	public void send(Message message) {
		Log.d("HTTP send","x");
		synchronized (mQueue) {
			mQueue.add(message);
		}
		Thread workerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d("HTTP run","x"+mQueue.isEmpty());
				synchronized (mQueue) {
					while (!mQueue.isEmpty()) {
						Message message = mQueue.peek();
						synchronized (this) {
							// TODO mUrl							
						}
						Log.d("HTTP","run");
						try {
							HttpClient httpclient = new DefaultHttpClient();
							HttpPost httppost = new HttpPost(mServer + mSubmitPage);

							Location location = message.getLocation();
							Log.d("http","location"+location.getLatitude());
							// Request parameters and other properties.
							List<NameValuePair> params = new ArrayList<NameValuePair>(2);
							params.add(new BasicNameValuePair("user", mUser));
							params.add(new BasicNameValuePair("lat", Double.toString(location.getLatitude())));
							params.add(new BasicNameValuePair("lon", Double.toString(location.getLongitude())));
							params.add(new BasicNameValuePair("timestamp", Long.toString(location.getTime())));
							if (location.hasAltitude()) {
								params.add(new BasicNameValuePair("altitude", Double.toString(location.getAltitude())));
							}
							httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

							//Execute and get the response.
							HttpResponse response = httpclient.execute(httppost);
							if (response.getStatusLine().getStatusCode() == 200) {
								Log.d("HttpConnectionManager","location updated on server");
								mQueue.poll();
							}
							else {
								Log.d("HttpConnectionManager",response.getStatusLine().getReasonPhrase()+response.getStatusLine().getStatusCode());
							}

						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		});

		workerThread.start();
	}

	public void send(Location location, String text) {
		send(new Message(location, text));
	}

	public void send(Location location) {
		send(new Message(location));
	}

	public void setUrl(String url) {
		synchronized (this) {
			mUrl = url;
		}
	}

	public class Message {
		private Location mLocation;
		private String mText;

		public Message(Location location, String text) {
			mLocation = location;
			mText = text;
		}

		public Message(Location location) {
			this(location, null);
		}

		public boolean hasText() {
			return (mText != null);
		}

		public String getText() {
			return mText;
		}

		public Location getLocation() {
			return mLocation;
		}
	}
}
