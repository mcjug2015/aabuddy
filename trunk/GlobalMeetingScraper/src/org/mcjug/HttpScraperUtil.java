package org.mcjug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.io.FileUtils.copyURLToFile;


public class HttpScraperUtil {
	public static final String CHARSET = "UTF-8";
	public static final String QUOTE = "\"";
	public static final String MEETING_SECTION_START = "<table class=" + QUOTE + "all-meetings" + QUOTE + ">";
	public static final String MEETING_SECTION_END = "</table>";
	
	/**
	 * Cannot construct
	 */
	private HttpScraperUtil() {}
	
	/**
	 * Create a List of Meeting objects that are retrieved from the given URL
	 * @param baseUrl the base URL to retrieve the meeting data from
	 * @param defaultRegionProperties a Properties object containing query parameters to append to the URL
	 * @param outputDirectoryName directory in which to store output files
	 * @return a List of Meeting objects
	 * @throws IllegalArgumentException
	 */
	public static List<Meeting> scrapeRegion(String baseUrl, String region, Properties defaultRegionProperties, String outputDirectoryName)
		throws IllegalArgumentException, MalformedURLException, IOException, NumberFormatException
	{
		List<Meeting> meetingList = new ArrayList<Meeting>();
		
		if (baseUrl == null)
			throw new IllegalArgumentException("The base URL passed to method HttpScraperUtil.scrapeRegion() must be non-null");
		else if (defaultRegionProperties == null)
			throw new IllegalArgumentException("The region properties passed to method HttpScraperUtil.scrapeRegion() must be non-null");
		else {
			// retrieve the first introductory page for the meetings in this region
			String baseRegionFileName = outputDirectoryName + File.separator + "region_" + region + ".html";
			URL queryUrl = new URL(baseUrl + "/meetings/aa/" + region);
			System.out.println("\r\n" +baseUrl + "/meetings/aa/" + region);
			File firstPageOutputFile = new File(baseRegionFileName);
			copyURLToFile(queryUrl, firstPageOutputFile);
			
			// extract necessary paging parameters from the home page for this region
			String parametersString = getPagingParameters(firstPageOutputFile);
			int lastEqualsIndex = 1;
			int lastPage = 1;
			if (parametersString != null) {
				lastEqualsIndex = parametersString.lastIndexOf("=");
				lastPage = Integer.parseInt(parametersString.substring(lastEqualsIndex+1));
			}
			int page;
			
			// download all pages from this region
			for (page = 1; page <= lastPage; page++) {
				System.out.println("Downloading page " + page + " of " + lastPage);
				queryUrl = new URL(baseUrl + parametersString.replaceAll("page="+lastPage, "page="+page));
				String regionFileName = outputDirectoryName + File.separator + "region_" + region + "_" + page + ".html";
				copyURLToFile(queryUrl, new File(regionFileName));
			}

			// Go through all the downloaded pages for this region, extract the
			// meeting data and form the meeting list.
			for (page = 1; page <= lastPage; page++) {
				boolean endOfMeetingTable = false;
				String regionFileName = outputDirectoryName + File.separator + "region_" + region + "_" + page + ".html";
				System.out.println("Parsing meeting file " + page + " of " + lastPage);
				BufferedReader in = new BufferedReader(new FileReader(regionFileName));
			    String oneLine;
			    skipLinesUntil(MEETING_SECTION_START, in);
	    		// skip over table header of results
			    endOfMeetingTable = !skipLinesUntil("</tr>", in);
			    while (!endOfMeetingTable) {
			    	endOfMeetingTable = !skipLinesUntil("<tr>", in);
			    	if (!endOfMeetingTable) {
			    		Meeting meeting = readOneMeeting(in, region);
			    		meetingList.add(meeting);
			    	}
			    }
			    in.close();
			}
		}
		return meetingList;
	}
	
