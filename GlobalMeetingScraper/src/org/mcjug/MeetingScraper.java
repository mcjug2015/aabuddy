package org.mcjug;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;


public class MeetingScraper extends JFrame {
	public static final String BASE_URL = "http://meetings.intherooms.com";
	/*
	public static final String[] STATES = new String[] {
		"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
		"HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
		"MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
		"NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
		"SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
	};
	*/
	public static final String[] STATES = new String[] {
		// "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA"
		"HI"
	};
	private static final int FRAME_WIDTH = 750;
	private static final int FRAME_HEIGHT = 500;
	
	private JButton exitButton = new JButton("Exit");
	private JButton parseButton = new JButton("Parse");
	private JButton selectInputDirectoryButton = new JButton("...");
	private JButton selectOutputDirectoryButton = new JButton("...");
	private JTextField inputDirectoryTextField = new JTextField();
	private JTextField outputDirectoryTextField = new JTextField();
	private JTextArea statusTextArea = new JTextArea();
		

	public MeetingScraper() {
		super("HTML Meeting Scraper");
		initGui();
	}
	
	protected void initGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		GUI.centerComponent(this);
		JComponent contentPane = (JComponent) getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(GUI.DEFAULT_BORDER, GUI.DEFAULT_BORDER, GUI.DEFAULT_BORDER, GUI.DEFAULT_BORDER));
		contentPane.setLayout(new BorderLayout());
		contentPane.add(getInputPanel(), BorderLayout.NORTH);
		contentPane.add(getOutputPanel(), BorderLayout.CENTER);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
	}
	
	protected JPanel getInputPanel() {
		String currentWorkingDirectory = System.getProperty("user.dir") + File.separator;
		inputDirectoryTextField.setEditable(false);
		inputDirectoryTextField.setText(currentWorkingDirectory + "inputdata");
		selectInputDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				selectDirectory(inputDirectoryTextField);
			}
		});
		outputDirectoryTextField.setEditable(false);
		outputDirectoryTextField.setText(currentWorkingDirectory + "outputdata");
		selectOutputDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				selectDirectory(outputDirectoryTextField);
			}
		});
		
		JPanel inputPanel = new JPanel(new GridBagLayout());
		inputPanel.add(new GUI.LabeledSeparator("Data Directories"), GUI.gbc(0, 0, 3));
		inputPanel.add(new JLabel("Input Data Directory:"), GUI.gbc(0, 1));
		inputPanel.add(inputDirectoryTextField, GUI.gbc(1, 1, 15.0));
		inputPanel.add(selectInputDirectoryButton, GUI.gbc(2, 1));
		inputPanel.add(new JLabel("Output Data Directory:"), GUI.gbc(0, 2));
		inputPanel.add(outputDirectoryTextField, GUI.gbc(1, 2, 15.0));
		inputPanel.add(selectOutputDirectoryButton, GUI.gbc(2, 2));
		return inputPanel;
	}
	
	protected JPanel getOutputPanel() {
		statusTextArea.setEditable(false);
		JPanel outputPanel = new JPanel(new GridBagLayout());
		outputPanel.setBorder(BorderFactory.createEmptyBorder(GUI.DEFAULT_BORDER, 0, GUI.DEFAULT_BORDER, 0));
		outputPanel.add(new GUI.LabeledSeparator("Parsing Status"), GUI.gbc(0, 0, 1));
		outputPanel.add(new JScrollPane(statusTextArea), GUI.gbc(0, 1, 1, 1.0, 10.0, GUI.BOTH));
		return outputPanel;
	}
	
	protected JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.add(new GUI.LabeledSeparator(), GUI.gbc(0, 0, 5));
		buttonPanel.add(GUI.createSpacerPanel(GUI.DEFAULT_BORDER, 1), GUI.gbc(0, 1));
		parseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				enableButtons(false);
				statusTextArea.setText("");
				ParseDataFilesTask task = new ParseDataFilesTask(
						statusTextArea, inputDirectoryTextField.getText(),
						outputDirectoryTextField.getText());
				task.execute();
			}
		});
		buttonPanel.add(parseButton, GUI.gbc(1, 1));
		buttonPanel.add(GUI.createSpacerPanel(GUI.DEFAULT_BORDER, 1), GUI.gbc(2, 1));
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				System.exit(0);
			}
		});
		buttonPanel.add(exitButton, GUI.gbc(3, 1));
		return buttonPanel;
	}
	
	protected String selectDirectory(JTextField directoryTextField) {
		String directory = directoryTextField.getText();
		JFileChooser fileChooser = new JFileChooser(directory);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			directory = fileChooser.getSelectedFile().getAbsolutePath();
			directoryTextField.setText(directory);
		}
		return directory;
	}
	
	protected void enableButtons(boolean enable) {
		selectInputDirectoryButton.setEnabled(enable);
		selectOutputDirectoryButton.setEnabled(enable);
		parseButton.setEnabled(enable);
		exitButton.setEnabled(enable);
	}

	class ParseDataFilesTask extends SwingWorker<Boolean, String> {
		private JTextArea textArea = null;
		private String inputDirectoryName = null;
		private String outputDirectoryName = null;
		
		public ParseDataFilesTask(JTextArea theTextArea, String theInputDirectoryName, String theOutputDirectoryName) {
			textArea = theTextArea;
			inputDirectoryName = theInputDirectoryName;
			outputDirectoryName = theOutputDirectoryName;
		}

		@Override
		public Boolean doInBackground() {
			Boolean successful = true;
			Properties defaultRegionProperties = null;
			try {
				File inputDirectory = new File(inputDirectoryName);
				if (!inputDirectory.exists()) {
					publish("ERROR: The input directory '" + inputDirectoryName + "' does not exist");
					successful = false;
				}
				File outputDirectory = new File(outputDirectoryName);
				if (successful && !outputDirectory.exists()) {
					publish("WARNING: The output directory '" + outputDirectoryName + "' does not exist. Creating the directory");
					outputDirectory.mkdirs();
				}
				if (successful) {
					defaultRegionProperties = getDefaultRegionProperties();
					if (defaultRegionProperties == null) {
						publish("ERROR: Unable to read default region Properties file from the input directory: " + inputDirectoryName);
						successful = false;
					}
				}
				if (successful) {
					boolean append = false;	// the first time the file is opened, create a new one
					for (int stateIndex = 0; stateIndex < STATES.length; stateIndex++) {
		    			List<Meeting> meetingList = getMeetingListForRegion(STATES[stateIndex], defaultRegionProperties);
		    			String outputFileName = outputDirectoryName + File.separator + "Meetings_" + STATES[stateIndex] + ".psv";
		    			if (stateIndex == 1)	// after the initial opening of the file, append (only needs to be set once)
		    				append = true;
		    			exportMeetingList(meetingList, outputFileName, append);
					}
				}
			}
			catch (Exception e) {
				publish("*** FATAL ERROR OCCURRED ***");
				publish(e.getMessage());
				e.printStackTrace();
				successful = false;
			}
			return successful;
		}
				
		/**
		 * Return a List of Meeting objects for the given region properties.
		 * @param regionProperties a Properties class containing parameters for that region
		 * @return List of Meeting objects
		 */
		protected List<Meeting> getMeetingListForRegion(String region, Properties defaultRegionProperties) {
			List<Meeting> meetingList = null;
			if (defaultRegionProperties != null) {
				try {
					publish("Downloading and parsing data for region: " + region);
					meetingList = HttpScraperUtil.scrapeRegion(BASE_URL, region, defaultRegionProperties, outputDirectoryName);
				}
				catch (IllegalArgumentException iae) {
					publish(iae.getMessage());
				}
				catch (IOException ioe) {
					publish(ioe.getMessage());
				}
			}
			return meetingList;
		}

		/**
		 * Returns a list of default region properties. In addition to those
		 * properties found in this file, added to them (or filled in from
		 * the state region files) will be latitude, longitude, proximity. 
		 */
		protected Properties getDefaultRegionProperties() {
			Properties defaultRegionProperties = null;
			try {
				String propertiesFileName = inputDirectoryName + File.separator + "default_region.properties";
				defaultRegionProperties = new Properties();
				defaultRegionProperties.load(new FileInputStream(propertiesFileName));
			}
			catch (Exception e) {
				e.printStackTrace();
				defaultRegionProperties = null;
			}
			return defaultRegionProperties;
		}

		/**
		 * Write the list of meetings to an output file.
		 * @param meetingList a List of Meeting objects
		 * @param outputFileName the file where to export the list of meeting objects
		 * @param append whether or not to append to the file
		 */
		protected void exportMeetingList(List<Meeting> meetingList, String outputFileName, boolean append) {
			if ((meetingList != null) && (!meetingList.isEmpty())) {
				publish("Exporting a meeting list of size " + meetingList.size());
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, append));
					for (int i = 0; i < meetingList.size(); i++) {
						Meeting meeting = meetingList.get(i);
						writer.write(meeting.toSeparatedString() + "\n");
					}
					writer.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		protected void process(List<String> chunks) {
			for (String status : chunks) {
				textArea.append(status + "\n");
			}
		}
		
		@Override
		protected void done() {
			enableButtons(true);
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			MeetingScraper app = new MeetingScraper();
			app.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
