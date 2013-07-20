import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GeocodeResult {

	public static void main(String[] args) {

		BufferedReader in = null;
		BufferedWriter writer = null;
		try {
			in = new BufferedReader(new FileReader("Addresses_output.txt"));
			
			String filename = "Addresses_out.txt";			
			writer = new BufferedWriter(new FileWriter(filename));
			
			String oneLine;
			while (((oneLine = in.readLine()) != null)) {

				if (oneLine.startsWith("Addresses_")) {
					writer.close();
					
					//oneLine is a file name. Either "Addresses_fail.txt" or "Addresses_debug.txt"
					filename = oneLine;
					writer = new BufferedWriter(new FileWriter(filename));
				} else {
					writer.write(oneLine + "\n");
				}
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
