package sk.estesadohodneme.locationshare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.location.Location;
import android.util.Log;

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
public class HttpConnectionManager {
	private Queue<Message> mQueue;
	private String mUrl;
	private String mDefaultUrl = "http://example.com/tracker.php?lat={lat}&lon={lon}&alt={alt}&speed={speed}&timestamp={time}";

	private static final String mServer = "http://fks.sk/~filip/livetracker/";
			//"http://nodla.wz.sk/livetracker/";
	private static final String mDefaultSubmitPage = mServer+"updateMyPosition.php";
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

			private String doubleToStr(double val) {
				return String.format(Locale.US, "%.6f", val);
			}
			
			private boolean POSTlocationMessage(Message message) {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(mDefaultSubmitPage);

				Location location = message.getLocation();

				// Request parameters and other properties.
				List<NameValuePair> params = new ArrayList<NameValuePair>(2);
				params.add(new BasicNameValuePair("user", mUser));
				params.add(new BasicNameValuePair("lat", doubleToStr(location.getLatitude())));
				params.add(new BasicNameValuePair("lon", doubleToStr(location.getLongitude())));
				params.add(new BasicNameValuePair("timestamp", Long.toString(location.getTime())));
				if (location.hasAltitude()) {
					params.add(new BasicNameValuePair("altitude", doubleToStr(location.getAltitude())));
				}
				
				try {
					httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
					//Execute and get the response.
					HttpResponse response = httpclient.execute(httppost);
					if (response.getStatusLine().getStatusCode() == 200) {
						Log.i("HttpConnectionManager","location updated on server");
						return true;
					}
					else {
						Log.i("HttpConnectionManager",response.getStatusLine().getReasonPhrase()+" "+response.getStatusLine().getStatusCode());
					}
				}
				catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
			
			private String buildGetUrl(Location location) {
				String url = mUrl;
				url = url.replace("{lat}", doubleToStr(location.getLatitude()));
				url = url.replace("{lon}", doubleToStr(location.getLongitude()));
				url = url.replace("{time}", Long.toString(location.getTime()));
				if (location.hasAltitude()) {
					url = url.replace("{alt}", doubleToStr(location.getAltitude()));
				}
				else {
					url = url.replace("{alt}", "0");
				}
				if (location.hasSpeed()) {
					url = url.replace("{speed}", doubleToStr(location.getSpeed()));
				}
				else {
					url = url.replace("{speed}", "0");
				}
				url = url.replace("{nick}", mUser);
				Log.i("HttpConnectionManager","GET url: "+url);
				return url;
			}
			
			private boolean GETlocationMessage(Message message) {
				HttpClient Client = new DefaultHttpClient();
				String url = buildGetUrl(message.getLocation());

				try {
					HttpGet httpget = new HttpGet(url);
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					String response;
					response = Client.execute(httpget, responseHandler);
					Log.i("HttpConnection", "response: " + response);
					return true;
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
			
			@Override
			public void run() {
				Log.d("HTTP run","x"+mQueue.isEmpty());
				synchronized (mQueue) {
					while (!mQueue.isEmpty()) {
						synchronized (this) {
							// TODO mUrl							
						}
						boolean success = false;
						if (mUrl.equals(mDefaultUrl)) {
							success = POSTlocationMessage(mQueue.peek());
						}
						else {
							success = GETlocationMessage(mQueue.peek());
						}
							
						if (success) {
							mQueue.poll();
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
