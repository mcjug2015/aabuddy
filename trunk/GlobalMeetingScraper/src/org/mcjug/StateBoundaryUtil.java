package org.mcjug;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class StateBoundaryUtil {
	private static final String STATE_DB_FILE_NAME = System.getProperty("user.dir") + File.separator +
			"stateboundarydb" + File.separator + "statesboundaries.xml";
	private static Polygon stateBoundary = null;
	
	
	public static void init(String state) {
		if ((state == null) || (state.trim().isEmpty())) {
			System.out.println("\r\nERROR: StateBoundaryUtil.init(): State parameter is null or empty\r\n");
			return;
		}
		
		String fullStateName = state.trim();
		if (fullStateName.length() == 2) {
			fullStateName = convertAbbreviationToFullStateName(state.trim());
		}

		System.out.println("Searching for state " + fullStateName);
		
		try {
			String oneLine = null;
			boolean stateNotFound = true;
			BufferedReader reader = new BufferedReader(new FileReader(STATE_DB_FILE_NAME));
			while ((stateNotFound) && ((oneLine = reader.readLine()) != null)) {
				if (oneLine.contains(fullStateName)) {
					stateNotFound = false;
				}
			}
			if (stateNotFound) {
				System.out.println("\r\nERROR: StateBoundaryUtil.init(): State parameter " + fullStateName + " not found\r\n");
			}
			else {
				stateBoundary = new Polygon();
				boolean stateEndTagNotFound = true;
				while (((oneLine = reader.readLine()) != null) && (stateEndTagNotFound)) {
					if (oneLine.contains("</state>")) {
						stateEndTagNotFound = false;
					}
					else if (oneLine.contains("point")) {
						String[] fields = oneLine.split("\"");
						if (fields.length == 5) {
							int lat = ((int) (Double.parseDouble(fields[1]) * 10000.0));
							int lon = ((int) (Double.parseDouble(fields[3]) * 10000.0));
							stateBoundary.addPoint(lat, lon);
						}
					}
					// else ignore line
				}
			}
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("\r\nERROR: StateBoundaryUtil.init(): State parameter " + fullStateName + " is invalid\r\n");
		}
	}
	
	public static boolean isLocationInState(double lat, double lon) {
		boolean isLocationInState = false;
		int latitude = (int) (lat * 10000.0);
		int longitude = (int) (lon * 10000.0);
		isLocationInState = stateBoundary.contains(latitude, longitude);
		return isLocationInState;
	}

	protected static String convertAbbreviationToFullStateName(String abbreviation) {
		String fullStateName = "";
		boolean abbreviationNotFound = true;
		int i = 0;
		
		if (abbreviation != null) {
			while ((i < STATE_ABBREVIATIONS.length) && (abbreviationNotFound)) {
				if (abbreviation.equalsIgnoreCase(STATE_ABBREVIATIONS[i])) {
					abbreviationNotFound = false;
					fullStateName = STATE_NAMES[i];
				}
				else {
					i++;
				}
			}
		}
		
		return fullStateName;
	}
	
	public static final String[] STATE_NAMES = new String[] {
		"Alabama", "Alaska", "Arizona", "Arkansas", "California",
		"Colorado", "Connecticut", "Delaware", "District of Columbia", "Florida",
		"Georgia", "Hawaii", "Idaho", "Illinois", "Indiana",
		"Iowa", "Kansas", "Kentucky", "Louisiana", "Maine",
		"Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey",
		"New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", 
		"Oklahoma", "Oregon", "Maryland", "Massachusetts", "Michigan", 
		"Minnesota", "Mississippi", "Missouri", "Pennsylvania", "Rhode Island", 
		"South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", 
		"Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"
	};
	
	public static final String[] STATE_ABBREVIATIONS = new String[] {
		"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL",
		"GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME",
		"MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH",
		"OK", "OR", "MD", "MA", "MI", "MN", "MS", "MO", "PA", "RI",
		"SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
	};

}
