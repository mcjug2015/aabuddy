package org.mcjug.messagemanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessageService extends IntentService {
	private final String TAG = getClass().getSimpleName();

	public static final String MESSAGE_NOTIFICATION_INTENT_ACTION = "org.mcjug.messageservice.MESSAGE_NOTIFICATION";
	private static final String MESSAGE_URL = "https://mcasg.org/meetingfinder/api/v1/server_message?is_active=true&format=json";

	public MessageService() {
		super("Message Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		retrieveMessage(getApplicationContext());
	}

	private void retrieveMessage(Context context) {
		DefaultHttpClient client = new DefaultHttpClient();

		try {
			HttpGet request = new HttpGet(MESSAGE_URL);
			HttpResponse response = client.execute(request);

			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				String jsonStr = getContent(response);

				if (jsonStr != null) {
					JSONObject jsonObj = new JSONObject(jsonStr);
					JSONArray jsonMessages = jsonObj.getJSONArray("objects");
					if (jsonMessages != null) {
						JSONObject jsonMessage;
						String message;
						for (int i = 0; i < jsonMessages.length(); i++) {
							jsonMessage = jsonMessages.getJSONObject(i);
							message = jsonMessage.getString("message");

							// Broadcast new message
							Intent intent = new Intent(MESSAGE_NOTIFICATION_INTENT_ACTION );
							intent.putExtra("message", message);
							context.sendBroadcast(intent);
						}
					}
				}
			} else {
				Log.d(TAG, "Error retrieving messages: " + statusLine.getStatusCode());
			}

		} catch (Exception e) {
			Log.d(TAG, "Error retrieving messages: " + e);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	public String getContent(HttpResponse httpResponse) throws Exception {
		StringBuilder responseStr = new StringBuilder();
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			InputStreamReader inputStream = new InputStreamReader(entity.getContent());
			try {
				BufferedReader buffer = new BufferedReader(inputStream);
				String line;
				while ((line = buffer.readLine()) != null) {
					responseStr.append(line);
				}
			} finally {
				inputStream.close();
			}
		}

		return responseStr.toString();
	}
}
