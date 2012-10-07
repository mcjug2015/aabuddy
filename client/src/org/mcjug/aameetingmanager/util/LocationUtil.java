package org.mcjug.aameetingmanager.util;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

public class LocationUtil {

	public static String getLastKnownLocation(Context context) {
		String address = "";
        try {
    		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		if (location == null) {
    			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    		}
  			address = getAddress(location, context);
		} catch (Exception e) {
		}
        return address;
	}
	
	public static String getAddress(Location location, Context context) {
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
		}
        return isValid;
	}
	
	public static Address getAddressFromLocationName(String addressName, Context context) throws Exception{
		Address location = null;
		Geocoder gc = new Geocoder(context, Locale.getDefault());
		List<Address> address = gc.getFromLocationName(addressName, 1);
		if (address != null && address.size() > 0) {
			location = address.get(0);
		} 
		return location;
	}	
}
