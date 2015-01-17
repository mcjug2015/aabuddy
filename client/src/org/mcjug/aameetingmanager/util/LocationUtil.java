package org.mcjug.aameetingmanager.util;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationUtil {
    private static final String TAG = LocationUtil.class.getSimpleName();

	public static Location getLastKnownLocation(Context context) {
		Location location = null;
        try {
        	Location gpsLocation = null;
    		Location networkLocation = null;
    		
    		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    			gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		}
    		
      		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
      			networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    		}
      		
      		if (gpsLocation != null && networkLocation != null) {
      			long timeDiff = Math.abs(gpsLocation.getTime() - networkLocation.getTime());
      			if (timeDiff < 60000 * 10) {
      				location = gpsLocation;
      			} else {
      				location = networkLocation;
      			}
      		} else if (gpsLocation != null) {
      			location = gpsLocation;
      		} else {
      			location = networkLocation;
      		}
    		
		} catch (Exception e) {
		    Log.d(TAG, "Error getting location");
		}
        return location;
	}
	
	public static String getFullAddress(Location location, Context context) {
		String addressStr = "";
		if (location != null) {
			try {
				Geocoder gc = new Geocoder(context, Locale.getDefault());
				List<Address> addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				StringBuilder sb = new StringBuilder();
				if (addresses.size() > 0) {
					Address address = addresses.get(0);
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
						sb.append(address.getAddressLine(i)).append("\n");
					}
				}

				int idx = sb.lastIndexOf("\n");
				if (idx != -1) {
					sb.replace(idx, idx + 1, "");
				}
				addressStr = sb.toString();
			} catch (Exception e) {
			    Log.d(TAG, "Error getting address");
			}
		}
		return addressStr;
	}
	
	public static String getShortAddress(Location location, Context context) {
		String addressStr = "";
		if (location != null) {
			try {
				Geocoder gc = new Geocoder(context, Locale.getDefault());
				List<Address> addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (addresses.size() > 0) {
					Address address = addresses.get(0);
					addressStr = address.getLocality() + "," + address.getAdminArea() + " " + address.getPostalCode();
				}
			} catch (Exception e) {
				Log.d(TAG, "Error getting address");
			}
		}
		return addressStr;
	}
	
	public static boolean validateAddress(String addressName, Context context) {
		boolean isValid = false;
        try {
			Geocoder gc = new Geocoder(context, Locale.getDefault());
			List<Address> address = gc.getFromLocationName(addressName, 1);
			if (address != null && address.size() > 0) {
            	isValid = true;
            } 
		} catch (Exception e) {
		    Log.d(TAG, "Error validating address");
		}
        return isValid;
	}
	
	public static Address getAddressFromLocationName(String addressName, Context context) throws Exception{
		Address location = null;
		try {
			Geocoder gc = new Geocoder(context, Locale.getDefault());
			List<Address> address = gc.getFromLocationName(addressName, 1);
			if (address != null && address.size() > 0) {
				location = address.get(0);
			}
		} catch (Exception e) {
		    Log.d(TAG, "Error getting address");
		}
		
		/*********** If does not work - imitate **************/
		if (location == null) {
			 location = new Address(Locale.getDefault());
			 location.setLatitude(38.9867417000000032);
			 location.setLongitude(-77.1008364999999998);
		}
		
		return location;
	}	
}
