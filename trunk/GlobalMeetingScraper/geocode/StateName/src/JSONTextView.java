import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONTextView {
	
	public static void main(String[] args) {
		
		BufferedReader in = null;
		BufferedWriter textWriter = null;
		try {
			in = new BufferedReader(new FileReader("Addresses_json.txt"));
			textWriter = new BufferedWriter(new FileWriter("Addresses_json_text_view.txt"));
			
			int from = 1;
			int begin = 0;
			int end = 0;
			int idx = 0;
			
			String oneLine = null;
			while (((oneLine = in.readLine()) != null)) {
				textWriter.write("[\r\n");
				
				boolean next = true;
				while (next) {
					idx = oneLine.indexOf("\"", from);
					begin = idx;
					idx = oneLine.indexOf("\",", idx + 1);
					if (idx != -1) {
						end = idx + 2;
						textWriter.write(oneLine.substring(begin, end) + "\r\n");
						
						from = end;
					} else {
						textWriter.write(oneLine.substring(begin, oneLine.length() - 1) + "\r\n");
						textWriter.write("]\r\n");
						next = false;
					}
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
				textWriter.close();
			}
			catch (IOException e) {
			}
		}
	}
}
