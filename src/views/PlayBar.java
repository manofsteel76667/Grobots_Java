package views;

import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

public class PlayBar extends JToolBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1525252623730417217L;
	ActionListener listener;
	public JButton playButton;
	public JButton pauseButton;
	public JButton backButton;
	public JButton forwardButton;

	public PlayBar(ActionListener l) {
		listener = l;
		makeButtons();
	}

	void makeButtons() {
		backButton = makeButton("back", "back", "Slow Down Simulation", "");
		add(backButton);
		pauseButton = makeButton("pause", "pause", "Pause Simulation", "");
		add(pauseButton);
		playButton = makeButton("play", "play", "Run Simulation", "");
		add(playButton);
		forwardButton = makeButton("forward", "forward", "Speed Up Simulation", "");
		add(forwardButton);
	}

	protected JButton makeButton(String imageName,
			String actionCommand, String toolTipText, String altText) {
		// Look for the image.
		String imgLocation = imageName + ".png";
		URL imageURL = getClass().getResource(imgLocation);

		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(listener);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, altText));
		} else { // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imgLocation);
		}

		return button;
	}
}
