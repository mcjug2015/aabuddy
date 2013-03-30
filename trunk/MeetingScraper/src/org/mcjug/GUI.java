package org.mcjug;

import java.awt.*;
import javax.swing.*;

/**
 * This class contains several utility methods used
 * to layout GUI components within a panel or frame.
 */
public class GUI
{
    public static final Insets DEFAULT_INSETS = new Insets(2, 1, 2, 1);
    public static final int DEFAULT_BORDER = 10;
    public static final int BOTH = GridBagConstraints.BOTH;
    public static final int HORIZ = GridBagConstraints.HORIZONTAL;
    public static final int NONE = GridBagConstraints.NONE;
    public static final int WEST = GridBagConstraints.WEST;
    public static final int EAST = GridBagConstraints.EAST;
    public static final int CENTER = GridBagConstraints.CENTER;


    /**
     * Center the component on the screen
     */
    public static void centerComponent(Component c)
	{
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - c.getWidth()) / 2;
        int y = (screenSize.height - c.getHeight()) / 2;
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        c.setLocation(x, y);
    }

	/**
	 * Convenience method for creating GridBagConstraints used by
	 * panels having a GridBagLayout.
	 */
	public static GridBagConstraints gbc(int x, int y)
	{
	    GridBagConstraints gbc = new GridBagConstraints(x, y, 1, 1, 1.0, 1.0,
	            WEST, NONE, DEFAULT_INSETS, 0, 0);
	    return gbc;
	}

	/**
	 * Convenience method for creating GridBagConstraints used by
	 * panels having a GridBagLayout.
	 */
	public static GridBagConstraints gbc(int x, int y, int gridWidth)
	{
	    GridBagConstraints gbc = new GridBagConstraints(x, y, gridWidth, 1, 1.0, 1.0,
	            WEST, HORIZ, DEFAULT_INSETS, 0, 0);
	    return gbc;
	}

	/**
	 * Convenience method for creating GridBagConstraints used by
	 * panels having a GridBagLayout.
	 */
	public static GridBagConstraints gbc(int x, int y, double weightX)
	{
	    GridBagConstraints gbc = new GridBagConstraints(x, y, 1, 1, weightX, 1.0,
	            WEST, HORIZ, DEFAULT_INSETS, 0, 0);
	    return gbc;
	}
	
	/**
	 * Convenience method for creating GridBagConstraints used by
	 * panels having a GridBagLayout.
	 */
	public static GridBagConstraints gbc(int x, int y, int gridWidth, int anchor)
	{
	    GridBagConstraints gbc = new GridBagConstraints(x, y, gridWidth, 1, 1.0, 1.0,
	            anchor, NONE, DEFAULT_INSETS, 0, 0);
	    return gbc;
	}

	/**
	 * Convenience method for creating GridBagConstraints used by
	 * panels having a GridBagLayout.
	 */
	public static GridBagConstraints gbc(int x, int y,
								int gridWidth, int gridHeight, int anchor)
	{
	    GridBagConstraints gbc = new GridBagConstraints(x, y, gridWidth, gridHeight,
	            1.0, 1.0, anchor, NONE, DEFAULT_INSETS, 0, 0);
	    return gbc;
	}

	
	/**
	 * Convenience method for creating GridBagConstraints used by
	 * panels having a GridBagLayout.
	 */
	public static GridBagConstraints gbc(int x, int y, int gridWidth, double weightX,
	        					double weightY, int fill)
	{
	    GridBagConstraints gbc = new GridBagConstraints(x, y, gridWidth, 1,
	            weightX, weightY, WEST, fill, DEFAULT_INSETS, 0, 0);
	    return gbc;
	}

	/**
	 * Convenience method for creating GridBagConstraints used by
	 * panels having a GridBagLayout.
	 */
	public static GridBagConstraints gbc(int x, int y, int gridWidth, int gridHeight,
	        			double weightX, double weightY, int anchor, int fill)
	{
	    GridBagConstraints gbc = new GridBagConstraints(x, y, gridWidth, gridHeight,
	            weightX, weightY, anchor, fill, DEFAULT_INSETS, 0, 0);
	    return gbc;
	}

	/**
	 * Create a spacer panel for use with the GridBag layout.
	 * @param width minimum width of the spacer panel
	 * @param height minimum height of the spacer panel
	 * @return a "dummy" JPanel
	 */
	public static JPanel createSpacerPanel(int minimumWidth, int minimumHeight) {
		JPanel spacerPanel = new JPanel();
		spacerPanel.setLayout(new BoxLayout(spacerPanel, BoxLayout.X_AXIS));
		spacerPanel.add(Box.createRigidArea(new Dimension(minimumWidth, minimumHeight)));
		return spacerPanel;
	}
	
	/**
	 * Custom GUI element used to label group areas within a frame.
	 * (A bit more compact than a panel with a line border and heading)
	 */
    public static class LabeledSeparator extends JPanel
    {
        private static final Color NAVY = new Color(0, 0, 153);

        public LabeledSeparator()
        {
            this(null);
        }

        public LabeledSeparator(String title)
        {
            setLayout(new BorderLayout());
            JPanel p = new JPanel(new BorderLayout());
            if (title != null)
            {
                JLabel titleLabel = new JLabel(title);
                Font font = titleLabel.getFont();
                titleLabel.setFont(font.deriveFont(Font.BOLD));
                titleLabel.setForeground(NAVY);
                p.add(titleLabel, BorderLayout.CENTER);
            }
            p.add(new JSeparator(), BorderLayout.SOUTH);
            add(p, BorderLayout.SOUTH);
        }
    }

}

