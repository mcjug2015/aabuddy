import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;


public class GeoMeetings {
	
	public static TreeMap<String, String> createLatLonMap () {
		
		TreeMap<String, String> latlonMap = new TreeMap<String, String>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader("Addresses_output.txt"));
			
			String oneLine;
			String address;
			String latLon;
			int idx = 0;
			while (((oneLine = in.readLine()) != null)) {
				idx = oneLine.indexOf('|');
				address = oneLine.substring(0, idx);
				latLon = oneLine.substring(idx + 1);
				
				latlonMap.put(address, latLon);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {
			}
		}
		
		return latlonMap;
	}
	
	//Original Meetings*.psv does not contain latitude and longitude.
	//If this is the case, those addresses having valid latitude and longitude will 
	//be saved to file "GeoMeetings.psv" along with corresponded latitude and longitude.
	//Other addresses will be saved to file "GeoMeetingsFail.psv".
	//If Meetings*.psv already has latitude and longitude, we will replace their values 
	//only for those addresses existing file "Addresses_output.txt".
	public static void populateLatLon(TreeMap<String, String> latLonMap) {
		
		BufferedReader in = null;
		BufferedWriter outGeo = null;
		BufferedWriter outGeoFail = null;
		BufferedWriter outSummary = null;
		try {
			in = new BufferedReader(new FileReader("Meetings.psv"));
			outGeo = new BufferedWriter(new FileWriter("GeoMeetings.psv"));
			
			String oneLine = null;
			String address = null;
			String latLon = null;
			boolean isFirstLine = true;
			boolean needToReplaceLatLon = false;
			int idx = 0;
			int idx6th = 0;
			int n = 0;
			int cntGeoMeeting = 0;
			int cntGeoMeetingFail = 0;
			while (((oneLine = in.readLine()) != null)) {
				//find the position of the fifth "|"
				n = 5;
				idx = -1;
				do {
					idx = oneLine.indexOf('|', idx + 1);
				} while (--n > 0  && idx != -1);
				
				//Determine if we want to populate new latitude and longitude 
				//or to replace latitude and longitude
				if (isFirstLine) {
					//Determine if file Meetings.psv contains latitude and longitude already.
					//if the sixth "!" exists, latitude and longitude are existed.
					if ((idx6th = oneLine.indexOf('|', idx + 1)) != -1) {
						needToReplaceLatLon = true;
					}
					else {	//populate new latitude and longitude
						outGeoFail = new BufferedWriter(new FileWriter("GeoMeetingsFail.psv"));
					}
					
					isFirstLine = false;
				}
				
				if (needToReplaceLatLon) {
					idx6th = oneLine.indexOf('|', idx + 1);
					address = oneLine.substring(idx + 1, idx6th);
					latLon = latLonMap.get(address);
					if (latLon != null) {
						cntGeoMeeting++;
						oneLine = oneLine.substring(0, idx6th + 1) + latLon;
					}
					
					outGeo.write(oneLine + "\n");
				} 
				else {	//populate new latitude and longitude
					address = oneLine.substring(idx + 1);
					latLon = latLonMap.get(address);
					if (latLon != null) {
						cntGeoMeeting++;
						oneLine = oneLine + "|" + latLon;
						
						outGeo.write(oneLine + "\n");
					}
					else {
						cntGeoMeetingFail++;
						outGeoFail.write(oneLine + "\n");
					}
				}
			}
			
			outSummary = new BufferedWriter(new FileWriter("Summary.txt"));
			int cntTreeMapSize = latLonMap.size();
			outSummary.write("Among files Addresses_output_nn.txt, there are " + cntTreeMapSize + " distinct addresses which have valid latitude and longitude.\r\n");
			outSummary.write("\r\n\r\n");
			
			if (needToReplaceLatLon) {
				outSummary.write("There are " + cntGeoMeeting + " meetings UPDATING latitude and longitude.\r\n");
			}
			else {
				outSummary.write("There are " + (cntGeoMeeting + cntGeoMeetingFail) + " meetings total.\r\n");
				outSummary.write("There are " + cntGeoMeeting + " meetings having valid latitude and longitude.\r\n");
				outSummary.write("There are " + cntGeoMeetingFail + " meetings NOT having valid latitude and longitude.\r\n");
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {
			}
			
			try {
				outGeo.close();
			}
			catch (IOException e) {
			}
			
			if (outGeoFail != null) {
				try {
					outGeo.close();
				}
				catch (IOException e) {
				}
			}
			
			try {
				outSummary.close();
			}
			catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) {
		
		TreeMap<String, String> latLonMap = createLatLonMap();
		populateLatLon(latLonMap);
	}
}
