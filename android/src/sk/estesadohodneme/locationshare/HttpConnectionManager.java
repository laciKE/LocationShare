package sk.estesadohodneme.locationshare;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.util.Log;

public class HttpConnectionManager {
	private Queue<Message> mQueue;
	private String mUrl;

	public HttpConnectionManager() {
		mQueue = new LinkedList<HttpConnectionManager.Message>();
	}

	public void send(Message message) {
		synchronized (mQueue) {
			mQueue.add(message);
		}
		Thread workerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mQueue) {
					while (!mQueue.isEmpty()) {
						Message message = mQueue.peek();
						HttpClient Client = new DefaultHttpClient();
						synchronized (this) {
							// TODO mUrl							
						}
						String url = "http://nodla.wz.sk/livetracker/tracker.php?user=locationshare";
						Location location = message.getLocation();
						url += "&lat=" + location.getLatitude() + "&lon="
								+ location.getLongitude() + "&timestamp="
								+ location.getTime();
						if (location.hasAltitude()) {
							url += "&altitude=" + location.getAltitude();
						}
						try {
							HttpGet httpget = new HttpGet(url);
							ResponseHandler<String> responseHandler = new BasicResponseHandler();
							String response;
							response = Client.execute(httpget, responseHandler);
							Log.i("HttpConnection", "response: " + response);
							mQueue.poll();
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
			Log.i("LIVETRACK", mUrl);
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
