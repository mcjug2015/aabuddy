import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;

public class DistinctAddress {
	static final int FILESIZE = 275;
	
	public static void main(String[] args) {
		int seq = 1;
		String jsonFileName = "Addresses_json_" + "%02d" + ".txt";
		
		BufferedReader in = null;
		BufferedWriter jsonWriter = null;
		int cnt = 1;
		try {
			in = new BufferedReader(new FileReader("SortedAddresses.txt"));
			
			String previousAddress = "1@#$";	// impossible address
			String currentAddress = null;
			boolean firstRecord = true;
			JSONArray list = new JSONArray();
			while (((currentAddress = in.readLine()) != null)) {
				if (currentAddress.isEmpty()) {
					continue;
				}
				
				if (!previousAddress.equalsIgnoreCase(currentAddress)) {
					// save a json array to Addresses_json_nn.txt
					if (cnt == 1) {
						if (firstRecord) {
							firstRecord = false;
						} else {
							jsonWriter = new BufferedWriter(new FileWriter(String.format(jsonFileName, seq++)));
							jsonWriter.write(list.toString());
							jsonWriter.flush();
							jsonWriter.close();
							
							list = new JSONArray();
						}
					}
					
					list.add(currentAddress);
					
					previousAddress = currentAddress;
					
					if (cnt == FILESIZE) {
						cnt = 1;
					}
					else {
						cnt++;
					}
				}
			}
			
			jsonWriter = new BufferedWriter(new FileWriter(String.format(jsonFileName, seq)));
			jsonWriter.write(list.toString());
			jsonWriter.flush();
			jsonWriter.close();
			
			in.close();	
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
				jsonWriter.close();
			}
			catch (IOException e) {
			}
		}
	}
}
