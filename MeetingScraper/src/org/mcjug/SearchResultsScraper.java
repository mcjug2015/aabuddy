package org.mcjug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


public class SearchResultsScraper {
	
	/**
	 * Scrape the specified file for a list of meetings
	 * @param inputFile the file to scrape
	 * @return an ArrayList of Meeting objects
	 */
	public static List<Meeting> scrapeFile(File inputFile) {
		List<Meeting> meetingList = new ArrayList<Meeting>();
		if (inputFile != null) {
			String meetingsXmlFragment = extractMeetingsResultsListFromFile(inputFile);
			if ((meetingsXmlFragment != null) && (!meetingsXmlFragment.isEmpty())) {
				meetingList = extractMeetingsFromXmlFragment(meetingsXmlFragment);
			}
		}
		return meetingList;
	}
	
	/**
	 * Scrape the file for the ordered list of results and extract into
	 * an XML fragment String.
	 * 
	 * @param inputFile the entire search results file
	 * @return an XML fragment String containing the list of meetings.
	 */
	private static String extractMeetingsResultsListFromFile(File inputFile) {
		boolean startTagFound = false;
		boolean endTagFound = false;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String oneLine;
			while ((!endTagFound) && ((oneLine = in.readLine()) != null)) {
				if (startTagFound) {
					int endTagIndex = oneLine.indexOf("/ol>");
					if (endTagIndex >= 0) {
						stringBuilder.append(oneLine.substring(0, endTagIndex+4));
						endTagFound = true;
					}
					else {
						stringBuilder.append(oneLine);
					}
				}
				else {
					int startTagIndex = oneLine.indexOf("<ol");
					if (startTagIndex >= 0) {
						stringBuilder.append(oneLine.substring(startTagIndex));
						startTagFound = true;
					}
				}
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!startTagFound)
			System.out.println("\nERROR: Unable to find start tag <ol> in file: " + inputFile.getName());
		if (!endTagFound)
			System.out.println("\nERROR: Unable to find end tag </ol> in file: " + inputFile.getName());
		
		return stringBuilder.toString();
	}

	/**
	 * From an ordered list XML fragment, extract all the meetings
	 * @param xmlFragment containing an ordered list of meeting search results
	 * @return a List of Meeting objects
	 */
	private static List<Meeting> extractMeetingsFromXmlFragment(String xmlFragment) {
		ArrayList<Meeting> meetingList = new ArrayList<Meeting>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Map<String, XPathExpression> xPathMap = getXPathExpressionMap();
			int endTagIndex = xmlFragment.indexOf("</ol>");
			int listItemStartIndex = xmlFragment.indexOf("<li");
			int listItemEndIndex = xmlFragment.indexOf("/li>", listItemStartIndex);
			while ((listItemStartIndex < endTagIndex) && (listItemEndIndex < endTagIndex)) {
				// Parse one meeting's fields
				String oneMeetingXmlFragment = xmlFragment.substring(listItemStartIndex, listItemEndIndex+4);
				Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(oneMeetingXmlFragment)));
				Element rootNode = doc.getDocumentElement();
				
				String name = xPathMap.get("name").evaluate(rootNode);
				String dayTime = xPathMap.get("dayTime").evaluate(rootNode);
				String extraInfo = xPathMap.get("extraInfo").evaluate(rootNode);
				String building = xPathMap.get("building").evaluate(rootNode);
				String streetAddress = xPathMap.get("streetAddress").evaluate(rootNode);
				String city = xPathMap.get("city").evaluate(rootNode);
				String state = xPathMap.get("state").evaluate(rootNode);
				String postalCode = xPathMap.get("postalCode").evaluate(rootNode);
				String specialDirections = xPathMap.get("specialDirections").evaluate(rootNode);
				Meeting meeting = new Meeting(name, dayTime, extraInfo, building, streetAddress,
												city, state, postalCode, specialDirections);
				meetingList.add(meeting);
				
				listItemStartIndex = xmlFragment.indexOf("<li", listItemEndIndex);
				if (listItemStartIndex >= 0) {
					listItemEndIndex = xmlFragment.indexOf("/li>", listItemStartIndex);
				}
				else {
					listItemStartIndex = endTagIndex + 1;
					listItemEndIndex = listItemStartIndex;
				}
			}
			
		}
		catch (Exception e) {
			System.out.println("\nERROR: Unable to parse the XML fragment:\n" + xmlFragment);
			e.printStackTrace();
		}
		return meetingList;
	}

	/**
	 * Retrieve the XPathExpression list from the xpath.properties file
	 * @return a Map containing all the XPathExpressions used to scrape the XML output
	 */
	private static Map<String, XPathExpression> getXPathExpressionMap() {
		final String xPathPropertiesFileName = System.getProperty("user.dir") + File.separator + "xpath.properties";
		HashMap<String, XPathExpression> xPathExpressions = new HashMap<String, XPathExpression>();
		try {
			Properties xPathProperties = new Properties();
			xPathProperties.load(new FileInputStream(xPathPropertiesFileName));
			Iterator<String> propertyNameIterator = xPathProperties.stringPropertyNames().iterator();
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();
			while (propertyNameIterator.hasNext()) {
				String propertyName = propertyNameIterator.next();
				XPathExpression expression = xpath.compile(xPathProperties.getProperty(propertyName));
				xPathExpressions.put(propertyName, expression);
			}
		}
		catch (Exception e) {
			System.out.println("\nERROR: Unable to load XPathExpressions from file: " + xPathPropertiesFileName);
			e.printStackTrace();
		}
		return xPathExpressions;
	}
	
}
