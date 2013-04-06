package org.mcjug;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
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

import static org.apache.commons.io.FileUtils.copyURLToFile;


public class PdfMeetingScraper extends JFrame {
	private static int FRAME_WIDTH = 750;
	private static int FRAME_HEIGHT = 500;
	private static String DEFAULT_URL = "http://aa-dc.org/sites/default/files/WhereAndWhen.1.4.13.pdf";
	private static String DEFAULT_INPUT_DIRECTORY = System.getProperty("user.dir") + File.separator + "pdfinputdata";
	private static String DEFAULT_OUTPUT_DIRECTORY = System.getProperty("user.dir") + File.separator + "outputdata";

	
	private JButton exitButton = new JButton("Exit");
	private JButton parseButton = new JButton("Parse");
	private JButton selectInputDirectoryButton = new JButton("...");
	private JButton selectOutputDirectoryButton = new JButton("...");
	private JTextField urlTextField = new JTextField(DEFAULT_URL);
	private JTextField inputDirectoryTextField = new JTextField(DEFAULT_INPUT_DIRECTORY);
	private JTextField outputDirectoryTextField = new JTextField(DEFAULT_OUTPUT_DIRECTORY);
	private JTextArea statusTextArea = new JTextArea();
		

	public PdfMeetingScraper() {
		super("PDF Meeting Scraper");
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
		inputDirectoryTextField.setEditable(false);
		selectInputDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				selectDirectory(inputDirectoryTextField);
			}
		});
		outputDirectoryTextField.setEditable(false);
		selectOutputDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				selectDirectory(outputDirectoryTextField);
			}
		});
		JPanel inputPanel = new JPanel(new GridBagLayout());
		inputPanel.add(new JLabel("Full URL of PDF file:"), GUI.gbc(0, 0));
		inputPanel.add(urlTextField, GUI.gbc(1, 0, 1));
		inputPanel.add(new JLabel("Save PDF to input directory:"), GUI.gbc(0, 1));
		inputPanel.add(inputDirectoryTextField, GUI.gbc(1, 1, 15.0));
		inputPanel.add(selectInputDirectoryButton, GUI.gbc(2, 1));
		inputPanel.add(new JLabel("Output directory:"), GUI.gbc(0, 2));
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
				DownloadPdfFileTask downloadTask = new DownloadPdfFileTask(
						statusTextArea, urlTextField.getText(), inputDirectoryTextField.getText());
				downloadTask.execute();
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

	protected void parseDataFile() {
		int lastSeparatorIndex = urlTextField.getText().lastIndexOf("/");
		String tmpFileName = urlTextField.getText().substring(lastSeparatorIndex+1);
		ParseDataFileTask parseTask = new ParseDataFileTask(
				statusTextArea, inputDirectoryTextField.getText()+File.separator+tmpFileName,
				outputDirectoryTextField.getText());
		parseTask.execute();
	}
	
	protected void enableButtons(boolean enable) {
		parseButton.setEnabled(enable);
		exitButton.setEnabled(enable);
	}

	class DownloadPdfFileTask extends SwingWorker<Boolean, String> {
		private JTextArea textArea = null;
		private String pdfFileUrl = null;
		private String inputDirectoryName = null;
		private String fullInputFileName = null;
		
		public DownloadPdfFileTask(JTextArea theTextArea, String thePdfFileUrl, String theInputDirectoryName) {
			textArea = theTextArea;
			pdfFileUrl = thePdfFileUrl;
			inputDirectoryName = theInputDirectoryName;
		}
		
		@Override
		public Boolean doInBackground() {
			Boolean successful = true;
			try {
				fullInputFileName = inputDirectoryName + File.separator + pdfFileUrl.substring(pdfFileUrl.lastIndexOf("/")+1);
				publish("Transferring file from: " + pdfFileUrl);
				publish("to: " + fullInputFileName);
				URL fromUrl = new URL(pdfFileUrl);
				File toFile = new File(fullInputFileName);
				copyURLToFile(fromUrl, toFile);
				publish("Transfer completed");
				publish(" ");
			}
			catch (Exception e) {
				publish("*** FATAL ERROR OCCURRED ***");
				publish(e.getMessage());
				successful = false;
			}
			return successful;
		}
		
		@Override
		protected void process(List<String> chunks) {
			for (String status : chunks) {
				textArea.append(status + "\n");
			}
		}
		
		@Override
		protected void done() {
			parseDataFile();
		}
	
	}
	
	class ParseDataFileTask extends SwingWorker<Boolean, String> {
		private JTextArea textArea = null;
		private String fullInputFileName = null;
		private String outputDirectoryName = null;
		private String fullOutputFileName = null;
		
		public ParseDataFileTask(JTextArea theTextArea, String theInputFileName, String theOutputDirectoryName) {
			textArea = theTextArea;
			fullInputFileName = theInputFileName;
			outputDirectoryName = theOutputDirectoryName;
		}

		@Override
		public Boolean doInBackground() {
			Boolean successful = true;
			try {
				publish("Parsing the file: " + fullInputFileName);
				int lastSeparatorIndex = fullInputFileName.lastIndexOf(File.separator);
				int lastPeriodIndex = fullInputFileName.lastIndexOf(".");
				fullOutputFileName = outputDirectoryName + File.separator + 
						fullInputFileName.substring(lastSeparatorIndex+1, lastPeriodIndex) + ".txt";
				publish("Extracting text to file: " + fullOutputFileName);
				publish("LOTS of work still to do");
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
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			PdfMeetingScraper app = new PdfMeetingScraper();
			app.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
