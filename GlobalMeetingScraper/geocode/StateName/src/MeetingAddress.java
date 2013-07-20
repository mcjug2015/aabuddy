import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MeetingAddress {

	public static void main(String[] args) {

		BufferedReader in = null;
		BufferedWriter writer = null;
		try {
			in = new BufferedReader(new FileReader("Meetings.txt"));
			writer = new BufferedWriter(new FileWriter("MeetingAddresses.txt"));
			
			String oneLine;
			String address;
			while (((oneLine = in.readLine()) != null)) {
				address = oneLine.substring(oneLine.lastIndexOf('|') + 1);
				
				writer.write(address + "\n");
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
				writer.close();
			}
			catch (IOException e) {
			}
		}
	}
}
