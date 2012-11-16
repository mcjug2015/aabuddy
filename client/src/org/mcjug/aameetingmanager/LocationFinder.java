package org.mcjug.aameetingmanager;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LocationFinder {
    private static final String TAG = LocationFinder.class.getSimpleName();

	private static final int LOCATION_TIMEOUT = 1000 * 60;
	private static final int MIN_UPDATE_TIME = 1000 * 5;
	private static final float MIN_UPDATE_DISTANCE = 50f;

	private LocationManager locationManager;
	private LocationListener locationListener;
	private LocationTimeoutTask locationTimeoutTask;
	private LocationResult locationResult;
	private Location networkOrPassiveLocation; 
	
	public LocationFinder(Context context, LocationResult locationResult) {
		this.locationResult = locationResult;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
	}
	
	public void requestLocation() {		
		Handler handler = new Handler();
		locationTimeoutTask = new LocationTimeoutTask();		 
		handler.postDelayed(locationTimeoutTask, LOCATION_TIMEOUT);

		locationListener = new LocationUpdater();
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			requestLocationUpdates(LocationManager.GPS_PROVIDER, locationListener);
		}
		
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationListener);
		}
		
		if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
			requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, locationListener);
		}		
	}	
	
	private void requestLocationUpdates(String providerName, LocationListener locationListener) {
		try {
			locationManager.requestLocationUpdates(providerName, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, locationListener);
		} catch (Exception ex) {
		    Log.d(TAG, "Error requesting location for provider: " + providerName);
		}
	}
	
	public static abstract class LocationResult {
		public abstract void setLocation(Location location);
	}
	
	private class LocationUpdater implements LocationListener {
		public void onLocationChanged(Location location) {
			String provider = location.getProvider();
			if (provider.equals(LocationManager.GPS_PROVIDER)) {
				locationManager.removeUpdates(locationListener);
				locationResult.setLocation(location);
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
				locationManager.removeUpdates(locationListener);
				locationResult.setLocation(networkOrPassiveLocation);
			} catch (Throwable e) {
			}
		}
	}
}