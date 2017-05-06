package org.mcjug.aameetingmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.mcjug.aameetingmanager.meeting.FindMeetingFragment;

import static android.support.v4.app.ActivityCompat.requestPermissions;

public class LocationFinder {
	private static final String TAG = LocationFinder.class.getSimpleName();

	private static final int LOCATION_TIMEOUT = 1000 * 30;
	private static final int MIN_UPDATE_TIME = 1000 * 5;
	private static final float MIN_UPDATE_DISTANCE = 50f;
	public static final int LOCATION_FINDER_REQUEST = 2;


	private LocationManager locationManager;
	private LocationListener locationListener;
	private LocationTimeoutTask locationTimeoutTask;
	private LocationResult locationResult;
	private Location networkOrPassiveLocation;

	private Context context;
	private Handler handler;

	public LocationFinder(Context context, LocationResult locationResult) {
		this.locationResult = locationResult;
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public boolean requestLocation() {
		handler = new Handler();
		locationTimeoutTask = new LocationTimeoutTask();
		handler.postDelayed(locationTimeoutTask, LOCATION_TIMEOUT);

		locationListener = new LocationUpdater();
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			return requestLocationUpdates(LocationManager.GPS_PROVIDER, locationListener);
		}

		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			return requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationListener);
		}

		if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
			return requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, locationListener);
		}
		return false;
	}

	private boolean requestLocationUpdates(String providerName, LocationListener locationListener) {
		try {
			if (ActivityCompat.checkSelfPermission(context,
					Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
					ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				if(context instanceof Activity) {
					requestPermissions((Activity) context, FindMeetingFragment.LOCATION_FINDER_PERMS, LOCATION_FINDER_REQUEST);
				}
			}
			locationManager.requestLocationUpdates(providerName, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, locationListener);
			return true;
		} catch (Exception ex) {
			Log.d(TAG, "Error requesting location for provider: " + providerName);
		}
		return false;
	}



	public static abstract class LocationResult {
		public abstract void setLocation(Location location);
	}

	private class LocationUpdater implements LocationListener {
		public void onLocationChanged(Location location) {
			String provider = location.getProvider();

			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				handler.removeCallbacks(locationTimeoutTask);
				if (ActivityCompat.checkSelfPermission(context,
						Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
						ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				locationManager.removeUpdates(locationListener);
				if (locationResult != null) {
					locationResult.setLocation(location);
				}
			} else {
				networkOrPassiveLocation = location;
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	private class LocationTimeoutTask implements Runnable {
		public void run() {
			try {
				if (ActivityCompat.checkSelfPermission(context,
						Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
						ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
					return;
				}
				locationManager.removeUpdates(locationListener);
				if (locationResult != null) {
					locationResult.setLocation(networkOrPassiveLocation);
				}
			} catch (Throwable e) {
			}
		}
	}



}