	/**
	 * Read the fields from the specified BufferedReader and
	 * create one Meeting object for that record.
	 * @param in the BufferedReader to read the file lines from
	 * @param region defalt region (state)
	 * @return a new Meeting
	 */
	public static Meeting readOneMeeting(BufferedReader in, String region) throws IOException {
		Meeting meeting = null;
	    String tmpStr;
	    int tmpIndex;
		String oneLine = "";
		boolean meetingRecordComplete = false;
		StringBuilder meetingStringBuilder = new StringBuilder();
	    while ((oneLine != null) && (!meetingRecordComplete)) {
	    	oneLine = in.readLine();
	    	if ((oneLine != null) && (!oneLine.trim().isEmpty())) {
		    	int recordEndIndex = oneLine.indexOf("</tr>");
		    	if (recordEndIndex >= 0) {
		    		meetingRecordComplete = true;
		    		if (recordEndIndex > 0)
		    			meetingStringBuilder.append(oneLine.substring(0, recordEndIndex));
		    	}
		    	else {
		    		meetingStringBuilder.append(oneLine.trim());
		    	}
	    	}
	    }
	    String[] meetingFields = meetingStringBuilder.toString().trim().split("</td>");
	    
	    tmpIndex = meetingFields[0].lastIndexOf("<");
	    tmpStr = meetingFields[0].substring(0, tmpIndex);
	    String name = tmpStr.substring(tmpStr.lastIndexOf(">")+1);

	    tmpIndex = meetingFields[1].lastIndexOf("<");
	    tmpStr = meetingFields[1].substring(0, tmpIndex);
	    String building = tmpStr.substring(tmpStr.lastIndexOf(">")+1);

	    tmpIndex = meetingFields[2].indexOf(">")+1;
	    tmpStr = meetingFields[2].substring(tmpIndex);
	    tmpStr = tmpStr.replaceAll("<br>", ", ");
	    tmpStr = tmpStr.replaceAll("<BR>", ", ");
	    tmpIndex = tmpStr.lastIndexOf(",");
	    String zipCode = "";
	    int zipIndex = tmpStr.lastIndexOf(" ");
	    if (zipIndex > tmpIndex)
	    	zipCode = " " + tmpStr.substring(zipIndex+1).trim();
	    String address = tmpStr.substring(0, tmpIndex) + ", " + region + zipCode;

	    tmpIndex = meetingFields[3].indexOf(">")+1;
	    String dayOfWeek = meetingFields[3].substring(tmpIndex).trim();
	    
	    tmpIndex = meetingFields[4].indexOf(">")+1;
	    String startTime = meetingFields[4].substring(tmpIndex).trim();
	    
	    meeting = new Meeting(name, dayOfWeek, startTime, building, address);
	    return meeting;
	}
	
	/**
	 * Skip through lines in the file (specified by BufferedReader in)
	 * until a specific search string is found.
	 * 
	 * @param searchString the String to search for
	 * @param in the BufferedReader for reading the file
	 * @return a boolean indicating if the search string was found
	 *         (or false if the end of file was reached; shouldn't happen)
	 * @throws IOException
	 */
	public static boolean skipLinesUntil(String searchString, BufferedReader in) throws IOException {
		String oneLine;
		boolean eof = false;	// end of file
		boolean eot = false;	// end of table
		boolean searchStringFound = false;
		while (!eof && !eot && !searchStringFound) {
			oneLine = in.readLine();
			if (oneLine == null)
				eof = true;
			else if (oneLine.trim().toLowerCase().contains(MEETING_SECTION_END))
				eot = true;
			else if (oneLine.trim().toLowerCase().contains(searchString))
				searchStringFound = true;
		}
	    return searchStringFound;
	}
	
	/**
	 * Get the paging parameters String for this file in order to cycle
	 * through all the result pages for this region.
	 * 
	 * @param firstPageFile File containing results of first search page
	 * @return a parameters String to be used for paging 
	 */
	public static String getPagingParameters(File firstPageFile) {
		String pagingParameters = null;
		if (firstPageFile != null) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(firstPageFile));
			    String oneLine;
			    while ((oneLine = in.readLine()) != null) {
			    	String lowerCaseLine = oneLine.trim().toLowerCase();
			    	if (lowerCaseLine.startsWith("<div class=") && (lowerCaseLine.indexOf("search-paging") > 0)) {
			    		int nextLinkIndex = oneLine.lastIndexOf("/meetings/search");
			    		if (nextLinkIndex > 0) {
			    			int lastPageLinkIndex = oneLine.substring(0, nextLinkIndex).lastIndexOf("/meetings/search");
			    			if (lastPageLinkIndex > 0) {
				    			int closingQuoteIndex = oneLine.indexOf(QUOTE, lastPageLinkIndex);
			    				if (closingQuoteIndex > lastPageLinkIndex) {
			    					pagingParameters = oneLine.substring(lastPageLinkIndex, closingQuoteIndex);
			    				}
			    			}
			    		}
			    	}
			    }
			    in.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				pagingParameters = null;
			}
		}
		return pagingParameters;
	}
	
	/**
	 * Create a String of URL query parameters from names/values in a Properties class
	 * @param regionProperties the Properties class containing the query parameters
	 * @return a String containing URL query parameters to be added to the end of a URL
	 * @throws IllegalArgumentException
	 */
	public static String createRegionQueryParameters(Properties regionProperties)
		throws IllegalArgumentException
	{
		StringBuilder queryParametersBuilder = new StringBuilder();
		Iterator<String> iterator = regionProperties.stringPropertyNames().iterator();
		while (iterator.hasNext()) {
			String paramName = iterator.next();
			String paramValue = regionProperties.getProperty(paramName);
			try {
				queryParametersBuilder.append(String.format("%s=%s&",
						URLEncoder.encode(paramName, CHARSET),
						URLEncoder.encode(paramValue, CHARSET)));
			}
			catch (UnsupportedEncodingException uee) {
				throw new IllegalArgumentException("The parameters passed to URLEncoder.encode() in method HttpScraperUtil.createRegionQueryParameters() are invalid");
			}
		}
		String queryParameters = queryParametersBuilder.toString();
		int qpLength = queryParameters.length();
		return queryParameters.substring(0, qpLength);	// truncate off the last ampersand
	}
}
