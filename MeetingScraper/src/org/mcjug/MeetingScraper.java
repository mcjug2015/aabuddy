package org.mcjug;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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
	private static int FRAME_WIDTH = 750;
	private static int FRAME_HEIGHT = 500;
	
	private JButton exitButton = new JButton("Exit");
	private JButton parseButton = new JButton("Parse");
	private JButton selectInputDirectoryButton = new JButton("...");
	private JButton selectOutputDirectoryButton = new JButton("...");
	private JTextField inputDirectoryTextField = new JTextField();
	private JTextField outputDirectoryTextField = new JTextField();
	private JTextArea statusTextArea = new JTextArea();
		

	public MeetingScraper() {
		super("WAIA Meeting Parser");
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
					publish("Parsing files in the directory: " + inputDirectoryName);
					File[] files = inputDirectory.listFiles();
					List<Meeting> allMeetingsList = new ArrayList<Meeting>();
					if ((files != null) && (files.length > 0)) {
						for (int i = 0; i < files.length; i++) {
							publish("Parsing file #" + (i+1) + ": " + files[i].getName() + "...");
							List<Meeting> meetingList = SearchResultsScraper.scrapeFile(files[i]);
							publish("     " + meetingList.size() + " meetings parsed");
							allMeetingsList.addAll(meetingList);
						}
						exportMeetingList(allMeetingsList);
						successful = true;
					}
					else {
						publish("WARNING: No files were found in the directory: " + inputDirectoryName);
						successful = false;
					}
				}
			}
			catch (Exception e) {
				publish("*** FATAL ERROR OCCURRED ***");
				publish(e.getMessage());
				successful = false;
			}
			return successful;
		}

		/**
		 * Write the list of meetings to an output file.
		 * @param meetingList a List of Meeting objects
		 */
		protected void exportMeetingList(List<Meeting> meetingList) {
			if ((meetingList != null) && (!meetingList.isEmpty())) {
				final String outputFileName = System.getProperty("user.dir") + File.separator +
						"outputdata" + File.separator + "meetings.txt";
				publish("Exporting a meeting list of size " + meetingList.size());
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
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
