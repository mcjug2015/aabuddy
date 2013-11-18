package org.mcjug;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;

public class GeoMeetingLatLonValidator {
	private static final String QUERY_STRING = "http://open.mapquestapi.com/nominatim/v1/reverse.php?format=xml&lat=LATITUDE&lon=LONGITUDE&zoom=18&addressdetails=1";

	public static String getHttpQueryResponse(String urlString) {
		String httpQueryResponse = null;
		
		try {
		    URL url = new URL(urlString);

		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    String str;
		    while ((str = in.readLine()) != null) {
		        httpQueryResponse += str;
		    }
		    in.close();
		}
		catch (Exception e) {
			httpQueryResponse = null;
			e.printStackTrace();
		}
		
		return httpQueryResponse;
	}
	
	public static String getStateFromLatLon(double latitude, double longitude) {
		String state = "Unknown";
		
		String queryString = QUERY_STRING.replaceFirst("LATITUDE", Double.toString(latitude));
		queryString = queryString.replaceFirst("LONGITUDE", Double.toString(longitude));
		String xmlResponse = getHttpQueryResponse(queryString);
		
		if (xmlResponse == null) {
			System.out.println("\r\n*** ERROR: Unable to get a response from: " + queryString);
		}
		else {
			int stateTagBeginIndex = xmlResponse.indexOf("<state>");
			int stateTagEndIndex = xmlResponse.indexOf("</state>");
			if (stateTagBeginIndex < 0) {
				System.out.println("Unable to find the <state> tag in the XML response:");
				System.out.println(xmlResponse);
			}
			else if ((stateTagEndIndex < 0) || (stateTagEndIndex < stateTagBeginIndex)) {
				System.out.println("Unable to find the </state> tag in the XML response:");
				System.out.println(xmlResponse);
			}
			else {
				state = xmlResponse.substring(stateTagBeginIndex+7, stateTagEndIndex);
			}
		}
		
		return state;
	}
	
	public static void validateGeoMeetingFiles(String abbreviation, String fullName) {
		String geoMeetingInputDirectory = System.getProperty("user.dir") + File.separator + "geooutput" + File.separator;
		String geoMeetingOutputDirectory = System.getProperty("user.dir") + File.separator + "geooutputvalidated" + File.separator;
		
		try {
			String geoMeetingInputFile = geoMeetingInputDirectory + abbreviation + "_GeoMeetings.psv";
			String geoMeetingOutputFile = geoMeetingOutputDirectory + abbreviation + "_GeoMeetings.psv";
			String geoMeetingErrorFile = geoMeetingOutputDirectory + abbreviation + "_GeoMeetings_ERRORS.psv";
		    BufferedReader reader = new BufferedReader(new FileReader(geoMeetingInputFile));
		    BufferedWriter writer = new BufferedWriter(new FileWriter(geoMeetingOutputFile));
		    BufferedWriter writerErrors = new BufferedWriter(new FileWriter(geoMeetingErrorFile));
		    String str;
		    int i = 0;
		    while ((str = reader.readLine()) != null) {
		        String[] fields = str.split("\\|");
		        if (fields.length < 8) {
		        	System.out.println("ERROR: In file " + geoMeetingInputFile + " the below line has the wrong number of fields:\r\n" + str);
		        }
		        else {
		        	double latitude = Double.parseDouble(fields[6]);
		        	double longitude = Double.parseDouble(fields[7]);
		        	String state = getStateFromLatLon(latitude, longitude);
		        	if (!state.equals(fullName)) {
		        		writerErrors.write(state + ": " + str + "\r\n");
		        		System.out.println("\r\nstate=" + state);
		        	}
		        	else 
		        		writer.write(str + "\r\n");
		        }
		        if ((i % 10) == 0)
		        	System.out.print(i + " ");
		        i++;
		    }
		    System.out.println(" ");
		    reader.close();
		    writer.close();
		    writerErrors.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		int stateIndex = 0;
		validateGeoMeetingFiles(STATE_ABBREVIATIONS[stateIndex], STATE_NAMES[stateIndex]);
	}

	public static final String[] STATE_ABBREVIATIONS = new String[] {
		"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
		"HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
		"MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
		"NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
		"SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
	};

	public static final String[] STATE_NAMES = new String[] {
		"Alabama", "Alaska", "Arizona", "Arkansas", "California",
		"Colorado", "Connecticut", "Deleware", "Florida", "Georgia",
		"Hawaii", "Idaho", "Illinois", "Indiana", "Iowa",
		"Kansas", "Kentucky", "Lousianna", "Maine", "Maryland",
		"Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri",
		"Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey",
		"New Mexico", "New York", "North Carolina", "North Dakota", "Ohio",
		"Oklahoma", "Oregan", "Pennsylvania", "Rhode Island", "South Carolina",
		"South Dakota", "Tennessee", "Texas", "Utah", "Vermont",
		"Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"
	};
}
