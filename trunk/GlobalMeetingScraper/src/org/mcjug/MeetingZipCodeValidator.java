package org.mcjug;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.TreeSet;

public class MeetingZipCodeValidator {
	private static TreeSet<String> validZipCodes = new TreeSet<String>();

	public static void validateMeetings() {
		for (int i = 0; i < STATE_ABBREVIATIONS.length; i++) {
			validateMeeting(STATE_ABBREVIATIONS[i]);
		}
		System.out.println("\r\nDone validating states");
	}
	
	public static void validateMeeting(String state) {
		try {
			System.out.println("Validating state " + state);
			String inputFileName = INPUT_DIRECTORY_NAME + state + "_GeoMeetings.psv";
			String outputFileName = OUTPUT_DIRECTORY_NAME + state + "_GeoMeetings.psv";
			String errorsFileName = OUTPUT_DIRECTORY_NAME + state + "_GeoMeetings_ERRORS.psv";
			getValidZipCodes(state);
			String oneLine = null;
			BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
			BufferedWriter writerErrors = new BufferedWriter(new FileWriter(errorsFileName));
			while ((oneLine = reader.readLine()) != null) {
				if (oneLine.contains("|")) {
					String[] fields = oneLine.split("\\|");
					String address = fields[5];
					String zipCode = extractZipCodeFromAddress(address.trim());
					if ((zipCode.trim().isEmpty()) || (validZipCodes.contains(zipCode))) {
						writer.write(oneLine + "\r\n");
					}
					else {
						writerErrors.write(oneLine + "\r\n");
					}
				}
			}
			reader.close();
			writer.close();
			writerErrors.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String extractZipCodeFromAddress(String address) {
		String zipCode = "";
		int leastZipCodeIndex = address.length() - 11; // 9 digit zip plus a dash
		int index = address.length() - 1;
		char c = address.charAt(index);
		while ((index >= leastZipCodeIndex) && (c >= '0') && (c <= '9')) {
			index--;
			c = address.charAt(index);
		}
		if ((index >= leastZipCodeIndex) && ((address.length() - index) > 5)) {
			zipCode = address.substring(index+1, index+6);	// only first five digits of zip code
		}
		return zipCode;
	}
	
	public static void getValidZipCodes(String state) {
		validZipCodes.clear();
		try {
			String oneLine = null;
			String databaseFileName = DATABASE_DIRECTORY_NAME + "zip_codes_" + state + ".csv";
			BufferedReader reader = new BufferedReader(new FileReader(databaseFileName));
			while ((oneLine = reader.readLine()) != null) {
				if (oneLine.contains(",")) {
					String[] fields = oneLine.split(",");
					validZipCodes.add(fields[0]);
				}
			}
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("\r\nMeeting Zip Code Validator");
		validateMeetings();
	}

	private static final String[] STATE_ABBREVIATIONS = new String[] {
		"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
		"HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
		"MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
		"NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
		"SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
	};

	private static final String INPUT_DIRECTORY_NAME = System.getProperty("user.dir") + File.separator + "geooutputvalidated" + File.separator;
	private static final String OUTPUT_DIRECTORY_NAME = System.getProperty("user.dir") + File.separator + "zipcodevalidated" + File.separator;
	private static final String DATABASE_DIRECTORY_NAME = System.getProperty("user.dir") + File.separator + "zipcodedb" + File.separator;
}
